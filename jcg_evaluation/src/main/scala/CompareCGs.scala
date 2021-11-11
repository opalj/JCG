import java.io.FileInputStream
import java.util

import play.api.libs.json.Json

import scala.collection.mutable
import scala.collection.JavaConverters._
import java.util.{HashSet => JHashSet}



/**
 * @author Dominik Helm
 * @author Florian Kuebler
 * @author Michael Reif
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
        var showAdditionalCalls = false
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
            case Array("--showAdditionalCalls", additionalCalls) ⇒
                showAdditionalCalls = additionalCalls == "t"
            case Array("--maxFindings", max) ⇒
                maxFindings = max.toInt
            case Array("--inPackage", pkg) ⇒
                inPackage = pkg
        }

        val cg1 = Json.parse(new FileInputStream(cg1Path)).validate[ReachableMethods].get.toMap
        val cg2 = Json.parse(new FileInputStream(cg2Path)).validate[ReachableMethods].get.toMap

        /*
        for {
            (m1, cs1) ← cg1
        } {
            val cs2 = cg2(m1)
            if(cs1.size != cs2.size){
                println(s" #### Method difference: ${m1.toString}")
                println(cs1.mkString("[", ",", "]"))
                println(cs2.mkString("[", ",", "]"))
            }
        }
         */

        if (showAdditional) {
            val additionalReachableMethods1 = extractAdditionalMethods(cg1, cg2).toSeq.sortBy(_.declaringClass).take(maxFindings)
            val additionalReachableMethods2 = extractAdditionalMethods(cg2, cg1).toSeq.sortBy(_.declaringClass).take(maxFindings)

            println(additionalReachableMethods1.mkString(" ##### Additional Methods - Input 1 #####\n\n\t", "\n\t", "\n\n"))
            println(additionalReachableMethods2.mkString(" ##### Additional Methods - Input 2 #####\n\n\t", "\n\t", "\n\n"))
        }

        val commonReachableMethods : java.util.HashSet[Method] = if (showCommon || showBoundaries) {
            val cg1Keys = cg1.keySet.iterator
            val cg2Keys = cg2.keySet
            val common = new java.util.HashSet[Method]()
            while(cg1Keys.hasNext) {
                val m1 = cg1Keys.next()
                if(cg2.contains(m1))
                    common add m1
            }
            common
        } else new util.HashSet[Method]()

        if (showCommon) {
            val seq = commonReachableMethods.asScala.toSeq
            println(seq.sortBy(_.declaringClass).take(maxFindings).mkString(" ##### Common Methods #####\n\n\t", "\n\t", "\n\n"))
        }

        if (showReachable) {
            val reachableInApp1 = extractReachableApplicationMethods(appPackages, cg1).toSeq.sortBy(_.declaringClass).take(maxFindings)
            val reachableInApp2 = extractReachableApplicationMethods(appPackages, cg2).toSeq.sortBy(_.declaringClass).take(maxFindings)

            println(reachableInApp1.mkString(" ##### Reachable Application Methods - Input 1 #####\n\n\t", "\n\t", "\n\n"))
            println(reachableInApp2.mkString(" ##### Reachable Application Methods - Input 2 #####\n\n\t", "\n\t", "\n\n"))

        }

        if (showBoundaries) {
            val boundaries1 = extractBoundaries(cg1, commonReachableMethods, inPackage).asScala.toSeq.sortBy(_.m.declaringClass).take(maxFindings)
            val boundaries2 = extractBoundaries(cg2, commonReachableMethods, inPackage).asScala.toSeq.sortBy(_.m.declaringClass).take(maxFindings)

            println(boundaries1.mkString(" ##### Boundary Methods - Input 1 #####\n\n\t", "\n\t", "\n\n"))
            println(boundaries2.mkString(" ##### Boundary Methods - Input 2 #####\n\n\t", "\n\t", "\n\n"))
        }

        if (showAdditionalCalls) {
            val additional1 = extractAdditionalCalls(cg1, cg2, inPackage).toSeq.sortBy(_._1.declaringClass).take(maxFindings)
            val additional2 = extractAdditionalCalls(cg2, cg1, inPackage).toSeq.sortBy(_._1.declaringClass).take(maxFindings)

            println(additional1.mkString(" ##### Additional Calls - Input 1 #####\n\n\t", "\n\t", "\n\n"))
            println(additional2.mkString(" ##### Additional Calls - Input 2 #####\n\n\t", "\n\t", "\n\n"))
        }
    }

    private def extractAdditionalMethods(
        baseCG: Map[Method, Set[CallSite]], comparedTo: Map[Method, Set[CallSite]]
    ): Set[Method] = {
        baseCG.keySet.filter(!comparedTo.contains(_))
//        baseCG.filter(m ⇒ !comparedTo.contains(m.._1)).keySet
    }

    private def extractReachableApplicationMethods(
        appPackages: List[String], cg: Map[Method, Set[CallSite]]
    ): Set[Method] = {
        cg.filter(rm ⇒ appPackages.iterator.exists(p ⇒ rm._1.declaringClass.startsWith(s"L$p/"))).keySet
    }

    case class MethodBoundary(m: Method, target: String)

    private def extractBoundaries(
        cg: Map[Method, Set[CallSite]], commonReachableMethods: JHashSet[Method], inPackage: String
    ): JHashSet[MethodBoundary] = {
        val boundaries = new JHashSet[MethodBoundary]()

        val itr = commonReachableMethods.iterator()
        while(itr.hasNext) {
            val caller = itr.next()
            if(caller.declaringClass.startsWith(inPackage)) {
                val callees = cg(caller).flatMap(_.targets)
                if(callees.iterator.exists(!commonReachableMethods.contains(_))){
                    val differences = new StringBuilder("\n\t\t")
                    callees.iterator.foreach { callee =>
                        if(!commonReachableMethods.contains(callee)) {
                            differences.append(s"${transitiveHull(callee, cg, commonReachableMethods)}: $callee\n\t\t")
                        }
                    }
                    boundaries.add(MethodBoundary(caller, differences.result()))
                }
            }
        }

        boundaries
//        commonReachableMethods.filter { caller ⇒
//            if (caller.declaringClass.startsWith(inPackage)) {
//                val callees = cg(caller).flatMap(_.targets)
//                callees.iterator.exists(target ⇒ !commonReachableMethods.contains(target))
//            } else {
//                false
//            }
//        }.map { caller ⇒
//            val callees = cg(caller).flatMap(_.targets)
//            (caller, callees.diff(commonReachableMethods).map(m ⇒ s"${transitiveHull(m, cg, commonReachableMethods)}: $m").mkString("\n\t\t", "\n\t\t", ""))
//        }
    }

    private def extractAdditionalCalls(
        cg: Map[Method, Set[CallSite]], otherCG: Map[Method, Set[CallSite]], inPackage: String
    ): Set[(Method, String)] = {
        var result = Set.empty[(Method, String)]
        cg.foreach {
            case (method, callSites) ⇒
                val otherCallSites = otherCG.get(method)
                if (otherCallSites.isDefined) {
                    callSites.foreach {
                        case CallSite(declared, line, pc, targets) ⇒
                            val differingCSOpt = otherCallSites.get.find(cs ⇒ cs.declaredTarget == declared && cs.line == line && cs.pc == pc)
                            if (differingCSOpt.isDefined) {
                                val differingCS = differingCSOpt.get
                                if (differingCS.targets.size < targets.size) {
                                    val diffs = targets -- differingCS.targets
                                    result += ((method, diffs.mkString("\n\t\t", "\n\t\t", "\n")))
                                }
                            } else {
                                result += ((method, targets.mkString("\n\t\t", "\n\t\t", "")))
                            }
                    }
                }
        }

        result
    }

    private def transitiveHull(method: Method, cg: Map[Method, Set[CallSite]], commonReachableMethods: JHashSet[Method]): (Int, Int) = {
        val reachableMethods = new java.util.HashSet[Method]()
        reachableMethods add method
        val nonCommon = new java.util.HashSet[Method]()
        nonCommon add method

        var worklist: mutable.Queue[Method] = mutable.Queue(method)

        while (worklist.nonEmpty) {
            val currentMethod = worklist.head
            worklist = worklist.tail
            for {
                cs ← cg(currentMethod)
                t ← cs.targets
            } {
                if (!reachableMethods.contains(t)) {
                    if (!commonReachableMethods.contains(t))
                        nonCommon add t
                    reachableMethods add t
                    worklist enqueue t
                }
            }
        }
        (reachableMethods.size, nonCommon.size)
    }
}
