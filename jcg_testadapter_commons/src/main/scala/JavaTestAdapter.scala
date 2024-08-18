trait JavaTestAdapter extends TestAdapter {
    override val language: String = "java"

    // Java-specific serializeCG
    def serializeCG(algorithm: String, target: String, mainClass: String, classPath: Array[String], JDKPath: String, analyzeJDK: Boolean, outputFile: String): Long

    // General serializeCG, overriding the base one
    override def serializeCG(algorithm: String, outputDirPath: String, inputDirPath: String): Long = {
        0
    }

    // Override serializeAllCGs if necessary
    override def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit = {
    }
}
