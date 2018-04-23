import java.io.File

import lib.annotations.documentation.CGFeature

object Evaluation {
    def main(args: Array[String]): Unit = {
        val rtJar = "/Library/Java/JavaVirtualMachines/jdk1.8.0_162.jdk/Contents/Home/jre/lib/rt.jar"
        val jarDir = new File("result/")
        if (jarDir.exists && jarDir.isDirectory) {
            val jars = jarDir.listFiles((_, name) ⇒ name.endsWith(".jar")).sorted
            // print header
            print("algorithm")
            for (tgt <- jars) {
                print(s"\t$tgt")
            }
            println()
            // do the hard work
            for (adapter ← List(new SootJCGAdatper(), new WalaJCGAdapter)) {
                // soot
                for (cgAlgo ← adapter.possibleAlgorithms()) {
                    print(s"${adapter.frameworkName()} $cgAlgo")
                    for (tgt ← jars) {
                        //print(s"\tproject $tgt\t")
                        adapter.serializeCG(cgAlgo, tgt.getAbsolutePath, rtJar, s"${adapter.frameworkName()}-$cgAlgo-${tgt.getName}.json")
                        System.gc()
                        print(s"\t${CGMatcher.matchCallSites(tgt.getAbsolutePath, s"${adapter.frameworkName()}-$cgAlgo-${tgt.getName}.json")}")
                    }
                    println()
                }
            }
            // List("RTA", "0-CFA", "1-CFA", "0-1-CFA")
            // WalaJCGAdapter.main(Array(cgAlgo, tgt.getAbsolutePath, s"wala-$cgAlgo-${tgt.getName}.json"))
        }
    }
}
