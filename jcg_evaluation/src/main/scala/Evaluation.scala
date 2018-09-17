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

    private var debug = true
    private var runHermes = false
    private var projectSpecificEvaluation = false
    private var runAnalyses = true
    private var isAnnotatedProject = true

    private var RESULTS_DIR_PATH = "evaluation/"
    private var INPUT_DIR = ""
    private val JRE_LOCATIONS_FILE = "jre.conf"
    private val EVALUATION_RESULT_FILE = "evaluation-result.tsv"

    private var PROJECT_FILTER = ""

    private var EVALUATION_ADAPTERS = List(SootJCGAdapter, WalaJCGAdapter, OpalJCGAdatper)

    def main(args: Array[String]): Unit = {
        parseArguments(args)

        val projectsDir = getProjectsDir

        val jreLocations = getJRELocations

        if (runHermes) {
            performHermesRun(projectsDir, jreLocations)
        }

        if (runAnalyses) {
            val resultsDir = new File(RESULTS_DIR_PATH)
            resultsDir.mkdirs()
            val locations: Map[String, Map[String, Set[Method]]] = createLocationsMapping(resultsDir)
            runAnalyses(projectsDir, resultsDir, jreLocations, locations)
        }
    }

    private def parseArguments(args: Array[String]): Unit = {
        args.sliding(2, 2).toList.collect {
            case Array("--input", i: String) ⇒ INPUT_DIR = i
            case Array("--output-dir", t: String) ⇒ RESULTS_DIR_PATH = t
            case Array("--filter", name: String) ⇒ PROJECT_FILTER = name
            case Array("--debug", value: String) ⇒ debug = value.toBoolean
            case Array("--hermes", value: String) ⇒ runHermes = value.toBoolean
            case Array("--analyze", value: String) ⇒ runAnalyses = value.toBoolean
            case Array("--project-specific", value: String) ⇒ projectSpecificEvaluation = value.toBoolean
            case Array("--testcase", value: String) ⇒ isAnnotatedProject = value.toBoolean
            case Array("--adapter", name: String) ⇒
                EVALUATION_ADAPTERS = EVALUATION_ADAPTERS.filter(_.frameworkName() == name)
                assert(EVALUATION_ADAPTERS.nonEmpty, s"$name is no known test adapter")
        }

        if (projectSpecificEvaluation)
            assert(runAnalyses, "`--analyze` must be set to true on `--project-specific true`")

        if (isAnnotatedProject)
            assert(runAnalyses, "`--analyze` must be set to true on `--testcase true`")

        assert(INPUT_DIR.nonEmpty, "no input directory specified")
    }

    private def getProjectsDir: File = {
        val projectsDir = new File(INPUT_DIR)

        assert(projectsDir.exists(), s"${projectsDir.getPath} does not exists")
        assert(projectsDir.isDirectory, s"${projectsDir.getPath} is not a directory")
        assert(
            projectsDir.listFiles(_.getName.endsWith(".conf")).nonEmpty,
            s"${projectsDir.getPath} does not contain *.conf files"
        )
        projectsDir
    }

    private def getJRELocations: Map[Int, Array[File]] = {
        val jreLocationsFile = new File(JRE_LOCATIONS_FILE)
        assert(jreLocationsFile.exists(), "please provide a jre.conf file")
        val jreLocations = JRELocation.mapping(new File(JRE_LOCATIONS_FILE))
        jreLocations
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

    private def createLocationsMapping(resultsDir: File): Map[String, Map[String, Set[Method]]] = {
        val locations: Map[String, Map[String, Set[Method]]] =
            if (projectSpecificEvaluation) {
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
        locations
    }

    private def runAnalyses(
        projectsDir:  File,
        resultsDir:   File,
        jreLocations: Map[Int, Array[File]],
        locationsMap: Map[String, Map[String, Set[Method]]]
    ): Unit = {
        val projectSpecFiles = projectsDir.listFiles { (_, name) ⇒
            name.endsWith(".conf") && name.startsWith(PROJECT_FILTER)
        }.sorted

        val outputTarget = getOutputTarget(resultsDir)
        val ow = new BufferedWriter(outputTarget)

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

                val cgFile = new File(outDir, "cg.json")
                assert(!cgFile.exists(), s"$cgFile already exists")

                try {
                    val cp = projectSpec.allClassPathEntryFiles(projectsDir).map(_.getCanonicalPath)

                    val elapsed = adapter.serializeCG(
                        cgAlgo,
                        projectSpec.target(projectsDir).getCanonicalPath,
                        projectSpec.main.orNull,
                        cp,
                        JRE_LOCATIONS_FILE,
                        projectSpec.java,
                        cgFile.getPath
                    )
                    assert(cgFile.exists(), "the adapter failed to write the call graph")

                    System.gc()

                    reportTiming(outDir, elapsed)

                    if (isAnnotatedProject) {
                        val result = CGMatcher.matchCallSites(projectSpec, jreLocations, projectsDir, cgFile, debug)
                        ow.write(s"\t${result.shortNotation}")
                    }

                    if (projectSpecificEvaluation) {
                        performProjectSpecificEvaluation(locationsMap, projectSpec, outDir, cgFile)
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

        ow.flush()
        ow.close()
    }

    private def reportTiming(outDir: File, elapsed: Long): Unit = {
        val seconds = elapsed / 1000000000d
        val pw = new PrintWriter(new File(outDir, "timings.txt"))
        pw.write(s"$seconds sec.")
        pw.close()
        println(s"analysis took $seconds sec.")
    }

    private def performProjectSpecificEvaluation(
        locationsMap: Map[String, Map[String, Set[Method]]],
        projectSpec:  ProjectSpecification,
        outDir:       File,
        jsFile:       File
    ): Unit = {
        val pw = new PrintWriter(new File(outDir, "pse.tsv"))
        val json = Json.parse(new FileInputStream(jsFile))
        val reachableMethods = json.validate[ReachableMethods].get.toMap
        for {
            (fId, locations) ← locationsMap(projectSpec.name)
            location ← locations
            if reachableMethods.contains(location)
        } {
            pw.println(s"${projectSpec.name}\t$fId\t$location)")
        }
        pw.close()
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
