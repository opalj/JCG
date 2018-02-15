name := "evaluation"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "de.tu-darmstadt.stg" %% "jcg-soot-testadapter" % "0.1"

libraryDependencies += "de.tu-darmstadt.stg" %% "jcg-wala-testadapter" % "0.1"

resolvers += "soot snapshot" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-snapshot/"

resolvers += "soot release" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-release/"

resolvers += Resolver.mavenLocal