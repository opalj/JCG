import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.io.Writer

import play.api.libs.json.JsSuccess
import play.api.libs.json.Json

import scala.io.Source

object Evaluation {

    val debug = true
    val runHermes = true
    val hermesResult = "hermes.csv"
    val hermesLocationsDir = "hermesResults/"
    val projectSpecifigEvaluation = true
    val runAnalyses = true
    val isAnnotatedProject = false
    val OUTPUT_FILENAME = "evaluation_results.tsv"
    val PROJECTS_DIR_PATH = "result/"
    val JRE_LOCATIONS_FILE = "jre.json"
    val EVALUATION_ADAPTERS = List(new SootJCGAdatper()) //, new WalaJCGAdapter())
    val HERMES_PROJECT_FILE = "hermes.json"

    def main(args: Array[String]): Unit = {
        val projectsDir = new File(PROJECTS_DIR_PATH)

        var jarFilter = ""
        var target = ""
        args.sliding(2, 2).toList.collect {
            case Array("--output", t: String)    ⇒ target = t
            case Array("--filter", name: String) ⇒ jarFilter = name
        }

        val jreLocations = JRELocation.mapping(new File(JRE_LOCATIONS_FILE))

        val outputTarget = getOutputTarget(target)
        val ow = new BufferedWriter(outputTarget)

        if (projectsDir.exists && projectsDir.isDirectory) {
            if (runHermes) {
                TestCaseHermesJsonExtractor.createHermesJsonFile(
                    projectsDir, jreLocations, new File(HERMES_PROJECT_FILE)
                )

                val hermesDefaultArgs = Array(
                    "-config", HERMES_PROJECT_FILE,
                    "-statistics", hermesResult
                )
                val writeLocationsArgs =
                    if (projectSpecifigEvaluation)
                        Array(
                            "-writeLocations", hermesLocationsDir
                        )
                    else Array.empty[String]

                org.opalj.hermes.HermesCLI.main(
                    hermesDefaultArgs ++ writeLocationsArgs
                )
            }
            val locations: Map[String, Map[String, Set[Method]]] =
                if (projectSpecifigEvaluation) {
                    val locations = new File(hermesLocationsDir)
                    assert(locations.exists() && locations.isDirectory)
                    (for {
                        projectLocation ← locations.listFiles(_.getName.endsWith(".tsv"))
                        line ← Source.fromFile(projectLocation).getLines().drop(1)
                    } yield {
                        val Array(projectId, featureId, _, _, className, methodName, md, _, _) = line.split("\t", -1)
                        val mdMatch = """\((.*)\)(.*)""".r.findFirstMatchIn(md).get
                        val paramsDescr = mdMatch.group(1)
                        val params =
                            if (paramsDescr.isEmpty)
                                Array.empty[String]
                            else
                                paramsDescr.split(";")
                        val returnType = mdMatch.group(2)
                        (projectId, featureId, Method(methodName, className, returnType, params))
                    }).groupBy(_._1).map {
                        case (pId, group1) ⇒ pId → group1.map { case (_, f, m) ⇒ f → m }.groupBy(_._1).map {
                            case (fId, group2) ⇒ fId → group2.map(_._2).toSet
                        }
                    }
                } else
                    Map.empty

            if (runAnalyses) {
                runAnalyses(projectsDir, jarFilter, ow, locations, jreLocations)
            }

            ow.flush()
            ow.close()
        }
    }

    private def runAnalyses(
        projectsDir:  File,
        jarFilter:    String,
        ow:           BufferedWriter,
        locationsMap: Map[String, Map[String, Set[Method]]],
        jreLocations: Map[Int, String]
    ): Unit = {
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
                                    Array(jreLocations(projectSpec.java)) ++ projectSpec.allClassPathEntryFiles.map(_.getAbsolutePath),
                                    jsFileName
                                )
                                System.gc()

                                if (isAnnotatedProject) {

                                    val result = CGMatcher.matchCallSites(
                                        projectSpec.target,
                                        jsFileName
                                    )
                                    ow.write(s"\t${result.shortNotation}")
                                }

                                if (projectSpecifigEvaluation) {
                                    val json = Json.parse(new FileInputStream(new File(jsFileName)))
                                    val callSites = json.validate[CallSites].get
                                    for {
                                        (fId, locations) ← locationsMap(projectSpec.name)
                                        location ← locations
                                    } {
                                        // todo we are unsound -> write that info somewhere
                                        // source and how often
                                        val unsound = callSites.callSites.exists { cs ⇒
                                            cs.method == location || cs.targets.contains(location)
                                        }
                                        if (unsound)
                                            println(s"${projectSpec.name} - $fId - $location)") //
                                    }
                                }

                            } catch {
                                case e: Throwable ⇒
                                    if (debug)
                                        println(projectSpec.name)
                                    println(e.printStackTrace())
                                    ow.write(s"\tE")
                            }
                        case _ ⇒ throw new IllegalArgumentException("invalid project.conf")
                    }

                }
                ow.newLine()
            }
        }
    }

    private def printHeader(ow: BufferedWriter, jars: Array[File]): Unit = {
        ow.write("algorithm")
        for (tgt ← jars) {
            ow.write(s"\t$tgt")
        }
        ow.newLine()
    }

    def getOutputTarget(target: String): Writer = target match {
        case "c" ⇒ new OutputStreamWriter(System.out)
        case "f" ⇒
            val outputFile = new File(OUTPUT_FILENAME);
            if (outputFile.exists()) {
                outputFile.delete()
                outputFile.createNewFile()
            }

            new FileWriter(outputFile, false)
        case _ ⇒ new OutputStreamWriter(System.out)
    }
}
