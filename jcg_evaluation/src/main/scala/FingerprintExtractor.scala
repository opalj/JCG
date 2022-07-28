import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.PrintWriter
import java.io.Writer

import org.opalj.log.GlobalLogContext
import org.opalj.log.OPALLogger
import play.api.libs.json.Json

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success

object FingerprintExtractor {

    val EVALUATION_RESULT_FILE_NAME = "evaluation-result.tsv"

    def getFingerprintFile(adapter: JCGTestAdapter, algorithm: String, resultsDir: File): File = {
        val fileName = s"${adapter.frameworkName()}-$algorithm.profile"
        new File(resultsDir, fileName)
    }

    def main(args: Array[String]): Unit = {

        var c = ConfigParser.parseConfig(args)
        if(c.isEmpty)
            System.exit(0)

        val config = c.get


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

        val adapters = if(config.adapters.nonEmpty) config.adapters else CommonEvaluationConfig.ALL_ADAPTERS

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
                    ow.synchronized{
                        System.gc()

                        val result = CGMatcher.matchCallSites(projectSpec, jreLocations(projectSpec.java), projectsDir, cgFile, config.debug)
                        ow.write(s"\t${result.shortNotation}")
                        fingerprintWriter.println(s"${projectSpec.name}\t${result.shortNotation}")
                        fingerprintWriter.flush()

                    }
                }
                if(config.parallel){
                    future.onComplete {
                        case Success(_) =>
                        case Failure(e) => e.printStackTrace
                    }
                } else {
                    try {
                        val duration =
                        if(config.timeout>=0)
                            config.timeout.seconds
                        else Duration.Inf
                        Await.ready(future, duration)
                    } catch {
                        case _: concurrent.TimeoutException =>
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

    def parseFingerprints(
        adapter:        JCGTestAdapter,
        algorithm:      String,
        fingerprintDir: File
    ): Set[String] = {
        val fingerprintFile = FingerprintExtractor.getFingerprintFile(adapter, algorithm, fingerprintDir)
        assert(fingerprintFile.exists(), s"${fingerprintFile.getPath} does not exists")

        Source.fromFile(fingerprintFile).getLines().map(_.split("\t")).collect {
            case Array(featureID, result) if result == Sound.shortNotation ⇒ featureID
        }.toSet
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
}
