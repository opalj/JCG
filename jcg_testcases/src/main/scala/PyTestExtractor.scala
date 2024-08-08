import java.io.File

object PyTestExtractor extends TestCaseExtractor {
    val language = "python"

    /*
     * (```json
     * ...
     * ```)?
     * ```python
     * # path/to/File.py
     * CODE SNIPPET
     * ```
     */
    private val re = """(?s)```(json\n(?<expectedCG>[^`]*)```\n```)?python(\n# ?(?<packageName>[^/]*)(?<fileName>[^\n]*)\n(?<codeSnippet>[^`]*))```""".r

    override def processLines(lines: String, resultsDir: File, temp: File): Unit = {
        reHeaders.findAllIn(lines).matchData.foreach(projectMatchResult => {
            val projectName = projectMatchResult.group("projectName").trim
            if (TestCaseExtractor.debug) {
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
                val cgFile = new File(resultsDir.getAbsolutePath, s"$projectName/$packageName${matchResult.group("fileName").split("\\.").head}.json")
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
    }
}
