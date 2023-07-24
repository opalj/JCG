import java.io.File
import java.io.PrintWriter

import play.api.libs.json.JsValue
import play.api.libs.json.Json

object FatJarProjectSpecExtractor {

    def main(args: Array[String]): Unit = {
        val projectsDir = new File(args(0))

        for (fatJar ← projectsDir.listFiles(_.getName.endsWith(".jar"))) {
            val name = fatJar.getName.replace(".jar", "")

            val projectSpec = ProjectSpecification(name, 8, None, fatJar.getAbsolutePath, None, None)

            val projectSpecJson: JsValue = Json.toJson(projectSpec)

            val projectSpecOut = new File(projectsDir, s"$name.conf")
            val pw = new PrintWriter(projectSpecOut)
            pw.write(Json.prettyPrint(projectSpecJson))
            pw.close()
        }
    }
}
