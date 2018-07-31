// We have centralized the configuration of all plug-ins here, to make this file easily
// useable by the dockerfile to configure the docker image used for building OPAL.

// to build fat-jars
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6")

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")

addSbtPlugin("com.eed3si9n" % "sbt-dirty-money" % "0.2.0")

// FOR THE DEPLOYMENT TO MAVEN CENTRAL
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.0")
