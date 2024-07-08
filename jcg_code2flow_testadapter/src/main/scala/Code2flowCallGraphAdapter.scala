import upickle.default._

import java.io.{BufferedWriter, File, FileWriter}
import scala.collection.mutable

object Code2flowCallGraphAdapter extends TestAdapter {
    val debug: Boolean = false

    val possibleAlgorithms: Array[String] = Array("NONE")
    val frameworkName: String = "code2flow"

    def main(args: Array[String]): Unit = {
        // generate call graphs for all algorithms
        for (algo <- possibleAlgorithms) {
            generateCallGraphs(s"results/js/$frameworkName", algo)
        }
    }

    private def generateCallGraphs(outputDirPath: String, algorithm: String): Unit = {
        val outputDir = new File(s"$outputDirPath/$algorithm")
        outputDir.mkdirs()
        val inputDirs = new File("testcasesOutput/js").list()

        // check if js-callgraph command is available
        val command = "code2flow"
        try {
            sys.process.Process(Seq(command, "--help")).!!
        } catch {
            case e: Exception => {
                println(s"${Console.RED}[Error]: $command command not found, make sure it is installed and in your PATH${Console.RESET}")
                return
            }
        }

        // generate callgraph for every testcase
        for (inputDir <- inputDirs) {
            val inputDirPath = s"testcasesOutput/js/$inputDir"
            val outputPath = s"${outputDir.getAbsolutePath}/$inputDir.json"
            val args = Seq(inputDirPath, "--output", outputPath, "-q", "--source-type=module", "--no-trimming")
            println(s"[DEBUG] executing ${(Seq(command) ++ args).mkString(" ")}")
            val processSucceeded = try {
                sys.process.Process(Seq(command) ++ args).!!
                true
            } catch {
                case e: Exception =>
                    println(s"${Console.RED}[Error]: $inputDir failed to generate${Console.RESET}")
                    false
            }

            if (processSucceeded) {
                val outputFile = new File(outputPath)
                try {
                    val json = toCommonFormat(outputFile)
                    val bw = new BufferedWriter(new FileWriter(outputFile))
                    bw.write(json)
                    bw.close()
                } catch {
                    case e: Exception =>
                        println(s"${Console.RED}[Error]: Failed to process and write the call graph for $inputDir${Console.RESET}")
                }
            }
        }

        println("Call graphs generated!")
    }

    private def toCommonFormat(cgFile: File): String = {
        //TODO: Check if file empty
        val cg = ujson.read(cgFile).obj("graph")
        val nodes: mutable.Map[String, Node] = cg("nodes").obj.map(node => getNodeFromKV(node, cgFile.getAbsolutePath))
        val edges: Array[Edge] = cg("edges").arr.map(edge => Edge(nodes(edge("source").str), nodes(edge("target").str))).toArray

        if (debug) {
            println("[DEBUG] nodes: " + nodes.mkString("\n"))
            println("[DEBUG] edges: " + edges.mkString("\n"))
        }
        val jsonCG = write(edges)
        jsonCG
    }

    private def getNodeFromKV(kv: (String, ujson.Value), folder: String): (String, Node) = {
        val splitName = kv._2("name").str.split("::")
        val label = if (splitName.last == "(global)") "global" else splitName.last
        val path = folder.split("/").last.split("\\.").head + "/" + splitName.head + ".js"
        val start = kv._2("label").str.split(":").head.toInt

        kv._1 -> Node(kv._1, label, path, start)
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
