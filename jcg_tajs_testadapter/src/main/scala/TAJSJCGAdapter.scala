import java.io.File
import java.io.FileWriter
import java.io.Writer
import scala.collection.mutable

import upickle.default._

object TAJSJCGAdapter extends JSTestAdapter {
    val debug: Boolean = false

    val possibleAlgorithms: Array[String] = Array("NONE")
    val frameworkName: String = "TAJS"
    private lazy val command: Option[Seq[String]] = {
        // read tajs location from tajs variable in tajs.properties
        val tajsProperties = new File("tajs.properties")
        if (!tajsProperties.exists()) {
            println("[ERROR] tajs.properties not found")
            None
        } else {
            import scala.util.Using

            val tajsLocation = Using(scala.io.Source.fromFile(tajsProperties)) { source =>
                source.getLines.find(_.replaceAll(" ", "").startsWith("tajs=")).getOrElse("")
            }.getOrElse("")

            val tajsJar = new File(s"${tajsLocation.split("=")(1)}dist/tajs-all.jar".trim())
            if (!tajsJar.exists()) {
                println("[ERROR] tajs location not found in tajs.properties")
                None
            }
            Some(Seq("java", "-jar", tajsJar.getAbsolutePath))
        }
    }

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
    private def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit = {
        for (algo <- possibleAlgorithms) {
            generateCallGraphs(inputDirPath, outputDirPath, algo)
        }
    }

    private def generateCallGraphs(inputDirPath: String, outputDirPath: String, algorithm: String): Unit = {
        val testDirs = new File(inputDirPath).list()
        val outputDir = new File(s"$outputDirPath/$algorithm")
        outputDir.mkdirs()

        // check if tajs command is available
        try {
            val out = sys.process.Process(command.get)
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
            val output = new FileWriter(outputDir.getAbsolutePath + "/" + testDir + ".json")
            serializeCG(algorithm, s"$inputDirPath/$testDir", output)
            output.close()
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
    def serializeCG(
        algorithm:      String,
        inputDirPath:   String,
        output:         Writer,
        adapterOptions: AdapterOptions
    ): Long = {
        if (command.isEmpty) throw new Exception(
            "TAJS command not available. Make sure you set the tajs variable in tajs.properties correctly."
        )
        // TAJS does not support multi-file projects
        val inputFilePath =
            new File(inputDirPath).listFiles()(0).listFiles().map(_.getAbsolutePath).filter(_.endsWith(
                ".js"
            )).head
        val args = Seq("-callgraph", inputFilePath)
        if (debug) println(s"[DEBUG] executing ${args.mkString(" ")}")
        val start = System.nanoTime()
        val processSucceeded =
            try {
                sys.process.Process(command.get ++ args).!!
                true
            } catch {
                case e: Exception =>
                    println(s"${Console.RED}[Error]: $inputFilePath failed to generate${Console.RESET}")
                    false
            }
        val end = System.nanoTime()

        if (processSucceeded) {
            try {
                val json = toCommonFormat(new File("out/callgraph.dot"))
                output.write(json)
            } catch {
                case e: Exception =>
                    println(s"${Console.RED}[Error]: Failed to process and write the call graph for $inputFilePath${Console.RESET}")
            }
        }
        end - start
    }

    private def toCommonFormat(cgFile: File): String = {
        val dotGraph = DOTGraphParser.parseFile(cgFile)
        val nodeMap: mutable.Map[String, Node] = mutable.Map()
        dotGraph.nodes.foreach(node => {
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
                case "<main>"          => "global"
                case _                 => label.split("\\(")(0)
            }

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
