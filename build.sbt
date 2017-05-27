lazy val commonSettings = Seq(
  assemblyOutputPath in (ThisBuild, assembly) := new File("./jars/")
)

lazy val general_expressions = project
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "general.jar"
  )

lazy val jvm_interaction_expressions = project
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "jvm_interaction.jar"
  )

lazy val lambda_expressions = project
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "lambda_expressions.jar"
  )

lazy val method_handle_expressions = project
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "method_handle.jar"
  )

lazy val polymorphic_frenzy_expressions = project
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "polymorphism.jar"
  )

lazy val reflection_expressions = project
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "reflection.jar"
  )

lazy val serialized_expressions = project
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "serialized.jar"
  )

lazy val statically_initialized_expressions = project
  .settings(commonSettings: _*)
  .settings(
    assemblyJarName in assembly := "statically_initialized.jar"
  )
