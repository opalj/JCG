import java.io.File

class ExpectedCG {
    var filePath: String = ""
    var directLinks: Array[Array[String]] = Array[Array[String]]()
    var indirectLinks: Array[Array[String]] = Array[Array[String]]()

    def this(jsonFile: File) = {
        this()
        filePath = jsonFile.getAbsolutePath
        val source = scala.io.Source.fromFile(jsonFile)
        val json = try source.mkString finally source.close()

        ujson.read(json)("directLinks").arr.foreach { directLink =>
            directLinks = directLinks :+ directLink.arr.map(_.str.trim).toArray
        }

        ujson.read(json)("indirectLinks").arr.foreach { indirectLink =>
            indirectLinks = indirectLinks :+ indirectLink.arr.map(_.str.trim).toArray
        }
    }
}
