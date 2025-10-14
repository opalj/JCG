import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors
import scala.collection.mutable
import scala.jdk.CollectionConverters._

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.ibm.wala.cast.ir.ssa.AstIRFactory
import com.ibm.wala.cast.js.ipa.callgraph.JSCallGraphUtil
import com.ibm.wala.cast.js.translator.CAstRhinoTranslatorFactory
import com.ibm.wala.cast.js.util.JSCallGraphBuilderUtil
import com.ibm.wala.cast.js.util.JSCallGraphBuilderUtil.CGBuilderType
import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.classLoader.SourceModule
import com.ibm.wala.classLoader.SourceURLModule
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ssa.IRFactory
import com.ibm.wala.types.MethodReference
import com.ibm.wala.types.TypeReference

object WalaJSJCGAdapter extends JSTestAdapter {

    val possibleAlgorithms: Array[String] = Array("1-CFA", "0-1-CFA")

    val frameworkName: String = "WALA"

    def serializeCG(
        algorithm: String,
        inputDirPath: String,
        output:         Writer,
        adapterOptions: AdapterOptions
    ): Long = {

        val builderType: CGBuilderType = algorithm match {
            case "1-CFA" ⇒ CGBuilderType.ONE_CFA
            case "0-1-CFA" ⇒ CGBuilderType.ZERO_ONE_CFA
        }

        JSCallGraphUtil.setTranslatorFactory(new CAstRhinoTranslatorFactory());

        val irFactory: IRFactory[IMethod] = AstIRFactory.makeDefaultFactory

        val scripts = Files.walk(Paths.get(inputDirPath)).filter { p ⇒
            Files.isRegularFile(p) && p.toString.endsWith(".js")
        }.map { filePath ⇒
            new SourceURLModule(
                JSCallGraphBuilderUtil.getURLforFile(
                    "",
                    String.valueOf(filePath),
                    classOf[JSCallGraphBuilderUtil].getClassLoader)
                )
        }.collect(Collectors.toList[SourceModule]).toArray(new Array[SourceModule](0))

        val before = System.nanoTime
        val cg: CallGraph = JSCallGraphBuilderUtil.makeScriptCG(scripts, builderType, irFactory)
        val after = System.nanoTime

        //val initialEntryPoints = cg.getFakeRootNode.iterateCallSites().asScala.map(_.getDeclaredTarget)
        val initialEntryPoints = cg.getEntrypointNodes.asScala.map(_.getMethod.getReference)

        val worklist = mutable.Queue(initialEntryPoints.toSeq: _*)
        val processed = mutable.Set(worklist.toSeq: _*)

        var reachableMethods = Set.empty[ReachableMethod]
        while (worklist.nonEmpty) {
            val currentMethod = worklist.dequeue()

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
                val currentMethodResolved = cg.getClassHierarchy.resolveMethod(currentMethod)

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

        // Write to JSON using a stream to fix issues with CGs too large for memory
        val data = ReachableMethods(reachableMethods)
        
        // using Jackson directly instead of play-json
        val factory = new JsonFactory()
        val generator = factory.createGenerator(output)
        generator.setPrettyPrinter(new DefaultPrettyPrinter())
        
        new ObjectMapper()
            .registerModule(DefaultScalaModule)
            .writerWithDefaultPrettyPrinter()
            .writeValue(generator, data)
        
        generator.close()


        after - before
    }

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
