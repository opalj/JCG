import java.io.File
import java.io.FileInputStream

import org.opalj.br
import org.opalj.br.Annotation
import org.opalj.br.AnnotationValue
import org.opalj.br.ArrayValue
import org.opalj.br.ClassValue
import org.opalj.br.ElementValuePair
import org.opalj.br.IntValue
import org.opalj.br.ObjectType
import org.opalj.br.StringValue
import org.opalj.br.Type
import org.opalj.br.VoidType
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.SomeProject
import org.opalj.log.GlobalLogContext
import org.opalj.log.LogContext
import org.opalj.log.LogMessage
import org.opalj.log.OPALLogger
import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import play.api.libs.json.Reads

case class CallSites(callSites: Set[CallSite])

case class CallSite(declaredTarget: Method, line: Int, method: Method, targets: Set[Method])

case class Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String])

class DevNullLogger extends OPALLogger {
    override def log(message: LogMessage)(implicit ctx: LogContext): Unit = {}
}

object CGMatcher {

    val callSiteAnnotationType = ObjectType("lib/annotations/callgraph/CallSite")
    val callSitesAnnotationType = ObjectType("lib/annotations/callgraph/CallSites")
    val indirectCallAnnotationType = ObjectType("lib/annotations/callgraph/IndirectCall")
    val indirectCallsAnnotationType = ObjectType("lib/annotations/callgraph/IndirectCalls")

    def matchCallSites(tgtJar: String, jsonPath: String, verbose: Boolean = false): Assessment = {
        OPALLogger.updateLogger(GlobalLogContext, new DevNullLogger())
        implicit val p: SomeProject = Project(new File(tgtJar), org.opalj.bytecode.RTJar)

        val json = Json.parse(new FileInputStream(new File(jsonPath)))
        implicit val methodReads: Reads[Method] = Json.reads[Method]
        implicit val callSiteReads: Reads[CallSite] = Json.reads[CallSite]
        implicit val callSitesReads: Reads[CallSites] = Json.reads[CallSites]
        val jsResult = json.validate[CallSites]
        jsResult match {
            case _: JsSuccess[CallSites] ⇒
                val computedCallSites = jsResult.get
                for (clazz ← p.allProjectClassFiles) {
                    for ((method, _) ← clazz.methodsWithBody) {
                        // check if the call site might not be ambiguous
                        if (method.annotations.exists { a ⇒
                            a.annotationType == callSiteAnnotationType ||
                                a.annotationType == callSitesAnnotationType ||
                                a.annotationType == indirectCallAnnotationType ||
                                a.annotationType == indirectCallsAnnotationType
                        }) {
                            val body = method.body.get
                            val invokations = body.instructions.zipWithIndex.filter { case (instr, pc) ⇒ instr != null && instr.isInvocationInstruction }
                            val lines = invokations.map {
                                case (instr, pc) ⇒
                                    (body.lineNumber(pc), instr.asInvocationInstruction.name)
                            }.toSet
                            if (lines.size != invokations.length) {
                                throw new RuntimeException(s"Multiple call sites with same name in the same line $tgtJar, ${method.name}")
                            }
                        }

                        for (annotation ← method.annotations) {

                            val callSiteAnnotations =
                                if (annotation.annotationType == callSiteAnnotationType)
                                    Seq(annotation)
                                else if (annotation.annotationType == callSitesAnnotationType)
                                    getAnnotations(annotation, "value")
                                else
                                    Seq.empty

                            val csAssessment = handleCallSiteAnnotations(
                                computedCallSites.callSites,
                                method,
                                callSiteAnnotations,
                                verbose)

                            if(csAssessment.isUnsound){
                                return Unsound;
                            }

                            val indirectCallAnnotations =
                                if (annotation.annotationType == indirectCallAnnotationType)
                                    Seq(annotation)
                                else if (annotation.annotationType == indirectCallsAnnotationType)
                                    getAnnotations(annotation, "value")
                                else
                                    Seq.empty

                            val icsAssessment = handleIndirectCallAnnotations(
                                computedCallSites.callSites,
                                method,
                                indirectCallAnnotations,
                                verbose
                            )

                            val finalAssessment = csAssessment.combine(icsAssessment)

                            if(!finalAssessment.isSound)
                                return finalAssessment
                        }
                    }
                }

                Sound
            case _ ⇒
                throw new RuntimeException("Unable to parse json")
        }
    }

    private def verifyCallSite(annotatedLineNumber: Int, src: br.Method, tgtName: String): Unit = {
        val body = src.body.get
        val existsInstruction = body.instructions.zipWithIndex.exists {
            case (null, _) ⇒ false
            case (instr, pc) if instr.isInvocationInstruction ⇒
                val lineNumber = body.lineNumber(pc)
                // todo parameter types
                lineNumber.isDefined && annotatedLineNumber == lineNumber.get && tgtName == instr.asInvocationInstruction.name
            case _ ⇒ false
        }
        if (!existsInstruction)
            throw new RuntimeException(s"There is no call to $tgtName in line $annotatedLineNumber")
    }

