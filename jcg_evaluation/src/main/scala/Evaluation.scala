import java.io.File

import lib.annotations.documentation.CGFeature

object Evaluation {
    def main(args: Array[String]): Unit = {
        val rtJar = "/Library/Java/JavaVirtualMachines/jdk1.8.0_161.jdk/Contents/Home/jre/lib/rt.jar"
        val jarDir = new File("result/")
        if (jarDir.exists && jarDir.isDirectory) {
            for (adapter ← List(new SootJCGAdatper(), new WalaJCGAdapter)) {
                // soot
                for (cgAlgo ← adapter.possibleAlgorithms()) {
                    println(s"${adapter.frameworkName()} $cgAlgo")
                    val jars = jarDir.listFiles((_, name) ⇒ name.endsWith(".jar"))
                    for (tgt ← jars) {
                        println(s"project $tgt")
                        adapter.serializeCG(cgAlgo, tgt.getAbsolutePath, rtJar, s"soot-$cgAlgo-${tgt.getName}.json")
                        System.gc()
                        println(CGMatcher.matchCallSites(tgt.getAbsolutePath, s"soot-$cgAlgo-${tgt.getName}.json"))
                    }
                }
            }
            // List("RTA", "0-CFA", "1-CFA", "0-1-CFA")
            // WalaJCGAdapter.main(Array(cgAlgo, tgt.getAbsolutePath, s"wala-$cgAlgo-${tgt.getName}.json"))
        }
    }
}
