import ujson.Value

import java.io.File
import scala.util.Using

trait CallGraph {
    def filePath: String

    def links: Seq[Seq[String]]

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

        for (edge <- expectedCG.links) {
            if (!links.map(_.mkString(",")).contains(edge.mkString(","))) {
                missingEdges :+= edge
            }
        }

        if (missingEdges.length > 0) println(s"Missing edges in $filePath: ${missingEdges.mkString(", ")}\n${links.mkString(", ")}}")

        missingEdges
    }

}