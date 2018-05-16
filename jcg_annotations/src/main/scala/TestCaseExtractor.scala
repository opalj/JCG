import java.io.File
import java.io.PrintWriter

import javax.tools.ToolProvider

import scala.io.Source

import org.apache.commons.io.FileUtils

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
            case Array("--md", f: String) => mdFilter = f
            case Array("--rsrcDir", dir: String) => fileDir = dir
        }

        val resources = new File(fileDir).
            listFiles(_.getPath.endsWith(".md")).
            filter(_.getName.startsWith(mdFilter))


        println(resources.mkString(", "))

        resources.foreach { sourceFile ⇒
            println(sourceFile)
            val source = Source.fromFile(sourceFile)
            val lines = try source.mkString finally source.close()

            val reHeaders = """(?s)\#\#([^\n]*)\n\[//\]: \# \(MAIN: ([^\n]*)\)\n(.*?)\[//\]: \# \(END\)""".r
            val re = """(?s)```java(\n// ([^/]*)([^\n]*)\n([^`]*))```""".r

            reHeaders.findAllIn(lines).matchData.foreach { projectMatchResult ⇒
                val projectName = projectMatchResult.group(1)
                val main = projectMatchResult.group(2)
                val srcFiles = re.findAllIn(projectMatchResult.group(3)).matchData.map { matchResult ⇒
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
                val args = Seq("jar", "cfe", s"../../../${result.getPath}/$projectName.jar", main) ++ allClassFileNames
                sys.process.Process(args, bin).!
                println(s"running $projectName.jar")
                sys.process.Process(Seq("java", "-jar", s"$projectName.jar"), result).!

            }
        }

        FileUtils.deleteDirectory(tmp)
    }

    def recursiveListFiles(f: File): Array[File] = {
        val these = f.listFiles((_, fil) ⇒ fil.endsWith(".class"))
        these ++ f.listFiles.filter(_.isDirectory).flatMap(recursiveListFiles)
    }
}
