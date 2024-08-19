import java.io._
import scala.io.Source


trait FingerprintExtractor {
    val EVALUATION_RESULT_FILE_NAME = "evaluation-result.tsv"
    val language: String

    def generateFingerprints(config: JCGConfig): Unit

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
}

object FingerprintExtractor {

    def main(args: Array[String]): Unit = {

        var c = ConfigParser.parseConfig(args)
        if (c.isEmpty)
            return

        val config = c.get

        config.language match {
            case "java" => JavaFingerprintExtractor.generateFingerprints(config)
            case "js" => JSFingerprintExtractor.generateFingerprints(config)
            case _ => println("Language not supported")
        }
    }

    def parseFingerprints(adapter: JavaTestAdapter, algorithm: String, fingerprintDir: File): Set[String] = {
        val fingerprintFile = FingerprintExtractor.getFingerprintFile(adapter, algorithm, fingerprintDir)
        assert(fingerprintFile.exists(), s"${fingerprintFile.getPath} does not exists")

        Source.fromFile(fingerprintFile).getLines().map(_.split("\t")).collect {
            case Array(featureID, result) if result == Sound.shortNotation => featureID
        }.toSet
    }

    private def getFingerprintFile(adapter: JavaTestAdapter, algorithm: String, resultsDir: File): File = {
        val fileName = s"${adapter.frameworkName}-$algorithm.profile"
        new File(resultsDir, fileName)
    }
}
