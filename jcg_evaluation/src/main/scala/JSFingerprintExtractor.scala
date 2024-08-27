import java.io.BufferedWriter
import java.io.File
import java.io.PrintWriter
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.TimeoutException
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt

object JSFingerprintExtractor extends FingerprintExtractor {
    val language = "js"

    override def generateFingerprints(config: JCGConfig): Unit = {
        if (config.debug) println("[DEBUG] " + config.language + " " + config.inputDir + " " + config.outputDir)
        println("Extracting JS fingerprints")
        val adapters = config.adapters.asInstanceOf[List[JSTestAdapter]]
        if (config.debug) println("Adapters: " + adapters.map(_.frameworkName).mkString(", "))

        // create output directories and execute all adapters
        val outputDir = config.outputDir
        val adapterOutputDir = config.outputDir
        val inputDir = config.inputDir
        executeAdaptersWithTimeout(config, adapters, inputDir, adapterOutputDir, config.timeout)
        return
        // executeAdaptersWithTimeout2(adapters, inputDir, adapterOutputDir, config.timeout)

        // parse expected call graph for test case from json files
        val expectedCGs: Array[ExpectedCG] =
            FileOperations.listFilesRecursively(inputDir, ".json")
                .filter(f => f.getAbsolutePath.contains("js"))
                .filter(f => f.getName.startsWith(config.projectFilter))
                .map(f => new ExpectedCG(f))
                .sorted(Ordering.by((f: ExpectedCG) => f.filePath))

        if (config.debug) {
            println("[DEBUG] expectedCGs:" + expectedCGs.map(_.filePath).mkString(","))
            val generatedCGFiles = FileOperations.listFilesRecursively(adapterOutputDir, ".json").filter(f =>
                f.getAbsolutePath.contains("js")
            )
            println("[DEBUG] generatedCGFiles: " + generatedCGFiles.mkString(","))
        }

        val outputWriter = new BufferedWriter(getOutputTarget(outputDir))

        printHeader(outputWriter, expectedCGs.map(_.jsonFile))

        for {
            adapter <- adapters
            algo <- adapter.possibleAlgorithms.filter(_.startsWith(config.algorithmFilter))
        } {
            outputWriter.write(s"${adapter.frameworkName}-$algo")

            println(s"Creating fingerprints for ${adapter.frameworkName} $algo")
            val fingerprintFile = getFingerprintFile(adapter.frameworkName, algo, outputDir)
            if (fingerprintFile.exists()) {
                fingerprintFile.delete()
            }
            val fingerprintWriter = new PrintWriter(fingerprintFile)

            for (expectedCG <- expectedCGs) {
                val testName: String = expectedCG.filePath.split("/").last
                if (config.debug) println("[DEBUG] Test name: " + testName + " " + algo)
                val generatedCGFile = new File(adapterOutputDir, s"${adapter.frameworkName}/$algo/").listFiles().find(
                    _.getName == testName
                ).orNull

                // if callgraph file does not exist write Error to result
                var assessment: Assessment = Error
                if (generatedCGFile != null) {
                    val generatedCG = new AdapterCG(generatedCGFile)
                    // check if call graph has missing edges
                    val isSound = generatedCG.compareLinks(expectedCG).length == 0
                    assessment = if (isSound) Sound else Unsound
                }

                fingerprintWriter.write(s"$testName -> $assessment\n")
                fingerprintWriter.flush()

                outputWriter.write(s"\t$assessment")
                outputWriter.flush()
            }
            outputWriter.newLine()
            fingerprintWriter.close()
        }
        outputWriter.flush()
        outputWriter.close()
    }

    /**
     * Creates output directories for each adapter and executes the adapters on the test cases.
     *
     * @param adapters  List of adapters to execute.
     * @param outputDir The output directory to write to.
     */
    private def executeAdapters(adapters: List[JSTestAdapter], inputDir: File, outputDir: File): Unit = {
        outputDir.mkdirs()

        for (adapter <- adapters) {
            // create output dir for every adapter
            val adapterDir = new File(outputDir, adapter.frameworkName)
            adapterDir.mkdirs()

            adapter.serializeAllCGs("testcasesOutput/js", adapterDir.getAbsolutePath)
        }
    }

    private def executeAdaptersWithTimeout(
        config:    JCGConfig,
        adapters:  List[TestAdapter],
        inputDir:  File,
        outputDir: File,
        timeout:   Int
    ): Unit = {
        outputDir.mkdirs()
        val testDirs = new File(inputDir.getAbsolutePath).list().sorted

        val timeoutWriter = new PrintWriter(new File("timeout.txt"))

        val ow = new BufferedWriter(getOutputTarget(outputDir))

        printHeader(ow, testDirs.map(new File(_)))
        for {
            adapter <- adapters
            cgAlgorithm <- adapter.possibleAlgorithms
        } {
            val fingerprintFile = getFingerprintFile(adapter.frameworkName, cgAlgorithm, outputDir)
            if (fingerprintFile.exists()) {
                fingerprintFile.delete()
            }
            val fingerprintWriter = new PrintWriter(fingerprintFile)

            val adapterDir = new File(s"$outputDir/${adapter.frameworkName}/$cgAlgorithm")
            adapterDir.mkdirs()
            ow.write(s"${adapter.frameworkName}-$cgAlgorithm")
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
                            println(e.getMessage)
                    }

                    // try reading and matching it
                    ow.synchronized {
                        // compare to expected call graph
                        val cgFile = new File(s"${adapterDir.getAbsolutePath}/$testDir.json")
                        println(cgFile)
                        val callGraph: Option[AdapterCG] = if (cgFile.exists()) Some(new AdapterCG(cgFile)) else None
                        val expectedCGFile =
                            FileOperations.listFilesRecursively(
                                new File(s"${inputDir.getAbsolutePath}/$testDir"),
                                ".json"
                            ).head

                        val expectedCG = new ExpectedCG(expectedCGFile)
                        val assessment: Assessment = callGraph match {
                            case Some(cg) =>
                                val isSound = cg.compareLinks(expectedCG).length == 0
                                if (isSound) Sound else Unsound
                            case None => Error
                        }

                        fingerprintWriter.write(s"$testDir -> $assessment\n")
                        fingerprintWriter.flush()
                        ow.write(s"\t$assessment")
                    }
                }
                try {
                    val duration =
                        if (timeout >= 0)
                            timeout.seconds
                        else Duration.Inf
                    Await.ready(future, duration)
                } catch {
                    case _: TimeoutException =>
                        println(s"Test case was interrupted after $timeout seconds")
                        System.gc()
                        val result = Timeout
                        ow.write(s"\t${result.shortNotation}")
                        fingerprintWriter.println(s"$testDir -> ${result.shortNotation}")
                        fingerprintWriter.flush()

                    case e: Throwable => println(e.getMessage)
                }

            }
            ow.newLine()
            fingerprintWriter.close()
        }
        timeoutWriter.close()
        ow.close()
    }
}
