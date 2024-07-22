import org.apache.commons.io.FileUtils

import java.io.File
import scala.io.Source
import scala.util.matching.Regex

trait TestCaseExtractor {
    val language: String
    /*
     * ##ProjectName
     * [//]: # (Main: path/to/Main.xyz)
     * multiple code snippets
     * [//]: # (END)
     */
    protected val reHeaders: Regex = ("""(?s)""" +
      """\#\#(?<projectName>[^\n]*)\n""" + // ##ProjectName
      """\[//\]: \# \((?:MAIN: (?<mainClass>[^\n]*)|LIBRARY)\)\n""" + // [//]: # (Main: path.to.Main.xyz) or [//]: # (LIBRARY)
      """(?<body>.*?)""" + // multiple code snippets
      """\[//\]: \# \(END\)""").r // [//]: # (END)

    /** Used to parse the body of the test file, i.e. the content between [//]: # (MAIN: global) and [//]: # (END) */
    protected val reBody: Regex

    /**
     * Extracts test cases from the given directory and writes them to the output directory.
     *
     * @param inputDir     the directory to search in
     * @param outputDir    the directory to write the test cases to
     * @param prefixFilter a filter to apply to the file names in the input directory
     */
    def extract(inputDir: File, outputDir: File, prefixFilter: String = ""): Unit = {
        val resources: Array[File] = FileOperations.getResources(new File(inputDir, language), prefixFilter)
        val resultsDir = new File(outputDir, language)
        val temp = new File("tmp")
        // Clear result directory if it already exists
        FileOperations.cleanUpDirectory(resultsDir)
        FileOperations.cleanUpDirectory(temp)

        resources.foreach(file => {
            if (TestCaseExtractor.debug) {
                println(file)
            }

            val source = Source.fromFile(file)
            val lines = try source.mkString finally source.close()

            processLines(lines, resultsDir, temp)
        })

        FileUtils.deleteDirectory(temp)
        if (resources.nonEmpty) {
            println(s"${Console.GREEN}Successfully extracted test cases for $language${Console.RESET}")
        }
    }

    /**
     * Processes the lines in a testcase markdown file.
     *
     * @param lines      the lines to process
     * @param resultsDir the directory to write the test cases to
     * @param temp       temporary directory to save intermediate files, will be cleared before and after processing
     */
    protected def processLines(lines: String, resultsDir: File, temp: File): Unit = {
        reHeaders.findAllIn(lines).matchData.foreach(projectMatchResult => {
            val projectName = projectMatchResult.group("projectName").trim
            if (TestCaseExtractor.debug) {
                println("[DEBUG] project", projectName)
            }

            // create Folder for project
            val outputFolder = new File(resultsDir, projectName)
            outputFolder.mkdirs()

            reBody.findAllIn(projectMatchResult.group("body")).matchData.foreach { matchResult =>
                val packageName = matchResult.group("packageName")
                val filePath = s"$projectName/$packageName${matchResult.group("fileName")}"
                val codeSnippet = matchResult.group("codeSnippet")
                val expectedCG = matchResult.group("expectedCG")

                val codeFile = new File(resultsDir.getAbsolutePath, filePath)
                val cgFile = new File(resultsDir.getAbsolutePath, s"$projectName/$packageName${matchResult.group("fileName").split('.').head}.json")
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
    var debug: Boolean = false

    /**
     * Extracts the test cases.
     *
     * @param args possible arguments are:
     *             '--rsrcDir dir', where 'dir' is the directory to search in, this folder should contain a js and/or java folder. If this option is not
     *             specified, the resource root directory will be used.
     *             '--md filter', where 'filter' is a prefix of the filenames to be included.
     *             '--lang lang', where 'lang' is the language of the test cases to extract ('js', 'java', 'all'). Default is 'all'.
     */
    def main(args: Array[String]): Unit = {
        val userDir = System.getProperty("user.dir")

        // parse arguments
        val mdFilter = getArgumentValue(args, "--md").getOrElse("")
        val language = getArgumentValue(args, "--lang").getOrElse("all")
        val resourceDir = new File(getArgumentValue(args, "--rsrcDir").getOrElse(userDir))
        val outputDir = new File(getArgumentValue(args, "--outDir").getOrElse("testcasesOutput"))
        if (args.contains("--debug")) {
            debug = true
        }

        val extractors = List[TestCaseExtractor](JavaTestExtractor, JSTestExtractor, PyTestExtractor).filter(extractor => language == "all" || language.contains(extractor.language))

        for (extractor <- extractors) {
            extractor.extract(resourceDir, outputDir, mdFilter)
        }
    }

    /**
     * Returns the value of the given CLI argument.
     * If the argument is not found, None is returned.
     *
     * @param args array holding the arguments to search in.
     */
    def getArgumentValue(args: Array[String], argName: String): Option[String] = {
        args.sliding(2, 2).collectFirst({
            case Array(`argName`, value: String) => value
        })
    }
}
