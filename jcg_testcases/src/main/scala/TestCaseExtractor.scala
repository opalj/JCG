import java.io.File

import org.apache.commons.io.FileUtils

trait TestCaseExtractor {
    def extract(outputDir: File, resources: Array[File]): Unit
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

    val pathSeparator = File.pathSeparator
    val debug = false

    /**
     * Extracts the test cases.
     *
     * @param args possible arguments are:
     *             '--rsrcDir dir', where 'dir' is the directory to search in. If this option is not
     *             specified, the resource root directory will be used.
     *             '--md filter', where 'filter' is a prefix of the filenames to be included.
     */
    def main(args: Array[String]): Unit = {
        val userDir = System.getProperty("user.dir")
        val tmp = new File("tmp")
        val resultJars = new File("testcaseJars")

        // clean up existing directories
        FileOperations.cleanUpDirectory(tmp)
        FileOperations.cleanUpDirectory(resultJars)

        // parse arguments
        val mdFilter = getArgumentValue(args, "--md").getOrElse("")
        val resourceDir = new File(getArgumentValue(args, "--rsrcDir").getOrElse(userDir))

        val javaDir = new File(resourceDir, "java")
        val jsDir = new File(resourceDir, "js")

        // extract test cases
        val javaResources = FileOperations.listMarkdownFiles(javaDir, mdFilter)
        println(javaResources.mkString(", "))
        JavaTestExtractor.extract(resultJars, javaResources)

        val jsResources = FileOperations.listMarkdownFiles(jsDir, mdFilter)
        println(jsResources.mkString(", "))
        JSTestExtractor.extract(new File("testcaseJS"), jsResources)

        FileUtils.deleteDirectory(tmp)
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
