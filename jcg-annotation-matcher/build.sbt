name := "jcg-annotation-matcher"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += "soot snapshot" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-snapshot/"

resolvers += "soot release" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-release/"

libraryDependencies += "de.tu-darmstadt.stg" % "sootconfig" % "1.1"

libraryDependencies += "de.opal-project" %% "bytecode-representation" % "1.1.0-SNAPSHOT"

