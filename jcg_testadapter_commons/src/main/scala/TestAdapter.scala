import java.io.Writer

trait TestAdapter {
    val frameworkName: String
    val language: String
    val possibleAlgorithms: Array[String]

    def serializeCG(
        algorithm:      String,
        inputDirPath:   String,
        output:         Writer,
        programArgs:    Array[String],
        adapterOptions: AdapterOptions = AdapterOptions.makeEmptyOptions()
    ): Long
}
