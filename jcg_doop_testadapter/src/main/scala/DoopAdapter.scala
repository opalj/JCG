
import java.io.File
import java.io.PrintWriter

import org.opalj.br.FieldType
import org.opalj.br.MethodDescriptor
import org.opalj.br.ObjectType
import org.opalj.br.ReturnType
import org.opalj.br.analyses.Project
import org.opalj.br.instructions.MethodInvocationInstruction
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Writes

import scala.collection.mutable
import scala.io.Source

case class CallSites(callSites: Set[CallSite])

case class CallSite(declaredTarget: Method, line: Int, method: Method, targets: Set[Method])

case class Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String])

object DoopAdapter extends JCGTestAdapter {

    override def serializeCG(algorithm: String, target: String, classPath: String, outputFile: String): Unit = ???

    override def possibleAlgorithms(): Array[String] = Array("context-insensitive")

    override def frameworkName(): String = "Doop"

    def main(args: Array[String]): Unit = {

        val source = Source.fromFile("/Users/floriankuebler/Desktop/SI1.jar.txt")
        val p = Project(Array(new File("result"), new File("/Users/floriankuebler/Documents/git/doop-benchmarks/JREs/jre1.7.0_95_debug")), Array.empty[File]) // TODO use real project here

        org.opalj.br.LineNumberTable
        val re = """\[\d+\]\*\d+, \[\d+\]<([^><]+(<clinit>|<init>)?[^>]*)>/([^/]+)/(\d+), \[\d+\]\*\d+, \[\d+\]<([^><]+(<clinit>|<init>)?[^>]*)>""".r ////([^/]+)/(\d+), \[\d+\\]\*\d+, \[\d+\]<([^><]+(<clinit>|<init>)?[^>]*)>""".r

        val callGraph = mutable.Map.empty[String, mutable.Map[(String, Int), mutable.Set[String]]].withDefault(s ⇒ mutable.OpenHashMap.empty.withDefault(s ⇒ mutable.Set.empty))

        for (line ← source.getLines()) {
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
        source.close()

        var resultingCallSites = Set.empty[CallSite]

        for {
            (caller, callSites) ← callGraph
        } {
            val callerMethod = toMethod(caller)
            p.classFile(toObjectType(callerMethod.declaringClass)) match {
                case Some(cf) ⇒
                    val returnType = ReturnType(callerMethod.returnType)
                    val parameterTypes = callerMethod.parameterTypes.map(FieldType.apply)
                    val md = MethodDescriptor(parameterTypes.toIndexedSeq, returnType)
                    cf.findMethod(callerMethod.name, md) match {
                        case Some(callerOpal) if callerOpal.body.isDefined ⇒
                            for (((declaredTgt, number), tgts) ← callSites) {
                                assert(tgts.nonEmpty)
                                val firstTgt = toMethod(tgts.head)
                                val tgtReturnType = ReturnType(firstTgt.returnType)
                                val tgtParamTypes = firstTgt.parameterTypes.map(FieldType.apply)
                                val tgtMD = MethodDescriptor(tgtParamTypes.toIndexedSeq, tgtReturnType)
                                val split = declaredTgt.split("""\.""")
                                val declaredType = s"L${split.slice(0, split.size - 1).mkString("/")};"
                                val name = split.last.replace("'", "")
                                val tgtMethods = tgts.map(toMethod)
                                val calls = callerOpal.body.get.collect {
                                    // todo what about lambdas?
                                    case instr: MethodInvocationInstruction if instr.name == name ⇒ instr //&& instr.declaringClass == FieldType(declaredType) ⇒ instr // && instr.methodDescriptor == tgtMD ⇒ instr
                                }

                                assert(calls.size > number)
                                val pc = calls(number).pc
                                val lineNumber = callerOpal.body.get.lineNumber(pc)

                                resultingCallSites += CallSite(
                                    firstTgt.copy(declaringClass = declaredType),
                                    lineNumber.getOrElse(-1),
                                    callerMethod,
                                    tgtMethods.toSet
                                )

                            }
                        case _ ⇒
                            throw new IllegalArgumentException()
                    }
                case None ⇒
            }
        }

        implicit val methodReads: Writes[Method] = Json.writes[Method]
        implicit val callSiteReads: Writes[CallSite] = Json.writes[CallSite]
        implicit val callSitesReads: Writes[CallSites] = Json.writes[CallSites]


        val callSitesJson: JsValue = Json.toJson(CallSites(resultingCallSites))
        val pw = new PrintWriter(new File("result.json" ))
        pw.write(Json.prettyPrint(callSitesJson))
        pw.close
    }

    def toMethod(methodStr: String): Method = {
        """([^:]+): ([^ ]+) ([^\(]+)\(([^\)]*)\)""".r.findFirstMatchIn(methodStr) match {
            case Some(m) ⇒
                val declClass = m.group(1)
                val returnType = m.group(2)
                val name = m.group(3)
                val params = if (m.group(4).isEmpty) Array.empty[String] else m.group(4).split(",")
                Method(name, toJVMType(declClass), toJVMType(returnType), params.map(toJVMType).toList)
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
        assert(jvmRefType.size > 2)
        ObjectType(jvmRefType.substring(1, jvmRefType.size - 1))
    }

}
