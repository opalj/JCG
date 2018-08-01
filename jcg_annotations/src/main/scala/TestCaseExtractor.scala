import java.io.File
import java.io.PrintWriter

import coursier.Cache
import coursier.Module
import coursier.Dependency
import coursier.Fetch
import coursier.FileError
import coursier.Resolution
import coursier.maven.MavenRepository
import javax.tools.ToolProvider

import scala.io.Source
import org.apache.commons.io.FileUtils
import play.api.libs.json._
import scalaz.\/

case class ProjectSpecification(
    name: String, target: String, main: Option[String], java: Int, cp: Option[Array[ClassPathEntry]]
)
object ProjectSpecification {
    implicit val configReads: Reads[ProjectSpecification] = Json.reads[ProjectSpecification]
    implicit val configWrites: OWrites[ProjectSpecification] = Json.writes[ProjectSpecification]
}

sealed trait ClassPathEntry {
    def getLocations: Seq[File]
}
object ClassPathEntry {
    implicit val classPathEntryReads: Reads[ClassPathEntry] =
        __.read[MavenClassPathEntry].map(x ⇒ x: ClassPathEntry) orElse __.read[LocalClassPathEntry].map(x ⇒ x: ClassPathEntry)

    implicit val classPathEntryWrites: Writes[ClassPathEntry] = Writes[ClassPathEntry] {
        case mvn: MavenClassPathEntry ⇒ MavenClassPathEntry.writer.writes(mvn)
        case lcl: LocalClassPathEntry ⇒ LocalClassPathEntry.writer.writes(lcl)
    }
}
case class MavenClassPathEntry(org: String, id: String, version: String) extends ClassPathEntry {
    override def getLocations: Seq[File] = {
        val start = Resolution(Set(Dependency(Module(org, id), version)))
        val repositories = Seq(Cache.ivy2Local, MavenRepository("https://repo1.maven.org/maven2"))
        val fetch = Fetch.from(repositories, Cache.fetch())

        val resolution = start.process.run(fetch).unsafePerformSync
        val r: Seq[\/[FileError, File]] = resolution.artifacts.map(Cache.file(_).run).map(_.unsafePerformSync)
        assert(r.forall(_.isRight))

        r.map(_.toOption.get)
    }
}
object MavenClassPathEntry {
    implicit val reader: Reads[MavenClassPathEntry] = Json.reads[MavenClassPathEntry]
    val writer: Writes[MavenClassPathEntry] = Json.writes[MavenClassPathEntry]
}

case class LocalClassPathEntry(path: String) extends ClassPathEntry {
    override def getLocations: Seq[File] = Seq(new File(path))
}
object LocalClassPathEntry {
    implicit val reader: Reads[LocalClassPathEntry] = Json.reads[LocalClassPathEntry]
    val writer: Writes[LocalClassPathEntry] = Json.writes[LocalClassPathEntry]
}

object TestCaseExtractor {
    def main(args: Array[String]): Unit = {

        val tmp = new File("tmp")
        if (tmp.exists())
            FileUtils.deleteDirectory(tmp)
        tmp.mkdirs()

        val result = new File("result")
        if (result.exists())
            FileUtils.deleteDirectory(result)
        result.mkdirs()

        var mdFilter = ""
        var fileDir = getClass.getResource("/").getPath
        args.sliding(2, 2).toList.collect {
            case Array("--md", f: String)        ⇒ mdFilter = f
            case Array("--rsrcDir", dir: String) ⇒ fileDir = dir
        }

        val resources = new File(fileDir).
            listFiles(_.getPath.endsWith(".md")).
            filter(_.getName.startsWith(mdFilter))

        println(resources.mkString(", "))

        resources.foreach { sourceFile ⇒
            println(sourceFile)
            val source = Source.fromFile(sourceFile)
            val lines = try source.mkString finally source.close()

            /*
             * ##ProjectName
             * [//]: # (Main: path/to/Main.java)
             * multiple code snippets
             * [//]: # (END)
             */
            val reHeaders = ("""(?s)"""+
                """\#\#([^\n]*)\n"""+ // ##ProjectName
                """\[//\]: \# \((?:MAIN: ([^\n]*)|LIBRARY)\)\n"""+ // [//]: # (Main: path.to.Main.java) or [//]: # (LIBRARY)
                """(.*?)"""+ // multiple code snippets
                """\[//\]: \# \(END\)""").r( // [//]: # (END)
                    "projectName", "mainClass", "body"
                )

            /*
             * ```java
             * // path/to/Class.java
             * CODE SNIPPET
             * ```
             */
            val re = """(?s)```java(\n// ([^/]*)([^\n]*)\n([^`]*))```""".r

            reHeaders.findAllIn(lines).matchData.foreach { projectMatchResult ⇒
                val projectName = projectMatchResult.group("projectName")
                val main = projectMatchResult.group("mainClass")
                assert(main == null || !main.contains("/"), "invalid main class, use '.' instead of '/'")
                val srcFiles = re.findAllIn(projectMatchResult.group("body")).matchData.map { matchResult ⇒
                    val packageName = matchResult.group(2)
                    val fileName = s"$projectName/src/$packageName${matchResult.group(3)}"
                    val codeSnippet = matchResult.group(4)

                    val file = new File(tmp.getPath, fileName)
                    file.getParentFile.mkdirs()
                    file.createNewFile()

                    val pw = new PrintWriter(file)
                    pw.write(codeSnippet)
                    pw.close()
                    file.getPath
                }

                val compiler = ToolProvider.getSystemJavaCompiler

                val bin = new File(tmp.getPath, s"$projectName/bin/")
                bin.mkdirs()

                val compilerArgs = (srcFiles ++ Seq("-d", bin.getPath)).toSeq

                compiler.run(null, null, null, compilerArgs: _*)

                val allClassFiles = recursiveListFiles(bin)
                val allClassFileNames = allClassFiles.map(_.getPath.replace(s"${tmp.getPath}/$projectName/bin/", ""))

                val jarOpts = Seq(if (main != null) "cfe" else "cf")
                val outPathCompiler = new File(s"../../../${result.getPath}/$projectName.jar")
                val mainOpt = Option(main)
                val args = Seq("jar") ++ jarOpts ++ Seq(outPathCompiler.getPath) ++ mainOpt ++ allClassFileNames
                sys.process.Process(args, bin).!

                if (main != null) {
                    println(s"running $projectName.jar")
                    sys.process.Process(Seq("java", "-jar", s"$projectName.jar"), result).!
                }

                val projectSpec = ProjectSpecification(
                    name = projectName, target = new File(bin, outPathCompiler.getPath).getCanonicalPath, main = mainOpt, java = 8, cp = None
                )

                val projectSpecJson: JsValue = Json.toJson(projectSpec)
                val projectSpecOut = new File(result, s"$projectName.conf")
                val pw = new PrintWriter(projectSpecOut)
                pw.write(Json.prettyPrint(projectSpecJson))
                pw.close()
            }
        }

        FileUtils.deleteDirectory(tmp)
    }

    def recursiveListFiles(f: File): Array[File] = {
        val these = f.listFiles((_, fil) ⇒ fil.endsWith(".class"))
        these ++ f.listFiles.filter(_.isDirectory).flatMap(recursiveListFiles)
    }
}
