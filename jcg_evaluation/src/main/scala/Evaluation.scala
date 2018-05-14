import java.io.File

import org.opalj.bytecode

object Evaluation {
    def main(args: Array[String]): Unit = {
        val rtJar = bytecode.RTJar.getAbsolutePath
        val jarDir = new File("result/")
        if (jarDir.exists && jarDir.isDirectory) {
            val jarFilter = if(args.length > 0) args(0) else "";
            val jars = jarDir.listFiles((_, name) ⇒ name.endsWith(".jar")).sorted.filter(_.getName.startsWith(jarFilter))
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
                        adapter.serializeCG(cgAlgo, tgt.getAbsolutePath, rtJar, s"${adapter.frameworkName()}-$cgAlgo-${tgt.getName}.json")
                        System.gc()
                        print(s"\t${CGMatcher.matchCallSites(tgt.getAbsolutePath, s"${adapter.frameworkName()}-$cgAlgo-${tgt.getName}.json")}")
                    }
                    println()
                }
            }
        }
    }
}
