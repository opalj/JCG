
javacOptions in ThisBuild ++= Seq("-parameters", "-source", "1.7", "-target", "1.7")

lazy val general_expressions = project
  .settings(
    assemblyJarName in assembly := "general.jar",
    assemblyOutputPath in assembly := new File("./jars/general.jar"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

lazy val jvm_interaction_expressions = project
  .settings(
    assemblyJarName in assembly := "jvm_interaction.jar",
    assemblyOutputPath in assembly := new File("./jars/jvm_interaction.jar"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

lazy val lambda_expressions = project
  .settings(
    assemblyJarName in assembly := "lambda_expressions.jar",
    assemblyOutputPath in assembly := new File("./jars/lambda_expressions.jar"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

lazy val method_handle_expressions = project
  .settings(
    assemblyJarName in assembly := "method_handle.jar",
    assemblyOutputPath in assembly := new File("./jars/method_handle.jar"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

lazy val polymorphic_frenzy_expressions = project
  .settings(
    assemblyJarName in assembly := "polymorphism.jar",
    assemblyOutputPath in assembly := new File("./jars/polymorphism.jar"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

lazy val reflection_expressions = project
  .settings(
    assemblyJarName in assembly := "reflection.jar",
    assemblyOutputPath in assembly := new File("./jars/reflection.jar"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

lazy val serialized_expressions = project
  .settings(
    assemblyJarName in assembly := "serialized.jar",
    assemblyOutputPath in assembly := new File("./jars/serialized.jar"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

lazy val statically_initialized_expressions = project
  .settings(
    assemblyJarName in assembly := "statically_initialized.jar",
    assemblyOutputPath in assembly := new File("./jars/statically_initialized.jar"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )
