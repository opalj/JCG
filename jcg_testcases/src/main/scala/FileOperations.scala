import org.apache.commons.io.FileUtils

import java.io.File
import java.io.PrintWriter

object FileOperations {
    private val pathSeparator = File.pathSeparator

    /**
     * Writes the given content to the given file and catches any exceptions.
     *
     * @param file    the file to write to
     * @param content the content to write
     */
    def writeToFile(file: File, content: String): Unit = {
        var printWriter: PrintWriter = null

        try {
            printWriter = new PrintWriter(file)
            printWriter.write(content)
        } catch {
            case e: Exception => println(s"Error writing to file: ${e.getMessage}")
        } finally {
            if (printWriter != null) {
                printWriter.close()
            }
        }
    }

    /**
     * Returns all files in the given directory that end with the given string.
     *
     * @param dir      the directory to search in
     * @param endsWith the string the files should end with
     */
    def listFilesRecursively(dir: File, endsWith: String = ""): Array[File] = {
        val currentFiles = dir.listFiles((_, file) => file.endsWith(endsWith))
        currentFiles ++ dir.listFiles.filter(_.isDirectory).flatMap(listFilesRecursively(_, endsWith))
    }

    /**
     * Cleans up the given directory.
     * If the directory does not exist, it will be created.
     *
     * @param dir the directory to clean up
     */
    def cleanUpDirectory(dir: File): Unit = {
        if (dir.exists()) {
            FileUtils.deleteDirectory(dir)
        }
        dir.mkdirs()
    }

    /**
     * Checks if the given directory contains files with any of the given extensions.
     *
     * @param dir        the directory to check
     * @param extensions the extensions to check for
     * @return true if the directory contains files with any of the given extensions, false otherwise
     */
    def hasFilesDeep(dir: File, extensions: String*): Boolean = {
        listFilesRecursively(dir).exists(f => extensions.exists(f.getName.endsWith))
    }

    /**
     * Finds all target directories in the current project.
     *
     * @return a list of target directories
     */
    def findJCGTargetDirs(): List[File] = {

        val root = new File(System.getProperty("user.dir"))

        val worklist = scala.collection.mutable.Queue(root)
        val result = scala.collection.mutable.ArrayBuffer.empty[File]
        while (worklist.nonEmpty) {
            val curElement = worklist.dequeue()
            if (curElement.isDirectory) {
                if (curElement.getName == "classes")
                    result += curElement
                else
                    worklist ++= curElement.listFiles()
            }
        }

        result.toList
    }

    /**
     * Converts a list of directories to a classpath string.
     *
     * @param dirs the directories to convert
     * @return the classpath string
     */
    def targetDirsToCP(dirs: List[File]): String = dirs.mkString(s"$pathSeparator")

    /**
     * Returns all markdown files in the given directory with the given prefix.
     *
     * @param inputDir     the directory to search in
     * @param filterPrefix a filter to apply to the file names
     * @return an array of markdown files
     */
    def getResources(inputDir: File, filterPrefix: String): Array[File] = {
        try {
            listFiles(inputDir, ".md", filterPrefix)
        } catch {
            case e: Exception =>
                println(s"${Console.RED}Error reading directory: ${inputDir.getAbsolutePath}. Make sure the directory contains a '$language' directory.${Console.RESET}")
                Array.empty
        }
    }

    /**
     * Returns all markdown files in the given directory.
     *
     * @param dir       the directory to search in
     * @param extension the extension of the files to list e.g '.md'
     * @param filter    a filter to apply to the file names
     */
    def listFiles(dir: File, extension: String, filter: String): Array[File] =
        dir.listFiles(_.getPath.endsWith(extension)).filter(_.getName.startsWith(filter))


    /**
     * Returns all directories in the given directory.
     *
     * @param dir the directory to search in
     * @return an array of directories in the given directory
     */
    def listDirs(dir: File): Array[File] = dir.listFiles().filter(_.isDirectory)
}
