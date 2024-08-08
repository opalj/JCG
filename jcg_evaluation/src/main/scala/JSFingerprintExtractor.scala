import java.io.BufferedWriter
import java.io.File
import java.io.PrintWriter

object JSFingerprintExtractor extends FingerprintExtractor {
    val language = "js"

    override def generateFingerprints(config: JCGConfig): Unit = {
        if (config.debug) println("[DEBUG] " + config.language + " " + config.inputDir + " " + config.outputDir)
        println("Extracting JS fingerprints")
        val adapters = List(JSCallGraphAdapter)

        // create output directories and execute all adapters
        val outputDir = config.outputDir
        val adapterOutputDir = config.outputDir
        val inputDir = config.inputDir
        executeAdapters(adapters, inputDir, adapterOutputDir)

        // parse expected call graph for test case from json files
        val expectedCGs: Array[ExpectedCG] =
            FileOperations.listFilesRecursively(inputDir, ".json")
              .filter(f => f.getAbsolutePath.contains("js"))
              .map(f => new ExpectedCG(f))
              .sorted(Ordering.by((f: ExpectedCG) => f.filePath))

        if (config.debug) {
            println("[DEBUG] expectedCGs:" + expectedCGs.map(_.filePath).mkString(","))
            val generatedCGFiles = FileOperations.listFilesRecursively(adapterOutputDir, ".json").filter(f => f.getAbsolutePath.contains("js"))
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
                val generatedCGFile = new File(adapterOutputDir, s"${adapter.frameworkName}/$algo/$testName")
                val generatedCG = new AdapterCG(generatedCGFile)

                // check if call graph has missing edges
                val isSound = generatedCG.compareLinks(expectedCG).length == 0

                fingerprintWriter.write(s"$testName -> $isSound\n")
                fingerprintWriter.flush()

                outputWriter.write(s"\t$isSound")
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
}


