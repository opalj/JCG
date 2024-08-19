trait JavaTestAdapter extends TestAdapter {
    override val language: String = "java"

    // Overload serializeCG for java
    def serializeCG(algorithm: String, target: String, mainClass: String, classPath: Array[String], JDKPath: String, analyzeJDK: Boolean, outputFile: String): Long

    // Override base methods
    override def serializeCG(algorithm: String, outputDirPath: String, inputDirPath: String): Long = {
        0L
    }

    override def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit = {
    }
}
