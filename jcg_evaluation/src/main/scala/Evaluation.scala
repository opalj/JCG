import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.io.Writer

import org.opalj.br.MethodDescriptor
import org.opalj.log.GlobalLogContext
import org.opalj.log.OPALLogger
import org.opalj.util.PerformanceEvaluation.time
import play.api.libs.json.Json

import scala.io.Source

object Evaluation {

    var debug = true
    var runHermes = false
    var projectSpecificEvaluation = false
    var runAnalyses = true
    var isAnnotatedProject = true

    var RESULTS_DIR_PATH = "evaluation/" // todo merge outputs
    val JRE_LOCATIONS_FILE = "jre.conf"
    val EVALUATION_ADAPTERS = List(SootJCGAdapter, WalaJCGAdapter, OpalJCGAdatper)

    def main(args: Array[String]): Unit = {
        var input = ""
        var jarFilter = ""
        var target = ""
        args.sliding(2, 2).toList.collect {
            case Array("--input", i: String)                ⇒ input = i
            case Array("--output", t: String)               ⇒ target = t
            case Array("--output-dir", t: String)           ⇒ RESULTS_DIR_PATH = t
            case Array("--filter", name: String)            ⇒ jarFilter = name
            case Array("--debug", value: String)            ⇒ debug = value.toBoolean
            case Array("--hermes", value: String)           ⇒ runHermes = value.toBoolean
            case Array("--analyze", value: String)          ⇒ runAnalyses = value.toBoolean
            case Array("--project-specific", value: String) ⇒ projectSpecificEvaluation = value.toBoolean
            case Array("--testcase", value: String)         ⇒ isAnnotatedProject = value.toBoolean
        }

        if (projectSpecificEvaluation)
            assert(runAnalyses, "`--analyze` must be set to true on `--project-specific true`")

        if (isAnnotatedProject)
            assert(runAnalyses, "`--analyze` must be set to true on `--testcase true`")

        assert(input.nonEmpty, "no input directory specified")
        val projectsDir = new File(input)

        assert(projectsDir.exists(), s"${projectsDir.getPath} does not exists")
        assert(projectsDir.isDirectory, s"${projectsDir.getPath} is not a directory")
        assert(
            projectsDir.listFiles(_.getName.endsWith(".conf")).nonEmpty,
            s"${projectsDir.getPath} does not contain *.conf files"
        )
        val resultsDir = new File(RESULTS_DIR_PATH)
        resultsDir.mkdirs()

        val jreLocations = JRELocation.mapping(new File(JRE_LOCATIONS_FILE))

        val outputTarget = getOutputTarget(target, resultsDir)
        val ow = new BufferedWriter(outputTarget)

        if (projectsDir.exists && projectsDir.isDirectory) {
            if (runHermes) {
                if (debug)
                    println("running hermes")

                val hermesFile = new File("hermes.json")
                assert(!hermesFile.exists())

                if (!debug)
                    OPALLogger.updateLogger(GlobalLogContext, new DevNullLogger())

                TestCaseHermesJsonExtractor.createHermesJsonFile(
                    projectsDir, jreLocations, hermesFile
                )

                val hermesDefaultArgs = Array(
                    "-config", hermesFile.getPath,
                    "-statistics", s"$RESULTS_DIR_PATH${File.separator}hermes.csv"
                )
                val writeLocationsArgs =
                    if (projectSpecificEvaluation)
                        Array(
                            "-writeLocations", RESULTS_DIR_PATH
                        )
                    else Array.empty[String]

                time {
                    org.opalj.hermes.HermesCLI.main(
                        hermesDefaultArgs ++ writeLocationsArgs
                    )
                } { t ⇒
                    if (debug)
                        println(s"hermes run took ${t.toSeconds} seconds")
                }
                hermesFile.delete()
            }
            val locations: Map[String, Map[String, Set[Method]]] =
                if (projectSpecificEvaluation) {
                    if (debug)
                        println("create locations mapping")
                    (for {
                        projectLocation ← resultsDir.listFiles(_.getName.endsWith(".tsv"))
                        line ← Source.fromFile(projectLocation).getLines().drop(1)
                        lineSplit = line.split("\t", -1)
                        if lineSplit.size == 9
                        Array(projectId, featureId, _, _, classString, methodName, mdString, _, _) = lineSplit
                        if methodName.nonEmpty && mdString.nonEmpty
                    } yield {
                        val projectName = projectId.replace("\"", "")
                        val featureName = featureId.replace("\"", "")
                        val className = classString.replace("\"", "")
                        val md = MethodDescriptor(mdString.replace("\"", ""))
                        val params = md.parameterTypes.map(_.toJVMTypeName).toList
                        val returnType = md.returnType.toJVMTypeName
                        (projectName, featureName, Method(methodName, className, returnType, params))
                    }).groupBy(_._1).map {
                        case (pId, group1) ⇒ pId → group1.map { case (_, f, m) ⇒ f → m }.groupBy(_._1).map {
                            case (fId, group2) ⇒ fId → group2.map(_._2).toSet
                        }
                    }
                } else
                    Map.empty

            if (runAnalyses) {
                runAnalyses(projectsDir, resultsDir, jarFilter, ow, locations, jreLocations)
            }

            ow.flush()
            ow.close()
        }
    }

