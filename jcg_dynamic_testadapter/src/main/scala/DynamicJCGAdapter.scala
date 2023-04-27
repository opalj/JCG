
object DynamicJCGAdapter extends JCGTestAdapter {

    override def possibleAlgorithms(): Array[String] = Array("Dynamic")

    override def frameworkName(): String = "Dynamic"

    override def serializeCG(
                       algorithm: String,
                       target: String,
                       mainClass: String,
                       classPath: Array[String],
                       JDKPath: String,
                       analyzeJDK: Boolean,
                       outputFile: String
                   ): Long = {

    }
}