import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigObject
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValueFactory
import org.opalj.hermes.Hermes
import org.opalj.log.GlobalLogContext
import org.opalj.log.OPALLogger
import org.opalj.util.PerformanceEvaluation.time
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object TestCaseHermesJsonExtractor {

    private val HERMES_FILE_NAME = "hermes.json"
    private val HERMES_RESULT_FILE_NAME = "hermes.csv"

    def createHermesConfig(
        projectsDir:       File,
        supportedFeatures: Set[String],
        jreLocations:      Map[Int, String],
        outputFile:        File
    ): Unit = {
        val allQueries = Hermes.featureQueries.toSet
        val requiredQueries = Hermes.featureQueries.filterNot(_.featureIDs.forall(supportedFeatures.contains))

        val baseConfig: Config = ConfigFactory.load().withValue(
            "org.opalj.br.reader.ClassFileReader.Invokedynamic.rewrite",
            ConfigValueFactory.fromAnyRef(true)
        ).withValue(
                "org.opalj.hermes.maxLocations",
                ConfigValueFactory.fromAnyRef(Int.MaxValue)
            )

        val toBeRegistered = allQueries.foldRight(List.empty[ConfigObject]) { (query, configValues) ⇒
            ConfigValueFactory.fromMap(Map(
                "query" → query.getClass, "activate" → requiredQueries.contains(query)
            ).asJava) :: configValues
        }

        val hermesConfig = baseConfig.withValue(
            "org.opalh.hermes.queries.registered",
            ConfigValueFactory.fromIterable(toBeRegistered.asJava)
        )

        assert(projectsDir.exists() && projectsDir.isDirectory)

        val projectSpecFiles = projectsDir.listFiles((_, name) ⇒ name.endsWith(".conf")).sorted

        val projects = for (projectSpecFile ← projectSpecFiles) yield {
            val json = Json.parse(new FileInputStream(projectSpecFile))
            val projectSpec = json.validate[ProjectSpecification].getOrElse {
                throw new IllegalArgumentException("invalid project.conf")
            }

            val allTargets = ArrayBuffer(projectSpec.target(projectsDir).getCanonicalPath)
            allTargets ++= JRELocation.getAllJREJars(jreLocations(projectSpec.java)).map(_.getCanonicalPath)
            allTargets ++= projectSpec.allClassPathEntryFiles(projectsDir).map(_.getCanonicalPath)
            ConfigValueFactory.fromMap(Map(
                "id" → projectSpec.name,
                "cp" → allTargets.mkString(File.pathSeparator)
            ).asJava)
        }

        val config = hermesConfig.withValue(
            "org.opalj.hermes.projects",
            ConfigValueFactory.fromIterable(projects.toSeq.asJava)
        )

        val configValue = config.root().render(ConfigRenderOptions.concise())

        val pw = new PrintWriter(outputFile)
        pw.write(configValue)
        pw.close()
    }

    def performHermesRun(
        projectsDir:               File,
        jreLocations:              Map[Int, String],
        config:                    CommonEvaluationConfig,
        fingerprintDir:            File,
        projectSpecificEvaluation: Boolean
    ): Unit = {
        println("running hermes")

        val hermesFile = new File(HERMES_FILE_NAME)
        assert(!hermesFile.exists(), s"there is already a $HERMES_FILE_NAME file")

        if (!config.DEBUG)
            OPALLogger.updateLogger(GlobalLogContext, new DevNullLogger())

        val supportedFeatures = config.EVALUATION_ADAPTERS.flatMap { adapter ⇒
            adapter.possibleAlgorithms().filter(_.startsWith(config.ALGORITHM_PREFIX_FILTER)).flatMap { algorithm ⇒
                FingerprintExtractor.parseFingerprints(adapter, algorithm, fingerprintDir)
            }
        }.toSet

        TestCaseHermesJsonExtractor.createHermesConfig(
            projectsDir, supportedFeatures, jreLocations, hermesFile
        )

        val hermesDefaultArgs = Array(
            "-config", hermesFile.getPath,
            "-statistics", s"${config.OUTPUT_DIR_PATH}${File.separator}$HERMES_RESULT_FILE_NAME"
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
}
