import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.PrintWriter
import java.io.Writer

import play.api.libs.json.Json

import scala.io.Source

object FingerprintExtractor {

    val EVALUATION_RESULT_FILE_NAME = "evaluation-result.tsv"

    def getFingerprintFile(adapter: JCGTestAdapter, algorithm: String, resultsDir: File): File = {
        val fileName = s"${adapter.frameworkName()}-$algorithm.profile"
        new File(resultsDir, fileName)
    }

    def main(args: Array[String]): Unit = {
        val config = CommonEvaluationConfig.processArguments(args)

        val projectsDir = EvaluationHelper.getProjectsDir(config.INPUT_DIR_PATH)
        val resultsDir = new File(config.OUTPUT_DIR_PATH)
        resultsDir.mkdirs()

        val jreLocations = EvaluationHelper.getJRELocations(config.JRE_LOCATIONS_FILE)
        assert(new File(config.JRE_LOCATIONS_FILE).exists(), "'jre.conf' not specified")

        val ow = new BufferedWriter(getOutputTarget(resultsDir))

        val projectSpecFiles = projectsDir.listFiles { (_, name) ⇒
            name.endsWith(".conf") && name.startsWith(config.PROJECT_PREFIX_FILTER)
        }.sorted

        printHeader(ow, projectSpecFiles)

        for {
            adapter ← config.EVALUATION_ADAPTERS
            cgAlgo ← adapter.possibleAlgorithms().filter(_.startsWith(config.ALGORITHM_PREFIX_FILTER))
        } {
            val fingerprintFile = getFingerprintFile(adapter, cgAlgo, resultsDir)
            if (fingerprintFile.exists()) {
                fingerprintFile.delete()
            }
            val fingerprintWriter = new PrintWriter(fingerprintFile)
            for (psf ← projectSpecFiles) {
                val projectSpec = Json.parse(new FileInputStream(psf)).validate[ProjectSpecification].get

                val outDir = config.getOutputDirectory(adapter, cgAlgo, projectSpec, resultsDir)
                outDir.mkdirs()

                val cgFile = new File(outDir, config.SERIALIZATION_FILE_NAME)
                assert(!cgFile.exists(), s"$cgFile already exists")

                try {
                    adapter.serializeCG(
                        cgAlgo,
                        projectSpec.target(projectsDir).getCanonicalPath,
                        projectSpec.main.orNull,
                        projectSpec.allClassPathEntryPaths(projectsDir),
                        jreLocations(projectSpec.java),
                        cgFile.getPath
                    )
                } catch {
                    case e: Throwable ⇒
                        if (config.DEBUG) {
                            println(e.printStackTrace())
                        }
                        ow.write(s"\tE")
                        fingerprintWriter.println(s"${projectSpec.name}\tE")
                }

                assert(cgFile.exists(), "the adapter failed to write the call graph")
                System.gc()

                val result = CGMatcher.matchCallSites(projectSpec, jreLocations(projectSpec.java), projectsDir, cgFile, config.DEBUG)
                ow.write(s"\t${result.shortNotation}")
                fingerprintWriter.println(s"${projectSpec.name}\t${result.shortNotation}")

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
