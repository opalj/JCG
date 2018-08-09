import java.io.File
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.io.Writer

import org.opalj.bytecode
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json

object Evaluation {

    val debug = false;
    val OUTPUT_FILENAME = "evaluation_results.tsv"
    val PROJECTS_DIR_PATH = "result/"
    val EVALUATION_ADAPTERS = List(new SootJCGAdatper(), new WalaJCGAdapter())

    def main(args: Array[String]): Unit = {
        val rtJar = bytecode.RTJar.getAbsolutePath
        val projectsDir = new File(PROJECTS_DIR_PATH)

        var jarFilter = ""
        var target = ""
        args.sliding(2, 2).toList.collect {
            case Array("--output", t: String)    ⇒ target = t
            case Array("--filter", name: String) ⇒ jarFilter = name
        }

        val outputTarget = getOutputTarget(target)
        val ow = new BufferedWriter(outputTarget)

        if (projectsDir.exists && projectsDir.isDirectory) {
            val projectSpecFiles = projectsDir.listFiles((_, name) ⇒ name.endsWith(".conf")).filter(_.getName.startsWith(jarFilter)).sorted
            printHeader(ow, projectSpecFiles)

            for (adapter ← EVALUATION_ADAPTERS) {
                for (cgAlgo ← adapter.possibleAlgorithms()) {
                    ow.write(s"${adapter.frameworkName()} $cgAlgo")
                    for (projectSpecFile ← projectSpecFiles) {

                        val json = Json.parse(new FileInputStream(projectSpecFile))

                        json.validate[ProjectSpecification] match {
                            case JsSuccess(projectSpec, _) ⇒
                                try {
                                    val jsFileName = s"${adapter.frameworkName()}-$cgAlgo-${projectSpec.name}.json"
                                    adapter.serializeCG(
                                        cgAlgo,
                                        projectSpec.target,
                                        projectSpec.main.orNull,
                                        Array(rtJar) ++ projectSpec.allClassPathEntryFiles.map(_.getAbsolutePath), //TODO add correct RT Jar
                                        jsFileName
                                    )
                                    System.gc()
                                    val result = CGMatcher.matchCallSites(
                                        projectSpec.target,
                                        jsFileName
                                    )
                                    ow.write(s"\t${result.shortNotation}")
                                } catch {
                                    case e: Throwable ⇒
                                        if (debug)
                                            println(e.printStackTrace());
                                        ow.write(s"\tE")
                                }
                            case _ ⇒ throw new IllegalArgumentException("invalid project.conf")
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
