import java.io.File
import scala.io.Source
import scala.util.Using

case class DOTGraph(nodes: List[String], edges: List[String])

object DOTGraphParser {
    /**
     * Parses a DOT file containing a graph and returns the nodes and edges of the graph.
     *
     * @param file the file to parse
     * @return a [[DOTGraph]] containing the nodes and edges of the graph
     */
    def parseFile(file: File): DOTGraph = {
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

            DOTGraph(nodes, edges)
        }.getOrElse {
            throw new IllegalArgumentException(s"[ERROR] Could not read file ${file.getAbsolutePath}")
        }
    }
}
