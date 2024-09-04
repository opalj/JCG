import scala.util.matching.Regex

object PyTestExtractor extends TestCaseExtractor {
    val language = "python"

    /*
     * (```json
     * ...
     * ```)?
     * ```python
     * # path/to/File.py
     * CODE SNIPPET
     * ```
     */
    protected val reBody: Regex =
        """(?s)```(json\n(?<expectedCG>[^`]*)```\n```)?python(\n# ?(?<packageName>[^/]*)(?<fileName>[^\n]*)\n(?<codeSnippet>[^`]*))```""".r
}
