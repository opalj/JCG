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
        val additionalReachableMethods1 = cg1.filter(m ⇒ !cg2.contains(m._1))
        val additionalReachableMethods2 = cg2.filter(m ⇒ !cg1.contains(m._1))
        val commonReachableMethods = cg1.filter(m ⇒ cg2.contains(m._1)).keySet

        val reachableInApp1 = extractReachableApplicationMethods(appPackages, cg1)

        val reachableInApp2 = extractReachableApplicationMethods(appPackages, cg2)

        val boundaries1 = extractBoundaries(cg1, commonReachableMethods)

        val boundaries2 = extractBoundaries(cg2, commonReachableMethods)
    }

    private def extractReachableApplicationMethods(
        appPackages: List[String], cg: Map[Method, Set[CallSite]]
    ): Set[Method] = {
        for {
            caller ← cg.keySet
            if appPackages.exists(p ⇒ caller.declaringClass.startsWith(s"L$p/"))
        } yield caller
    }

    private def extractBoundaries(
        cg: Map[Method, Set[CallSite]], commonReachableMethods: Set[Method]
    ): Set[Method] = {
        for {
            caller ← commonReachableMethods
            callees = cg(caller).flatMap(_.targets)
            if callees.exists(target ⇒ !commonReachableMethods.contains(target))
        } yield caller
    }
}