    private def handleCallSiteAnnotations(
        computedCallSites:   Set[CallSite],
        method:              br.Method,
        callSiteAnnotations: Seq[Annotation],
        verbose:             Boolean
    )(implicit p: SomeProject): Assessment = {
        for (callSiteAnnotation ← callSiteAnnotations) {
            val line = getLineNumber(callSiteAnnotation)
            val name = getName(callSiteAnnotation)
            val returnType = getType(callSiteAnnotation, "returnType")
            val parameterTypes = getParameterList(callSiteAnnotation)
            verifyCallSite(line, method, name)
            val annotatedMethod = convertMethod(method)

            computedCallSites.find { cs ⇒
                cs.line == line && cs.method == annotatedMethod && cs.declaredTarget.name == name
            } match {
                case Some(computedCallSite) ⇒

                    val computedTargets = computedCallSite.targets.map(_.declaringClass)

                    for (annotatedTgt ← getResolvedTargets(callSiteAnnotation)) {
                        if (!computedTargets.contains(annotatedTgt)) {
                            if (verbose) println(s"$line:${annotatedMethod.declaringClass}#${annotatedMethod.name}:\t there is no call to $annotatedTgt#$name")
                            return Unsound;
                        } else {
                            if (verbose) println("found it")
                        }
                    }

                    for (prohibitedTgt ← getProhibitedTargets(callSiteAnnotation)) {
                        if (computedTargets.contains(prohibitedTgt)) {
                            if (verbose) println(s"$line:${annotatedMethod.declaringClass}#${annotatedMethod.name}:\t there is a call to prohibited target $prohibitedTgt#$name")
                            return Imprecise;
                        } else {
                            if (verbose) println("no call to prohibited")
                        }
                    }
                case _ ⇒
                    //throw new RuntimeException(s"$line:${annotatedMethod.declaringClass}#${annotatedMethod.name}:\t there is no callsite to method $name")
                    return Unsound
            }
        }

        Sound
    }

    private def verifyCallExistance(annotatedLineNumber: Int, method: br.Method): Unit = {
        val body = method.body.get
        val existsCall = body.instructions.zipWithIndex.exists {
            case (instr, pc) ⇒
                val lineNumber = body.lineNumber(pc)
                instr != null && instr.isInvocationInstruction && lineNumber.isDefined && lineNumber.get == annotatedLineNumber
        }
//        if (!existsCall)
//            System.err.println(s"There is no call in line $annotatedLineNumber")
    }

    private def handleIndirectCallAnnotations(
        computedCallSites:       Set[CallSite],
        source:                  br.Method,
        indirectCallAnnotations: Seq[Annotation],
        verbose:                 Boolean
    )(implicit p: SomeProject): Assessment= {
        for (annotation ← indirectCallAnnotations) {
            val line = getLineNumber(annotation)
            verifyCallExistance(line, source)
            val name = getName(annotation)
            val returnType = getReturnType(annotation).toJVMTypeName
            val parameterTypes = getParameterList(annotation).map(_.toJVMTypeName)
            for (declaringClass ← getResolvedTargets(annotation)) {
                val annotatedTarget = Method(name, declaringClass, returnType, parameterTypes)
                val annotatedSource = convertMethod(source)
                if (!callsIndirectly(computedCallSites, annotatedSource, annotatedTarget, verbose))
                    return Unsound;
            }
        }
        Sound
    }

    private def callsIndirectly(
        computedCallSites: Set[CallSite],
        source:            Method,
        annotatedTarget:   Method,
        verbose:           Boolean
    ): Boolean = {
        var visited: Set[Method] = Set(source)
        var workset: Set[Method] = Set(source)

        while (workset.nonEmpty) {
            val currentSource = workset.head
            workset = workset.tail

            for (tgt ← computedCallSites.filter(_.method == currentSource).flatMap(_.targets)) {
                if (tgt == annotatedTarget) {
                    if (verbose) println(s"Found transitive call $source -> $annotatedTarget")
                    return true;
                }

                if (!visited.contains(tgt)) {
                    visited += tgt
                    workset += tgt
                }
            }
        }

        if (verbose) println(s"Missed transitive call $source -> $annotatedTarget")

        false
    }

    def main(args: Array[String]): Unit = {
        matchCallSites(args(0), args(1), verbose = true)
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

    def getName(callSite: Annotation): String = { //@CallSite -> String
        val sv = callSite.elementValuePairs collectFirst {
            case ElementValuePair("name", StringValue(string)) ⇒ string
        }
        sv.get
    }

    def getLineNumber(callSite: Annotation): Int = { //@CallSite -> int
        val iv = callSite.elementValuePairs collectFirst {
            case ElementValuePair("line", IntValue(int)) ⇒ int
        }
        iv.getOrElse(-1)
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

    def getResolvedTargets(annotation: Annotation)(implicit p: SomeProject): List[String] = {
        val av = annotation.elementValuePairs collectFirst {
            case ElementValuePair("resolvedTargets", ArrayValue(ab)) ⇒
                ab.toIndexedSeq.map(_.asInstanceOf[StringValue].value)
        }
        val callTargets = av.getOrElse(List()).toList
        checkJVMTypeString(callTargets)
        callTargets
    }

    def getProhibitedTargets(annotation: Annotation)(implicit p: SomeProject): List[String] = {
        val av = annotation.elementValuePairs collectFirst {
            case ElementValuePair("prohibitedTargets", ArrayValue(ab)) ⇒
                ab.toIndexedSeq.map(_.asInstanceOf[StringValue].value)
        }
        val callTargets = av.getOrElse(List()).toList
        checkJVMTypeString(callTargets)
        callTargets
    }

    def checkJVMTypeString(callTargets: List[String])(implicit p: SomeProject): Unit = {
        if (!callTargets.forall { ct ⇒
            val re = "L([^;]*);".r
            re findFirstMatchIn ct match {
                case Some(m) ⇒ p.classHierarchy.isKnown(ObjectType(m.group(1)))
                case None    ⇒ false
            }
        }) {
            throw new RuntimeException("Call targets must be given in JVM notation and the type must exist")
        }
    }
}
