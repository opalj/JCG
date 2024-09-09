
import java.io.File
import java.io.Writer
import java.net.URL
import java.nio.file.Files

import scala.collection.compat.immutable
import scala.collection.mutable
import scala.io.Source
import scala.sys.process.Process

import org.apache.commons.io.FileUtils
import play.api.libs.json.Json
import play.api.libs.json.JsValue

import org.opalj.br.ClassFile
import org.opalj.br.FieldType
import org.opalj.br.FieldTypes
import org.opalj.br.MethodDescriptor
import org.opalj.br.ObjectType
import org.opalj.br.ReferenceType
import org.opalj.br.ReturnType
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.SomeProject
import org.opalj.br.instructions.Instruction
import org.opalj.br.instructions.INVOKEDYNAMIC
import org.opalj.br.instructions.MethodInvocationInstruction

/**
 * This is an experimental stage [[JavaTestAdapter]] as it is not possible to run Doop without
 * installing it (and a data-log engine).
 * Therefore, this object has the capability of converting the output of the CallGraphEdge table
 * into the [[ReachableMethods]] data-format.
 *
 * @author Florian Kuebler
 */
object DoopAdapter extends JavaTestAdapter {

    val possibleAlgorithms: Array[String] = Array("context-insensitive")
    val frameworkName: String = "Doop"

    private def createJsonRepresentation(
        doopEdges: Source, doopReachable: Source, tgtJar: File, jreDir: File, output: Writer
    ): Unit = {
        implicit val p: Project[URL] = Project(Array(tgtJar, jreDir), Array.empty[File])

        val callGraph = extractDoopCG(doopEdges, doopReachable)

        val reachableMe: ReachableMethods = convertToReachableMethods(callGraph)

        val callSitesJson: JsValue = Json.toJson(reachableMe)

        output.write(Json.prettyPrint(callSitesJson))
    }

    private def resolveBridgeMethod(
        bridgeMethod: org.opalj.br.Method
    )(implicit classFile: ClassFile, p: SomeProject): org.opalj.br.Method = {
        val methods = classFile.findMethod(bridgeMethod.name).filter { m ⇒
            !m.isBridge && (m.returnType match {
                case rt: ReferenceType ⇒ p.classHierarchy.isSubtypeOf(
                    rt, bridgeMethod.returnType.asReferenceType
                )
                case rt ⇒ rt == bridgeMethod.returnType
            })
        }
        assert(methods.size == 1)
        methods.head
    }

    private def computeCallSite(
        declaredTgt:  String,
        number:       Int,
        tgts:         Set[String],
        callerMethod: Method,
        callerOpal:   org.opalj.br.Method
    )(implicit
        classFile: ClassFile,
      project: SomeProject): CallSite = {
        assert(tgts.nonEmpty)
        val firstTgt = toMethod(tgts.head)
        val tgtReturnType = ReturnType(firstTgt.returnType)
        val tgtParamTypes: FieldTypes = immutable.ArraySeq(firstTgt.parameterTypes.map(FieldType.apply): _*)
        val tgtMD = MethodDescriptor(tgtParamTypes, tgtReturnType)
        val split = declaredTgt.split("""\.""")
        val declaredType = s"L${split.slice(0, split.size - 1).mkString("/")};"
        val name = split.last.replace("'", "")
        val tgtMethods = tgts.map(toMethod)
        // todo what abot <clinit> etc where no call is in the bytecode
        val declObjType = FieldType(declaredType)

        val getInstr: PartialFunction[Instruction, Instruction] = {
            // todo what about lambdas?
            case instr: MethodInvocationInstruction if (
                instr.name == name &&
                    (instr.declaringClass == declObjType ||
                        declObjType == ObjectType.Object && instr.declaringClass.isArrayType)
                ) ⇒ instr //&& instr.declaringClass == FieldType(declaredType) ⇒ instr // && instr.methodDescriptor == tgtMD ⇒ instr
            case instr: INVOKEDYNAMIC ⇒ instr
                //throw new Error()
        }

        val calls = callerOpal.body.get.collect(getInstr)

        if (calls.size <= number && callerOpal.isBridge) {
            computeCallSite(declaredTgt, number, tgts, callerMethod, resolveBridgeMethod(callerOpal))
        } else {
            assert(calls.size > number)
            val pc = calls(number).pc
            val lineNumber = callerOpal.body.get.lineNumber(pc)

            CallSite(
                firstTgt.copy(declaringClass = declaredType),
                lineNumber.getOrElse(-1),
                Some(pc),
                tgtMethods
            )
        }
    }

