trait TestAdapter {
    val frameworkName: String
    val language: String

    def possibleAlgorithms: Array[String]
}
