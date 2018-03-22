import java.io.File
import java.io.PrintWriter

import javax.tools.ToolProvider

import scala.io.Source

object Test {
    def main(args: Array[String]): Unit = {
        val source = Source.fromResource("Reflection.md")
        val lines = try source.mkString finally source.close()

        val re = """(?s)```java(\n// ([^/]*)([^\n]*)\n([^`]*))```""".r

        re.findAllIn(lines).matchData.foreach { matchResult ⇒
            val projectName = matchResult.group(2)
            val fileName = s"result/$projectName/src/$projectName"+matchResult.group(3)
            val codeSnippet = matchResult.group(4)

            val file = new File(fileName)
            val parent = file.getParentFile()

            parent.mkdirs()
            file.createNewFile()
            val pw = new PrintWriter(file)
            pw.write(codeSnippet)
            pw.close()

            val compiler = ToolProvider.getSystemJavaCompiler


            val bin =  new File(s"result/$projectName/bin/")
            bin.mkdirs()
            compiler.run(null, null, null, file.getPath, "-d", bin.getPath)

            val args = Seq("jar", "cf", s"../../$projectName.jar") ++ recursiveListFiles(bin).map(_.getPath.replace(s"result/$projectName/bin/", ""))
            println(sys.process.Process(args, bin).!!)


        }
    }

    def recursiveListFiles(f: File): Array[File] = {
        val these = f.listFiles((_, fil) => fil.endsWith(".class"))
        these ++ f.listFiles.filter(_.isDirectory).flatMap(recursiveListFiles)
    }
}
