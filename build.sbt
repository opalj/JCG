javacOptions in ThisBuild ++= Seq("-parameters")

lazy val commonSettings = Seq(
    scalaVersion := "2.12.4",
    organization := "de.tu-darmstadt.stg",
    version := "0.1"
)

lazy val jcg_annotations = project.settings(
    commonSettings
)

lazy val jcg_annotation_matcher = project.settings(
    commonSettings
)

lazy val jcg_wala_testadapter = project.settings(
    commonSettings
)

lazy val jcg_soot_testadapter = project.settings(
    commonSettings
)

lazy val jcg_opal_testadapter = project.settings(
    commonSettings
)

lazy val jcg_evaluation = project.settings(
    commonSettings
).dependsOn(
    jcg_annotations,
    jcg_annotation_matcher,
    jcg_opal_testadapter,
    jcg_wala_testadapter,
    jcg_soot_testadapter
)

lazy val general_expressions = project
  .settings(
      commonSettings,
    assemblyJarName in assembly := "general.jar",
    assemblyOutputPath in assembly := new File("./jars/general.jar"),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  ).dependsOn(jcg_annotations)

lazy val jvm_interaction_expressions = project
  .settings(
      commonSettings,
      assemblyJarName in assembly := "jvm_interaction.jar",
      assemblyOutputPath in assembly := new File("./jars/jvm_interaction.jar"),
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  ).dependsOn(jcg_annotations)

lazy val lambda_expressions = project
  .settings(
      commonSettings,
      assemblyJarName in assembly := "lambda_expressions.jar",
      assemblyOutputPath in assembly := new File("./jars/lambda_expressions.jar"),
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  ).dependsOn(jcg_annotations)

lazy val method_handle_expressions = project
  .settings(
      commonSettings,
      assemblyJarName in assembly := "method_handle.jar",
      assemblyOutputPath in assembly := new File("./jars/method_handle.jar"),
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  ).dependsOn(jcg_annotations)

lazy val polymorphic_frenzy_expressions = project
  .settings(
      commonSettings,
      assemblyJarName in assembly := "polymorphism.jar",
      assemblyOutputPath in assembly := new File("./jars/polymorphism.jar"),
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  ).dependsOn(jcg_annotations)

lazy val reflection_expressions = project
  .settings(
      commonSettings,
      assemblyJarName in assembly := "reflection.jar",
      assemblyOutputPath in assembly := new File("./jars/reflection.jar"),
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  ).dependsOn(jcg_annotations)

lazy val serialized_expressions = project
  .settings(
      commonSettings,
      assemblyJarName in assembly := "serialized.jar",
      assemblyOutputPath in assembly := new File("./jars/serialized.jar"),
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  ).dependsOn(jcg_annotations)

lazy val statically_initialized_expressions = project
  .settings(
      commonSettings,
      assemblyJarName in assembly := "statically_initialized.jar",
      assemblyOutputPath in assembly := new File("./jars/statically_initialized.jar"),
      assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  ).dependsOn(jcg_annotations)
