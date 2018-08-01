import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.io.Writer

import org.opalj.bytecode
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json

object Evaluation {

    val debug = true;
    val OUTPUT_FILENAME = "evaluation_results.tsv"
    val JAR_DIR_PATH = "result/"
    val EVALUATION_ADAPTERS = List(new SootJCGAdatper(), new WalaJCGAdapter())

    def main(args: Array[String]): Unit = {
        val rtJar = bytecode.RTJar.getAbsolutePath
        val dir = new File(JAR_DIR_PATH)

        var jarFilter = ""
        var target = ""
        args.sliding(2, 2).toList.collect {
            case Array("--output", t: String)    ⇒ target = t
            case Array("--filter", name: String) ⇒ jarFilter = name
        }

        val outputTarget = getOutputTarget(target)
        val ow = new BufferedWriter(outputTarget)

        if (dir.exists && dir.isDirectory) {
            val projectSpecs = dir.listFiles((_, name) ⇒ name.endsWith(".conf")).filter(_.getName.startsWith(jarFilter)).sorted
            printHeader(ow, projectSpecs)
            for (adapter ← EVALUATION_ADAPTERS) {
                for (cgAlgo ← adapter.possibleAlgorithms()) {
                    ow.write(s"${adapter.frameworkName()} $cgAlgo")
                    for (projectSpecFile ← projectSpecs) {
                        val jsValue = Json.parse(new FileInputStream(projectSpecFile))
                        jsValue.validate[ProjectSpecification] match {
                            case JsSuccess(projectSpecification, _) ⇒
                                try {
                                    adapter.serializeCG(
                                        cgAlgo,
                                        projectSpecification.target,
                                        projectSpecification.main.orNull,
                                        Array(rtJar) ++ projectSpecification.cp.toArray.flatten.flatMap(_.getLocations).map(_.getAbsolutePath),
                                        s"${adapter.frameworkName()}-$cgAlgo-${projectSpecification.name}.json"
                                    )
                                    System.gc()
                                    val result = CGMatcher.matchCallSites(projectSpecification.target, s"${adapter.frameworkName()}-$cgAlgo-${projectSpecification.name}.json")
                                    ow.write(s"\t${result.shortNotation}")
                                } catch {
                                    case e: Throwable ⇒
                                        ow.write(s"\tE")
                                }
                            case _ ⇒ throw new IllegalArgumentException()
                        }
                    }
                    ow.newLine()
                }
            }
            ow.flush()
            ow.close()
        }
    }

    private def printHeader(ow: BufferedWriter, jars: Array[File]) = {
        ow.write("algorithm")
        for (tgt ← jars) {
            ow.write(s"\t$tgt")
        }
        ow.newLine()
    }

    def getOutputTarget(target: String): Writer = {
        target match {
            case "c" ⇒ new OutputStreamWriter(System.out)
            case "f" ⇒ {
                val outputFile = new File(OUTPUT_FILENAME);
                if (outputFile.exists()) {
                    outputFile.delete()
                    outputFile.createNewFile()
                }

                new FileWriter(outputFile, false)
            }
            case _ ⇒ new OutputStreamWriter(System.out)
        }
    }
}
