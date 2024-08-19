trait TestAdapter {
    val frameworkName: String
    val language: String
    val possibleAlgorithms: Array[String]

    def serializeCG(algorithm: String, inputDirPath: String, outputDirPath: String, adapterOptions: AdapterOptions = AdapterOptions.default): Long

    def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit
}
