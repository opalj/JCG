import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter

import org.opalj.br.MethodDescriptor
import org.opalj.log.GlobalLogContext
import org.opalj.log.OPALLogger
import org.opalj.util.PerformanceEvaluation.time
import play.api.libs.json.Json

import scala.io.Source

object Evaluation {
    private var runHermes = false
    private var projectSpecificEvaluation = false
    private var runAnalyses = true

    private var FINGERPRINT_DIR = ""

    def main(args: Array[String]): Unit = {
        CommonEvaluationConfig.processArguments(args)
        val config = CommonEvaluationConfig.processArguments(args)
        parseArguments(args)

        val projectsDir = EvaluationHelper.getProjectsDir(config.INPUT_DIR_PATH)

        val jreLocations = EvaluationHelper.getJRELocations(config.JRE_LOCATIONS_FILE)

        if (runHermes) {
            performHermesRun(projectsDir, jreLocations, config)
        }

        if (runAnalyses) {
            val resultsDir = new File(config.OUTPUT_DIR_PATH)
            resultsDir.mkdirs()
            val locations: Map[String, Map[String, Set[Method]]] = createLocationsMapping(resultsDir)
            runAnalyses(projectsDir, resultsDir, jreLocations, locations, config)
        }
    }

    private def parseArguments(args: Array[String]): Unit = {
        args.sliding(2, 1).toList.collect {
            case Array("--analyze", value: String) ⇒ runAnalyses = value.toBoolean
            case Array("--hermes")                 ⇒ runHermes = true
            case Array("--project-specific")       ⇒ projectSpecificEvaluation = true
        }

        if (projectSpecificEvaluation) {
            assert(runAnalyses, "`--analyze` must be set to true on `--project-specific true`")
            assert(FINGERPRINT_DIR.nonEmpty, "no fingerprint directory specified")
        }
    }

    private def performHermesRun(
        projectsDir: File, jreLocations: Map[Int, String], config: CommonEvaluationConfig
    ): Unit = {
        println("running hermes")

        val hermesFile = new File("hermes.json")
        assert(!hermesFile.exists(), "there is already a hermes.json file")

        if (!config.DEBUG)
            OPALLogger.updateLogger(GlobalLogContext, new DevNullLogger())

        TestCaseHermesJsonExtractor.createHermesJsonFile(
            projectsDir, jreLocations, hermesFile
        )

        val hermesDefaultArgs = Array(
            "-config", hermesFile.getPath,
            "-statistics", s"${config.OUTPUT_DIR_PATH}${File.separator}hermes.csv"
        )
        val writeLocationsArgs =
            if (projectSpecificEvaluation)
                Array(
                    "-writeLocations", config.OUTPUT_DIR_PATH
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
        jreLocations: Map[Int, String],
        locationsMap: Map[String, Map[String, Set[Method]]],
        config:       CommonEvaluationConfig
    ): Unit = {
        val projectSpecFiles = projectsDir.listFiles { (_, name) ⇒
            name.endsWith(".conf") && name.startsWith(config.PROJECT_PREFIX_FILTER)
        }.sorted

        for {
            adapter ← config.EVALUATION_ADAPTERS
            cgAlgo ← adapter.possibleAlgorithms().filter(_.startsWith(config.ALGORITHM_PREFIX_FILTER))
            psf ← projectSpecFiles
        } {

            val projectSpec = Json.parse(new FileInputStream(psf)).validate[ProjectSpecification].get

            println(s"running ${adapter.frameworkName()} $cgAlgo against ${projectSpec.name}")

            val outDir = new File(
                resultsDir,
                s"${projectSpec.name}${File.separator}${adapter.frameworkName()}${File.separator}$cgAlgo"
            )
            outDir.mkdirs()

            val cgFile = new File(outDir, "cg.json")
            assert(!cgFile.exists(), s"$cgFile already exists")

            val elapsed = try {
                adapter.serializeCG(
                    cgAlgo,
                    projectSpec.target(projectsDir).getCanonicalPath,
                    projectSpec.main.orNull,
                    projectSpec.allClassPathEntryPaths(projectsDir),
                    jreLocations(projectSpec.java),
                    cgFile.getPath
                )
            } catch {
                case e: Throwable ⇒
                    println(s"exception in project ${projectSpec.name}")
                    if (config.DEBUG) {
                        println(e.printStackTrace())
                    }
                    -1
            }

            assert(cgFile.exists(), "the adapter failed to write the call graph")

            System.gc()

            reportTiming(outDir, elapsed)

            if (projectSpecificEvaluation) {
                performProjectSpecificEvaluation(locationsMap, projectSpec, outDir, cgFile)
            }
        }
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
}
