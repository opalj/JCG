import java.io.File

object Evaluation {
    def main(args: Array[String]): Unit = {
        val rtJar = "/Library/Java/JavaVirtualMachines/jdk1.8.0_161.jdk/Contents/Home/jre/lib/rt.jar"
        val jarDir = new File("jars/")
        if (jarDir.exists && jarDir.isDirectory) {
            val jars = jarDir.listFiles((_, name) ⇒ name.endsWith(".jar"))
            for (tgt ← jars) {
                println(tgt)
                //run soot
                for (cgAlgo ← List("CHA", "VTA", "RTA", "SPARK")) {
                    println(s"soot $cgAlgo")
                    SootJCGAdatper.main(Array(cgAlgo, tgt.getAbsolutePath, rtJar, s"soot-$cgAlgo-${tgt.getName}.json"))
                    System.gc()
                    val (x, y) = CGMatcher.matchCallSites(tgt.getAbsolutePath, s"soot-$cgAlgo-${tgt.getName}.json")
                    println(x + " " + y)
                }

                //run wala
                for (cgAlgo ← List("CHA", "RTA", "0-CFA", "1-CFA", "0-1-CFA")) {
                    println(s"wala $cgAlgo")
                    WalaJCGAdapter.main(Array(cgAlgo, tgt.getAbsolutePath, s"wala-$cgAlgo-${tgt.getName}.json"))
                    System.gc()
                    CGMatcher.matchCallSites(tgt.getAbsolutePath, s"wala-$cgAlgo-${tgt.getName}.json")
                }
            }
        }
    }
}
