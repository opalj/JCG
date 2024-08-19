case class AdapterOptions(
                           mainClass: String,
                           classPath: Array[String],
                           JDKPath: String,
                           analyzeJDK: Boolean
                         )

object AdapterOptions {
    val default: AdapterOptions = AdapterOptions("", Array.empty, "", analyzeJDK = false)
}