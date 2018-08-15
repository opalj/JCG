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
    var runHermes = true
    var projectSpecificEvaluation = false
    var runAnalyses = false
    var isAnnotatedProject = false

    val RESULTS_DIR_PATH = "evaluation/" // todo merge outputs
    val JRE_LOCATIONS_FILE = "jre.json"
    val EVALUATION_ADAPTERS = List(new SootJCGAdatper(), new WalaJCGAdapter())

    def main(args: Array[String]): Unit = {
        var input = ""
        var jarFilter = ""
        var target = ""
        args.sliding(2, 2).toList.collect {
            case Array("--input", i: String)                ⇒ input = i
            case Array("--output", t: String)               ⇒ target = t
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

                org.opalj.hermes.HermesCLI.main(
                    hermesDefaultArgs ++ writeLocationsArgs
                )
                hermesFile.delete()
            }
            val locations: Map[String, Map[String, Set[Method]]] =
                if (projectSpecificEvaluation) {
                    if (debug)
                        println("create locations mapping")
                    (for {
                        projectLocation ← resultsDir.listFiles(_.getName.endsWith(".tsv"))
                        line ← Source.fromFile(projectLocation).getLines().drop(1)
                    } yield {
                        val Array(projectId, featureId, _, _, classString, methodName, mdString, _, _) = line.split("\t", -1)
                        val className = classString.replace("\"", "")
                        val md = MethodDescriptor(mdString.replace("\"", ""))
                        val params = md.parameterTypes.map(_.toJVMTypeName).toList
                        val returnType = md.returnType.toJVMTypeName
                        (projectId, featureId, Method(methodName, className, returnType, params))
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
        jreLocations: Map[Int, String]
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

                    if (debug)
                        println(s"running ${adapter.frameworkName()} $cgAlgo against ${projectSpec.name}")

                    val outDir = new File(resultsDir, s"${projectSpec.name}${File.separator}${adapter.frameworkName()}${File.separator}$cgAlgo")
                    outDir.mkdirs()

                    val jsFile = new File(outDir, "cg.json")
                    try {
                        time {
                            adapter.serializeCG(
                                cgAlgo,
                                projectSpec.target(projectsDir).getCanonicalPath,
                                projectSpec.main.orNull,
                                Array(jreLocations(projectSpec.java)) ++ projectSpec.allClassPathEntryFiles(projectsDir).map(_.getCanonicalPath),
                                jsFile.getPath
                            )
                        } { t ⇒
                            if (debug)
                                println(s"analysis took ${t.toSeconds} s")
                        }

                        System.gc()

                        if (isAnnotatedProject) {
                            val result = CGMatcher.matchCallSites(
                                projectSpec.target(projectsDir).getCanonicalPath,
                                jsFile.getPath
                            )
                            ow.write(s"\t${result.shortNotation}")
                        }

                    } catch {
                        case e: Throwable ⇒
                            if (debug) {
                                println(projectSpec.name)
                                println(e.printStackTrace())
                            }

                            ow.write(s"\tE")
                    }
                    if (projectSpecificEvaluation && jsFile.exists()) {
                        val pw = new PrintWriter(new File(outDir, "pse.tsv"))
                        val json = Json.parse(new FileInputStream(jsFile))
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
                                pw.println(s"${projectSpec.name}\t$fId\t$location)") //
                        }
                        pw.close
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
