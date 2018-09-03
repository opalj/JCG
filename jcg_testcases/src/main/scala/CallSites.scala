import play.api.libs.json.Reads
import play.api.libs.json.Json
import play.api.libs.json.Writes

case class ReachableMethods(reachableMethods: Set[ReachableMethod]) {
    lazy val toMap: Map[Method, Set[CallSite]] = {
        reachableMethods.groupBy(_.method).map { case (k, v) ⇒ k → v.flatMap(_.callSites) }
    }
}

object ReachableMethods {
    implicit val reachableMethodsReads: Reads[ReachableMethods] = Json.reads[ReachableMethods]

    implicit val reachableMethodsWrites: Writes[ReachableMethods] = Json.writes[ReachableMethods]
}

case class ReachableMethod(method: Method, callSites: Set[CallSite])

object ReachableMethod {
    implicit val reachableMethodsReads: Reads[ReachableMethod] = Json.reads[ReachableMethod]

    implicit val reachableMethodsWrites: Writes[ReachableMethod] = Json.writes[ReachableMethod]
}

case class CallSite(declaredTarget: Method, line: Int, targets: Set[Method])

object CallSite {
    implicit val callSiteReads: Reads[CallSite] = Json.reads[CallSite]

    implicit val callSiteWrites: Writes[CallSite] = Json.writes[CallSite]
}

case class Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String])

object Method {
    implicit val methodReads: Reads[Method] = Json.reads[Method]

    implicit val methodWrites: Writes[Method] = Json.writes[Method]
}