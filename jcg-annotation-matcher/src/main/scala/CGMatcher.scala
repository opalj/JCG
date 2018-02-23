import java.io.File
import java.io.FileInputStream

import org.opalj.br.analyses.Project
import org.opalj.br.Annotation
import org.opalj.br.AnnotationValue
import org.opalj.br.ArrayValue
import org.opalj.br.BooleanValue
import org.opalj.br.ClassValue
import org.opalj.br.ElementValuePair
import org.opalj.br.IntValue
import org.opalj.br.ObjectType
import org.opalj.br.StringValue
import org.opalj.br.Type
import org.opalj.br.VoidType
import org.opalj.log.GlobalLogContext
import org.opalj.log.LogContext
import org.opalj.log.LogMessage
import org.opalj.log.OPALLogger
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import play.api.libs.json.Reads

case class CallSites(callSites: Set[CallSite])

case class CallSite(declaredTarget: Method, line: Int, method: Method, targets: Set[String])

case class Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String])

class DevNullLogger extends OPALLogger {
    override def log(message: LogMessage)(implicit ctx: LogContext): Unit = {}
}

object CGMatcher {

    val callSiteAnnotationType = ObjectType("lib/annotations/callgraph/CallSite")
    val callSitesAnnotationType = ObjectType("lib/annotations/callgraph/CallSites")
    val invokedConstructorType = ObjectType("lib/annotations/callgraph/InvokedConstructor")
    val invokedConstructorsType = ObjectType("lib/annotations/callgraph/InvokedConstructors")

    def matchCallSites(tgtJar: String, jsonPath: String): (Int, Int) = {
        OPALLogger.updateLogger(GlobalLogContext, new DevNullLogger())
        val p = Project(new File(tgtJar))


        val json = Json.parse(new FileInputStream(new File(jsonPath)))
        implicit val methodReads: Reads[Method] = Json.reads[Method]
        implicit val callSiteReads: Reads[CallSite] = Json.reads[CallSite]
        implicit val callSitesReads: Reads[CallSites] = Json.reads[CallSites]
        val jsResult = json.validate[CallSites]
        jsResult match {
            case _: JsSuccess[CallSites] ⇒
                val computedCallSites = jsResult.get
                var missedTargets = 0
                var missedCallSites = 0
                for (clazz ← p.allProjectClassFiles) {
                    for ((method, _) ← clazz.methodsWithBody) {
                        for (annotation ← method.annotations) {

                            val callSiteAnnotations =
                                if (annotation.annotationType == callSiteAnnotationType ||
                                    annotation.annotationType == invokedConstructorType)
                                    List(annotation)
                                else if (annotation.annotationType == callSitesAnnotationType ||
                                    annotation.annotationType == invokedConstructorsType)
                                    getAnnotations(annotation, "value")
                                else
                                    Nil

                            for (callSiteAnnotation ← callSiteAnnotations) {
                                val line = getLineNumber(callSiteAnnotation)
                                val name =
                                    if (callSiteAnnotation.annotationType == callSiteAnnotationType)
                                        getString(callSiteAnnotation, "name")
                                    else "<init>"
                                val returnType =
                                    if (callSiteAnnotation.annotationType == callSiteAnnotationType)
                                        getType(callSiteAnnotation, "returnType")
                                    else VoidType
                                val parameterTypes = getParameterList(callSiteAnnotation)
                                val annotatedMethod = convertMethod(method)
                                val tmp = computedCallSites.callSites.filter { cs ⇒
                                    cs.line == line && cs.method == annotatedMethod && cs.declaredTarget.name == name
                                }

                                computedCallSites.callSites.find { cs ⇒
                                    cs.line == line && cs.method == annotatedMethod && cs.declaredTarget.name == name
                                } match {
                                    case Some(computedCallSite) ⇒
                                        val annotatedTargets =
                                            if (callSiteAnnotation.annotationType == callSiteAnnotationType)
                                                getAnnotations(callSiteAnnotation, "resolvedMethods").map(getString(_, "receiverType"))
                                            else
                                                List(getString(callSiteAnnotation, "receiverType"))
                                        for (annotatedTgt ← annotatedTargets) {
                                            if (!computedCallSite.targets.contains(annotatedTgt)) {
                                                println(s"$line:${annotatedMethod.declaringClass}#${annotatedMethod.name}:\t there is no call to $annotatedTgt#$name")
                                                missedTargets += 1
                                            } else {
                                                println("found it")
                                            }
                                        }
                                    case _ ⇒
                                        println(s"$line:${annotatedMethod.declaringClass}#${annotatedMethod.name}:\t there is no callsite to method $name")
                                        missedCallSites +=1
                                }
                            }
                        }
                    }
                }
                (missedTargets, missedCallSites)
            case _ ⇒
                throw new RuntimeException("Unable to parse json")
        }
    }

    def main(args: Array[String]): Unit = {
        matchCallSites(args(0), args(1))
    }

    def convertMethod(method: org.opalj.br.Method): Method = {
        val name = method.name
        val declaringClass = method.classFile.thisType.toJVMTypeName
        val returnType = method.returnType.toJVMTypeName
        val parameterTypes = method.parameterTypes.map(_.toJVMTypeName).toList

        Method(name, declaringClass, returnType, parameterTypes)
    }

    //
    // UTILITY FUNCTIONS
    //
    def getAnnotations(callSites: Annotation, label: String): Seq[Annotation] = { //@CallSites -> @CallSite[]
        val avs = callSites.elementValuePairs collectFirst {
            case ElementValuePair(`label`, ArrayValue(array)) ⇒ array
        }
        avs.getOrElse(IndexedSeq.empty).map { cs ⇒ cs.asInstanceOf[AnnotationValue].annotation }
    }

    def getString(callSite: Annotation, label: String): String = { //@CallSite -> String
        val sv = callSite.elementValuePairs collectFirst {
            case ElementValuePair(`label`, StringValue(string)) ⇒ string
        }
        sv.getOrElse("")
    }

    def getLineNumber(callSite: Annotation): Int = { //@CallSite -> int
        val iv = callSite.elementValuePairs collectFirst {
            case ElementValuePair("line", IntValue(int)) ⇒ int
        }
        iv.getOrElse(-1)
    }

    def getBoolean(callSite: Annotation, label: String): Boolean = { //@CallSite -> boolean
        val bv = callSite.elementValuePairs collectFirst {
            case ElementValuePair(`label`, BooleanValue(bool)) ⇒ bool
        }
        bv.getOrElse(false)
    }

    def getType(annotation: Annotation, label: String): Type = { //@CallSite -> Type
        val cv = annotation.elementValuePairs collectFirst {
            case ElementValuePair(`label`, ClassValue(declaringType)) ⇒ declaringType
        }
        cv.getOrElse(VoidType)
    }

    def getReturnType(annotation: Annotation): Type = { //@CallSite -> Type
        getType(annotation, "returnType")
    }

    def getParameterList(callSite: Annotation): List[Type] = { //@CallSite -> Seq[FieldType]
        val av = callSite.elementValuePairs collectFirst {
            case ElementValuePair("parameterTypes", ArrayValue(ab)) ⇒
                ab.toIndexedSeq.map(ev ⇒
                    ev.asInstanceOf[ClassValue].value)
        }
        av.getOrElse(List()).toList
    }
}
