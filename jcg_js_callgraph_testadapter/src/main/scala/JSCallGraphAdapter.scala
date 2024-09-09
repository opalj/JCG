import java.io.File
import java.io.FileWriter
import java.io.Writer
import play.api.libs.json.Json
import scala.io.Source
import scala.util.Using

object JSCallGraphAdapter extends JSTestAdapter {
    val debug: Boolean = false

    val possibleAlgorithms: Array[String] = Array("NONE", "ONESHOT", "DEMAND")
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
            val output = new FileWriter(outputDir.getAbsolutePath + "/" + testDir + ".json")
            serializeCG(
                algorithm,
                s"$inputDirPath/$testDir",
                output
            )
            output.close()
        })

        println("Call graphs generated!")
    }

    def serializeCG(
        algorithm:      String,
        inputDirPath:   String,
        output:         Writer,
        adapterOptions: AdapterOptions
    ): Long = {
        // delete and create temp folder
        val tempFile = new File(s"temp/$frameworkName/$algorithm/out.json")
        tempFile.getParentFile.mkdirs()

        val args = Seq(
            "--cg",
            inputDirPath,
            "--output",
            tempFile.getAbsolutePath,
            "--strategy",
            algorithm
        )
        if (debug) println(s"[DEBUG] executing ${(Seq(command) ++ args).mkString(" ")}")

        val start = System.nanoTime()
        try {
            sys.process.Process(Seq(command) ++ args).!!
        } catch {
            case e: Exception => println(s"[Error]: $command failed for $inputDirPath")
        }
        val end = System.nanoTime()
        Using(Source.fromFile(tempFile)) { source => output.write(Json.prettyPrint(Json.parse(source.mkString))) }

        if (debug) println(s"Call graph for $inputDirPath generated in ${end - start} ns")
        end - start
    }
}
