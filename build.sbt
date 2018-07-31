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
    name := "JCG Annotation Matcher",
    libraryDependencies += "de.opal-project" %% "bytecode-representation" % "1.1.0-SNAPSHOT",
    aggregate in assembly := false
).dependsOn(jcg_annotations, jcg_testadapter_commons)

lazy val jcg_wala_testadapter = project.settings(
    commonSettings,
    name := "JCG WALA Test Adapter",
    libraryDependencies += "com.ibm.wala" % "com.ibm.wala.core" % "1.4.3",
    libraryDependencies += "com.ibm.wala" % "com.ibm.wala.util" % "1.4.3",
    libraryDependencies += "com.ibm.wala" % "com.ibm.wala.shrike" % "1.4.3",
    libraryDependencies += "com.googlecode.json-simple" % "json-simple" % "1.1.1",
    aggregate in assembly := false,
    publishArtifact := false
).dependsOn(jcg_testadapter_commons)

lazy val jcg_soot_testadapter = project.settings(
    commonSettings,
    name := "JCG Soot Test Adapter",
    resolvers += "soot snapshot" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-snapshot/",
    resolvers += "soot release" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-release/",
    libraryDependencies += "de.tu-darmstadt.stg" % "sootconfig" % "1.1",
    libraryDependencies += "com.googlecode.json-simple" % "json-simple" % "1.1.1",
    aggregate in assembly := false,
    publishArtifact := false
).dependsOn(jcg_testadapter_commons)

//lazy val jcg_opal_testadapter = project.settings(
//    commonSettings,
//    aggregate in assembly := false
//).dependsOn(jcg_testadapter_commons)

lazy val jcg_doop_testadapter = project.settings(
    commonSettings,
    name := "JCG DOOP Test Adapter",
    libraryDependencies += "de.opal-project" % "bytecode-representation_2.12" % "1.1.0-SNAPSHOT",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.6.9",
    aggregate in assembly := false,
    publishArtifact := false
).dependsOn(
    jcg_annotation_matcher,
    jcg_testadapter_commons
)

lazy val jcg_testadapter_commons = project.settings(
    commonSettings,
    name := "JCG Test Adapter Commons",
    aggregate in assembly := false
)

lazy val jcg_evaluation = project.settings(
    commonSettings,
    name := "JCG Evaluation",
    resolvers += "soot snapshot" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-snapshot/",
    resolvers += "soot release" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-release/",
    resolvers += Resolver.mavenLocal,
    libraryDependencies += "de.opal-project" %% "opal-developer-tools" % "1.1.0-SNAPSHOT",
    publishArtifact := false
).dependsOn(
    jcg_annotations,
    jcg_annotation_matcher,
    //    jcg_opal_testadapter,
    jcg_wala_testadapter,
    jcg_soot_testadapter
)
