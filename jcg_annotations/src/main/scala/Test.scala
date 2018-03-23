import java.io.File
import java.io.PrintWriter

import javax.tools.ToolProvider

import scala.io.Source

import org.apache.commons.io.FileUtils

object Test {
    def main(args: Array[String]): Unit = {

        val tmp = new File("tmp")
        if (tmp.exists())
            FileUtils.deleteDirectory(tmp)
        tmp.mkdirs()

        val result = new File("result")
        if (result.exists())
            FileUtils.deleteDirectory(result)
        result.mkdirs()

        val resources = new File(getClass.getResource("/").getPath).listFiles(_.getPath.endsWith(".md"))
        resources.foreach { sourceFile ⇒
            println(sourceFile)
            val source = Source.fromFile(sourceFile)
            val lines = try source.mkString finally source.close()

            val re = """(?s)```java(\n// ([^/]*)([^\n]*)\n([^`]*))```""".r

            re.findAllIn(lines).matchData.foreach { matchResult ⇒
                val projectName = matchResult.group(2)
                val fileName = s"$projectName/src/$projectName"+matchResult.group(3)
                val codeSnippet = matchResult.group(4)

                val file = new File(tmp.getPath, fileName)
                file.getParentFile.mkdirs()
                file.createNewFile()

                val pw = new PrintWriter(file)
                pw.write(codeSnippet)
                pw.close()

                val compiler = ToolProvider.getSystemJavaCompiler

                val bin = new File(tmp.getPath, s"$projectName/bin/")
                bin.mkdirs()
                compiler.run(null, null, null, file.getPath, "-d", bin.getPath)

                val allFiles = recursiveListFiles(bin)
                val allFileNames = allFiles.map(_.getPath.replace(s"${tmp.getPath}/$projectName/bin/", ""))
                val args = Seq("jar", "cf", s"../../../${result.getPath}/$projectName.jar") ++ allFileNames
                sys.process.Process(args, bin).!
            }
        }

        FileUtils.deleteDirectory(tmp)

    }

    def recursiveListFiles(f: File): Array[File] = {
        val these = f.listFiles((_, fil) ⇒ fil.endsWith(".class"))
        these ++ f.listFiles.filter(_.isDirectory).flatMap(recursiveListFiles)
    }
}
