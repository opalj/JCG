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
                val targetLabel = f("target")("label").strOpt.getOrElse("")
                val targetFileName = f("target")("file").strOpt.getOrElse("").split("/").last.split("\\.").head

                val source = if (sourceLabel == "anon") s"$sourceFileName.<anonymous:${f("source")("start")("row").numOpt.getOrElse(0.0).toInt}>"
                else if (sourceLabel == "global") s"<$sourceLabel>"
                else s"$sourceFileName.$sourceLabel".trim

                val target = if (targetLabel == "anon") s"$sourceFileName.<anonymous:${f("target")("start")("row").numOpt.getOrElse(0.0).toInt}>"
                else s"$targetFileName.$targetLabel".trim

                Seq(source, target)
            }).toSeq
        }
    }
}