    private def convertToReachableMethods(
        callGraph: Map[String, Map[(String, Int), Set[String]]]
    )(implicit project: Project[URL]): ReachableMethods = {
        var reachableMethods = Set.empty[ReachableMethod]
        var reachableMethodsSet = Set.empty[Method]

        for {
            (caller, callSites) ← callGraph
        } {
            val callerMethod = toMethod(caller)
            reachableMethodsSet += callerMethod
            var resultingCallSites = Set.empty[CallSite]
            project.classFile(toObjectType(callerMethod.declaringClass)) match {
                case Some(cf) ⇒
                    implicit val classFile: ClassFile = cf
                    val returnType = ReturnType(callerMethod.returnType)
                    val parameterTypes: FieldTypes = scala.collection.compat.immutable.ArraySeq(callerMethod.parameterTypes.map(FieldType.apply): _*)
                    val md = MethodDescriptor(parameterTypes, returnType)

                    cf.findMethod(callerMethod.name, md) match {
                        case Some(callerOpal) if callerOpal.body.isDefined ⇒
                            for (((declaredTgt, number), tgts) ← callSites) {
                                try {
                                    resultingCallSites += computeCallSite(
                                        declaredTgt, number, tgts, callerMethod, callerOpal
                                    )
                                } catch {
                                    case _: AssertionError ⇒
                                        println(s"Callsite not found: $declaredTgt/$number in $callerMethod")
                                }

                            }
                        case _ ⇒
                        // todo
                        //throw new IllegalArgumentException()
                    }
                case None ⇒
            }
            reachableMethods += ReachableMethod(callerMethod, resultingCallSites)
        }

        for {
            (_, callSites) ← callGraph
            (_, tgts) ← callSites
            tgt ← tgts
        } {
            val calleeMethod = toMethod(tgt)
            if (!reachableMethodsSet.contains(calleeMethod)) {
                reachableMethodsSet += calleeMethod
                reachableMethods += ReachableMethod(calleeMethod, Set.empty)
            }
        }
        ReachableMethods(reachableMethods)
    }

    private def extractDoopCG(
        doopEdges: Source, doopReachable: Source
    ): Map[String, Map[(String, Int), Set[String]]] = {
        val callGraph = mutable.Map.empty[String, mutable.Map[(String, Int), mutable.Set[String]]].withDefault(_ ⇒ mutable.OpenHashMap.empty.withDefault(_ ⇒ mutable.Set.empty))

        for (line ← doopEdges.getLines()) {
            val Array(_, callerDeclaredTgtNumber, _, tgtStr) = line.split("\t")
            try {
                val (callerStr, declaredTgt, numberString) =
                    if (callerDeclaredTgtNumber.contains("native ")) {
                        val Array(callerStr, declaredTgt) = callerDeclaredTgtNumber.split("/")
                        (callerStr, declaredTgt, "0")
                    } else if ("<main-thread-init>/0" == callerDeclaredTgtNumber) {
                        ("<java.lang.Thread: java.lang.Thread currentThread()>", "java.lang.Thread.<init>", "0")
                    } else if ("<thread-group-init>/0" == callerDeclaredTgtNumber) {
                        ("<java.lang.Thread: java.lang.Thread currentThread()>", "java.lang.ThreadGroup.<init>", "0")
                    } else {
                        val Array(callerStr, declaredTgt, numberString) = callerDeclaredTgtNumber.split("/")
                        (callerStr, declaredTgt, numberString)
                    }
                val caller = callerStr.slice(1, callerStr.length - 1)
                val tgt = tgtStr.slice(1, tgtStr.length - 1)
                val number = numberString.toInt
                // there is at most one occurrence per line

                val currentCallsites = callGraph(caller)
                val callSite = declaredTgt → number
                val currentCallees = currentCallsites(callSite)

                currentCallees += tgt
                currentCallsites += (callSite → currentCallees)
                callGraph += (caller → currentCallsites)
            } catch {
                case _: Throwable ⇒
                    println()
            }

        }
        doopEdges.close()

        for (line ← doopReachable.getLines()) {
            val tgt = line.slice(1, line.length - 1)
            if (!callGraph.contains(tgt))
                callGraph += (tgt → mutable.Map.empty)
        }
        doopReachable.close()

        callGraph.map { case (k, v) ⇒ k → v.map { case (k, v) ⇒ k → v.toSet }.toMap }.toMap
    }

