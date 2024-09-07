import java.io.File
import java.io.PrintWriter

import org.opalj.bytecode
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Writes

object XCorpusProjectSpecExtractor {

    def main(args: Array[String]): Unit = {
        val xCorpusDir = new File(args(0))
        assert(xCorpusDir.exists())
        for {
            dataDir ← xCorpusDir.listFiles(_.isDirectory)
            projectRootDir ← dataDir.listFiles(_.isDirectory)
        } {

            val projectDir = new File(projectRootDir, "project")
            val bin = new File(projectDir, "bin.zip")
            assert(bin.exists())

            val libDir = new File(projectDir, "default-lib")

            val libs = if (libDir.exists())
                libDir.listFiles(f ⇒ f.getName.endsWith(".jar")).map(_.getAbsolutePath)
            else Array.empty[String]

            val projectName = projectRootDir.getName

            // todo we need the main class and the java version
            val projectSpec = ProjectSpecification(
                projectName, 8, None, bin.getAbsolutePath, Some(libs.map(LocalClassPathEntry(_))), None
            )
            val projectSpecJson: JsValue = Json.toJson(projectSpec)

            // todo better output dir
            val projectSpecOut = new File(xCorpusDir, s"$projectName.conf")
            val pw = new PrintWriter(projectSpecOut)
            pw.write(Json.prettyPrint(projectSpecJson))
            pw.close()
        }
    }

}
