import java.io._
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import org.opalj.log.GlobalLogContext
import org.opalj.log.OPALLogger

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

        val projectSpecFiles = projectsDir.listFiles { (_, name) =>
            name.endsWith(".conf") && name.startsWith(config.projectFilter)
        }.sorted

        val ow = new BufferedWriter(getOutputTarget(resultsDir))
        printHeader(ow, projectSpecFiles)

        for {
            adapter <- config.adapters
            cgAlgorithm <- adapter.possibleAlgorithms.filter(_.startsWith(config.algorithmFilter))
        } {
            ow.write(s"${adapter.frameworkName}-$cgAlgorithm")

            println(s"creating fingerprint for ${adapter.frameworkName} $cgAlgorithm")
            val fingerprintWriter: PrintWriter = makeFingerprintWriter(resultsDir, adapter, cgAlgorithm)

            for (psf <- projectSpecFiles) {
                val projectSpec = Json.parse(new FileInputStream(psf)).validate[ProjectSpecification].get

                val outDir = EvaluationHelper.getOutputDirectory(adapter, cgAlgorithm, projectSpec, resultsDir)
                outDir.mkdirs()

                val cgFile = new File(outDir, config.SERIALIZATION_FILE_NAME)
                if (cgFile.exists()) {
                    cgFile.delete()
                }

                val output = new BufferedWriter(new FileWriter(cgFile))

                println(s"performing test case: ${projectSpec.name}")

                val future = Future {
                    try {
                        adapter.serializeCG(
                            cgAlgorithm,
                            projectSpec.target(projectsDir).getCanonicalPath,
                            output,
                            AdapterOptions.makeJavaOptions(
                                projectSpec.main.orNull,
                                projectSpec.allClassPathEntryPaths(projectsDir),
                                jreLocations(projectSpec.java),
                                analyzeJDK = false
                            )
                        )
                    } catch {
                        case e: Throwable =>
                            if (config.debug) {
                                println(e.printStackTrace())
                            }
                    } finally {
                        output.close()
                    }
                    ow.synchronized {
                        System.gc()

                        val result = CGMatcher.matchCallSites(
                            projectSpec,
                            jreLocations(projectSpec.java),
                            projectsDir,
                            cgFile,
                            config.debug
                        )
                        ow.write(s"\t${result.shortNotation}")
                        fingerprintWriter.println(s"${projectSpec.name}\t${result.shortNotation}")
                        fingerprintWriter.flush()
                        println(s"${projectSpec.name}\t${result.shortNotation}")

                    }
                }
                if (config.parallel) {
                    future.onComplete {
                        case Success(_) =>
                        case Failure(e) => e.printStackTrace()
                    }
                } else {
                    tryAwaitGenerateCG(config.timeout, ow, fingerprintWriter, projectSpec.name, future)
                }
            }
            ow.newLine()
            fingerprintWriter.close()
        }
        ow.close()
    }
}
