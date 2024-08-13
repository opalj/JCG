import upickle.default.ReadWriter
import upickle.default.macroRW

case class Position(row: Int)

case class Node(id: String, label: String, file: String, start: Position)

case class Edge(source: Node, target: Node)

// Companion objects for serialization
object Position {
    implicit val rw: ReadWriter[Position] = macroRW
}

object Node {
    implicit val rw: ReadWriter[Node] = macroRW
}

object Edge {
    implicit val rw: ReadWriter[Edge] = macroRW
}
