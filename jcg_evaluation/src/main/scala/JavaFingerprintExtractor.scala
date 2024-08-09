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
import scala.util.Failure
import scala.util.Success

object JavaFingerprintExtractor extends FingerprintExtractor {
    val language = "java"

    override def generateFingerprints(config: JCGConfig): Unit = {
        if (!config.debug)
            OPALLogger.updateLogger(GlobalLogContext, new DevNullLogger())

        val projectsDir = EvaluationHelper.getProjectsDir(config.inputDir.getAbsolutePath)
        val resultsDir = config.outputDir
        resultsDir.mkdirs()

        val jreLocations = EvaluationHelper.getJRELocations(config.JRE_LOCATIONS_FILE)
        assert(new File(config.JRE_LOCATIONS_FILE).exists(), "'jre.conf' not specified")

        val ow = new BufferedWriter(getOutputTarget(resultsDir))

        val projectSpecFiles = projectsDir.listFiles { (_, name) =>
            name.endsWith(".conf") && name.startsWith(config.projectFilter)
        }.sorted

        printHeader(ow, projectSpecFiles)

        val adapters = if (config.adapters.nonEmpty) config.adapters else CommonEvaluationConfig.ALL_JAVA_ADAPTERS

        for {
            adapter <- adapters
            cgAlgorithm <- adapter.possibleAlgorithms.filter(_.startsWith(config.algorithmFilter))
        } {
            ow.write(s"${adapter.frameworkName}-$cgAlgorithm")

            println(s"creating fingerprint for ${adapter.frameworkName} $cgAlgorithm")
            val fingerprintFile = getFingerprintFile(adapter.frameworkName, cgAlgorithm, resultsDir)
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
                        case e: Throwable =>
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
}
