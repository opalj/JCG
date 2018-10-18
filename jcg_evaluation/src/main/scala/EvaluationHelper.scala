import java.io.File

// todo make a factory
class CommonEvaluationConfig(
        val DEBUG:                   Boolean,
        val INPUT_DIR_PATH:          String,
        val OUTPUT_DIR_PATH:         String,
        val EVALUATION_ADAPTERS:     List[JCGTestAdapter],
        val PROJECT_PREFIX_FILTER:   String,
        val ALGORITHM_PREFIX_FILTER: String
) {
    val JRE_LOCATIONS_FILE = "jre.conf"

    val SERIALIZATION_FILE_NAME = "cg.json"

    def getOutputDirectory(
        adapter:     JCGTestAdapter,
        algorithm:   String,
        projectSpec: ProjectSpecification,
        resultsDir:  File
    ): File = {
        val dirName = s"${projectSpec.name}${File.separator}${adapter.frameworkName()}${File.separator}$algorithm"
        new File(resultsDir, dirName)
    }
}

object CommonEvaluationConfig {

    private val ALL_ADAPTERS = List(SootJCGAdapter, WalaJCGAdapter, OpalJCGAdatper)

    def processArguments(args: Array[String]): CommonEvaluationConfig = {
        var DEBUG = false
        var OUTPUT_DIR_PATH = ""
        var INPUT_DIR_PATH = ""

        var EVALUATION_ADAPTERS = List.empty[JCGTestAdapter]

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
                val adapter = ALL_ADAPTERS.find(_.frameworkName().toLowerCase == name.toLowerCase)
                assert(adapter.nonEmpty, s"'$name' is not a valid framework adapter")
                EVALUATION_ADAPTERS ++= adapter
        }

        args.sliding(1, 1).toList.collect {
            case Array("--debug") ⇒ DEBUG = true
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
            if(EVALUATION_ADAPTERS.isEmpty) ALL_ADAPTERS else EVALUATION_ADAPTERS,
            PROJECT_PREFIX_FILTER,
            ALGORITHM_PREFIX_FILTER
        )
    }
}

object EvaluationHelper {
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
}
