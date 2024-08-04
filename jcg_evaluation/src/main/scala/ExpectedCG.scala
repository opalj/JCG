import java.io.File

class ExpectedCG(val jsonFile: File) extends CallGraph {
    private val json = parseFileToJson(jsonFile).getOrElse(ujson.Null)
    val filePath: String = jsonFile.getAbsolutePath
    val links: Seq[Seq[String]] = parseLinks("directLinks")
    val indirectLinks: Seq[Seq[String]] = parseLinks("indirectLinks")

    /**
     * Parses the links from the given JSON file.
     *
     * @param linkType the type of links to parse
     * @return the parsed links
     */
    private def parseLinks(linkType: String): Seq[Seq[String]] = {
        if (json.isNull) Seq.empty
        else {
            json(linkType).arrOpt match {
                case Some(links) => links.map(_.arr.map(_.str.trim).toSeq).toSeq
                case None => Seq.empty
            }
        }
    }
}
