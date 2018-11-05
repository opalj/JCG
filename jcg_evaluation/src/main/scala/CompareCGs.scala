import java.io.FileInputStream

import play.api.libs.json.Json

/**
 * @author Dominik Helm
 * @author Florian Kuebler
 */
object CompareCGs {

    def main(args: Array[String]): Unit = {
        var cg1Path = ""
        var cg2Path = ""
        var appPackages = List.empty[String]

        var showBoundaries = false
        var showCommon = false
        var showReachable = false
        var showAdditional = false
        var maxFindings = Int.MaxValue

        var inPackage = ""

        args.sliding(2, 2).toList.collect {
            case Array("--input1", cg) ⇒
                assert(cg1Path.isEmpty, "--input1 is specified multiple times")
                cg1Path = cg
            case Array("--input2", cg) ⇒
                assert(cg2Path.isEmpty, "--input2 is specified multiple times")
                cg2Path = cg
            case Array("--package", pkg) ⇒
                appPackages ::= pkg
            case Array("--showBoundaries", boundaries) ⇒
                showBoundaries = boundaries == "t"
            case Array("--showCommon", common) ⇒
                showCommon = common == "t"
            case Array("--showReachable", reachable) ⇒
                showReachable = reachable == "t"
            case Array("--showAdditional", additional) ⇒
                showAdditional = additional == "t"
            case Array("--maxFindings", max) ⇒
                maxFindings = max.toInt
            case Array("--inPackage", pkg) =>
                inPackage = pkg
        }

        val cg1 = Json.parse(new FileInputStream(cg1Path)).validate[ReachableMethods].get.toMap
        val cg2 = Json.parse(new FileInputStream(cg2Path)).validate[ReachableMethods].get.toMap

        if (showAdditional) {
            val additionalReachableMethods1 = extractAdditionalMethods(cg1, cg2).toSeq.sortBy(_.declaringClass).take(maxFindings)
            val additionalReachableMethods2 = extractAdditionalMethods(cg2, cg1).toSeq.sortBy(_.declaringClass).take(maxFindings)

            println(additionalReachableMethods1.mkString(" ##### Additional Methods - Input 1 #####\n\n", "\n\t", "\n\n"))
            println(additionalReachableMethods2.mkString(" ##### Additional Methods - Input 2 #####\n\n", "\n\t", "\n\n"))
        }

        val commonReachableMethods = if (showCommon || showBoundaries) {
            cg1.filter(m ⇒ cg2.contains(m._1)).keySet
        } else
            Set.empty[Method]

        if(showCommon) {
            println(commonReachableMethods.toSeq.sortBy(_.declaringClass).take(maxFindings).mkString(" ##### Common Methods #####\n\n", "\n\t", "\n\n"))
        }

        if(showReachable) {
            val reachableInApp1 = extractReachableApplicationMethods(appPackages, cg1).toSeq.sortBy(_.declaringClass).take(maxFindings)
            val reachableInApp2 = extractReachableApplicationMethods(appPackages, cg2).toSeq.sortBy(_.declaringClass).take(maxFindings)

            println(reachableInApp1.mkString(" ##### Reachable Application Methods - Input 1 #####\n\n", "\n\t", "\n\n"))
            println(reachableInApp2.mkString(" ##### Reachable Application Methods - Input 2 #####\n\n", "\n\t", "\n\n"))

        }

        if (showBoundaries) {
            val boundaries1 = extractBoundaries(cg1, commonReachableMethods, inPackage).toSeq.sortBy(_.declaringClass).take(maxFindings)
            val boundaries2 = extractBoundaries(cg2, commonReachableMethods, inPackage).toSeq.sortBy(_.declaringClass).take(maxFindings)

            println(boundaries1.mkString(" ##### Boundary Methods - Input 1 #####\n\n", "\n\t", "\n\n"))
            println(boundaries2.mkString(" ##### Boundary Methods - Input 2 #####\n\n", "\n\t", "\n\n"))
        }
    }

    private def extractAdditionalMethods(
        baseCG: Map[Method, Set[CallSite]], comparedTo: Map[Method, Set[CallSite]]
    ): Set[Method] = {
        baseCG.filter(m ⇒ !comparedTo.contains(m._1)).keySet
    }

    private def extractReachableApplicationMethods(
        appPackages: List[String], cg: Map[Method, Set[CallSite]]
    ): Set[Method] = {
        cg.filter(rm ⇒ appPackages.exists(p ⇒ rm._1.declaringClass.startsWith(s"L$p/"))).keySet
    }

    private def extractBoundaries(
        cg: Map[Method, Set[CallSite]], commonReachableMethods: Set[Method], inPackage: List[String]
    ): Set[Method] = {
        commonReachableMethods.filter { caller ⇒
            if(caller.declaringClass.startsWith(inPackage)){
                val callees = cg(caller).flatMap(_.targets)
                callees.exists(target ⇒ !commonReachableMethods.contains(target))
            } else {
                false
            }
        }
    }
}
