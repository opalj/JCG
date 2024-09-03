import java.io.BufferedWriter
import java.io.File
import java.io.PrintWriter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object JSFingerprintExtractor extends FingerprintExtractor {
    val language = "js"

    override def generateFingerprints(config: JCGConfig): Unit = {
        if (config.debug) println("[DEBUG] " + config.language + " " + config.inputDir + " " + config.outputDir)
        println("Extracting JS fingerprints")
        if (config.debug) println("Adapters: " + config.adapters.map(_.frameworkName).mkString(", "))

        val outputDir = config.outputDir
        val inputDir = config.inputDir
        outputDir.mkdirs()
        val testDirs = new File(inputDir.getAbsolutePath).list().sorted

        val ow = new BufferedWriter(getOutputTarget(outputDir))
        printHeader(ow, testDirs.map(new File(_)))

        for {
            adapter <- config.adapters
            cgAlgorithm <- adapter.possibleAlgorithms.filter(_.startsWith(config.algorithmFilter))
        } {
            ow.write(s"${adapter.frameworkName}-$cgAlgorithm")

            println(s"creating fingerprint for ${adapter.frameworkName} $cgAlgorithm")
            val fingerprintWriter: PrintWriter = makeFingerprintWriter(outputDir, adapter, cgAlgorithm)

            // create output dir for every adapter-cgAlgorithm combination
            val adapterDir = new File(s"$outputDir/${adapter.frameworkName}/$cgAlgorithm")
            adapterDir.mkdirs()
            for (testDir <- testDirs.filter(_.startsWith(config.projectFilter))) {
                val future = Future {
                    // execute adapter
                    try {
                        adapter.serializeCG(
                            cgAlgorithm,
                            s"${inputDir.getAbsolutePath}/$testDir",
                            adapterDir.getAbsolutePath
                        )
                    } catch {
                        case e: Throwable =>
                            if (config.debug) {
                                println(e.getMessage)
                            }
                    }

                    // try reading and matching resulting call graph
                    ow.synchronized {
                        System.gc()

                        val result: Assessment = assessCG(inputDir, adapterDir, testDir)

                        ow.write(s"\t${result.shortNotation}")
                        fingerprintWriter.write(s"$testDir -> $result\n")
                        fingerprintWriter.flush()
                    }
                }
                tryAwaitGenerateCG(config.timeout, ow, fingerprintWriter, testDir, future)

            }
            ow.newLine()
            fingerprintWriter.close()
        }
        ow.close()
    }

    private def assessCG(inputDir: File, adapterDir: File, testName: String) = {
        // compare to expected call graph
        val cgFile = new File(s"${adapterDir.getAbsolutePath}/$testName.json")
        println(cgFile)
        val callGraph: Option[AdapterCG] = if (cgFile.exists()) Some(new AdapterCG(cgFile)) else None
        val expectedCGFile =
            FileOperations.listFilesRecursively(
                new File(s"${inputDir.getAbsolutePath}/$testName"),
                ".json"
            ).head

        val expectedCG = new ExpectedCG(expectedCGFile)
        val assessment: Assessment = callGraph match {
            case Some(cg) =>
                val isSound = cg.compareLinks(expectedCG).length == 0
                if (isSound) Sound else Unsound
            case None => Error
        }
        assessment
    }
}
