import java.io.File

class ExpectedCG(val jsonFile: File) extends CallGraph {
    private val json = parseFileToJson(jsonFile).getOrElse(ujson.Null)
    val filePath: String = jsonFile.getAbsolutePath
    val links: Seq[Seq[String]] = parseLinks("directLinks")
    val indirectLinks: Seq[String] = parseLinks("indirectLinks").flatMap(_.headOption)

    /**
     * Parses the links from the given JSON file.
     *
     * @param linkType the type of links to parse
     * @return the parsed links
     */
    private def parseLinks(linkType: String): Seq[Seq[String]] = {
        if (json.isNull) Seq.empty
        else {
            json(linkType) match {
                case ujson.Arr(links) =>
                    // Handle the case where the value is an array of arrays or strings
                    links.map {
                        case ujson.Arr(arr) => arr.map(_.str.trim).toSeq
                        case ujson.Str(str) => Seq(str.trim)
                        case _              => Seq.empty
                    }.toSeq
                case ujson.Str(str) => Seq(Seq(str.trim))
                case _              => Seq.empty
            }
        }
    }
}
