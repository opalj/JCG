import upickle.default._

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import scala.collection.mutable

object Code2flowCallGraphAdapter extends JSTestAdapter {
    val debug: Boolean = false

    val possibleAlgorithms: Array[String] = Array("NONE")
    val frameworkName: String = "code2flow"
    private val command = "code2flow"

    def main(args: Array[String]): Unit = serializeAllCGs("testcasesOutput/js", s"results/js/$frameworkName")

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
        val outputDir = new File(s"$outputDirPath/$algorithm")
        outputDir.mkdirs()
        val testDirs = new File(inputDirPath).list()

        // check if js-callgraph command is available
        val command = "code2flow"
        try {
            sys.process.Process(Seq(command, "--help")).!!
        } catch {
            case e: Exception =>
                println(s"${Console.RED}[Error]: $command command not found, make sure it is installed and in your PATH${Console.RESET}")
                return

        }

        // generate callgraph for every testcase
        testDirs.foreach(testDir => {
            serializeCG(algorithm, s"$inputDirPath/$testDir", outputDir.getAbsolutePath)
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
    def serializeCG(algorithm: String, inputDirPath: String, outputDirPath: String, adapterOptions: AdapterOptions): Long = {
        val outputPath = s"$outputDirPath/${inputDirPath.split(File.separator).last}.json"
        val args = Seq(inputDirPath, "--output", outputPath, "-q", "--source-type=module", "--no-trimming")
        if (debug) println(s"[DEBUG] executing ${(Seq(command) ++ args).mkString(" ")}")

        // Generate call graph
        val start = System.nanoTime()
        val processSucceeded = try {
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
            val outputFile = new File(outputPath)
            try {
                val json = toCommonFormat(outputFile)
                val bw = new BufferedWriter(new FileWriter(outputFile))
                bw.write(json)
                bw.close()
            } catch {
                case e: Exception => println(s"${Console.RED}[Error]: Failed to process and write the call graph for $inputDirPath${Console.RESET}")
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
        val cg = ujson.read(cgFile).obj("graph")
        val nodes: mutable.Map[String, Node] = cg("nodes").obj.map(node => getNodeFromKV(node, cgFile.getAbsolutePath))
        val edges: Array[Edge] = cg("edges").arr.map(edge => Edge(nodes(edge("source").str), nodes(edge("target").str))).toArray

        if (debug) {
            println("[DEBUG] nodes:\n" + nodes.mkString("\n"))
            println("[DEBUG] edges:\n" + edges.mkString("\n"))
        }
        val jsonCG = write(edges)
        jsonCG
    }

    private def getNodeFromKV(kv: (String, ujson.Value), folder: String): (String, Node) = {
        val splitName = kv._2("name").str.split("::")
        val label = if (splitName.last == "(global)") "global" else splitName.last
        val path = folder.split("/").last.split("\\.").head + "/" + splitName.head + ".js"
        val start = kv._2("label").str.split(":").head.toInt

        kv._1 -> Node(kv._1, label, path, Position(start))
    }


}
