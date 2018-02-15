name := "jcg-wala-testadapter"

version := "0.1"

organization := "de.tu-darmstadt.stg"

scalaVersion := "2.12.4"

resolvers += Resolver.mavenLocal

libraryDependencies += "com.ibm.wala" % "com.ibm.wala.core" % "1.4.4-SNAPSHOT"

libraryDependencies += "com.ibm.wala" % "com.ibm.wala.util" % "1.4.4-SNAPSHOT"

libraryDependencies += "com.ibm.wala" % "com.ibm.wala.shrike" % "1.4.4-SNAPSHOT"

libraryDependencies += "com.googlecode.json-simple" % "json-simple" % "1.1.1"