    private def runAnalyses(
        projectsDir:  File,
        resultsDir:   File,
        jarFilter:    String,
        ow:           BufferedWriter,
        locationsMap: Map[String, Map[String, Set[Method]]],
        jreLocations: Map[Int, Array[File]]
    ): Unit = {
        val projectSpecFiles = projectsDir.listFiles((_, name) ⇒ name.endsWith(".conf")).filter(_.getName.startsWith(jarFilter)).sorted
        printHeader(ow, projectSpecFiles)

        for (adapter ← EVALUATION_ADAPTERS) {
            for (cgAlgo ← adapter.possibleAlgorithms()) {
                ow.write(s"${adapter.frameworkName()} $cgAlgo")
                for (projectSpecFile ← projectSpecFiles) {

                    val json = Json.parse(new FileInputStream(projectSpecFile))

                    val projectSpec = json.validate[ProjectSpecification].getOrElse {
                        throw new IllegalArgumentException("invalid project.conf")
                    }

                    println(s"running ${adapter.frameworkName()} $cgAlgo against ${projectSpec.name}")

                    val outDir = new File(resultsDir, s"${projectSpec.name}${File.separator}${adapter.frameworkName()}${File.separator}$cgAlgo")
                    outDir.mkdirs()

                    val jsFile = new File(outDir, "cg.json")
                    try {
                        val jreJars = jreLocations(projectSpec.java).map(_.getCanonicalPath)
                        val cp = projectSpec.allClassPathEntryFiles(projectsDir).map(_.getCanonicalPath)

                        val elapsed = adapter.serializeCG(
                            cgAlgo,
                            projectSpec.target(projectsDir).getCanonicalPath,
                            projectSpec.main.orNull,
                            jreJars ++ cp,
                            jsFile.getPath
                        )

                        println(s"analysis took ${elapsed / 1000000000d}")

                        System.gc()

                        if (isAnnotatedProject) {
                            val result = CGMatcher.matchCallSites(
                                projectSpec,
                                jreLocations,
                                projectsDir,
                                jsFile.getPath,
                                debug
                            )
                            ow.write(s"\t${result.shortNotation}")
                        }

                        if (projectSpecificEvaluation && jsFile.exists()) {
                            val pw = new PrintWriter(new File(outDir, "pse.tsv"))
                            val json = Json.parse(new FileInputStream(jsFile))
                            val reachableMethods = json.validate[ReachableMethods].get.toMap
                            for {
                                (fId, locations) ← locationsMap(projectSpec.name)
                                location ← locations
                                if reachableMethods.contains(location) // we are unsound
                            } {
                                pw.println(s"${projectSpec.name}\t$fId\t$location)")
                            }
                            pw.close()
                        }

                    } catch {
                        case e: Throwable ⇒
                            println(s"exception in project ${projectSpec.name}")
                            if (debug) {
                                println(e.printStackTrace())
                            }
                            if (isAnnotatedProject)
                                ow.write(s"\tE")
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

    def getOutputTarget(target: String, resultsDir: File): Writer = target match {
        case "c" ⇒ new OutputStreamWriter(System.out)
        case "f" ⇒
            val outputFile = new File(resultsDir, "evaluation-result.tsv")
            if (outputFile.exists()) {
                outputFile.delete()
                outputFile.createNewFile()
            }

            new FileWriter(outputFile, false)
        case _ ⇒ new OutputStreamWriter(System.out)
    }
}
