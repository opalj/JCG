import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter

import org.opalj.bytecode
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Writes

import scala.collection.mutable.ArrayBuffer


private case class ORG(org: OPALJ)
private case class OPALJ(opalj: HERMES)
private case class HERMES(hermes: HermesProjects)
private case class HermesProjects(projects: Array[HermesProject])
private case class HermesProject(id: String, cp: String)

object TestCaseHermesJsonExtractor {

    def main(args: Array[String]): Unit = {
        val testCasesDir = new File(args(0))
        assert(testCasesDir.exists() && testCasesDir.isDirectory)

        val projectSpecFiles = testCasesDir.listFiles((_, name) ⇒ name.endsWith(".conf")).sorted

        val projects = for (projectSpecFile ← projectSpecFiles) yield {
            val json = Json.parse(new FileInputStream(projectSpecFile))

            json.validate[ProjectSpecification] match {
                case JsSuccess(projectSpec, _) ⇒
                    val allTargets = ArrayBuffer(projectSpec.target)
                    // todo correct java version
                    allTargets += bytecode.JRELibraryFolder.getAbsolutePath
                    allTargets ++= projectSpec.allClassPathEntryFiles.map(_.getAbsolutePath)
                    HermesProject(projectSpec.name, allTargets.mkString(File.pathSeparator))
                case _ ⇒
                    throw new IllegalArgumentException("invalid project.conf")
            }

        }

        implicit val projectWrites: Writes[HermesProject] = Json.writes[HermesProject]
        implicit val projectsWrites: Writes[HermesProjects] = Json.writes[HermesProjects]

        implicit val hermesWrites: Writes[HERMES] = Json.writes[HERMES]
        implicit val opaljWrites: Writes[OPALJ] = Json.writes[OPALJ]
        implicit val orgWrites: Writes[ORG] = Json.writes[ORG]

        val json: JsValue = Json.toJson(ORG(OPALJ(HERMES(HermesProjects(projects)))))
        val outFile = new File("result.json") // todo specify outputfile
        val pw = new PrintWriter(outFile)
        pw.write(Json.prettyPrint(json))
        pw.close()
    }

}
