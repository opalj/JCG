
import java.io.File
import java.io.PrintWriter
import java.io.Writer
import java.util
import java.util.stream.Collectors

import com.ibm.wala.classLoader.Language.JAVA
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import com.ibm.wala.ipa.callgraph.impl.Util
import com.ibm.wala.ipa.cha.ClassHierarchyFactory
import com.ibm.wala.types.MethodReference
import com.ibm.wala.types.TypeReference
import com.ibm.wala.util.NullProgressMonitor
import com.ibm.wala.util.config.AnalysisScopeReader
import play.api.libs.json.Json
import scala.collection.JavaConverters._
import scala.collection.mutable

object WalaJCGAdapter extends JavaTestAdapter {

    def serializeCG(
        algorithm: String,
        inputDirPath: String,
        output:         Writer,
        adapterOptions: AdapterOptions
    ): Long = {
        val mainClass = adapterOptions.getString("mainClass")
        val classPath = adapterOptions.getStringArray("classPath")
        val JDKPath = adapterOptions.getString("JDKPath")
        val analyzeJDK = adapterOptions.getBoolean("analyzeJDK")

        val before = System.nanoTime
        val cl = Thread.currentThread.getContextClassLoader

        var cp = util.Arrays.stream(classPath).collect(Collectors.joining(File.pathSeparator))
        cp = inputDirPath + File.pathSeparator + cp

        // write wala.properties with the specified JDK and store it in the classpath
        val tmp = new File("tmp")
        tmp.mkdirs()
        val walaPropertiesFile = new File(tmp, "wala.properties")
        val pw = new PrintWriter(walaPropertiesFile)
        pw.println(s"java_runtime_dir = $JDKPath")
        pw.close()

        /*val sysloader = classOf[WalaProperties].getClassLoader.asInstanceOf[URLClassLoader]
        val sysclass = classOf[URLClassLoader]
        val m = sysclass.getDeclaredMethod("addURL", classOf[URL])
        m.setAccessible(true)
        m.invoke(sysloader, tmp.toURI.toURL)*/

        val ex = if (analyzeJDK) {
            new File(cl.getResource("no-exclusions.txt").getFile)
        } else {
            // TODO exclude more of the jdk
            new File(cl.getResource("Java60RegressionExclusions.txt").getFile)
        }

        val scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(cp, ex)

        // we do not need the wala.properties anymore!
        walaPropertiesFile.delete()
        tmp.delete()

        val classHierarchy = ClassHierarchyFactory.make(scope)

        val entrypoints =
            if (mainClass == null) {
                new AllSubtypesOfApplicationEntrypoints(scope, classHierarchy)
            } else {
                val mainClassWala = "L" + mainClass.replace(".", "/")
                Util.makeMainEntrypoints(scope, classHierarchy, mainClassWala)
            }

        val options = new AnalysisOptions(scope, entrypoints)
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL)

        val cache = new AnalysisCacheImpl

        val cg =
            if (algorithm.contains("0-CFA")) {
                val ncfaBuilder = Util.makeZeroCFABuilder(JAVA, options, cache, classHierarchy, scope)
                ncfaBuilder.makeCallGraph(options)
            } else if (algorithm.contains("0-1-CFA")) {
                val cfaBuilder = Util.makeZeroOneCFABuilder(JAVA, options, cache, classHierarchy, scope)
                cfaBuilder.makeCallGraph(options)
            } else if (algorithm.contains("1-CFA")) {
                val cfaBuilder = Util.makeNCFABuilder(1, options, cache, classHierarchy, scope)
                cfaBuilder.makeCallGraph(options)
            } else if (algorithm.contains("RTA")) {
                val rtaBuilder = Util.makeRTABuilder(options, cache, classHierarchy, scope)
                rtaBuilder.makeCallGraph(options, new NullProgressMonitor)
            } else if (algorithm.contains("CHA")) {
                import com.ibm.wala.ipa.callgraph.cha.CHACallGraph
                val CG = new CHACallGraph(classHierarchy)
                CG.init(entrypoints)
                CG
            } else throw new IllegalArgumentException
        val after = System.nanoTime

        val initialEntryPoints = cg.getFakeRootNode.iterateCallSites().asScala.map(_.getDeclaredTarget)

        val worklist = mutable.Queue(initialEntryPoints.toSeq: _*)
        val processed = mutable.Set(worklist.toSeq: _*)

        var reachableMethods = Set.empty[ReachableMethod]
        while (worklist.nonEmpty) {
            val currentMethod = worklist.dequeue()

            val currentMethodResolved = classHierarchy.resolveMethod(currentMethod)
            if (currentMethodResolved == null) {

            }
            val callSites = for {
                cgNode ← cg.getNodes(currentMethod).asScala
                cs ← cgNode.iterateCallSites().asScala
            } yield {
                val tgtsWala = cg.getPossibleTargets(cgNode, cs).asScala.map(_.getMethod.getReference)
                tgtsWala.foreach { tgt ⇒
                    if (!processed.contains(tgt)) {
                        worklist += tgt
                        processed += tgt
                    }
                }
                if (currentMethodResolved != null) {
                    val declaredTarget = cs.getDeclaredTarget
                    val line = try {
                        currentMethodResolved.getLineNumber(cs.getProgramCounter)
                    } catch {
                        case _: ArrayIndexOutOfBoundsException ⇒ -1
                    }
                    val tgts = tgtsWala.map(createMethodObject).toSet
                    Some(CallSite(
                        createMethodObject(declaredTarget),
                        line,
                        Some(cs.getProgramCounter),
                        tgts
                    ))
                } else {
                    None
                }
            }

            reachableMethods +=
                ReachableMethod(createMethodObject(currentMethod), callSites.toSet.flatten)
        }

        val prettyPrint = Json.prettyPrint(Json.toJson(ReachableMethods(reachableMethods)))
        output.write(prettyPrint)

        after - before
    }

    val possibleAlgorithms: Array[String] = Array("0-1-CFA", "RTA", "0-CFA", "1-CFA", "CHA") //Array("0-1-CFA") //"RTA = "0-CFA = "1-CFA = "0-1-CFA")

    val frameworkName: String = "WALA"

    private def createMethodObject(method: MethodReference): Method = {
        val name = method.getName.toString
        val declaringClass = toJVMString(method.getDeclaringClass)
        val returnType = toJVMString(method.getReturnType)
        val indexes = 0 until method.getNumberOfParameters
        val params = indexes.map(i ⇒ toJVMString(method.getParameterType(i))).toList

        Method(name, declaringClass, returnType, params)

    }

    private def toJVMString(typeReference: TypeReference): String =
        if (typeReference.isClassType || isArrayOfClassType(typeReference)) {
            typeReference.getName.toString+";"
        } else {
            typeReference.getName.toString
        }

    private def isArrayOfClassType(typeReference: TypeReference): Boolean = {
        if (typeReference.isArrayType) {
            val elementType = typeReference.getArrayElementType
            if (elementType.isClassType) {
                true
            } else {
                isArrayOfClassType(elementType)
            }
        } else {
            false
        }
    }
}
