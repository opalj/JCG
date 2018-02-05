name := "jcg-soot-testadapter"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += "soot snapshot" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-snapshot/"

resolvers += "soot release" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-release/"

libraryDependencies += "de.tu-darmstadt.stg" % "sootconfig" % "1.1"

libraryDependencies += "com.googlecode.json-simple" % "json-simple" % "1.1.1"