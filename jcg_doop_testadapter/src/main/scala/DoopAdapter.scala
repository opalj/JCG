
import java.io.File
import java.io.PrintWriter
import java.net.URL

import org.opalj.br.ClassFile
import org.opalj.br.FieldType
import org.opalj.br.FieldTypes
import org.opalj.br.MethodDescriptor
import org.opalj.br.ObjectType
import org.opalj.br.ReferenceType
import org.opalj.br.ReturnType
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.SomeProject
import org.opalj.br.instructions.INVOKEDYNAMIC
import org.opalj.br.instructions.MethodInvocationInstruction
import org.opalj.collection.immutable.RefArray
import org.opalj.log.GlobalLogContext
import org.opalj.log.OPALLogger
import play.api.libs.json.JsValue
import play.api.libs.json.Json

import scala.collection.mutable
import scala.io.Source

/**
 * This is an experimental stage [[JCGTestAdapter]] as it is not possible to run Doop without
 * installing it (and a data-log engine).
 * Therefore, this object has the capability of converting the output of the CallGraphEdge table
 * into the [[ReachableMethods]] data-format.
 *
 * @author Florian Kuebler
 */
object DoopAdapter extends JCGTestAdapter {

    override def possibleAlgorithms(): Array[String] = Array("context-insensitive")
    override def frameworkName(): String = "Doop"

    def main(args: Array[String]): Unit = {
        OPALLogger.updateLogger(GlobalLogContext, new DevNullLogger())

        val doopResults = new File(args(0)).listFiles{ f ⇒
            f.isFile && f.getName.endsWith(".jar.txt")
        }
        val jreDir = new File(args(1))

        if (!jreDir.exists())
            throw new IllegalArgumentException("Please specify the path to the JRE as 2nd" +
                "command line argument")

        for (doopResult ← doopResults.sorted) {
            val source = Source.fromFile(doopResult)
            val tgtJar = new File(s"result/${doopResult.getName.replace(".txt", "")}")
            if (!tgtJar.exists()) {
                throw new IllegalArgumentException(s"${tgtJar.getAbsolutePath} does not exist")
            }
            val projectSpec: ProjectSpecification = ProjectSpecification(
                doopResult.getName.replace(".jar.txt", ""), // name of the jar
                -1, // ignore java version
                None, // ignore main
                tgtJar.getAbsolutePath, // path to jar
                None // ignore other class path entries
            )

            println(s"${tgtJar.getName}")
            val outFile = createJsonRepresentation(source, tgtJar, jreDir)
            val matchingResult = CGMatcher.matchCallSites(
                projectSpec, jreDir.getAbsolutePath, new File(""), outFile
            )
            println(matchingResult.shortNotation)
        }
    }

    def createJsonRepresentation(doopResult: Source, tgtJar: File, jreDir: File): File = {
        implicit val p: Project[URL] = Project(Array(tgtJar, jreDir), Array.empty[File])

        val callGraph = extractDoopCG(doopResult)

        val reachableMe: ReachableMethods = convertToReachableMethods(callGraph)

        val callSitesJson: JsValue = Json.toJson(reachableMe)
        val jarName = tgtJar.getName
        val dn = jarName.replace(".jar", "")
        val fn = jarName.replace(".jar", ".json")
        val outFile = new File(s"tmp/DOOP/$dn/${fn}")
        val pw = new PrintWriter(outFile)
        pw.write(Json.prettyPrint(callSitesJson))
        pw.close()
        outFile
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
        val tgtParamTypes: FieldTypes =
            RefArray.from(firstTgt.parameterTypes.map(FieldType.apply).toArray)
        val tgtMD = MethodDescriptor(tgtParamTypes, tgtReturnType)
        val split = declaredTgt.split("""\.""")
        val declaredType = s"L${split.slice(0, split.size - 1).mkString("/")};"
        val name = split.last.replace("'", "")
        val tgtMethods = tgts.map(toMethod)
        // todo what abot <clinit> etc where no call is in the bytecode
        val calls = callerOpal.body.get.collect {
            // todo what about lambdas?
            case instr: MethodInvocationInstruction if instr.name == name ⇒ instr //&& instr.declaringClass == FieldType(declaredType) ⇒ instr // && instr.methodDescriptor == tgtMD ⇒ instr
            case instr: INVOKEDYNAMIC                                     ⇒ instr
            //throw new Error()
        }

        if (calls.size <= number && callerOpal.isBridge) {
            computeCallSite(declaredTgt, number, tgts, callerMethod, resolveBridgeMethod(callerOpal))
        } else {
            assert(calls.size > number)
            val pc = calls(number).pc
            val lineNumber = callerOpal.body.get.lineNumber(pc)

            CallSite(
                firstTgt.copy(declaringClass = declaredType),
                lineNumber.getOrElse(-1),
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
                    val parameterTypes: FieldTypes =
                        RefArray.from(callerMethod.parameterTypes.map(FieldType.apply).toArray)
                    val md = MethodDescriptor(parameterTypes, returnType)

                    cf.findMethod(callerMethod.name, md) match {
                        case Some(callerOpal) if callerOpal.body.isDefined ⇒
                            for (((declaredTgt, number), tgts) ← callSites) {
                                resultingCallSites += computeCallSite(
                                    declaredTgt, number, tgts, callerMethod, callerOpal
                                )

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

    private def extractDoopCG(doopResult: Source): Map[String, Map[(String, Int), Set[String]]] = {
        val callGraph = mutable.Map.empty[String, mutable.Map[(String, Int), mutable.Set[String]]].withDefault(_ ⇒ mutable.OpenHashMap.empty.withDefault(_ ⇒ mutable.Set.empty))

        val re = """\[\d+\]\*\d+, \[\d+\]<([^><]+(<clinit>|<init>)?[^>]*)>/([^/]+)/(\d+), \[\d+\]\*\d+, \[\d+\]<([^><]+(<clinit>|<init>)?[^>]*)>""".r ////([^/]+)/(\d+), \[\d+\\]\*\d+, \[\d+\]<([^><]+(<clinit>|<init>)?[^>]*)>""".r
        for (line ← doopResult.getLines()) {
            // there is at most one occurrence per line

            re.findFirstMatchIn(line) match {
                case Some(x) ⇒
                    val caller = x.group(1)
                    val declaredTgt = x.group(3)
                    val number = x.group(4).toInt
                    val tgt = x.group(5)

                    val currentCallsites = callGraph(caller)
                    val callSite = declaredTgt → number
                    val currentCallees = currentCallsites(callSite)

                    currentCallees += tgt
                    currentCallsites += (callSite → currentCallees)
                    callGraph += (caller → currentCallsites)
                case _ ⇒ // no match
            }
        }
        doopResult.close()
        callGraph.map { case (k, v) ⇒ k → v.map { case (k, v) ⇒ k → v.toSet }.toMap }.toMap
    }

    def toMethod(methodStr: String): Method = {
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

    def toJVMType(t: String): String = {
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

    def toObjectType(jvmRefType: String): ObjectType = {
        assert(jvmRefType.length > 2)
        ObjectType(jvmRefType.substring(1, jvmRefType.length - 1))
    }

    override def serializeCG(
        algorithm:  String,
        target:     String,
        mainClass:  String,
        classPath:  Array[String],
        JDKPath:    String,
        analyzeJDK: Boolean,
        outputFile: String
    ): Long = ???
}
