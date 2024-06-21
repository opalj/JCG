import org.apache.commons.io.FileUtils

import java.io.{File, PrintWriter}

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
     * Returns all markdown files in the given directory.
     *
     * @param dir    the directory to search in
     * @param filter a filter to apply to the file names
     */
    def listMarkdownFiles(dir: File, filter: String): Array[File] = {
        dir.listFiles(_.getPath.endsWith(".md")).filter(_.getName.startsWith(filter))
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
     * Recursively lists all files in the given directory.
     *
     * @param f the directory to list files from
     * @return an array of files
     */
    def recursiveListFiles(f: File): Array[File] = {
        val these = f.listFiles((_, fil) ⇒ fil.endsWith(".class"))
        these ++ f.listFiles.filter(_.isDirectory).flatMap(recursiveListFiles)
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
}
