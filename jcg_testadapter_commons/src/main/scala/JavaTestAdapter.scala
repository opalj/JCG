trait JavaTestAdapter extends TestAdapter {
    val language = "java"
    /**
     * Constructs a call graph for the given target and saves the serialized version
     * ({@link ReachableMethods}) into the specified output file and return the elapsed time in
     * nanoseconds
     *
     * @param mainClass the main-class to be analysed (x.y.Foo) or `null` in case of a library
     * @return the elapsed nanoseconds
     */
    def serializeCG(algorithm: String,
                    target: String,
                    mainClass: String,
                    classPath: Array[String],
                    JDKPath: String,
                    analyzeJDK: Boolean,
                    outputFile: String): Long
}
