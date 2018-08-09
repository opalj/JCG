import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes

case class CallSites(callSites: Set[CallSite])

object CallSites {
    implicit val callSitesReads: Reads[CallSites] = Json.reads[CallSites]

    implicit val callSitesWrites: Writes[CallSites] = Json.writes[CallSites]
}

case class CallSite(declaredTarget: Method, line: Int, method: Method, targets: Set[Method])

object CallSite {
    implicit val callSiteReads: Reads[CallSite] = Json.reads[CallSite]

    implicit val callSiteWrites: Writes[CallSite] = Json.writes[CallSite]
}

case class Method(name: String, declaringClass: String, returnType: String, parameterTypes: Array[String])

object Method {
    implicit val methodReads: Reads[Method] = Json.reads[Method]

    implicit val methodWrites: Writes[Method] = Json.writes[Method]
}