import java.io.File
import scala.collection.mutable.ArrayBuffer
import upickle.default._

import java.io.BufferedWriter
import java.io.FileWriter

object PyCGCallGraphAdapter extends JSTestAdapter {
    val debug: Boolean = true
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
    override def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit = {
        for (algo <- possibleAlgorithms) {
            generateCallGraphs(inputDirPath, outputDirPath, algo)
        }
    }

    private def generateCallGraphs(inputDirPath: String, outputDirPath: String, algorithm: String): Unit = {
        val outputDir = new File(s"$outputDirPath/$algorithm")
        outputDir.mkdirs()
        println(inputDirPath)
        val testDirs = new File(inputDirPath).list()

        // check if pycg command is available
        try {
            sys.process.Process(Seq(command, "-h")).!!
        } catch {
            case e: Exception => println(s"${Console.RED}[ERROR]: $command not found${Console.RESET}")
        }

        testDirs.foreach(testDir => {
            serializeCG(algorithm, outputDir.getAbsolutePath, s"$inputDirPath/$testDir")
            return
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
        val outputPath = s"$outputDirPath/${inputDirPath.split(File.separator).last}.json"
        val files = new File(inputDirPath).listFiles()(0).listFiles().filter(_.getName.endsWith(".py")).map(_.getAbsolutePath)
        val mainFilePath = if (files.length == 1) files(0) else files.find(_.contains("main")).getOrElse(files(0))
        println(mainFilePath)
        val args = Seq(mainFilePath, "-o", outputPath, "--fasten")
        if (debug) println(s"[DEBUG] executing ${(Seq(command) ++ args).mkString(" ")}")

        // clear output file if already exists
        val outputFile = new File(outputPath)
        if (outputFile.exists()) {
            outputFile.delete()
        }

        val start = System.nanoTime()
        val processSucceeded = try {
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
            val outputFile = new File(outputPath)
            //try {
                val json = toCommonFormat(outputFile)
                val bw = new BufferedWriter(new FileWriter(outputFile))
                bw.write(json)
                bw.close()

        }

        end - start
    }

    private def toCommonFormat(cgFile: File): String = {
        val file = ujson.read(cgFile).obj
        val cg = file("graph")

        // merge "internalCalls", "resolvedCalls" and externalCalls array
        val edges = cg("internalCalls").arr ++ cg("resolvedCalls").arr ++ cg("externalCalls").arr
        println(edges)
        val nodes = parseNodes(file)
        /*val resultEdges: Array[Edge] = edges.map(edgeArr => {
            // array contains three elements, last one is always empty object
            val source = edgeArr(0).str
            val target = edgeArr(1).str
            Edge(nodes.find(_.id == source).get, nodes.find(_.id == target).get)
        }).toArray*/
        val jsonCG = write(edges)
        jsonCG
    }

    /**
     * Parses the nodes from the given JSON object.
     * @param cg The JSON object to parse the nodes from.
     * @return An array of nodes.
     */
    private def parseNodes(cg: ujson.Obj): Array[Node] = {
        val internalModules = cg("modules").obj("internal").obj.values
        val nodes: ArrayBuffer[Node] = ArrayBuffer()

        internalModules.foreach(module => {
            val filePath = module("sourceFile").str

            val namespaces = module("namespaces").obj
            namespaces.foreach(jsonNode => {
                val node = getNodeFromKV(jsonNode, filePath)

                println(s"ID: ${node.id}, Label: ${node.label}, File: ${node.file}, Row: ${node.start}")
                nodes += node
            })
        })


        val externalModules = cg("modules").obj("external").obj
        externalModules.foreach(module => {
            val filePath = if (module._1 == ".builtin") "Native" else module._2("sourceFile").str
            val namespaces = module._2("namespaces").obj

            println(namespaces)
            namespaces.foreach(jsonNode => {
                val node = getNodeFromKV(jsonNode, filePath)

                //println(s"ID: ${node.id}, Label: ${node.label}, File: ${node.file}, Row: ${node.start}")
                nodes += node
            })
        })
        //println(internalModules)
        println("Nodes:", nodes)
        nodes.toArray
    }


    private def getNodeFromKV(kv: (String, ujson.Value), filePath: String): Node = {
        val label = kv._2("namespace").strOpt.getOrElse("").split("/").last
        val row = kv._2("metadata").objOpt match {
            case Some(metadata) => metadata.get("first").flatMap(_.numOpt).getOrElse(0.0).toInt
            case None => 0
        }

        Node(kv._1, label, filePath, Position(row))
    }
}
