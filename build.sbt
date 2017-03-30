name := "Annotated Java Call Graph (JCG)"


//version 		in ThisBuild := "0.8.10-SNAPSHOT"
version 		in ThisBuild := "0.8.10"
// RELEASED version 		in ThisBuild := "0.8.9"
organization 	in ThisBuild := "de.opal-project"
homepage 		in ThisBuild := Some(url("http://www.opal-project.de"))
licenses 		in ThisBuild := Seq("BSD-2-Clause" -> url("http://opensource.org/licenses/BSD-2-Clause"))


resolvers in ThisBuild += Resolver.jcenterRepo
resolvers in ThisBuild += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

// the tests/analysis are already parallelized
parallelExecution in ThisBuild := false
parallelExecution in Global := false

logBuffered in ThisBuild := false

javacOptions in ThisBuild ++= Seq("-encoding", "utf8")

testOptions in ThisBuild := {
		baseDirectory.map(bd =>
    		Seq(Tests.Argument("-u",  bd.getAbsolutePath + "/shippable/testresults"))
		).value
	}

testOptions in ThisBuild += Tests.Argument("-o")

javaOptions in ThisBuild ++= Seq(
	"-Xmx3G", "-Xms1024m", "-Xnoclassgc",
	"-XX:NewRatio=1", "-XX:SurvivorRatio=8", "-XX:+UseParallelGC","-XX:+AggressiveOpts")

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18)
EclipseKeys.withSource := true
EclipseKeys.withJavadoc := true

//
//
// Publish jars to Eclipse plugin project
//
//
//addCommandAlias("copyToEclipsePlugin", "; set publishTo in ThisBuild := Some(Resolver.file(\"file\", new File(\"TOOLS/ep/lib\"))) ; publish")

//
//
// SETTINGS REQUIRED TO PUBLISH OPAL ON MAVEN CENTRAL
//
//

publishMavenStyle in ThisBuild := true

publishTo in ThisBuild := {
	val nexus = "https://oss.sonatype.org/"
	if (isSnapshot.value)
		Some("snapshots" at nexus + "content/repositories/snapshots")
	else
		Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

