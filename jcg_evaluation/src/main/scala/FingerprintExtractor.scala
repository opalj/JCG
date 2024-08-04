import org.opalj.log.GlobalLogContext
import org.opalj.log.OPALLogger
import play.api.libs.json.Json

import java.io._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.TimeoutException
import scala.concurrent.duration.Duration
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.util.Failure
import scala.util.Success

object FingerprintExtractor {

    val EVALUATION_RESULT_FILE_NAME = "evaluation-result.tsv"

    def main(args: Array[String]): Unit = {

        var c = ConfigParser.parseConfig(args)
        if (c.isEmpty)
            System.exit(0)

        val config = c.get

        config.language match {
            case "java" => generateJavaFingerprints(config)
            case "js" => generateJSFingerprints(config)
            case _ => println("Language not supported")
        }
    }

    private def generateJavaFingerprints(config: JCGConfig): Unit = {
        if (!config.debug)
            OPALLogger.updateLogger(GlobalLogContext, new DevNullLogger())

        val projectsDir = EvaluationHelper.getProjectsDir(config.inputDir.getAbsolutePath)
        val resultsDir = config.outputDir
        resultsDir.mkdirs()

        val jreLocations = EvaluationHelper.getJRELocations(config.JRE_LOCATIONS_FILE)
        assert(new File(config.JRE_LOCATIONS_FILE).exists(), "'jre.conf' not specified")

        val ow = new BufferedWriter(getOutputTarget(resultsDir))

        val projectSpecFiles = projectsDir.listFiles { (_, name) ⇒
            name.endsWith(".conf") && name.startsWith(config.projectFilter)
        }.sorted

        printHeader(ow, projectSpecFiles)

        val adapters = if (config.adapters.nonEmpty) config.adapters else CommonEvaluationConfig.ALL_ADAPTERS

        for {
            adapter <- adapters
            cgAlgorithm <- adapter.possibleAlgorithms().filter(_.startsWith(config.algorithmFilter))
        } {
            ow.write(s"${adapter.frameworkName()}-$cgAlgorithm")

            println(s"creating fingerprint for ${adapter.frameworkName()} $cgAlgorithm")
            val fingerprintFile = getFingerprintFile(adapter, cgAlgorithm, resultsDir)
            if (fingerprintFile.exists()) {
                fingerprintFile.delete()
            }
            val fingerprintWriter = new PrintWriter(fingerprintFile)
            for (psf <- projectSpecFiles) {
                val projectSpec = Json.parse(new FileInputStream(psf)).validate[ProjectSpecification].get

                val outDir = config.getOutputDirectory(adapter, cgAlgorithm, projectSpec, resultsDir)
                outDir.mkdirs()

                val cgFile = new File(outDir, config.SERIALIZATION_FILE_NAME)
                if (cgFile.exists()) {
                    cgFile.delete()
                }

                println(s"performing test case: ${projectSpec.name}")

                val future = Future {
                    try {
                        adapter.serializeCG(
                            cgAlgorithm,
                            projectSpec.target(projectsDir).getCanonicalPath,
                            projectSpec.main.orNull,
                            projectSpec.allClassPathEntryPaths(projectsDir),
                            jreLocations(projectSpec.java),
                            false,
                            cgFile.getAbsolutePath
                        )
                    } catch {
                        case e: Throwable ⇒
                            if (config.debug) {
                                println(e.printStackTrace())
                            }
                    }
                    ow.synchronized {
                        System.gc()

                        val result = CGMatcher.matchCallSites(projectSpec, jreLocations(projectSpec.java), projectsDir, cgFile, config.debug)
                        ow.write(s"\t${result.shortNotation}")
                        fingerprintWriter.println(s"${projectSpec.name}\t${result.shortNotation}")
                        fingerprintWriter.flush()
                        println(s"${projectSpec.name}\t${result.shortNotation}")

                    }
                }
                if (config.parallel) {
                    future.onComplete {
                        case Success(_) =>
                        case Failure(e) => e.printStackTrace
                    }
                } else {
                    try {
                        val duration =
                            if (config.timeout >= 0)
                                config.timeout.seconds
                            else Duration.Inf
                        Await.ready(future, duration)
                    } catch {
                        case _: TimeoutException =>
                            println(s"Test case was interrupted after ${config.timeout} seconds")
                            System.gc()
                            val result = Timeout
                            ow.write(s"\t${result.shortNotation}")
                            fingerprintWriter.println(s"${projectSpec.name}\t${result.shortNotation}")
                            fingerprintWriter.flush()
                        case e: Throwable => println(e.getMessage)
                    }
                }
            }
            ow.newLine()
            fingerprintWriter.close()
        }

        ow.flush()
        ow.close()
    }

