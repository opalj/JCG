javacOptions in ThisBuild ++= Seq("-parameters")

lazy val commonSettings = Seq(
    scalaVersion := "2.12.6",
    organization := "de.opal-project",
    homepage := Some(url("https://bitbucket.org/delors/jcg")),
    licenses := Seq("BSD-2-Clause" -> url("http://opensource.org/licenses/BSD-2-Clause")),
    resolvers += Resolver.sonatypeRepo("snapshots"),
    version := "1.0",
    publishMavenStyle := true,
    publishTo := MavenPublishing.publishTo(isSnapshot.value),
    pomExtra := MavenPublishing.pomNodeSeq()
)

lazy val jcg_annotations = project.settings(
    commonSettings,
    name := "JCG Annotations",
    libraryDependencies += "commons-io" % "commons-io" % "2.5",
    aggregate in assembly := false,
    compileOrder := CompileOrder.Mixed
)

lazy val jcg_annotation_matcher = project.settings(
    commonSettings,
    aggregate in assembly := false,
    publishArtifact := false
).dependsOn(jcg_annotations, jcg_testadapter_commons)

lazy val jcg_wala_testadapter = project.settings(
    commonSettings,
    aggregate in assembly := false,
    publishArtifact := false
).dependsOn(jcg_testadapter_commons)

lazy val jcg_soot_testadapter = project.settings(
    commonSettings,
    aggregate in assembly := false,
    publishArtifact := false
).dependsOn(jcg_testadapter_commons)

//lazy val jcg_opal_testadapter = project.settings(
//    commonSettings,
//    aggregate in assembly := false
//).dependsOn(jcg_testadapter_commons)

lazy val jcg_doop_testadapter = project.settings(
    commonSettings,
    aggregate in assembly := false,
    publishArtifact := false
).dependsOn(
    jcg_annotation_matcher,
    jcg_testadapter_commons
)

lazy val jcg_testadapter_commons = project.settings(
    commonSettings,
    aggregate in assembly := false,
    publishArtifact := false
)

lazy val jcg_evaluation = project.settings(
    commonSettings,
    publishArtifact := false
).dependsOn(
    jcg_annotations,
    jcg_annotation_matcher,
//    jcg_opal_testadapter,
    jcg_wala_testadapter,
    jcg_soot_testadapter
)
