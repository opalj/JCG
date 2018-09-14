import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
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

    var RESULTS_DIR_PATH = "evaluation/"
    val JRE_LOCATIONS_FILE = "jre.conf"
    val EVALUATION_RESULT_FILE = "evaluation-result.tsv"

    val EVALUATION_ADAPTERS = List(OpalJCGAdatper) //SootJCGAdapter, WalaJCGAdapter, OpalJCGAdatper)

    def main(args: Array[String]): Unit = {
        var input = ""
        var jarFilter = ""
        args.sliding(2, 2).toList.collect {
            case Array("--input", i: String)                ⇒ input = i
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

        val jreLocationsFile = new File(JRE_LOCATIONS_FILE)
        assert(jreLocationsFile.exists(), "please provide a jre.conf file")
        val jreLocations = JRELocation.mapping(new File(JRE_LOCATIONS_FILE))

        val outputTarget = getOutputTarget(resultsDir)
        val ow = new BufferedWriter(outputTarget)

        if (runHermes) {
            performHermesRun(projectsDir, jreLocations)
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
                    val params = md.parameterTypes.map[String](_.toJVMTypeName).toList
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
            runAnalyses(projectsDir, resultsDir, jarFilter, ow, jreLocations, locations)
        }

        ow.flush()
        ow.close()
    }

    private def performHermesRun(projectsDir: File, jreLocations: Map[Int, Array[File]]): Unit = {
        println("running hermes")

        val hermesFile = new File("hermes.json")
        assert(!hermesFile.exists(), "there is already a hermes.json file")

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
            println(s"hermes run took ${t.toSeconds} seconds")
        }

        hermesFile.delete()
    }

    private def runAnalyses(
        projectsDir:   File,
        resultsDir:    File,
        projectFilter: String,
        ow:            BufferedWriter,
        jreLocations:  Map[Int, Array[File]],
        locationsMap:  Map[String, Map[String, Set[Method]]]
    ): Unit = {
        val projectSpecFiles = projectsDir.listFiles((_, name) ⇒ name.endsWith(".conf")).filter(_.getName.startsWith(projectFilter)).sorted
        printHeader(ow, projectSpecFiles)

        for {
            adapter ← EVALUATION_ADAPTERS
            cgAlgo ← adapter.possibleAlgorithms()
        } {
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
                    val cp = projectSpec.allClassPathEntryFiles(projectsDir).map(_.getCanonicalPath)

                    val elapsed = adapter.serializeCG(
                        cgAlgo,
                        projectSpec.target(projectsDir).getCanonicalPath,
                        projectSpec.main.orNull,
                        cp,
                        JRE_LOCATIONS_FILE,
                        projectSpec.java,
                        jsFile.getPath
                    )

                    System.gc()

                    val seconds = elapsed / 1000000000d
                    val pw = new PrintWriter(new File(outDir, "timings.txt"))
                    pw.write(s"$seconds sec.")
                    pw.close()
                    println(s"analysis took $seconds sec.")

                    if (isAnnotatedProject) {
                        val result = CGMatcher.matchCallSites(
                            projectSpec,
                            jreLocations,
                            projectsDir,
                            jsFile,
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

    private def printHeader(ow: BufferedWriter, jars: Array[File]): Unit = {
        ow.write("algorithm")
        for (tgt ← jars) {
            ow.write(s"\t$tgt")
        }
        ow.newLine()
    }

    private def getOutputTarget(resultsDir: File): Writer = {
        val outputFile = new File(resultsDir, EVALUATION_RESULT_FILE)
        if (outputFile.exists()) {
            outputFile.delete()
            outputFile.createNewFile()
        }

        new FileWriter(outputFile, false)
    }
}
