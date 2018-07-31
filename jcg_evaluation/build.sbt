name := "Evaluation"

resolvers += "soot snapshot" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-snapshot/"

resolvers += "soot release" at "https://soot-build.cs.uni-paderborn.de/nexus/repository/soot-release/"

resolvers += Resolver.mavenLocal

libraryDependencies += "de.opal-project" %% "opal-developer-tools" % "1.1.0-SNAPSHOT"