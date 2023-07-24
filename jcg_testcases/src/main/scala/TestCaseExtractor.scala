import scala.io.Source
import java.io.PrintWriter
import java.io.File

import javax.tools.ToolProvider
import org.apache.commons.io.FileUtils
import play.api.libs.json.JsValue
import play.api.libs.json.Json

/**
 * This tool searches for '.md' files in the resources (or in the specified directory) to extracts
 * the test cases.
 * The extraction include, compiling, packaging and running (in order to find errors).
 * For each extracted test case, a project specification file ([[ProjectSpecification]])
 * is generated.
 *
 * @author Michael Reif
 * @author Florian Kuebler
 */
object TestCaseExtractor {

    val pathSeparator = File.pathSeparator

    /**
     * Extracts the test cases.
     * @param args possible arguments are:
     *      '--rsrcDir dir', where 'dir' is the directory to search in. If this option is not
     *          specified, the resource root directory will be used.
     *      '--md filter', where 'filter' is a prefix of the filenames to be included.
     */
    def main(args: Array[String]): Unit = {

        val debug = false

        val userDir = System.getProperty("user.dir")

        val tmp = new File("tmp")
        if (tmp.exists())
            FileUtils.deleteDirectory(tmp)

        tmp.mkdirs()

        val result = new File("testcaseJars")
        if (result.exists())
            FileUtils.deleteDirectory(result)
        result.mkdirs()

        var mdFilter = ""
        var fileDir: Option[String]= None

        args.sliding(2, 2).toList.collect {
            case Array("--md", f: String)        ⇒ mdFilter = f
            case Array("--rsrcDir", dir: String) ⇒ fileDir = Some(dir)
        }

        val resourceDir = new File(fileDir.getOrElse(userDir))

        // get all markdown files
        val resources = resourceDir.
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
                val projectName = projectMatchResult.group("projectName").trim
                val main = projectMatchResult.group("mainClass")
                assert(main == null || !main.contains("/"), "invalid main class, use '.' instead of '/'")
                val srcFiles = re.findAllIn(projectMatchResult.group("body")).matchData.map { matchResult ⇒
                    val packageName = matchResult.group(2)
                    val fileName = s"$projectName/src/$packageName${matchResult.group(3)}"
                    val codeSnippet = matchResult.group(4)

                    val file = new File(tmp.getAbsolutePath, fileName)
                    file.getParentFile.mkdirs()
                    file.createNewFile()

                    val pw = new PrintWriter(file)
                    pw.write(codeSnippet)
                    pw.close()
                    file.getAbsolutePath
                }

                val compiler = ToolProvider.getSystemJavaCompiler

                val bin = new File(tmp.getAbsolutePath, s"$projectName/bin/")
                bin.mkdirs()

                val targetDirs = findJCGTargetDirs()
                val classPath = Seq(".", targetDirsToCP(targetDirs), System.getProperty("java.home")).mkString(s"$pathSeparator")

                val compilerArgs = Seq("-cp", s"$classPath", "-d", bin.getAbsolutePath, "-encoding", "UTF-8", "-source", "1.8", "-target", "1.8") ++ srcFiles

                if(debug) {
                    println(compilerArgs.mkString("[DEBUG] Compiler args: \n\n", "\n", "\n\n"))
                }

                compiler.run(null, null, null, compilerArgs: _*)

                val allClassFiles = recursiveListFiles(bin.getAbsoluteFile)

                if(debug) {
                    println(allClassFiles.mkString("[DEBUG] Produced class files: \n\n", "\n", "\n\n"))
                }

                val allClassFileNames = allClassFiles.map(_.getAbsolutePath.replace(s"${tmp.getAbsolutePath}/$projectName/bin/", ""))


                val jarOpts = Seq(if (main != null) "cfe" else "cf")
                val outPathCompiler = new File(s"${result.getAbsolutePath}/$projectName.jar")
                val mainOpt = Option(main)
                val args = Seq("jar") ++ jarOpts ++ Seq(outPathCompiler.getAbsolutePath) ++ mainOpt ++ allClassFileNames

                if(debug) {
                    println(args.mkString("[DEBUG] Jar args: \n\n", "\n", "\n\n"))
                }

                sys.process.Process(args, bin).!

                if (main != null) {
                    println(s"running $projectName.jar")
                    sys.process.Process(Seq("java", "-jar", s"$projectName.jar"), result).!
                }

                val projectSpec = ProjectSpecification(
                    name = projectName,
                    target = new File(outPathCompiler.getAbsolutePath).getCanonicalPath,
                    main = mainOpt,
                    java = 8,
                    cp = None,
                    jvm_args = None
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

    def findJCGTargetDirs() : List[File] = {

        val root = new File(System.getProperty("user.dir"))

        val worklist = scala.collection.mutable.Queue(root)
        val result = scala.collection.mutable.ArrayBuffer.empty[File]
        while(worklist.nonEmpty){
            val curElement = worklist.dequeue()
            if(curElement.isDirectory){
                if(curElement.getName == "classes")
                    result += curElement
                else
                    worklist ++= curElement.listFiles()
            }
        }

        result.toList
    }

    def targetDirsToCP(dirs: List[File]) : String = dirs.mkString(s"$pathSeparator")
}
