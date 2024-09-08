import java.io.File
import java.io.FileWriter
import java.io.Writer
import scala.collection.mutable.ArrayBuffer

import upickle.default._

object PyCGAdapter extends PyTestAdapter {
    val debug: Boolean = false
    val frameworkName: String = "PyCG"
    val possibleAlgorithms: Array[String] = Array("NONE")
    private val command = "pycg"

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
        val testDirs = new File(inputDirPath).list()

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
        val tempFile = new File(s"temp/$frameworkName/$algorithm/out.json")
        tempFile.getParentFile.mkdirs()

        val args = Seq(mainFilePath, "-o", tempFile.getAbsolutePath, "--fasten")
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
            }
        }

        end - start
    }

    private def toCommonFormat(cgFile: File): String = {
        val file = ujson.read(cgFile).obj
        val cg = file("graph")

        // merge "internalCalls", "resolvedCalls" and externalCalls array
        val edges = cg("internalCalls").arr ++ cg("resolvedCalls").arr ++ cg("externalCalls").arr
        val nodes = parseNodes(file)
        if (debug) {
            println("[DEBUG] nodes:\n" + nodes.mkString("\n"))
            println("[DEBUG] edges:\n" + edges.mkString("\n"))
        }

        val resultEdges: Array[Edge] = edges.map(edgeArr => {
            // array contains three elements, last one is always empty object
            val source = edgeArr(0).str
            val target = edgeArr(1).str
            Edge(nodes.find(_.id == source).get, nodes.find(_.id == target).get)
        }).toArray
        val jsonCG = write(resultEdges)
        jsonCG
    }

    /**
     * Parses the nodes from the given JSON object.
     *
     * @param cg The JSON object to parse the nodes from.
     * @return An array of nodes.
     */
    private def parseNodes(cg: ujson.Obj): Array[Node] = {
        val nodes: ArrayBuffer[Node] = ArrayBuffer()

        val internalModules = cg("modules").obj("internal").obj.values
        internalModules.foreach(module => {
            val filePath = module("sourceFile").str
            val namespaces = module("namespaces").obj

            nodes ++= getNodesFromNamespace(namespaces, filePath)
        })

        val externalModules = cg("modules").obj("external").obj
        externalModules.foreach(module => {
            val filePath = if (module._1 == ".builtin") "Native" else module._2("sourceFile").str
            val namespaces = module._2("namespaces").obj

            nodes ++= getNodesFromNamespace(namespaces, filePath)
        })

        nodes.toArray
    }

    /**
     * Extract nodes from a namespace object.
     *
     * @param namespace The namespace object to extract nodes from.
     * @param filePath  The file path of the namespace.
     * @return An array of nodes extracted from the namespace.
     */
    private def getNodesFromNamespace(namespace: ujson.Obj, filePath: String): Array[Node] = {
        val nodes: ArrayBuffer[Node] = ArrayBuffer()
        namespace.obj.foreach(jsonNode => {
            val node = getNodeFromKV(jsonNode, filePath)
            nodes += node
        })
        nodes.toArray
    }

    /**
     * Extract call graph node from json key-value pair.
     *
     * @param kv       key-value pair.
     * @param filePath The file path of the nodes.
     * @return The node extracted from the key-value pair.
     */
    private def getNodeFromKV(kv: (String, ujson.Value), filePath: String): Node = {
        var label = kv._2("namespace").strOpt.getOrElse("").split("/").last
        val row = kv._2("metadata").objOpt match {
            case Some(metadata) => metadata.get("first").flatMap(_.numOpt).getOrElse(0.0).toInt
            case None           => 0
        }

        val mainPath = filePath.split("\\.py").head.replaceAll("/", ".")

        label = label match {
            case l if l == mainPath         => "global"
            case l if l.contains("<lambda") => "anon"
            case _                          => label.replaceAll("\\(\\)", "")
        }

        Node(kv._1, label, filePath, Position(row))
    }

}
