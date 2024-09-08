class AdapterOptions private (val options: Map[String, Any]) {

    def getOptionAs[T](key: String): Option[T] = options.get(key).flatMap {
        case value: T => Some(value)
        case _        => None
    }

    def getString(key: String): String = getOptionAs[String](key).orNull

    def getBoolean(key: String): Boolean = getOptionAs[Boolean](key).getOrElse(false)

    def getStringArray(key: String): Array[String] = getOptionAs[Array[String]](key).getOrElse(Array.empty)
}

object AdapterOptions {

    /**
     * Creates a new AdapterOptions object for Java test adapters.
     */
    def makeJavaOptions(
        mainClass:   String,
        classPath:   Array[String],
        JDKPath:     String,
        analyzeJDK:  Boolean,
        target:      String        = "",
        jvmArgs:     Array[String] = Array.empty,
        programArgs: Array[String] = Array.empty
    ): AdapterOptions = {
        new AdapterOptions(Map(
            "mainClass" -> mainClass,
            "classPath" -> classPath,
            "JDKPath" -> JDKPath,
            "analyzeJDK" -> analyzeJDK,
            "target" -> target,
            "jvmArgs" -> jvmArgs,
            "programArgs" -> programArgs
        ))
    }

    /**
     * Creates an empty AdapterOptions object.
     */
    def makeEmptyOptions(): AdapterOptions = {
        new AdapterOptions(Map.empty)
    }
}
