import java.io.File

trait TestCaseExtractor {
    val pathSeparator: String = File.pathSeparator
    val debug = false
    val language: String

    /**
     * Extracts test cases from the given directory and writes them to the output directory.
     *
     * @param inputDir     the directory to search in
     * @param outputDir    the directory to write the test cases to
     * @param prefixFilter a filter to apply to the file names in the input directory
     */
    def extract(inputDir: File, outputDir: File, prefixFilter: String = ""): Unit

    /**
     * Returns all markdown files in the given directory with the given prefix.
     *
     * @param inputDir     the directory to search in
     * @param filterPrefix a filter to apply to the file names
     * @return an array of markdown files
     */
    protected def getResources(inputDir: File, filterPrefix: String): Array[File] = {
        try {
            FileOperations.listMarkdownFiles(inputDir, filterPrefix)
        } catch {
            case e: Exception =>
                println(s"${Console.RED}Error reading directory: ${inputDir.getAbsolutePath}. Make sure the directory contains a '$language' directory.${Console.RESET}")
                Array.empty
        }
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
        val outputDir = new File("testcasesOutput")

        // parse arguments
        val mdFilter = getArgumentValue(args, "--md").getOrElse("")
        val language = getArgumentValue(args, "--lang").getOrElse("all")
        val resourceDir = new File(getArgumentValue(args, "--rsrcDir").getOrElse(userDir))

        val extractors = List[TestCaseExtractor](JavaTestExtractor, JSTestExtractor).filter(lang => language == "all" || language.contains(lang.language))

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
