import sbt._
import Keys._

object JCGBuild extends Build {
    lazy val general_expressions = Project(id = "general_expressions",
                           base = file("general_expressions"))

    lazy val inner_class_expressions = Project(id = "inner_class_expressions",
                           base = file("inner_class_expressions"))

    lazy val lambda_expressions = Project(id = "lambda_expressions",
                           base = file("lambda_expressions"))

    lazy val method_handle_expressions = Project(id = "method_handle_expressions",
                           base = file("method_handle_expressions"))

    lazy val polymorphic_frenzy_expressions = Project(id = "polymorphic_frenzy_expressions",
                           base = file("polymorphic_frenzy_expressions"))

    lazy val reflection_expressions = Project(id = "reflection_expressions",
                           base = file("reflection_expressions"))

    lazy val serialized_expressions = Project(id = "serialized_expressions",
                           base = file("serialized_expressions"))

    lazy val statically_initialized_expressions = Project(id = "statically_initialized_expressions",
                           base = file("statically_initialized_expressions"))

}
