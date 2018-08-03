import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.io.Writer

import org.opalj.bytecode

object Evaluation {

    val debug = false;
    val OUTPUT_FILENAME = "evaluation_results.tsv"
    val JAR_DIR_PATH = "result/"
    val EVALUATION_ADAPTERS = List(new SootJCGAdatper(), new WalaJCGAdapter())

    def main(args: Array[String]): Unit = {
        val rtJar = bytecode.RTJar.getAbsolutePath
        val jarDir = new File(JAR_DIR_PATH)

        var jarFilter = ""
        var target = ""
        args.sliding(2, 2).toList.collect {
            case Array("--output", t: String)    ⇒ target = t
            case Array("--filter", name: String) ⇒ jarFilter = name
        }

        val outputTarget = getOutputTarget(target)
        val ow = new BufferedWriter(outputTarget)

        if (jarDir.exists && jarDir.isDirectory) {
            val jars = jarDir.listFiles((_, name) ⇒ name.endsWith(".jar")).sorted.filter(_.getName.startsWith(jarFilter))
            printHeader(ow, jars)
            for (adapter ← EVALUATION_ADAPTERS) {
                for (cgAlgo ← adapter.possibleAlgorithms()) {
                    ow.write(s"${adapter.frameworkName()} $cgAlgo")
                    for (tgt ← jars) {
                        try {
                            adapter.serializeCG(cgAlgo, tgt.getAbsolutePath, Array(rtJar), s"${adapter.frameworkName()}-$cgAlgo-${tgt.getName}.json")
                            System.gc()
                            val result = CGMatcher.matchCallSites(tgt.getAbsolutePath, s"${adapter.frameworkName()}-$cgAlgo-${tgt.getName}.json")
                            ow.write(s"\t${result.shortNotation}")
                        } catch {
                            case e: Throwable ⇒
                                if(debug)
                                    println(e.printStackTrace());
                                ow.write(s"\tE")
                        }
                    }
                    ow.newLine()
                }
            }
            ow.flush()
            ow.close()
        }
    }

    private def printHeader(ow: BufferedWriter, jars: Array[File]) = {
        ow.write("algorithm")
        for (tgt ← jars) {
            ow.write(s"\t$tgt")
        }
        ow.newLine()
    }

    def getOutputTarget(target: String): Writer = {
        target match {
            case "c" ⇒ new OutputStreamWriter(System.out)
            case "f" ⇒ {
                val outputFile = new File(OUTPUT_FILENAME);
                if (outputFile.exists()) {
                    outputFile.delete()
                    outputFile.createNewFile()
                }

                new FileWriter(outputFile, false)
            }
            case _ ⇒ new OutputStreamWriter(System.out)
        }
    }
}
