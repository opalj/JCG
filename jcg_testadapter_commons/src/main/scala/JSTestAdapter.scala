
trait JSTestAdapter extends TestAdapter {

    /**
     * Generates a single call graph.
     *
     * @param algorithm     The algorithm to use for generating the call graph.
     * @param outputDirPath The directory to write the call graph to.
     * @param inputDirPath  The directory containing the input files to generate a call graph for.
     * @return The time taken to generate the call graph in nanoseconds.
     */
    def serializeCG(algorithm: String, outputDirPath: String, inputDirPath: String): Long

    /**
     * Generates all call graphs with the given algorithm.
     * @param inputDirPath The directory containing the input files to generate call graphs for.
     * @param outputDirPath The directory to write the call graphs to.
     */
    def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit
}
