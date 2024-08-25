trait TestAdapter {
    val frameworkName: String
    val language: String
    val possibleAlgorithms: Array[String]

    def serializeCG(
        algorithm:      String,
        inputDirPath:   String,
        outputDirPath:  String,
        adapterOptions: AdapterOptions = new AdapterOptions(Map())
    ): Long
}
