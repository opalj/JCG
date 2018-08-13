import java.io.File
import java.io.FileInputStream
import java.io.PrintWriter

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

    def createHermesJsonFile(
        projectsDir: File, jreLocations: Map[Int, String], outputFile: File
    ): Unit = {
        assert(projectsDir.exists() && projectsDir.isDirectory)

        val projectSpecFiles = projectsDir.listFiles((_, name) ⇒ name.endsWith(".conf")).sorted

        val projects = for (projectSpecFile ← projectSpecFiles) yield {
            val json = Json.parse(new FileInputStream(projectSpecFile))
            val projectSpec = json.validate[ProjectSpecification].getOrElse {
                throw new IllegalArgumentException("invalid project.conf")
            }

            val allTargets = ArrayBuffer(projectSpec.target)
            allTargets += jreLocations(projectSpec.java)
            allTargets ++= projectSpec.allClassPathEntryFiles(projectsDir).map(_.getCanonicalPath)
            HermesProject(projectSpec.name, allTargets.mkString(File.pathSeparator))

        }

        implicit val projectWrites: Writes[HermesProject] = Json.writes[HermesProject]
        implicit val projectsWrites: Writes[HermesProjects] = Json.writes[HermesProjects]

        implicit val hermesWrites: Writes[HERMES] = Json.writes[HERMES]
        implicit val opaljWrites: Writes[OPALJ] = Json.writes[OPALJ]
        implicit val orgWrites: Writes[ORG] = Json.writes[ORG]

        val json: JsValue = Json.toJson(ORG(OPALJ(HERMES(HermesProjects(projects)))))
        val pw = new PrintWriter(outputFile)
        pw.write(Json.prettyPrint(json))
        pw.close()
    }

    def main(args: Array[String]): Unit = {
        val jreLocations = JRELocation.mapping(new File(args(1)))
        createHermesJsonFile(new File(args(0)), jreLocations, new File(args(1)))
    }

}
