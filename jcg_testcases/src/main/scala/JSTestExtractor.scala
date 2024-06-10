import TestCaseExtractorApp.debug

import java.io.File
import scala.io.Source

object JSTestExtractor extends TestCaseExtractor {
    val language = "js"

    override def extract(inputDir: File, outputDir: File, prefixFilter: String = ""): Unit = {
        val resources = getResources(new File(inputDir, "js"), prefixFilter)
        val resultsDir = new File(outputDir, "js")

        // Clear result directory if it already exists
        FileOperations.cleanUpDirectory(resultsDir)

        resources.foreach(file => {
            if (debug) {
                println(file)
            }

            // read lines from given testcase file
            val source = Source.fromFile(file)
            val lines = try source.mkString finally source.close()

            /*
             * ##ProjectName
             * [//]: # (Main: path/to/Main.js)
             * multiple code snippets
             * [//]: # (END)
             */
            val reHeaders = ("""(?s)""" +
              """\#\#(?<projectName>[^\n]*)\n""" + // ##ProjectName
              """\[//\]: \# \((?:MAIN: (?<mainClass>[^\n]*)|LIBRARY)\)\n""" + // [//]: # (Main: path.to.Main.js) or [//]: # (LIBRARY)
              """(?<body>.*?)""" + // multiple code snippets
              """\[//\]: \# \(END\)""").r // [//]: # (END)

            /*
             * (```json
             * ...
             * ```)?
             * ```js
             * // path/to/File.js
             * CODE SNIPPET
             * ```
             */
            val re = """(?s)```(json\n(?<expectedCG>[^`]*)```\n```)?js(\n// ?(?<packageName>[^/]*)(?<fileName>[^\n]*)\n(?<codeSnippet>[^`]*))```""".r

            reHeaders.findAllIn(lines).matchData.foreach(projectMatchResult => {
                val projectName = projectMatchResult.group("projectName").trim
                if (debug) {
                    println("[DEBUG] project", projectName)
                }

                // create Folder for project
                val outputFolder = new File(resultsDir, projectName)
                outputFolder.mkdirs()

                re.findAllIn(projectMatchResult.group("body")).matchData.foreach { matchResult =>
                    val packageName = matchResult.group("packageName")
                    val filePath = s"$projectName/$packageName${matchResult.group("fileName")}"
                    val codeSnippet = matchResult.group("codeSnippet")
                    val expectedCG = matchResult.group("expectedCG")

                    val codeFile = new File(resultsDir.getAbsolutePath, filePath)
                    val cgFile = new File(resultsDir.getAbsolutePath, s"$projectName/$packageName${matchResult.group("fileName")}on")
                    codeFile.getParentFile.mkdirs()
                    codeFile.createNewFile()
                    FileOperations.writeToFile(codeFile, codeSnippet)

                    if (expectedCG != null) {
                        cgFile.getParentFile.mkdirs()
                        cgFile.createNewFile()
                        FileOperations.writeToFile(cgFile, expectedCG)
                    }

                    codeFile.getAbsolutePath
                }
            })
        })

        println(s"Extracted test cases for js from $inputDir to $outputDir")
    }
}
