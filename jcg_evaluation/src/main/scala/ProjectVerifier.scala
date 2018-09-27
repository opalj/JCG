import java.io.File
import java.io.FileInputStream

import play.api.libs.json.Json

/**
 * A small command line tool, to verify the annotated projects.
 * For usage see [[CommonEvaluationConfig]].
 * Verification is done using the [[AnnotationVerifier]].
 *
 * @author Florian Kuebler
 */
object ProjectVerifier {

    def main(args: Array[String]): Unit = {
        val config = CommonEvaluationConfig.processArguments(args)
        val projectsDir = EvaluationHelper.getProjectsDir(config.INPUT_DIR_PATH)
        val projectSpecFiles = projectsDir.listFiles { (_, name) ⇒
            name.endsWith(".conf") && name.startsWith(config.PROJECT_PREFIX_FILTER)
        }.sorted

        val parent = new File("")

        for (projectSpecFile ← projectSpecFiles) {
            val projectSpec = Json.parse(
                new FileInputStream(projectSpecFile)
            ).validate[ProjectSpecification].get

            AnnotationVerifier.verifyProject(projectSpec, parent)
        }
    }

}
