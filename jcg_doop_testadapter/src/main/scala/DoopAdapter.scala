
import scala.collection.mutable
import scala.io.Source


case class Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String])

object DoopAdapter {

    def main(args: Array[String]): Unit = {

        val source = Source.fromFile("/Users/floriankuebler/Desktop/edges.txt")

        val re = """\[\d+\]\*\d+, \[\d+\]<([^><]+(<clinit>|<init>)?[^>]*)>/([^/]+)/(\d+), \[\d+\]\*\d+, \[\d+\]<([^><]+(<clinit>|<init>)?[^>]*)>""".r ////([^/]+)/(\d+), \[\d+\\]\*\d+, \[\d+\]<([^><]+(<clinit>|<init>)?[^>]*)>""".r

        val callGraph = mutable.Map.empty[String, mutable.Map[(String, Int), mutable.Set[String]]].withDefaultValue(mutable.OpenHashMap.empty.withDefaultValue(mutable.Set.empty))

        for (line ← source.getLines()) {
            // there is at most one occurrence per line

            re.findFirstMatchIn(line) match {
                case Some(x) ⇒
                    val caller = x.group(1)
                    val declaredTgt = x.group(3)
                    val number = x.group(4).toInt
                    val tgt = x.group(5)

                    val callSite = declaredTgt → number
                    val currentCallsites = callGraph(caller)
                    val currentCallees = currentCallsites(callSite)

                    currentCallees += tgt
                    currentCallsites += (callSite → currentCallees)
                    callGraph += (caller → currentCallsites)
                case _ ⇒ // no match
            }
        }
        source.close()
        println("read file")

        for {
            (caller, callSites) ← callGraph
        } {
            val callerMethod = toMethod(caller)

            for (((declaredTgt, number), tgts) ← callSites) {
                val tgtMethods = tgts.map(toMethod)
            }
        }
    }

    def toMethod(methodStr: String): Method = {
        """([^:]+): ([^ ]+) ([^\(]+)\(([^\)]*)\)""".r.findFirstMatchIn(methodStr) match {
            case Some(m) ⇒
                val declClass = m.group(1)
                val returnType = m.group(2)
                val name = m.group(3)
                val params = m.group(4).split(",")
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
}
