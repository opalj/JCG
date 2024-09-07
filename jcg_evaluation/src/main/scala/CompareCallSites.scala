
import java.io.File
import java.io.FileInputStream

import play.api.libs.json.Json

/**
 * @author Michael Reif
 */
object CompareCallSites {

    def main(args: Array[String]): Unit = {
        var cg1Path = ""
        var cg2Path = ""

        var methodName = ""
        var declaringClassName = ""
        var sizeGap = 1
        var exact = false

        args.sliding(2, 2).toList.collect {
            case Array("--input1", cg) ⇒
                assert(cg1Path.isEmpty, "--input1 is specified multiple times")
                cg1Path = cg
            case Array("--input2", cg) ⇒
                assert(cg2Path.isEmpty, "--input2 is specified multiple times")
                cg2Path = cg
            case Array("--name", name) ⇒
                methodName = name
            case Array("--class", declClass) ⇒
                declaringClassName = declClass
            case Array("--sizeGap", gap) ⇒
                sizeGap = gap.toInt
        }

        val cg1 = EvaluationHelper.readCG(new File(cg1Path)).toMap
        val cg2 = EvaluationHelper.readCG(new File(cg2Path)).toMap

        val commonReachableMethods = cg1.filter(m ⇒ cg2.contains(m._1)).keySet

        val methodFilter = (methodName, declaringClassName) match {
            case ("", "") => (m: Method) => true
            case (_,"") => (m: Method) => m.name == methodName
            case ("",_) => (m: Method) => m.declaringClass == declaringClassName
            case _ => (m: Method) => m.name == methodName && m.declaringClass == declaringClassName
        }


        for {
            m <- commonReachableMethods.filter(methodFilter)
            cg1targets = cg1(m).flatMap(_.targets)
            cg2targets = cg2(m).flatMap(_.targets)
        } {
            val sizeDiff = (cg1targets.size, cg2targets.size) match {
                case (x, y) if x == y => 0
                case (x, y) if x > y => x - y
                case (x, y) if x < y => y - x
            }

            sizeDiff match {
                case 0 if sizeGap == 0 && !cg1targets.equals(cg2targets) => printDiff(m, cg1targets, cg2targets)
                case x if x >= sizeGap => printDiff(m, cg1targets, cg2targets)
                case _ => /* do nothing they are definately the same */
            }
        }
    }

    def printDiff(method: Method, cg1targets: Set[Method], cg2targets: Set[Method]) : Unit = {

        val resultInput1 = cg1targets.filter(!cg2targets.contains(_))
        val resultInput2 = cg2targets.filter(!cg1targets.contains(_))
        val common = cg1targets.intersect(cg2targets)

        println("################################################################")
        println(s"####### Callsite: ${method.toString} #######")
        println("################################################################\n\n\n")

        println(common.mkString("############ Common #############\n\n\t", "\n\t", "\n\n"))
        println(resultInput1.mkString("############ CG 1 #############\n\n\t", "\n\t", "\n\n"))
        println(resultInput2.mkString("############ CG 2 #############\n\n\t", "\n\t", "\n\n"))
    }
}
