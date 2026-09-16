import org.jcg.taieadapter.Tai_e_TestAdapterImpl

object Tai_e_JCG_Adapter extends JavaTestAdapter {
  override val frameworkName: String = "Taie"
  override val possibleAlgorithms: Array[String] = Array("CHA", "PTA")

  override def serializeCG(
    algorithm: String,
    target: String,
    output: java.io.Writer,
    adapterOptions: AdapterOptions
  ): Long = {
    new Tai_e_TestAdapterImpl().serializeCG(
      algorithm,
      target,
      output,
      adapterOptions.getString("mainClass"),
      adapterOptions.getStringArray("classPath"),
      adapterOptions.getString("JDKPath"),
      adapterOptions.getBoolean("analyzeJDK")
    )
  }
}