    /**
     * Prints the header of the evaluation result file.
     *
     * @param ow   The writer to write the header to.
     * @param jars The list of jars to write to the header.
     */
    private def printHeader(ow: BufferedWriter, jars: Array[File]): Unit = {
        ow.write("algorithm")
        for (tgt <- jars) {
            ow.write(s"\t$tgt")
        }
        ow.newLine()
    }

    /**
     * Returns a FileWriter for the evaluation result file.
     *
     * @param resultsDir The directory where the evaluation result file should be created.
     * @return A FileWriter for the evaluation result file.
     */
    private def getOutputTarget(resultsDir: File): Writer = {
        val outputFile = new File(resultsDir, EVALUATION_RESULT_FILE_NAME)
        if (outputFile.exists()) {
            outputFile.delete()
            outputFile.createNewFile()
        }
        new FileWriter(outputFile, false)
    }

    private def generateJSFingerprints(config: JCGConfig): Unit = {
        if (config.debug) println("[DEBUG] " + config.language + " " + config.inputDir + " " + config.outputDir)
        println("Extracting JS fingerprints")
        val adapters = List(JSCallGraphAdapter)

        // create output directories and execute all adapters
        val outputDir = config.outputDir
        val adapterOutputDir = config.outputDir
        executeAdapters(adapters, adapterOutputDir)

        // parse expected call graph for test case from json files
        val expectedCGs: Array[ExpectedCG] =
            FileOperations.listFilesDeep(config.inputDir, ".json")
              .filter(f => f.getAbsolutePath.contains("js"))
              .map(f => new ExpectedCG(f))
              .sorted(Ordering.by((f: ExpectedCG) => f.filePath))

        if (config.debug) {
            println("[DEBUG] expectedCGs:" + expectedCGs.map(_.filePath).mkString(","))
            val generatedCGFiles = FileOperations.listFilesDeep(adapterOutputDir, ".json").filter(f => f.getAbsolutePath.contains("js"))
            println("[DEBUG] generatedCGFiles: " + generatedCGFiles.mkString(","))
        }

        val outputWriter = new BufferedWriter(getOutputTarget(outputDir))


        // compare expected and generated call graphs and write results to file
        for (adapter <- adapters) {
            val algoDirs = FileOperations.listDirs(new File(adapterOutputDir, adapter.frameworkName))
            println("AlgoDirs: " + algoDirs.map(_.getName).mkString(","))
            outputWriter.write(adapter.frameworkName)
            outputWriter.newLine()

            for (algoDir <- algoDirs) {
                outputWriter.write("\t" + algoDir.getName)
                outputWriter.newLine()
                for (expectedCG <- expectedCGs) {
                    val testName: String = expectedCG.filePath.split("/").last
                    if (config.debug) println("[DEBUG] Test name: " + testName + " " + algoDir.getName)
                    val generatedCGFile = algoDir.listFiles().find(_.getName == testName).get
                    val generatedCG = new AdapterCG(generatedCGFile)

                    // check if call graph has missing edges
                    val isSound = compareCGs(expectedCG, generatedCG).length == 0

                    outputWriter.write("\t\t" + testName + " -> " + isSound)
                    outputWriter.flush()
                }
                outputWriter.newLine()
            }
        }
        outputWriter.close()
    }

    /**
     * Compares the expected call graph with the generated call graph.
     *
     * @return Array of edges missing from generated call graph
     */
    private def compareCGs(expectedCG: ExpectedCG, generatedCG: AdapterCG): Array[Array[String]] = {
        var missingEdges: Array[Array[String]] = Array()

        for (edge <- expectedCG.directLinks) {
            if (!generatedCG.links.map(_.mkString(",")).contains(edge.mkString(","))) {
                missingEdges :+= edge
            }
        }

        missingEdges
    }

    /**
     * Creates output directories for each adapter and executes the adapters on the test cases.
     *
     * @param adapters  List of adapters to execute.
     * @param outputDir The output directory to write to.
     */
    private def executeAdapters(adapters: List[TestAdapter], outputDir: File): Unit = {
        outputDir.mkdirs()

        for (adapter <- adapters) {
            // create output dir for every adapter
            val adapterDir = new File(outputDir, adapter.frameworkName)
            adapterDir.mkdirs()

            adapter.main(Array())
        }
    }

    def parseFingerprints(adapter: JCGTestAdapter, algorithm: String, fingerprintDir: File): Set[String] = {
        val fingerprintFile = FingerprintExtractor.getFingerprintFile(adapter, algorithm, fingerprintDir)
        assert(fingerprintFile.exists(), s"${fingerprintFile.getPath} does not exists")

        Source.fromFile(fingerprintFile).getLines().map(_.split("\t")).collect {
            case Array(featureID, result) if result == Sound.shortNotation => featureID
        }.toSet
    }

    private def getFingerprintFile(adapter: JCGTestAdapter, algorithm: String, resultsDir: File): File = {
        val fileName = s"${adapter.frameworkName()}-$algorithm.profile"
        new File(resultsDir, fileName)
    }
}
