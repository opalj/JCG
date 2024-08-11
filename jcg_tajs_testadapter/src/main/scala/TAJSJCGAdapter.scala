import upickle.default._

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import scala.collection.mutable

object TAJSJCGAdapter extends JSTestAdapter {
    val debug: Boolean = false

    val possibleAlgorithms: Array[String] = Array("NONE")
    val frameworkName: String = "TAJS"
    private var command = Seq.empty[String]

    def main(args: Array[String]): Unit = {
        // generate call graphs for all algorithms
        serializeAllCGs("testcasesOutput/js", s"results/js/$frameworkName")
    }

    /**
     * Generates all call graphs with the given algorithm.
     *
     * @param inputDirPath  The directory containing the input files to generate call graphs for.
     * @param outputDirPath The directory to write the call graphs to.
     */
    override def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit = {
        for (algo <- possibleAlgorithms) {
            generateCallGraphs(inputDirPath, outputDirPath, algo)
        }
    }

    private def generateCallGraphs(inputDirPath: String, outputDirPath: String, algorithm: String): Unit = {
        val testDirs = new File(inputDirPath).list()
        val outputDir = new File(s"$outputDirPath/$algorithm")
        outputDir.mkdirs()

        // read tajs location from tajs variable in tajs.properties
        val tajsProperties = new File("tajs.properties")
        if (!tajsProperties.exists()) {
            println("[ERROR] tajs.properties not found")
            return
        }
        val tajsLocation = scala.io.Source.fromFile(tajsProperties).getLines.find(_.replaceAll(" ", "").startsWith("tajs=")).getOrElse("")

        val tajsJar = new File(s"${tajsLocation.split("=")(1)}dist/tajs-all.jar".trim())
        if (!tajsJar.exists()) {
            println("[ERROR] tajs location not found in tajs.properties")
            return
        }

        // build tajs command to execute from cli
        command = Seq("java", "-jar", tajsJar.getAbsolutePath)

        // check if tajs command is available
        try {
            val out = sys.process.Process(command)
            println(out)
        } catch {
            case e: Exception => {
                println(s"${Console.RED}[Error]: $command command not found, make sure you set the tajs variable in tajs.properties to the root path of the TAJS project${Console.RESET}")
                return
            }
        }

        // generate callgraph for every testcase
        testDirs.foreach(testDir => {
            // Extract input files, TAJS only supports single file projects
            val inputFilePaths = new File(s"$inputDirPath/$testDir").listFiles()(0).listFiles().map(_.getAbsolutePath).filter(_.endsWith(".js"))
            serializeCG(algorithm, outputDir.getAbsolutePath, inputFilePaths(0))
        })
    }

    /**
     * Generates a single call graph.
     *
     * @param algorithm     The algorithm to use for generating the call graph.
     * @param outputDirPath The directory to write the call graph to.
     * @param inputDirPath  The directory containing the input files to generate a call graph for.
     * @return The time taken to generate the call graph in nanoseconds.
     */
    override def serializeCG(algorithm: String, outputDirPath: String, inputDirPath: String): Long = {
        // TAJS does not support multi-file projects
        val outputPath = s"$outputDirPath/${inputDirPath.split(File.separator).last.split("\\.").head}.json"
        val args = Seq("-callgraph", inputDirPath)
        if (debug) println(s"[DEBUG] executing ${args.mkString(" ")}")
        val start = System.nanoTime()
        val processSucceeded = try {
            sys.process.Process(command ++ args).!!
            true
        } catch {
            case e: Exception =>
                println(s"${Console.RED}[Error]: $inputDirPath failed to generate${Console.RESET}")
                false
        }
        val end = System.nanoTime()

        if (processSucceeded) {
            try {
                val json = toCommonFormat(new File("out/callgraph.dot"))
                val bw = new BufferedWriter(new FileWriter(outputPath))
                bw.write(json)
                bw.close()
            } catch {
                case e: Exception =>
                    println(s"${Console.RED}[Error]: Failed to process and write the call graph for $inputDirPath${Console.RESET}")
            }
        }
        end - start
    }

    private def toCommonFormat(cgFile: File): String = {
        val (nodes, edges) = DOTGraphParser.parseFile(cgFile)
        val nodeMap: mutable.Map[String, Node] = mutable.Map()
        nodes.foreach(node => {
            // extract label field from node
            var label = node.split("label=\"")(1).split("\"").head
            val id = node.split("\\[").head.trim
            var filePath = if (label.split("\\\\n").length > 1) label.split("\\\\n")(1).split(":").head else ""
            // only keep last folder and filename
            filePath = filePath.split("/").takeRight(2).mkString("/")
            filePath = if (filePath == "HOST(string-replace-model.js)") "Native" else filePath
            val start = if (label.split(":").length > 1) label.split(":")(1).split(">").head.toInt else 0
            label = label.split("\\\\n").head.split(":").head
            if (debug) println(s"$id: $label at line $start in $filePath")

            label = label match {
                case s"function($rest" => if (filePath != "Native") s"<anonymous:$start>" else label
                case "<main>" => "global"
                case _ => label.split("\\(")(0)
            }

            nodeMap += (id -> Node(id, label, filePath, Position(start)))
        })

        val edgeArray = edges.map(edge => Edge(nodeMap(edge.split("->").head.trim), nodeMap(edge.split("->").last.trim)))

        if (debug) {
            println("[DEBUG] nodes: " + nodeMap.mkString("\n"))
            println("[DEBUG] edges: " + edgeArray.mkString("\n"))
        }

        val jsonCG = write(edgeArray)
        jsonCG
    }

    case class Position(row: Int)

    case class Node(id: String, label: String, file: String, start: Position)

    case class Edge(source: Node, target: Node)

    private object Position {
        implicit val rw: ReadWriter[Position] = macroRW
    }

    private object Node {
        implicit val rw: ReadWriter[Node] = macroRW
    }

    private object Edge {
        implicit val rw: ReadWriter[Edge] = macroRW
    }
}
