import java.io.File
import java.io.FileInputStream
import java.util

import play.api.libs.json.Json
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import java.util.{HashSet ⇒ JHashSet}

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

        var showMethodPrecisionRecall = false
        var showEdgePrecisionRecall = false
        var showBoundaries = false
        var showCommon = false
        var showReachable = false
        var showAdditional = false
        var showAdditionalCalls = false
        var maxFindings = Int.MaxValue

        var inPackage = ""
        var strict = true

        args.sliding(2, 1).toList.collect {
            case Array("--input1", cg) ⇒
                assert(cg1Path.isEmpty, "--input1 is specified multiple times")
                cg1Path = cg
            case Array("--input2", cg) ⇒
                assert(cg2Path.isEmpty, "--input2 is specified multiple times")
                cg2Path = cg
            case Array("--package", pkg) ⇒
                appPackages ::= pkg
            case Array("--showPrecisionRecall", preRec) ⇒
                preRec match {
                    case "methods" ⇒
                        showMethodPrecisionRecall = true
                    case "edges" ⇒
                        showEdgePrecisionRecall = true
                    case "all" ⇒
                        showMethodPrecisionRecall = true
                        showEdgePrecisionRecall = true
                }
            case Array("--maxFindings", max) ⇒
                maxFindings = max.toInt
            case Array("--inPackage", pkg) ⇒
                inPackage = pkg
        }

        args.sliding(1, 1).toList.collect {
            case Array("--showBoundaries") ⇒ showBoundaries = true
            case Array("--showCommon") ⇒ showCommon = true
            case Array("--showReachable") ⇒ showReachable = true
            case Array("--showAdditional") ⇒ showAdditional = true
            case Array("--showAdditionalCalls") ⇒ showAdditionalCalls = true
            case Array("--nonStrict") ⇒ strict = false

        }

        val cg1 = EvaluationHelper.readCG(new File(cg1Path)).toMap
        val cg2 = EvaluationHelper.readCG(new File(cg2Path)).toMap

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

        if (showMethodPrecisionRecall) {
            val falsePositive = extractAdditionalMethods(cg2, cg1, inPackage).size
            val positive = if(inPackage.isEmpty) cg2.size else cg2.keysIterator.count(_.declaringClass.startsWith(inPackage))
            val truth = if(inPackage.isEmpty) cg1.size else cg1.keysIterator.count(_.declaringClass.startsWith(inPackage))
            val truePositive = positive - falsePositive
            println(f"Method precision: $truePositive/$positive = ${truePositive.toDouble / positive * 100}%.2f%%")
            println(f"Method recall: $truePositive/$truth = ${truePositive.toDouble / truth * 100}%.2f%%")
            println(f"Method F1-score: ${2 * truePositive.toDouble / (truePositive + falsePositive + truth) * 100}%.2f")
        }

        if (showEdgePrecisionRecall) {
            val falsePositive = countAdditionalEdges(cg2, cg1, inPackage, strict)
            val positive = edgeCount(cg2, inPackage)
            val truth = edgeCount(cg1, inPackage)
            val truePositive = positive - falsePositive
            println(f"Edge precision: $truePositive/$positive = ${truePositive.toDouble / positive * 100}%.2f%%")
            println(f"Edge recall: $truePositive/$truth = ${truePositive.toDouble / truth * 100}%.2f%%")
            println(f"Edge F1-score: ${2 * truePositive.toDouble / (truePositive + falsePositive + truth) * 100}%.2f")
        }

        if (showAdditional) {
            val additionalReachableMethods1 = extractAdditionalMethods(cg1, cg2, inPackage).toSeq.sortBy(_.declaringClass).take(maxFindings)
            println(additionalReachableMethods1.mkString(" ##### Additional Methods - Input 1 #####\n\n\t", "\n\t", "\n\n"))

            val additionalReachableMethods2 = extractAdditionalMethods(cg2, cg1, inPackage).toSeq.sortBy(_.declaringClass).take(maxFindings)
            println(additionalReachableMethods2.mkString(" ##### Additional Methods - Input 2 #####\n\n\t", "\n\t", "\n\n"))
        }

        val commonReachableMethods: java.util.HashSet[Method] = if (showCommon || showBoundaries) {
            val cg1Keys = cg1.keySet.iterator
            val common = new java.util.HashSet[Method]()
            while (cg1Keys.hasNext) {
                val m1 = cg1Keys.next()
                if (cg2.contains(m1))
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
            println(boundaries1.mkString(" ##### Boundary Methods - Input 1 #####\n\n\t", "\n\t", "\n\n"))

            val boundaries2 = extractBoundaries(cg2, commonReachableMethods, inPackage).asScala.toSeq.sortBy(_.m.declaringClass).take(maxFindings)
            println(boundaries2.mkString(" ##### Boundary Methods - Input 2 #####\n\n\t", "\n\t", "\n\n"))
        }

        if (showAdditionalCalls) {
            val additional1 = extractAdditionalCalls(cg1, cg2, inPackage, strict).toSeq.sortBy(_._1.declaringClass).take(maxFindings)
            val additional2 = extractAdditionalCalls(cg2, cg1, inPackage, strict).toSeq.sortBy(_._1.declaringClass).take(maxFindings)

            println(additional1.mkString(" ##### Additional Calls - Input 1 #####\n\n\t", "\n\t", "\n\n"))
            println(additional2.mkString(" ##### Additional Calls - Input 2 #####\n\n\t", "\n\t", "\n\n"))
        }

        val sites = cg1.keySet.filter(!cg2.contains(_)).map { m ⇒
            val css = cg1(m)
            if(css.nonEmpty) {
                val additional = cg1(m).maxBy { cs ⇒
                    cs.targets.size
                }
                (m, additional.pc, additional.targets.size)
            } else {
                (m, None, 0)
            }
        }

        //println(sites.toSeq.sortBy(_._3).takeRight(100).mkString(" #### Impactful Call Sites ####\n\n\t", "\n\t", "\n\n"))
    }

    private def edgeCount(cg: Map[Method, Set[CallSite]], inPackage: String): Int = {
        cg.foldLeft(0) { (acc, rm) ⇒
            if(rm._1.declaringClass.startsWith(inPackage))
                acc + rm._2.foldLeft(0)((acc, cs) ⇒ acc + cs.targets.size)
            else acc
        }
    }

    private def extractAdditionalMethods(
        baseCG: Map[Method, Set[CallSite]], comparedTo: Map[Method, Set[CallSite]], inPackage: String
    ): Set[Method] = {
        baseCG.keySet.filter(m => !comparedTo.contains(m) && m.declaringClass.startsWith(inPackage))
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
        while (itr.hasNext) {
            val caller = itr.next()
            if (caller.declaringClass.startsWith(inPackage)) {
                val callees = cg(caller).flatMap(_.targets)
                if (callees.iterator.exists(!commonReachableMethods.contains(_))) {
                    val differences = new StringBuilder("\n\t\t")
                    callees.iterator.foreach { callee ⇒
                        if (!commonReachableMethods.contains(callee)) {
                            differences.append(s"${transitiveHull(callee, cg, commonReachableMethods)}: $callee\n\t\t")
                        }
                    }
                    boundaries.add(MethodBoundary(caller, differences.result()))
                }
            }
        }

        boundaries
    }

    private def countAdditionalEdges(
        cg: Map[Method, Set[CallSite]], otherCG: Map[Method, Set[CallSite]], inPackage: String, strict: Boolean
    ): Int = {
        var result = 0
        cg.foreach {
            case (method, callSites) if method.declaringClass.startsWith(inPackage) ⇒
                val otherCallSites = otherCG.get(method)
                callSites.foreach {
                    case CallSite(declared, line, pc, targets) ⇒
                        if (otherCallSites.isDefined) {
                            if(pc.isEmpty){
                                val possibleCSs = otherCallSites.get.filter{cs ⇒
                                    (!strict || cs.declaredTarget == declared) && cs.line == line && cs.targets.exists{target ⇒
                                        target.name == declared.name && target.returnType == declared.returnType && target.parameterTypes == declared.parameterTypes
                                    }}
                                val diffs = possibleCSs.foldLeft(targets)((acc, cs) ⇒ acc -- cs.targets)
                                result += diffs.size
                            } else {
                                val differingCSOpt = otherCallSites.get.find(cs ⇒ (!strict || cs.declaredTarget == declared && cs.line == line) && cs.pc == pc && cs.targets.exists { target ⇒
                                    target.name == declared.name && target.returnType == declared.returnType && target.parameterTypes == declared.parameterTypes
                                })
                                if (differingCSOpt.isDefined) {
                                    val differingCS = differingCSOpt.get
                                    val diffs = targets -- differingCS.targets
                                    result += diffs.size
                                } else {
                                    result += targets.size
                                }
                            }
                        } else {
                            result += targets.size
                        }
                }
            case _ ⇒
        }

        result
    }

    private def extractAdditionalCalls(
        cg: Map[Method, Set[CallSite]], otherCG: Map[Method, Set[CallSite]], inPackage: String, strict: Boolean
    ): Set[(Method, String)] = {
        var result = Set.empty[(Method, String)]
        cg.foreach {
            case (method, callSites) if method.declaringClass.startsWith(inPackage) ⇒
                val otherCallSites = otherCG.get(method)
                if (otherCallSites.isDefined) {
                    callSites.foreach {
                        case CallSite(declared, line, pc, targets) ⇒
                            val differingCSOpt = otherCallSites.get.find(cs ⇒ (!strict || cs.declaredTarget == declared && cs.line == line) && cs.pc == pc)
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
            case _ ⇒
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
            if (cg.contains(currentMethod)) {
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
        }
        (reachableMethods.size, nonCommon.size)
    }
}
