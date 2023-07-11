/**
 * Performs the project specific evaluation.
 *
 * @author Florian Kuebler
 */
object ProjectSpecificEvaluator {

    def projectSpecificEvaluation(
        reachableMethods:   Set[Method], // from serialized call graph
        locationsMap:       Map[String, Set[Method]], // feature id (query) -> locations
        supportedTestCases: Set[String] // contains supported feature ids (test cases)
    ): Iterable[(Method, String)] = {
        for {
            (featureID, locations) ← locationsMap
            if !isFeatureSupported(featureID, supportedTestCases)
            location ← locations
            if reachableMethods contains location
        } yield (location, featureID)
    }

    def isFeatureSupported(featureID: String, supportedTestCases: Set[String]): Boolean = {
        distinctFeatureIDs(featureID) forall { f ⇒
            supportedTestCases contains correspondingTestCaseID(f)
        }
    }

    // todo document "CSR1+CSR2" -> ["CSR1", "CSR2"]
    def distinctFeatureIDs(queryFeatureID: String): Array[String] = {
        queryFeatureID split "\\+"
    }

    // todo document "TMR1.1" -> "TMR1"
    def correspondingTestCaseID(featureID: String): String = {
        val split = featureID split "\\."
        assert(split.size <= 2)
        split(0)
    }
}
