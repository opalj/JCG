import upickle.default.{ReadWriter, macroRW}

// Define the Position class
case class Position(row: Int)

// Define the Node class
case class Node(id: String, label: String, file: String, start: Position)

// Define the Edge class
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
