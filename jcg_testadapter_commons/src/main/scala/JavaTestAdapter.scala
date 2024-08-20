trait JavaTestAdapter extends TestAdapter {
    override val language: String = "java"

    override def serializeAllCGs(inputDirPath: String, outputDirPath: String): Unit = {
    }
}
