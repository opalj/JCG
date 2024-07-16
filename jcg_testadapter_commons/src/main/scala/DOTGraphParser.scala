import java.io.File
import scala.io.Source
import scala.util.Using

object DOTGraphParser {
    /**
     * Parses a DOT file and returns the nodes and edges
     *
     * @param file       the file to parse
     * @param outputFile the file to write the output to
     * @return a tuple containing the nodes and edges
     */
    def parseFile(file: File): (List[String], List[String]) = {
        if (!file.exists()) {
            throw new IllegalArgumentException(s"[ERROR] File ${file.getAbsolutePath} does not exist")
        }

        val nodeRE = """\w+\s*\[.+]""".r
        val edgeRE = """\w+\s*->\s*\w+""".r

        Using(Source.fromFile(file)) { fileBuffered =>
            val lines = fileBuffered.getLines().toList
            var nodes = List[String]()
            var edges = List[String]()
            for (line <- lines) {
                nodeRE.findFirstMatchIn(line) match {
                    case Some(x) => nodes = nodes :+ x.toString
                    case None =>
                }

                edgeRE.findFirstMatchIn(line) match {
                    case Some(x) => edges = edges :+ x.toString
                    case None =>
                }
            }

            (nodes, edges)
        }.getOrElse {
            throw new IllegalArgumentException(s"[ERROR] Could not read file ${file.getAbsolutePath}")
        }
    }
}
