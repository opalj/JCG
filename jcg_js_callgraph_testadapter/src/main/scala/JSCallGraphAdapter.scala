import java.io.File
import java.io.FileWriter
import java.io.Writer

object JSCallGraphAdapter extends JSTestAdapter {
    val debug: Boolean = false

    val possibleAlgorithms: Array[String] = Array("NONE", "ONESHOT", "DEMAND", "FULL")
    val frameworkName: String = "js-callgraph"
    private val command = "js-callgraph"

    def main(args: Array[String]): Unit = {
        // generate call graphs for all algorithms
        serializeAllCGs("testcasesOutput/js", s"results/js/$frameworkName")
    }

    private def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit = {
        for (algo <- possibleAlgorithms) {
            generateCallGraphs(inputDirPath, outputDirPath, algo)
        }
    }

    /**
     * Generates all call graphs with the given algorithm.
     *
     * @param outputDirPath The directory to write the call graphs to.
     * @param algorithm     The algorithm to use for generating the call graphs.
     */
    private def generateCallGraphs(inputDirPath: String, outputDirPath: String, algorithm: String): Unit = {
        val outputDir = new File(s"$outputDirPath/$algorithm")
        outputDir.mkdirs()
        val testDirs = new File(inputDirPath).list()

        // check if js-callgraph command is available
        try {
            sys.process.Process(Seq(command, "-h")).!!
        } catch {
            case e: Exception => println(s"[Error]: $command not found")
        }

        // generate callgraph for every testcase
        testDirs.foreach(testDir => {
            serializeCG(algorithm, s"$inputDirPath/$testDir", new FileWriter(outputDir.getAbsolutePath), Array.empty)
        })

        println("Call graphs generated!")
    }

    def serializeCG(
        algorithm:      String,
        inputDirPath:   String,
        output:         Writer,
        programArgs:    Array[String],
        adapterOptions: AdapterOptions
    ): Long = {
        val args = Seq("--cg", inputDirPath, "--output", outputPath, "--strategy", algorithm)
        if (debug) println(s"[DEBUG] executing ${(Seq(command) ++ args).mkString(" ")}")

        val start = System.nanoTime()
        try {
            sys.process.Process(Seq(command) ++ args).!!
        } catch {
            case e: Exception => println(s"[Error]: $command failed for $inputDirPath")
        }
        val end = System.nanoTime()
        if (debug) println(s"Call graph for $inputDirPath generated in ${end - start} ns")
        end - start
    }
}
