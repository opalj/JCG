import java.io.File

class AdapterCG(val jsonFile: File) extends CallGraph {
    val filePath: String = jsonFile.getAbsolutePath
    var links: Seq[Seq[String]] = {
        val json = parseFileToJson(jsonFile).getOrElse(ujson.Null)
        if (json.isNull) Seq.empty
        else {
            json.arr.map(f => {
                val sourceLabel = f("source")("label").strOpt.getOrElse("")
                val sourceFileName = f("source")("file").strOpt.getOrElse("").split("/").last.split("\\.").head
                val sourceLineNum = f("source")("start")("row").numOpt.getOrElse(0.0).toInt
                val targetLabel = f("target")("label").strOpt.getOrElse("")
                val targetFileName = f("target")("file").strOpt.getOrElse("").split("/").last.split("\\.").head
                val targetLineNum = f("target")("start")("row").numOpt.getOrElse(0.0).toInt

                val source = if (sourceLabel == "anon") s"$sourceFileName.<anonymous:$sourceLineNum>"
                else if (sourceLabel == "global") s"<$sourceLabel>"
                else s"$sourceFileName.$sourceLabel:$sourceLineNum".trim

                val target = if (targetLabel == "anon") s"$sourceFileName.<anonymous:$targetLineNum>"
                else s"$targetFileName.$targetLabel:$targetLineNum".trim

                Seq(source, target)
            }).toSeq
        }
    }
}
