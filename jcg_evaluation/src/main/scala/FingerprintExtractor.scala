import java.io._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.TimeoutException
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.io.Source

trait FingerprintExtractor {
    val EVALUATION_RESULT_FILE_NAME = "evaluation-result.tsv"
    val language: String

    def generateFingerprints(config: JCGConfig): Unit = {
        if (config.debug) println("[DEBUG] " + config.language + " " + config.inputDir + " " + config.outputDir)
        println(s"Extracting $language fingerprints")
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
                val output = new BufferedWriter(new FileWriter(adapterDir.getAbsolutePath + "/" + testDir + ".json"))

                val future = Future {
                    // execute adapter
                    try {
                        adapter.serializeCG(
                            cgAlgorithm,
                            s"${inputDir.getAbsolutePath}/$testDir",
                            output
                        )
                    } catch {
                        case e: Throwable =>
                            if (config.debug) {
                                println(e.getMessage)
                            }
                    } finally {
                        output.close()
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

    /**
     * Compares the call graph of a test case with the expected call graph and returns assessment.
     * @param inputDir The directory containing the expected call graphs.
     * @param adapterDir The directory containing the generated call graphs.
     * @param testName The name of the test case.
     * @return The assessment of the call graph (Sound, Unsound, Error). Assessment is Error if no generated CG exists.
     */
    def assessCG(inputDir: File, adapterDir: File, testName: String): Assessment = {
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

    /**
     * Returns a FileWriter for the evaluation result file.
     *
     * @param resultsDir The directory where the evaluation result file should be created.
     * @return A FileWriter for the evaluation result file.
     */
    protected def getOutputTarget(resultsDir: File): Writer = {
        val outputFile = new File(resultsDir, EVALUATION_RESULT_FILE_NAME)
        if (outputFile.exists()) {
            outputFile.delete()
            outputFile.createNewFile()
        }
        new FileWriter(outputFile, false)
    }

    protected def getFingerprintFile(adapter: String, algorithm: String, resultsDir: File): File = {
        val fileName = s"${adapter}-$algorithm.profile"
        new File(resultsDir, fileName)
    }

    /**
     * Prints the header of the evaluation result file.
     *
     * @param ow        The writer for the evaluation result file.
     * @param testFiles The test files which will be used as headers.
     */
    protected def printHeader(ow: BufferedWriter, testFiles: Array[File]): Unit = {
        ow.write("algorithm")
        for (file <- testFiles) {
            ow.write(s"\t${file.getAbsolutePath.split("/").last}")
        }
        ow.newLine()
    }

    /**
     * Creates a PrintWriter for the fingerprint file.
     * @param resultsDir The directory where the fingerprint file should be created.
     * @param adapter The test adapter used to create fingerprints.
     * @param cgAlgorithm The call graph algorithm used to create the fingerprints.
     * @return
     */
    protected def makeFingerprintWriter(resultsDir: File, adapter: TestAdapter, cgAlgorithm: String): PrintWriter = {
        val fingerprintFile = getFingerprintFile(adapter.frameworkName, cgAlgorithm, resultsDir)
        if (fingerprintFile.exists()) {
            fingerprintFile.delete()
        }
        val fingerprintWriter = new PrintWriter(fingerprintFile)
        fingerprintWriter
    }

    /**
     * Expects future that generates call graph, awaits it for a given timeout
     * and on timeout writes the timeout to the fingerprint and evaluation file.
     * @param timeout The timeout in seconds.
     * @param ow The writer for the evaluation result file.
     * @param fingerprintWriter The writer for the fingerprint file.
     * @param testName The name of the current test case.
     * @param future The future that generates the call graph.
     */
    protected def tryAwaitGenerateCG(
        timeout:           Int,
        ow:                BufferedWriter,
        fingerprintWriter: PrintWriter,
        testName:          String,
        future:            Future[Unit]
    ): Unit = {
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
                fingerprintWriter.println(s"$testName\t${result.shortNotation}")
                fingerprintWriter.flush()
            case e: Throwable => println(e.getMessage)
        }
    }
}

object FingerprintExtractor {

    def main(args: Array[String]): Unit = {

        var c = ConfigParser.parseConfig(args)
        if (c.isEmpty)
            return

        val config = c.get

        config.language match {
            case "java"   => JavaFingerprintExtractor.generateFingerprints(config)
            case "js"     => JSFingerprintExtractor.generateFingerprints(config)
            case "python" => PyFingerprintExtractor.generateFingerprints(config)
            case _        => println("Language not supported")
        }
    }

    def parseFingerprints(adapter: TestAdapter, algorithm: String, fingerprintDir: File): Set[String] = {
        val fingerprintFile = FingerprintExtractor.getFingerprintFile(adapter, algorithm, fingerprintDir)
        assert(fingerprintFile.exists(), s"${fingerprintFile.getPath} does not exists")

        Source.fromFile(fingerprintFile).getLines().map(_.split("\t")).collect {
            case Array(featureID, result) if result == Sound.shortNotation => featureID
        }.toSet
    }

    private def getFingerprintFile(adapter: TestAdapter, algorithm: String, resultsDir: File): File = {
        val fileName = s"${adapter.frameworkName}-$algorithm.profile"
        new File(resultsDir, fileName)
    }
}
