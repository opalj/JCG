
trait JSTestAdapter extends TestAdapter {
    def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit
    val language = "js"
}
