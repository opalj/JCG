import ujson.Value

import java.io.File
import scala.util.Using

trait CallGraph {
    def filePath: String

    def links: Seq[Seq[String]]

    /**
     * Parses a given file to a JSON object.
     *
     * @param file The file to parse.
     * @return The JSON object parsed from the file.
     */
    def parseFileToJson(file: File): Option[Value] = {
        Using(scala.io.Source.fromFile(file)) { source =>
            ujson.read(source.mkString)
        }.toOption
    }

    /**
     * Compares the expected call graph links with the generated call graph links.
     *
     * @param expectedCG The expected callgraph against which the generated call graph links are compared.
     * @return Array of edges missing from the generated call graph links.
     */
    def compareLinks(expectedCG: CallGraph): Array[Seq[String]] = {
        var missingEdges: Array[Seq[String]] = Array()

        for (expectedEdge <- expectedCG.links) {
            if (links.isEmpty || !links.exists(edge => edgesMatch(edge, expectedEdge))) {
                missingEdges :+= expectedEdge
            }
        }

        missingEdges
    }

    /**
     * Compares two edges and returns true if they match, false otherwise. Ignores line numbers, if expected edge does not contain them.
     * @param edge The edge to compare.
     * @param expectedEdge The expected edge to compare.
     * @return True if the edges match, false otherwise.
     */
    private def edgesMatch(edge: Seq[String], expectedEdge: Seq[String]): Boolean = {
        edge.zip(expectedEdge).forall { case (e, ee) =>
            // if expected edge does not contain line numbers, remove them
            if(!ee.contains(":")) {
                e.split(":").head == ee
            } else {
                e == ee
            }
        }
    }
}
