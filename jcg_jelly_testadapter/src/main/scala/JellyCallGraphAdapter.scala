import java.io.File
import java.io.FileWriter
import java.io.Writer
import scala.util.Using

import upickle.default._

object JellyCallGraphAdapter extends JSTestAdapter {
    val debug: Boolean = false

    val possibleAlgorithms: Array[String] = Array("NONE")
    val frameworkName: String = "jelly"
    private val command = "jelly"

    def main(args: Array[String]): Unit = serializeAllCGs("testcasesOutput/js", s"results/js/$frameworkName")

    /**
     * Generates all call graphs with the given algorithm.
     *
     * @param inputDirPath  The directory containing the input files to generate call graphs for.
     * @param outputDirPath The directory to write the call graphs to.
     */
    private def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit = {
        for (algo <- possibleAlgorithms) {
            generateCallGraphs(inputDirPath, outputDirPath, algo)
        }
    }

    private def generateCallGraphs(inputDirPath: String, outputDirPath: String, algorithm: String): Unit = {
        val outputDir = new File(s"$outputDirPath/$algorithm")
        outputDir.mkdirs()
        val testDirs = new File(inputDirPath).list()

        // check if js-callgraph command is available
        try {
            sys.process.Process(Seq(command, "--help")).!!
        } catch {
            case e: Exception =>
                println(s"${Console.RED}[Error]: $command command not found, make sure it is installed and in your PATH${Console.RESET}")
                return
        }

        // generate callgraph for every testcase
        testDirs.foreach(testDir => {
            val output = new FileWriter(outputDir.getAbsolutePath + "/" + testDir + ".json")
            serializeCG(algorithm, s"$inputDirPath/$testDir", output)
            output.close()
        })

        println("Call graphs generated!")
    }

    /**
     * Generates a single call graph.
     *
     * @param algorithm     The algorithm to use for generating the call graph.
     * @param outputDirPath The directory to write the call graph to.
     * @param inputDirPath  The directory containing the input files to generate a call graph for.
     * @return The time taken to generate the call graph in nanoseconds.
     */
    def serializeCG(
        algorithm:      String,
        inputDirPath:   String,
        output:         Writer,
        adapterOptions: AdapterOptions
    ): Long = {
        val tempFile = new File(s"temp/$frameworkName/$algorithm/out.html")
        tempFile.getParentFile.mkdirs()

        val args = Seq("--approx", inputDirPath, "-m", tempFile.getAbsolutePath)
        if (debug) println(s"[DEBUG] executing ${(Seq(command) ++ args).mkString(" ")}")

        // Generate call graph
        val start = System.nanoTime()
        val processSucceeded =
            try {
                sys.process.Process(Seq(command) ++ args).!!
                true
            } catch {
                case e: Exception =>
                    println(s"${Console.RED}[Error]: $inputDirPath failed to generate${Console.RESET}")
                    false
            }
        val end = System.nanoTime()

        // Process output and convert to common call graph format
        if (processSucceeded) {
            try {
                val json = toCommonFormat(tempFile)
                output.write(json)
            } catch {
                case e: Exception =>
                    println(s"${Console.RED}[Error]: Failed to process and write the call graph for $inputDirPath${Console.RESET}")
                    println(e)
                    println(e.getStackTrace.mkString("\n"))
            }
        }

        end - start
    }

    /**
     * Converts the code2flow call graph output to the common call graph format.
     *
     * @param cgFile The code2flow call graph json file.
     * @return The call graph in the common format.
     */
    private def toCommonFormat(cgFile: File): String = {
        val jsonData = getJSONData(cgFile.getAbsolutePath)
        val graphData = jsonData("graphs").arr
        val cg = graphData.head("elements").arr

        val filterFuncs = (node: ujson.Value) => {
            val kind = node("data").obj("kind").str

            // the bool is stored as a string for some reason..
            val isEntry = node("data").obj.contains("isEntry") && node("data").obj("isEntry").str == "true"
            kind == "function" || (kind == "module" && isEntry)
        }
        val nodes = cg.filter(filterFuncs).map(node =>
            getNodeFromObj(node("data").obj, cgFile.getAbsolutePath)
        ).toMap

        val calls = cg.filter(node => node("data").obj("kind").str == "call").map(call => {
            val data = call("data").obj
            val source = nodes(data("source").num.toInt.toString)
            val target = nodes(data("target").num.toInt.toString)

            Edge(source, target)
        }).toArray

        if (debug) {
            println("[DEBUG] nodes:\n" + nodes.mkString("\n"))
            println("[DEBUG] edges:\n" + calls.mkString("\n"))
        }
        val jsonCG = write(calls)
        jsonCG
    }

    private def getJSONData(filePath: String): ujson.Value = {
        val htmlContent = Using.resource(scala.io.Source.fromFile(filePath))(_.mkString)
        val dataRegex = """(?s)const data = (\{.*?});""".r
        val dataMatch = dataRegex.findFirstMatchIn(htmlContent)

        dataMatch match {
            case Some(m) =>
                if (debug) println("[DEBUG] DATA", m.group(1))
                ujson.read(m.group(1)) // get the JSON content inside the `{...}`
            case None =>
                throw new RuntimeException("No JSON data found in the file.")
        }
    }

    private def getNodeFromObj(node: ujson.Obj, folder: String): (String, Node) = {
        // find global scope
        val filePath = node("fullName").str.split("/").takeRight(3).mkString("/").split(":").head

        if (node("kind").str == "module") {
            val fileName = node("name").str.split("/").last
            return node("id").num.toInt.toString -> Node(node("id").num.toInt.toString, "global", filePath, Position(0))
        }

        val name = node("name").str
        val id = node("id").num.toInt.toString
        val splitName = name.split(" ")
        val label = {
            if (splitName.head == "global") "global"
            else if (splitName.head == "<anon>") "anon"
            else splitName.head
        }

        // foo 2:1:4:2
        val start = splitName.last.split(":").head.toInt

        id -> Node(id, label, filePath, Position(start))
    }
}
