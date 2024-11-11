import java.io.File
import java.io.FileWriter
import java.io.Writer
import scala.collection.mutable

import upickle.default._

object PyanAdapter extends PyTestAdapter {
    val debug: Boolean = true
    val frameworkName: String = "Pyan"
    val possibleAlgorithms: Array[String] = Array("NONE")
    private val command = "pyan3"

    def main(args: Array[String]): Unit = {
        serializeAllCGs("testcasesOutput/python", s"results/python/$frameworkName")
    }

    /**
     * Generates all call graphs with the given algorithm.
     *
     * @param inputDirPath  The directory containing the input files to generate call graphs for.
     * @param outputDirPath The directory to write the call graphs to.
     */
    def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit = {
        for (algo <- possibleAlgorithms) {
            generateCallGraphs(inputDirPath, outputDirPath, algo)
        }
    }

    private def generateCallGraphs(inputDirPath: String, outputDirPath: String, algorithm: String): Unit = {
        val outputDir = new File(s"$outputDirPath/$algorithm")
        outputDir.mkdirs()
        val testDirs = new File(inputDirPath).list().filter(_.startsWith("OO"))

        // check if pycg command is available
        try {
            sys.process.Process(Seq(command, "-h")).!!
        } catch {
            case e: Exception => println(s"${Console.RED}[ERROR]: $command not found${Console.RESET}")
        }

        testDirs.foreach(testDir => {
            val output = new FileWriter(outputDir.getAbsolutePath + "/" + testDir + ".json")
            serializeCG(algorithm, s"$inputDirPath/$testDir", output)
            output.close()
        })
    }

    override def serializeCG(
        algorithm:      String,
        inputDirPath:   String,
        output:         Writer,
        adapterOptions: AdapterOptions
    ): Long = {
        val files =
            new File(inputDirPath).listFiles()(0).listFiles().filter(_.getName.endsWith(".py")).map(_.getAbsolutePath)
        val mainFilePath = if (files.length == 1) files(0) else files.find(_.contains("main")).getOrElse(files(0))
        if (debug) println(mainFilePath)

        // delete and create temp folder
        val tempFile = new File(s"temp/$frameworkName/$algorithm/out.dot")
        tempFile.getParentFile.mkdirs()

        val args = Seq(mainFilePath, "--annotated", "-u", "--dot", "--file", tempFile.getAbsolutePath)
        if (debug) println(s"[DEBUG] executing ${(Seq(command) ++ args).mkString(" ")}")

        val start = System.nanoTime()
        val processSucceeded =
            try {
                sys.process.Process(Seq(command) ++ args).!!
                true
            } catch {
                case e: Exception =>
                    println(s"${Console.RED}[ERROR]: $command failed for $mainFilePath${Console.RESET}")
                    false
            }
        val end = System.nanoTime()
        if (debug) println(s"Call graph for $inputDirPath generated in ${end - start} ns")

        // process output and convert to common call graph format
        if (processSucceeded) {
            try {
                val json = toCommonFormat(tempFile)
                output.write(json)
            } catch {
                case e: Exception =>
                    println(s"${Console.RED}[ERROR]: Failed to process and write the call graph for $inputDirPath${Console.RESET}")
                    println(e)
                    println(e.getStackTrace.mkString("\n"))
            }
        }

        end - start
    }

    private def toCommonFormat(cgFile: File): String = {
        val dotGraph = DOTGraphParser.parseFile(cgFile)
        // println(dotGraph.nodes.mkString("\n"))
        // println("END")
        val nodeMap: mutable.Map[String, Node] = mutable.Map()
        dotGraph.nodes.filter(node => node.split(" ").head.trim != "graph" && node.contains("label=")).foreach(node => {
            // extract label field from node
            // println(node)
            var label = node.split("label=\"")(1).split("\"").head
            val id = node.split("\\[").head.trim
            if (label.split("\",").head == id) label = "global"

            var filePath = if (label.split("\\\\n").length > 1) label.split("\\\\n")(2).split(":").head else ""
            // only keep last folder and filename
            filePath = filePath.split("/").takeRight(2).mkString("/")

            val start = if (label.split(":").length > 1) label.split(":")(1).split(",").head.toInt else 0
            label = label.split("\\\\n").head
            if (debug) println(s"$id: $label at line $start in $filePath")

            // pyan does not support natives
            // pyan does not seem to support lambdas

            label = label.split("\\(").head

            nodeMap += (id -> Node(id, label, filePath, Position(start)))
        })

        val edgeArray =
            dotGraph.edges.map(edge => Edge(nodeMap(edge.split("->").head.trim), nodeMap(edge.split("->").last.trim)))

        if (debug) {
            println("[DEBUG] nodes: " + nodeMap.mkString("\n"))
            println("[DEBUG] edges: " + edgeArray.mkString("\n"))
        }

        val jsonCG = write(edgeArray)
        jsonCG
    }
}
