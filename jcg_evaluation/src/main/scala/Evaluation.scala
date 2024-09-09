import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.zip.GZIPOutputStream
import play.api.libs.json.Json
import scala.io.Source

import org.opalj.br.MethodDescriptor

object Evaluation {

    private var runHermes = false
    private var projectSpecificEvaluation = false
    private var excludeJDK = false
    private var runAnalyses = true
    private var allQueries = false
    private var programArgs = Array.empty[String]

    private var FINGERPRINT_DIR = ""

    def main(args: Array[String]): Unit = {

        // val c = parseConfig(args)

        val config = CommonEvaluationConfig.processArguments(args)
        parseArguments(args)

        val projectsDir = EvaluationHelper.getProjectsDir(config.INPUT_DIR_PATH)

        val jreLocations = EvaluationHelper.getJRELocations(config.JRE_LOCATIONS_FILE)

        if (runHermes) {
            TestCaseHermesJsonExtractor.performHermesRun(
                projectsDir,
                jreLocations,
                config,
                new File(FINGERPRINT_DIR),
                allQueries,
                projectSpecificEvaluation
            )
        }

        if (runAnalyses) {
            val resultsDir = new File(config.OUTPUT_DIR_PATH)
            resultsDir.mkdirs()
            val locations: Map[String, Map[String, Set[Method]]] = createLocationsMapping(resultsDir)
            runAnalyses(projectsDir, resultsDir, jreLocations, locations, config, programArgs)
        }
    }

    private def parseArguments(args: Array[String]): Unit = {
        args.sliding(1, 1).toList.collect {
            case Array("--hermes")           => runHermes = true
            case Array("--project-specific") => projectSpecificEvaluation = true
            case Array("--exclude-jdk")      => excludeJDK = true
            case Array("--all-queries")      => allQueries = true
        }
        args.sliding(2, 1).toList.collect {
            case Array("--fingerprint-dir", dir) =>
                assert(FINGERPRINT_DIR.isEmpty, "multiple fingerprint directories specified")
                FINGERPRINT_DIR = dir
            case Array("--analyze", value: String) => runAnalyses = value.toBoolean
        }
        val argsIndex = args.indexOf("--program-args") + 1
        if (argsIndex > 0) {
            val argsEndIndex = args.indexWhere(_.startsWith("--"), argsIndex)
            programArgs = args.slice(argsIndex, if (argsEndIndex >= 0) argsEndIndex else args.length)
        }

        if (projectSpecificEvaluation) {
            assert(runAnalyses, "`--analyze` must be set to true on `--project-specific true`")
            assert(FINGERPRINT_DIR.nonEmpty, "no fingerprint directory specified")
        }

        if (runHermes) {
            assert(
                FINGERPRINT_DIR.nonEmpty || allQueries,
                "hermes requires the fingerprints or `--all-queries` must be set"
            )
        }
    }

    private def createLocationsMapping(resultsDir: File): Map[String, Map[String, Set[Method]]] = {
        val locations: Map[String, Map[String, Set[Method]]] =
            if (projectSpecificEvaluation) {
                println("create locations mapping")
                (for {
                    projectLocation <- resultsDir.listFiles(_.getName.endsWith(".tsv"))
                    line <- Source.fromFile(projectLocation).getLines().drop(1)
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
                    case (pId, group1) => pId -> group1.map { case (_, f, m) => f -> m }.groupBy(_._1).map {
                            case (fId, group2) => fId -> group2.map(_._2).toSet
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
        config:       CommonEvaluationConfig,
        programArgs:  Array[String]
    ): Unit = {
        val projectSpecFiles = projectsDir.listFiles { (_, name) =>
            name.endsWith(".conf") && name.startsWith(config.PROJECT_PREFIX_FILTER)
        }.sorted

        for {
            adapter <- config.EVALUATION_ADAPTERS
            cgAlgo <-
                adapter.possibleAlgorithms.filter(_.toLowerCase().startsWith(config.ALGORITHM_PREFIX_FILTER.toLowerCase()))
            psf <- projectSpecFiles
        } {

            val projectSpec = Json.parse(new FileInputStream(psf)).validate[ProjectSpecification].get

            println(s"running ${adapter.frameworkName} $cgAlgo against ${projectSpec.name}")

            val outDir = EvaluationHelper.getOutputDirectory(adapter, cgAlgo, projectSpec, resultsDir)
            outDir.mkdirs()

            val cgFile = new File(outDir, config.SERIALIZATION_FILE_NAME)
            if (cgFile.exists())
                cgFile.delete()

            val output =
                if (cgFile.getName.endsWith(".zip") || cgFile.getName.endsWith(".gz"))
                    new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(cgFile)))
                else
                    new BufferedWriter(new FileWriter(cgFile))

            val elapsed =
                try {
                    adapter.serializeCG(
                        cgAlgo,
                        projectSpec.target(projectsDir).getCanonicalPath,
                        new FileWriter(cgFile.getPath),
                        AdapterOptions.makeJavaOptions(
                            projectSpec.main.orNull,
                            projectSpec.allClassPathEntryPaths(projectsDir),
                            jreLocations(projectSpec.java),
                            !excludeJDK,
                            programArgs = programArgs
                        )
                    )

                } catch {
                    case e: Throwable =>
                        println(s"exception in project ${projectSpec.name}")
                        if (config.DEBUG) {
                            e.printStackTrace()
                        }
                        -1
                }

            System.gc()

            reportTiming(outDir, elapsed)

            if (projectSpecificEvaluation) {
                assert(cgFile.exists(), "the adapter failed to write the call graph")
                performProjectSpecificEvaluation(
                    projectSpec,
                    adapter,
                    cgAlgo,
                    locationsMap,
                    outDir,
                    cgFile
                )
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
        projectSpec:  ProjectSpecification,
        adapter:      TestAdapter,
        algorithm:    String,
        locationsMap: Map[String, Map[String, Set[Method]]],
        outDir:       File,
        jsFile:       File
    ): Unit = {
        val fingerprint = FingerprintExtractor.parseFingerprints(adapter, algorithm, new File(FINGERPRINT_DIR))
        val locations = locationsMap(projectSpec.name)
        val reachableMethods = Json.parse(new FileInputStream(jsFile)).validate[ReachableMethods].get.toMap

        val projectSpecificLocations = ProjectSpecificEvaluator.projectSpecificEvaluation(
            reachableMethods.keySet,
            locations,
            fingerprint
        )

        val pw = new PrintWriter(new File(outDir, "pse.tsv"))
        for ((location, fID) <- projectSpecificLocations) {
            pw.println(s"${projectSpec.name}\t$fID\t$location")
        }
        pw.close()
    }
}
