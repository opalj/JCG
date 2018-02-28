name := "Java9 module expressions"

// TODO Move the Java annotations to test-fixtures!
unmanagedSourceDirectories in Test := (javaSource in Test).value :: (scalaSource in Test).value :: Nil

fork in run := true
