import scala.util.matching.Regex

object JSTestExtractor extends TestCaseExtractor {
    val language = "js"

    /*
     * (```json
     * ...
     * ```)?
     * ```js
     * // path/to/File.js
     * CODE SNIPPET
     * ```
     */
    protected val reBody: Regex = """(?s)```(json\n(?<expectedCG>[^`]*)```\n```)?js(\n// ?(?<packageName>[^/]*)(?<fileName>[^\n]*)\n(?<codeSnippet>[^`]*))```""".r
}
