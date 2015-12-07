name := "Annotated Java Call Graph (JCG)"

version := "0.0.1-SNAPSHOT"

scalacOptions in (Compile, doc) := Opts.doc.title("The Annotated Java Call Graph Project")

lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "de.opal-project",
  scalaVersion := "2.11.7"
)

lazy val app = (project in file("app")).
  settings(commonSettings: _*)