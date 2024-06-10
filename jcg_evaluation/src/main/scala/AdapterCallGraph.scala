import java.io.File

class AdapterCallGraph {

    var filePath: String = ""
    var algorithm: String = ""
    var links: Array[Array[String]] = Array()

    def this(jsonFile: File) = {
        this()
        val source = scala.io.Source.fromFile(jsonFile)
        val json = try source.mkString finally source.close()
        filePath = jsonFile.getAbsolutePath

        ujson.read(json).arr.foreach(f => {
            var sourceLabel = f("source")("label").str
            val sourceFileName = f("source")("file").str.split("/").last.split("\\.").head
            var targetLabel = f("target")("label").str
            val targetFileName = f("target")("file").str.split("/").last.split("\\.").head

            if (sourceLabel == "anon") {
                sourceLabel = "<anonymous" + ":" + f("source")("start")("row").num.toInt.toString + ">"
            }

            if (targetLabel == "anon") {
                targetLabel = "<anonymous" + ":" + f("target")("start")("row").num.toInt.toString + ">"
            }

            links :+= Array(if (sourceLabel == "global") s"<$sourceLabel>" else (sourceFileName + "." + sourceLabel).trim, (targetFileName + "." + targetLabel).trim)

        })
    }
}
