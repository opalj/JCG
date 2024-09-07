import java.io.File
import java.io.FileInputStream
import java.util.zip.GZIPInputStream

import play.api.libs.json.Json

// todo make a factory
class CommonEvaluationConfig(
    val DEBUG:                   Boolean,
    val INPUT_DIR_PATH:          String,
    val OUTPUT_DIR_PATH:         String,
    val EVALUATION_ADAPTERS:     List[TestAdapter],
    val PROJECT_PREFIX_FILTER:   String,
    val ALGORITHM_PREFIX_FILTER: String,
    val ZIP:                     Boolean
) {
    val JRE_LOCATIONS_FILE = "jre.conf"
    val SERIALIZATION_FILE_NAME = if(ZIP) "cg.zip" else "cg.json"
}

case class JCGConfig(
    inputDir:        File              = new File("."),
    outputDir:       File              = new File("."),
    adapters:        List[TestAdapter] = List.empty,
    projectFilter:   String            = "",
    algorithmFilter: String            = "",
    timeout:         Int               = -1,
    runHermes:       Boolean           = false,
    pseval:          Boolean           = false,
    excludeJDK:      Boolean           = false,
    runAnalyses:     Boolean           = true,
    allQueries:      Boolean           = false,
    fingerprintDir:  File              = new File(""),
    debug:           Boolean           = false,
    parallel:        Boolean           = false,
    language:        String            = ""
) {
    val JRE_LOCATIONS_FILE = "jre.conf"
    val SERIALIZATION_FILE_NAME = "cg.json"
}

object ConfigParser {
    private val ALL_ADAPTERS: List[TestAdapter] =
        EvaluationHelper.ALL_JS_ADAPTERS ++ EvaluationHelper.ALL_JAVA_ADAPTERS ++ EvaluationHelper.ALL_PY_ADAPTERS

    def parseConfig(args: Array[String]): Option[JCGConfig] = {
        import scopt.OParser
        val builder = OParser.builder[JCGConfig]
        val parser = {
            import builder._
            OParser.sequence(
                programName("Java Call Graph Tests"),
                head("JCG", "0.4.0"),
                opt[Unit]('h', "runHermes")
                    .action((x, c) => c.copy(runHermes = true))
                    .text("Hermes will be run on the target project")
                    .optional(),
                opt[File]('i', "inputDir")
                    .action((dir, c) => c.copy(inputDir = dir))
                    .text("Defines the directory with the configuration files for the input projects.")
                    .required().maxOccurs(1)
                    .validate { dir =>
                        if (dir.exists() && dir.isDirectory) success
                        else failure(s"Value ${dir.getAbsolutePath} must exist and must be a directory.")
                    }
                    .validate { dir =>
                        if (FileOperations.hasFilesDeep(dir, ".conf", ".js", ".py")) success
                        else failure(s"${dir.getAbsolutePath} does not contain *.conf, *.js or *.py files")
                    },
                opt[File]('o', "outputDir")
                    .action { (dir, c) =>
                        if (!dir.exists()) dir.mkdirs()
                        c.copy(outputDir = dir)
                    }
                    .text("Defines the output directory; all files will be placed here.")
                    .required().maxOccurs(1),
                opt[String]("project-prefix")
                    .action((prefix, c) => c.copy(projectFilter = prefix))
                    .text("Defines a prefix-based filter for the input project's name. If applied only projects starting with the <prefix> will be processed.")
                    .valueName("prefix")
                    .maxOccurs(1).optional(),
                opt[String]("algorithm-prefix")
                    .action((prefix, c) => c.copy(algorithmFilter = prefix))
                    .text("Defines a prefix-based filter for the adapters call-graph algorithms names. (e.g. filter only for RTAs)")
                    .valueName("prefix")
                    .maxOccurs(1).optional(),
                opt[String]('t', name = "timeout")
                    .action((t, c) => c.copy(timeout = Integer.valueOf(t)))
                    .valueName("timeout")
                    .maxOccurs(1).optional(),
                opt[String]('a', "adapter")
                    .action { (adapterName, c) =>
                        val adapter = ALL_ADAPTERS.find(_.frameworkName.toLowerCase == adapterName.toLowerCase)
                        if (adapter.isEmpty) failure("The given <adapter> is not yet registered as valid adapter.")
                        val newAdapters = c.adapters.::(adapter.get)
                        c.copy(adapters = newAdapters)
                    }
                    .text("Run the pipeline for a selecton of adapters. (e.g., the <OPAL> to run the OPAL's algorithms)")
                    .valueName("adapter")
                    .optional()
                    .unbounded(),
                opt[Unit]('d', "debug")
                    .action((_, c) => c.copy(debug = true))
                    .hidden()
                    .optional(),
                opt[Unit]('p', "parallel")
                    .action((_, c) => c.copy(parallel = true))
                    .hidden()
                    .optional(),
                opt[File]('f', "fingerprintDir")
                    .action((dir, c) => c.copy(fingerprintDir = dir))
                    .text("provide a fingerprint for a project-specific evaluation")
                    .valueName("<path/to/dir>")
                    .optional(),
                opt[String]('l', "language")
                    .action((lang, c) => c.copy(language = lang))
                    .text("provide the language of the projects")
                    .valueName("language")
                    .required(),
                checkConfig(c =>
                    // check if adapters match language
                    if (c.adapters.map(_.language).forall(_ == c.language)) success
                    else failure("The given adapters do not match the language of the projects.")
                )
            )
        }

        OParser.parse(parser, args, JCGConfig()) match {
            case Some(config) =>
                // If no adapter is specified, all adapters of specified language are used
                if (config.adapters.isEmpty) {
                    val adapters = ALL_ADAPTERS.filter(_.language == config.language)
                    Some(config.copy(adapters = adapters))
                } else {
                    Some(config)
                }
            case _ => None
        }

    }

}

