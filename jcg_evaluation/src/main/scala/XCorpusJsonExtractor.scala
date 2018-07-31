import java.io.File
import java.io.PrintWriter

import org.opalj.bytecode
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Writes

private case class ORG(org: OPALJ)
private case class OPALJ(opalj: HERMES)
private case class HERMES(hermes: Projects)
private case class Projects(projects: Array[Project])
private case class Project(id: String, cp: String)

object XCorpusJsonExtractor {

    def main(args: Array[String]): Unit = {
        val XCorpusDir = new File(args(0))
        assert(XCorpusDir.exists())
        val projects = for {
            dataDir ← XCorpusDir.listFiles(_.isDirectory)
            projectRootDir ← dataDir.listFiles(_.isDirectory)
        } yield {

            val projectDir = new File(projectRootDir, "project")
            val bin = new File(projectDir, "bin.zip")
            assert(bin.exists())

            val libDir = new File(projectDir, "default-lib")

            val libs = if (libDir.exists())
                libDir.listFiles(f ⇒ f.getName.endsWith(".jar")).map(_.getAbsolutePath) ++ Array(bytecode.JRELibraryFolder.getAbsolutePath)
            else Array(bytecode.JRELibraryFolder.getAbsolutePath)

            val projectName = projectRootDir.getName

            val cp = s"${bin.getAbsolutePath}${File.pathSeparator}${libs.mkString(File.pathSeparator)}"

            Project(projectName, cp)
        }

        implicit val projectWrites: Writes[Project] = Json.writes[Project]
        implicit val projectsWrites: Writes[Projects] = Json.writes[Projects]

        implicit val hermesWrites: Writes[HERMES] = Json.writes[HERMES]
        implicit val opaljWrites: Writes[OPALJ] = Json.writes[OPALJ]
        implicit val orgWrites: Writes[ORG] = Json.writes[ORG]

        val json: JsValue = Json.toJson(ORG(OPALJ(HERMES(Projects(projects)))))
        val outFile = new File("xcorpus.json")
        val pw = new PrintWriter(outFile)
        pw.write(Json.prettyPrint(json))
        pw.close()
    }

}
