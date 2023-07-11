import play.api.libs.json.Reads
import play.api.libs.json.Json
import play.api.libs.json.Writes

/**
 * Representation of all Methods that are reachable in the represented call graph.
 *
 * @author Florian Kuebler
 */
case class ReachableMethods(reachableMethods: Set[ReachableMethod]) {

    /**
     * Converts the set of reachable methods into a mapping from method to the set of call sites.
     */
    lazy val toMap: Map[Method, Set[CallSite]] = {
        reachableMethods.groupBy(_.method).map { case (k, v) ⇒ k → v.flatMap(_.callSites) }
    }
}

object ReachableMethods {
    implicit val reachableMethodsReads: Reads[ReachableMethods] = Json.reads[ReachableMethods]

    implicit val reachableMethodsWrites: Writes[ReachableMethods] = Json.writes[ReachableMethods]
}

/**
 * A reachable method contains of the `method` itself and the call sites within that method.
 */
case class ReachableMethod(method: Method, callSites: Set[CallSite])

object ReachableMethod {
    implicit val reachableMethodsReads: Reads[ReachableMethod] = Json.reads[ReachableMethod]

    implicit val reachableMethodsWrites: Writes[ReachableMethod] = Json.writes[ReachableMethod]
}

/**
 * A call site has a `declaredTarget` method, is associated with a line number (-1 if unknown) and
 * contains the set of computed target methods (`targets`).
 */
case class CallSite(declaredTarget: Method, line: Int, pc: Option[Int], targets: Set[Method])

object CallSite {
    implicit val callSiteReads: Reads[CallSite] = Json.reads[CallSite]

    implicit val callSiteWrites: Writes[CallSite] = Json.writes[CallSite]
}

/**
 * A method is represented using the `name`, the `declaringClass`, its `returnType` and its
 * `parameterTypes`.
 */
case class Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String]) {

    override def toString: String = {
        s"$declaringClass { $returnType $name(${parameterTypes.mkString(", ")})}"
    }

    def nameBasedEquals(other: Method): Boolean = {
        other.name == this.name && other.declaringClass == this.declaringClass
    }
}

object Method {
    implicit val methodReads: Reads[Method] = Json.reads[Method]

    implicit val methodWrites: Writes[Method] = Json.writes[Method]
}