    private def toMethod(methodStr: String): Method = {
        """([^:]+): ([^ ]+) ([^\(]+)\(([^\)]*)\)""".r.findFirstMatchIn(methodStr) match {
            case Some(m) ⇒
                val declClass = m.group(1)
                val returnType = m.group(2)
                val name = m.group(3)
                val params = if (m.group(4).isEmpty) Array.empty[String] else m.group(4).split(",")
                Method(
                    name, toJVMType(declClass), toJVMType(returnType), params.map(toJVMType).toList
                )
            case None ⇒ throw new IllegalArgumentException()
        }
    }

    private def toJVMType(t: String): String = {
        if (t.endsWith("[]"))
            s"[${toJVMType(t.substring(0, t.length - 2))}"
        else t match {
            case "byte"    ⇒ "B"
            case "short"   ⇒ "S"
            case "int"     ⇒ "I"
            case "long"    ⇒ "J"
            case "float"   ⇒ "F"
            case "double"  ⇒ "D"
            case "boolean" ⇒ "Z"
            case "char"    ⇒ "C"
            case "void"    ⇒ "V"
            case _         ⇒ s"L${t.replace(".", "/")};"

        }
    }

    private def toObjectType(jvmRefType: String): ObjectType = {
        assert(jvmRefType.length > 2)
        ObjectType(jvmRefType.substring(1, jvmRefType.length - 1))
    }

    override def serializeCG(
        algorithm:      String,
        inputDirPath:   String,
        output:         Writer,
        adapterOptions: AdapterOptions
    ): Long = {
        val env = System.getenv

        val mainClass = adapterOptions.getString("mainClass")
        val classPath = adapterOptions.getStringArray("classPath")
        val JDKPath = adapterOptions.getString("JDKPath")
        val analyzeJDK = adapterOptions.getBoolean("analyzeJDK")

        assert(env.containsKey("DOOP_HOME"))
        val doopHome = new File(env.get("DOOP_HOME"))
        assert(doopHome.exists())
        assert(doopHome.isDirectory)

        val doopPlatformDirs = Files.createTempDirectory(null).toFile
        val doopJDKPath = new File(doopPlatformDirs, "JREs/jre1.8/lib/")
        doopJDKPath.mkdirs()
        FileUtils.copyDirectory(new File(JDKPath), doopJDKPath)

        val outDir = Files.createTempDirectory(null).toFile

        assert(algorithm == "context-insensitive")

        var args = Array("./doop", "-a", "context-insensitive", "-t", "1440", "--platform", "java_8", "-i", inputDirPath) ++ classPath
        if (analyzeJDK) {
           args ++= JRELocation.getAllJREJars(JDKPath).map(_.getCanonicalPath)
        }

        // args ++= Array("--reflection-classic")

        if (mainClass != null)
            args ++= Array("--main", mainClass)

        val status = Process(Array("./gradlew", "tasks"), Some(doopHome)).!
        if (status != 0)
            throw new RuntimeException("failed to run doop")

        val before = System.nanoTime()

        Process(
            args,
            Some(doopHome),
            "DOOP_HOME" → doopHome.getAbsolutePath,
            "DOOP_OUT" → outDir.getAbsolutePath,
            "DOOP_PLATFORMS_LIB" → doopPlatformDirs.getAbsolutePath
        ).!

        val after = System.nanoTime()

        val cgCsv = new File(doopHome, "last-analysis/CallGraphEdge.csv")
        val rmCsv = new File(doopHome, "last-analysis/Reachable.csv")
        createJsonRepresentation(
            Source.fromFile(cgCsv),
            Source.fromFile(rmCsv),
            new File(inputDirPath),
            new File(JDKPath),
            output
        )

        FileUtils.deleteDirectory(doopPlatformDirs)
        FileUtils.deleteDirectory(outDir)

        after - before
    }
}
