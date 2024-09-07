import play.api.libs.json.JsValue
import play.api.libs.json.Json

import java.io.File
import java.io.PrintWriter
import javax.tools.ToolProvider
import scala.util.matching.Regex

object JavaTestExtractor extends TestCaseExtractor {
    val language = "java"

    /*
     * ```java
     * // path/to/Class.java
     * CODE SNIPPET
     * ```
     */
    protected val reBody: Regex = """(?s)```java(\n// ([^/]*)([^\n]*)\n([^`]*))```""".r

    override protected def processLines(lines: String, resultsDir: File, temp: File): Unit = {
        reHeaders.findAllIn(lines).matchData.foreach(projectMatchResult => {
            val projectName = projectMatchResult.group("projectName").trim
            val main = projectMatchResult.group("mainClass")
            assert(main == null || !main.contains("/"), "invalid main class, use '.' instead of '/'")
            val srcFiles = reBody.findAllIn(projectMatchResult.group("body")).matchData.map { matchResult ⇒
                val packageName = matchResult.group(2)
                val fileName = s"$projectName/src/$packageName${matchResult.group(3)}"
                val codeSnippet = matchResult.group(4)

                val file = new File(temp.getAbsolutePath, fileName)
                file.getParentFile.mkdirs()
                file.createNewFile()

                val pw = new PrintWriter(file)
                pw.write(codeSnippet)
                pw.close()
                file.getAbsolutePath
            }

            val compiler = ToolProvider.getSystemJavaCompiler

            val bin = new File(temp.getAbsolutePath, s"$projectName/bin/")
            bin.mkdirs()

            val targetDirs = FileOperations.findJCGTargetDirs()
            val classPath = Seq(".", FileOperations.targetDirsToCP(targetDirs), System.getProperty("java.home")).mkString(s"${File.pathSeparator}")

            val compilerArgs = Seq("-cp", s"$classPath", "-d", bin.getAbsolutePath, "-encoding", "UTF-8", "-source", "1.8", "-target", "1.8") ++ srcFiles

            if (TestCaseExtractor.debug) {
                println(compilerArgs.mkString("[DEBUG] Compiler args: \n\n", "\n", "\n\n"))
            }

            compiler.run(null, null, null, compilerArgs: _*)

            val allClassFiles = FileOperations.listFilesRecursively(bin.getAbsoluteFile, ".class")

            if (TestCaseExtractor.debug) {
                println(allClassFiles.mkString("[DEBUG] Produced class files: \n\n", "\n", "\n\n"))
            }

            val allClassFileNames = allClassFiles.map(_.getAbsolutePath.replace(s"${temp.getAbsolutePath}/$projectName/bin/", ""))


            val jarOpts = Seq(if (main != null) "cfe" else "cf")
            val outPathCompiler = new File(s"${resultsDir.getAbsolutePath}/$projectName.jar")
            val mainOpt = Option(main)
            val args = Seq("jar") ++ jarOpts ++ Seq(outPathCompiler.getAbsolutePath) ++ mainOpt ++ allClassFileNames

            if (TestCaseExtractor.debug) {
                println(args.mkString("[DEBUG] Jar args: \n\n", "\n", "\n\n"))
            }

            val exitCode = sys.process.Process(args, bin).!

            if (TestCaseExtractor.debug && exitCode != 0) {
                println(s"[DEBUG] EXIT CODE $exitCode")
            }

            if (main != null) {
                println(s"running $projectName.jar")
                sys.process.Process(Seq("java", "-jar", s"$projectName.jar"), resultsDir).!
            }

            val projectSpec = ProjectSpecification(
                name = projectName,
                java = 8,
                main = mainOpt,
                target = new File(outPathCompiler.getAbsolutePath).getCanonicalPath,
                cp = None,
                jvm_args = None
            )

            val projectSpecJson: JsValue = Json.toJson(projectSpec)
            val projectSpecOut = new File(resultsDir, s"$projectName.conf")
            val pw = new PrintWriter(projectSpecOut)
            pw.write(Json.prettyPrint(projectSpecJson))
            pw.close()
        })
    }
}
