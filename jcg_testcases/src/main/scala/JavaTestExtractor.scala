import TestCaseExtractorApp.debug
import TestCaseExtractorApp.pathSeparator
import org.apache.commons.io.FileUtils
import play.api.libs.json.JsValue
import play.api.libs.json.Json

import java.io.File
import java.io.PrintWriter
import javax.tools.ToolProvider
import scala.io.Source

object JavaTestExtractor extends TestCaseExtractor {
    val language = "java"

    override def extract(inputDir: File, outputDir: File, prefixFilter: String = ""): Unit = {
        val resources: Array[File] = getResources(new File(inputDir, language), prefixFilter)
        val resultsDir = new File(outputDir, language)
        val tmp = new File("tmp")

        FileOperations.cleanUpDirectory(tmp)
        FileOperations.cleanUpDirectory(resultsDir)

        resources.foreach { sourceFile ⇒
            if (debug) {
                println(sourceFile)
            }
            val source = Source.fromFile(sourceFile)
            val lines = try source.mkString finally source.close()

            /*
             * ##ProjectName
             * [//]: # (Main: path/to/Main.java)
             * multiple code snippets
             * [//]: # (END)
             */
            val reHeaders = ("""(?s)""" +
              """\#\#(?<projectName>[^\n]*)\n""" + // ##ProjectName
              """\[//\]: \# \((?:MAIN: (?<mainClass>[^\n]*)|LIBRARY)\)\n""" + // [//]: # (Main: path.to.Main.java) or [//]: # (LIBRARY)
              """(?<body>.*?)""" + // multiple code snippets
              """\[//\]: \# \(END\)""").r // [//]: # (END)
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

                val targetDirs = FileOperations.findJCGTargetDirs()
                val classPath = Seq(".", FileOperations.targetDirsToCP(targetDirs), System.getProperty("java.home")).mkString(s"$pathSeparator")

                val compilerArgs = Seq("-cp", s"$classPath", "-d", bin.getAbsolutePath, "-encoding", "UTF-8", "-source", "1.8", "-target", "1.8") ++ srcFiles

                if (debug) {
                    println(compilerArgs.mkString("[DEBUG] Compiler args: \n\n", "\n", "\n\n"))
                }

                compiler.run(null, null, null, compilerArgs: _*)

                val allClassFiles = FileOperations.recursiveListFiles(bin.getAbsoluteFile)

                if (debug) {
                    println(allClassFiles.mkString("[DEBUG] Produced class files: \n\n", "\n", "\n\n"))
                }

                val allClassFileNames = allClassFiles.map(_.getAbsolutePath.replace(s"${tmp.getAbsolutePath}/$projectName/bin/", ""))


                val jarOpts = Seq(if (main != null) "cfe" else "cf")
                val outPathCompiler = new File(s"${resultsDir.getAbsolutePath}/$projectName.jar")
                val mainOpt = Option(main)
                val args = Seq("jar") ++ jarOpts ++ Seq(outPathCompiler.getAbsolutePath) ++ mainOpt ++ allClassFileNames

                if (debug) {
                    println(args.mkString("[DEBUG] Jar args: \n\n", "\n", "\n\n"))
                }

                val exitCode = sys.process.Process(args, bin).!

                if (debug && exitCode != 0) {
                    println(s"[DEBUG] EXIT CODE $exitCode")
                }

                if (main != null) {
                    println(s"running $projectName.jar")
                    sys.process.Process(Seq("java", "-jar", s"$projectName.jar"), resultsDir).!
                }

                val projectSpec = ProjectSpecification(
                    name = projectName,
                    target = new File(outPathCompiler.getAbsolutePath).getCanonicalPath,
                    main = mainOpt,
                    java = 8,
                    cp = None
                )

                val projectSpecJson: JsValue = Json.toJson(projectSpec)
                val projectSpecOut = new File(resultsDir, s"$projectName.conf")
                val pw = new PrintWriter(projectSpecOut)
                pw.write(Json.prettyPrint(projectSpecJson))
                pw.close()
            }

        }


        FileUtils.deleteDirectory(tmp)
        if (resources.nonEmpty) println(s"Extracted test cases for java from $inputDir to $outputDir")
    }
}
