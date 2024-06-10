import org.opalj.log.{GlobalLogContext, OPALLogger}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.Json

import java.io._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future, TimeoutException}
import scala.io.Source
import scala.util.{Failure, Success}

object FingerprintExtractor {

    val EVALUATION_RESULT_FILE_NAME = "evaluation-result.tsv"

    def main(args: Array[String]): Unit = {

        var c = ConfigParser.parseConfig(args)
        if (c.isEmpty)
            System.exit(0)

        val config = c.get

        config.language match {
            case "java" => getJavaFingerprints(config)
            case "js" => getJSFingerprints(config)
            case _ => println("Language not supported")
        }
    }

    private def getJavaFingerprints(config: JCGConfig): Unit = {
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
            adapter ← adapters
            cgAlgorithm ← adapter.possibleAlgorithms().filter(_.startsWith(config.algorithmFilter))
        } {
            ow.write(s"${adapter.frameworkName()}-${cgAlgorithm}")

            println(s"creating fingerprint for ${adapter.frameworkName()} $cgAlgorithm")
            val fingerprintFile = getFingerprintFile(adapter, cgAlgorithm, resultsDir)
            if (fingerprintFile.exists()) {
                fingerprintFile.delete()
            }
            val fingerprintWriter = new PrintWriter(fingerprintFile)
            for (psf ← projectSpecFiles) {
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

    def getJSFingerprints(config: JCGConfig): Unit = {
        if (config.debug) println("[DEBUG] " + config.language + " " + config.inputDir + " " + config.outputDir)
        println("Extracting JS fingerprints")
        val adapters = List(JSCallGraphAdapter)
        val testCasesPath = "testcasesOutput/js/"

        // create output directories and execute adapters
        val outputDir = config.fingerprintDir
        val adapterOutputDir = config.outputDir
        executeAdapters(adapters, outputDir)

        // parse expected call graph for test case
        val expectedCGs: Array[ExpectedCG] = FileOperations.listJsonFilesDeep(config.inputDir).filter(f => f.getAbsolutePath.contains("js")).map(f => new ExpectedCG(f))
        if (config.debug) println("[DEBUG] expectedCGs:" + expectedCGs.map(_.filePath).mkString(","))

        val generatedCGFiles = FileOperations.listJsonFilesDeep(adapterOutputDir).filter(f => f.getAbsolutePath.contains("js"))
        if (config.debug) println("[DEBUG] generatedCGFiles: " + generatedCGFiles.mkString(","))

        var adapterMap = Map[String, Map[String, Map[String, Boolean]]]()

        for (adapter <- adapters) {
            val algoDirs = listDirs(new File(adapterOutputDir, adapter.frameworkName))
            var algorithmMap = Map[String, Map[String, Boolean]]()

            for (algoDir <- algoDirs) {
                var testCaseMap = Map[String, Boolean]()
                for (expectedCG <- expectedCGs) {
                    val testName: String = expectedCG.filePath.split("/").last
                    if (config.debug) println("[DEBUG] Test name: " + testName + " " + algoDir.getName)
                    val generatedCGFile = algoDir.listFiles().find(_.getName == testName).get
                    val generatedCG = new AdapterCallGraph(generatedCGFile)

                    if (compareCGs(expectedCG, generatedCG).length > 0) {
                        testCaseMap += (testName.split("\\.").head -> false)
                    } else {
                        testCaseMap += (testName.split("\\.").head -> true)
                    }
                }
                algorithmMap += (algoDir.getName -> testCaseMap)
            }
            adapterMap += (adapter.frameworkName -> algorithmMap)
        }


        println("Results: ")
        println(adapterMap.map(x => " --- " + x._1 + "---- \n" + x._2.map(y => y._1 + "\n\t" + y._2.map(z => z._1 + " -> " + z._2).toSeq.sorted.mkString("\n\t")).mkString("\n")).mkString)
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

            // execute adapter (algorithms) on test cases
            // write results to file
            adapter.main(Array())
        }
    }

    private def listDirs(dir: File) = {
        dir.listFiles().filter(_.isDirectory)
    }

    private def compareCGs(expectedCG: ExpectedCG, generatedCG: AdapterCallGraph): Array[Array[String]] = {
        var missingEdges: Array[Array[String]] = Array()

        for (edge <- expectedCG.directLinks) {
            if (!generatedCG.links.map(_.mkString(",")).contains(edge.mkString(","))) {
                //println("[DEBUG] Edge not found: " + edge.mkString(" ->"))
                missingEdges :+= edge
            }
        }

        return missingEdges
    }

    private def printHeader(ow: BufferedWriter, jars: Array[File]): Unit = {
        ow.write("algorithm")
        for (tgt ← jars) {
            ow.write(s"\t$tgt")
        }
        ow.newLine()
    }

    private def getOutputTarget(resultsDir: File): Writer = {
        val outputFile = new File(resultsDir, EVALUATION_RESULT_FILE_NAME)
        if (outputFile.exists()) {
            outputFile.delete()
            outputFile.createNewFile()
        }
        new FileWriter(outputFile, false)
    }

    def getFingerprintFile(adapter: JCGTestAdapter, algorithm: String, resultsDir: File): File = {
        val fileName = s"${adapter.frameworkName()}-$algorithm.profile"
        new File(resultsDir, fileName)
    }

    def parseFingerprints(
                           adapter: JCGTestAdapter,
                           algorithm: String,
                           fingerprintDir: File
                         ): Set[String] = {
        val fingerprintFile = FingerprintExtractor.getFingerprintFile(adapter, algorithm, fingerprintDir)
        assert(fingerprintFile.exists(), s"${fingerprintFile.getPath} does not exists")

        Source.fromFile(fingerprintFile).getLines().map(_.split("\t")).collect {
            case Array(featureID, result) if result == Sound.shortNotation ⇒ featureID
        }.toSet
    }
}
