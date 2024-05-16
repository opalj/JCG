/**
 * @author Michael Reif
 */
object RebuildAndTest {

    def main(args: Array[String]): Unit = {
        TestCaseExtractorApp.main(args)
        Evaluation.main(args)
    }
}
