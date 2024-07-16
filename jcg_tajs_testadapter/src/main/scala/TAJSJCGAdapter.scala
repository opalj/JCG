import upickle.default._

import java.io.{BufferedWriter, File, FileWriter}
import scala.collection.mutable

object TAJSJCGAdapter extends TestAdapter {
    val debug: Boolean = false

    val possibleAlgorithms: Array[String] = Array("NONE")
    val frameworkName: String = "TAJS"

    def main(args: Array[String]): Unit = {
        // generate call graphs for all algorithms
        for (algo <- possibleAlgorithms) {
            generateCallGraphs(s"results/js/$frameworkName", algo)
        }
    }

    private def generateCallGraphs(outputDirPath: String, algorithm: String): Unit = {
        val inputDirs = new File("testcasesOutput/js").list()
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
        val command = Seq("java", "-jar", tajsJar.getAbsolutePath)

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
        inputDirs.foreach(inputDir => {
            val inputDirPath = new File(s"testcasesOutput/js/$inputDir").listFiles()(0).listFiles().map(_.getAbsolutePath).filter(_.endsWith(".js"))
            val outputPath = s"${outputDir.getAbsolutePath}/$inputDir.json"
            val args = Seq("-callgraph", inputDirPath(0))
            println(s"[DEBUG] executing ${args.mkString(" ")}")
            val processSucceeded = try {
                sys.process.Process(command ++ args).!!
                true
            } catch {
                case e: Exception =>
                    println(s"${Console.RED}[Error]: $inputDir failed to generate${Console.RESET}")
                    false
            }

            if (processSucceeded) {
                try {
                    val json = toCommonFormat(new File("out/callgraph.dot"))
                    val bw = new BufferedWriter(new FileWriter(outputPath))
                    bw.write(json)
                    bw.close()
                } catch {
                    case e: Exception =>
                        println(s"${Console.RED}[Error]: Failed to process and write the call graph for $inputDir${Console.RESET}")
                }
            }
        })
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

            nodeMap += (id -> Node(id, label, filePath, start))
        })

        val edgeArray = edges.map(edge => Edge(nodeMap(edge.split("->").head.trim), nodeMap(edge.split("->").last.trim)))

        if (debug) {
            println("[DEBUG] nodes: " + nodeMap.mkString("\n"))
            println("[DEBUG] edges: " + edgeArray.mkString("\n"))
        }

        val jsonCG = write(edgeArray)
        jsonCG
    }

    case class Node(id: String, label: String, file: String, start: Int)

    case class Edge(source: Node, target: Node)

    private object Node {
        implicit val rw: ReadWriter[Node] = macroRW
    }

    private object Edge {
        implicit val rw: ReadWriter[Edge] = macroRW
    }
}