object CommonEvaluationConfig {

    def processArguments(args: Array[String]): CommonEvaluationConfig = {
        var DEBUG = false
        var OUTPUT_DIR_PATH = ""
        var INPUT_DIR_PATH = ""
        var ZIP = false

        var EVALUATION_ADAPTERS = List.empty[JavaTestAdapter]

        var PROJECT_PREFIX_FILTER = ""
        var ALGORITHM_PREFIX_FILTER = ""

        args.sliding(2, 1).toList.collect {
            case Array("--input", i) ⇒
                assert(INPUT_DIR_PATH.isEmpty, "multiple input directories specified")
                INPUT_DIR_PATH = i
            case Array("--output", o) ⇒
                assert(OUTPUT_DIR_PATH.isEmpty, "multiple output directories specified")
                OUTPUT_DIR_PATH = o
            case Array("--project-prefix", prefix) ⇒
                assert(PROJECT_PREFIX_FILTER.isEmpty, "multiple project filters specified")
                PROJECT_PREFIX_FILTER = prefix
            case Array("--algorithm-prefix", prefix) ⇒
                assert(ALGORITHM_PREFIX_FILTER.isEmpty, "multiple algorithm filters specified")
                ALGORITHM_PREFIX_FILTER = prefix
            case Array("--adapter", name) ⇒ // you can use this option multiple times
                val adapter = EvaluationHelper.ALL_JAVA_ADAPTERS.find(_.frameworkName.toLowerCase == name.toLowerCase)
                assert(adapter.nonEmpty, s"'$name' is not a valid framework adapter")
                EVALUATION_ADAPTERS ++= adapter
        }

        args.sliding(1, 1).toList.collect {
            case Array("--debug") ⇒ DEBUG = true
            case Array("--zip") ⇒ ZIP = true
        }

        assert(INPUT_DIR_PATH.nonEmpty, "no input directory specified")
        assert(OUTPUT_DIR_PATH.nonEmpty, "no output directory specified")
        val outputDir = new File(OUTPUT_DIR_PATH)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        new CommonEvaluationConfig(
            DEBUG,
            INPUT_DIR_PATH,
            OUTPUT_DIR_PATH,
            if (EVALUATION_ADAPTERS.isEmpty) EvaluationHelper.ALL_JAVA_ADAPTERS else EVALUATION_ADAPTERS,
            PROJECT_PREFIX_FILTER,
            ALGORITHM_PREFIX_FILTER,
            ZIP
        )
    }
}

object EvaluationHelper {
    val ALL_JAVA_ADAPTERS: List[JavaTestAdapter] = List(SootJCGAdapter, WalaJCGAdapter, OpalJCGAdatper, DoopAdapter)
    val ALL_JS_ADAPTERS: List[JSTestAdapter] = List(JSCallGraphAdapter, Code2flowCallGraphAdapter, TAJSJCGAdapter)
    val ALL_PY_ADAPTERS: List[PyTestAdapter] = List(PyCGAdapter)

    def getProjectsDir(inputPath: String): File = {
        val projectsDir = new File(inputPath)

        assert(projectsDir.exists(), s"${projectsDir.getPath} does not exists")
        assert(projectsDir.isDirectory, s"${projectsDir.getPath} is not a directory")
        assert(
            projectsDir.listFiles(_.getName.endsWith(".conf")).nonEmpty,
            s"${projectsDir.getPath} does not contain *.conf files"
        )
        projectsDir
    }

    def getJRELocations(jreLocationsPath: String): Map[Int, String] = {
        val jreLocationsFile = new File(jreLocationsPath)
        assert(jreLocationsFile.exists(), "please provide a jre.conf file")
        val jreLocations = JRELocation.mapping(jreLocationsFile)
        jreLocations
    }

    def getOutputDirectory(
        adapter:     TestAdapter,
        algorithm:   String,
        projectSpec: ProjectSpecification,
        resultsDir:  File
    ): File = {
        val dirName = s"${ projectSpec.name }${ File.separator }${ adapter.frameworkName }${ File.separator }$algorithm"
        new File(resultsDir, dirName)
    }

    def readCG(cgFile: File): ReachableMethods = {
        val input =
            if (cgFile.getName.endsWith(".zip") || cgFile.getName.endsWith(".gz"))
                new GZIPInputStream(new FileInputStream(cgFile))
            else
                new FileInputStream(cgFile)

        Json.parse(input).validate[ReachableMethods].get
    }
}
