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

        args.sliding(2, 2).toList.collect {
            case Array("--input1", cg) ⇒
                assert(cg1Path.isEmpty, "--input1 is specified multiple times")
                cg1Path = cg
            case Array("--input2", cg) ⇒
                assert(cg2Path.isEmpty, "--input2 is specified multiple times")
                cg2Path = cg
            case Array("--package", pkg) ⇒
                appPackages ::= pkg
        }

        val cg1 = Json.parse(new FileInputStream(cg1Path)).validate[ReachableMethods].get.toMap
        val cg2 = Json.parse(new FileInputStream(cg2Path)).validate[ReachableMethods].get.toMap

        //TODO output!
        val additionalReachableMethods1 = extractAdditionalMethods(cg1, cg2)
        val additionalReachableMethods2 = extractAdditionalMethods(cg2, cg1)

        val commonReachableMethods = cg1.filter(m ⇒ cg2.contains(m._1)).keySet

        val reachableInApp1 = extractReachableApplicationMethods(appPackages, cg1)

        val reachableInApp2 = extractReachableApplicationMethods(appPackages, cg2)

        val boundaries1 = extractBoundaries(cg1, commonReachableMethods)

        val boundaries2 = extractBoundaries(cg2, commonReachableMethods)
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
        cg: Map[Method, Set[CallSite]], commonReachableMethods: Set[Method]
    ): Set[Method] = {
        commonReachableMethods.filter { caller ⇒
            val callees = cg(caller).flatMap(_.targets)
            callees.exists(target ⇒ !commonReachableMethods.contains(target))
        }
    }
}
