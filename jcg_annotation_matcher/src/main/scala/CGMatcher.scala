import java.io.File
import java.io.FileInputStream

import lib.annotations.callgraph.DirectCall
import lib.annotations.callgraph.DirectCalls
import lib.annotations.callgraph.IndirectCall
import lib.annotations.callgraph.IndirectCalls
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
import org.opalj.log.OPALLogger
import play.api.libs.json.Json

/**
 * For a given project and a computed (serialized as json representation of [[ReachableMethods]])
 * it computes an [[Assessment]] whether the computed call graph is Sound/Unsound or Imprecise.
 *
 * @author Florian Kuebler
 */
object CGMatcher {

    private val directCallAnnotationType =
        ObjectType(classOf[DirectCall].getName.replace(".", "/"))
    private val directCallsAnnotationType =
        ObjectType(classOf[DirectCalls].getName.replace(".", "/"))
    private val indirectCallAnnotationType =
        ObjectType(classOf[IndirectCall].getName.replace(".", "/"))
    private val indirectCallsAnnotationType =
        ObjectType(classOf[IndirectCalls].getName.replace(".", "/"))

    /**
     * Computes whether computed call graph (represented as a json file of [[ReachableMethods]])
     * is sound/unsound/imprecise with regards to the annotations in the specified target project.
     *
     * @param parent in case any specified location is a relative path, the `parent` will be used
     *               as root.
     */
    def matchCallSites(
        projectSpec:         ProjectSpecification,
        JREPath:             String,
        parent:              File,
        serializedCallGraph: File,
        verbose:             Boolean              = false
    ): Assessment = {
        if (!verbose)
            OPALLogger.updateLogger(GlobalLogContext, new DevNullLogger())

        val jreFiles = JRELocation.getAllJREJars(JREPath)
        implicit val p: SomeProject = Project(
            Array(projectSpec.target(parent)) ++ projectSpec.allClassPathEntryFiles(parent) ++ jreFiles,
            Array.empty[File]
        )

        if(!serializedCallGraph.exists()){
            return Error;
        }

        val computedReachableMethods =
            Json.parse(new FileInputStream(serializedCallGraph)).validate[ReachableMethods].get.toMap

        for {
            clazz ← p.allProjectClassFiles
            method ← clazz.methodsWithBody
            if isAnnotatedMethod(method)
        } {
            // check if the call site might not be ambiguous
            checkNoAmbiguousCalls(method, projectSpec.name)

            val annotatedMethod = convertMethod(method)

            for (annotation ← method.annotations) {

                val callSiteAnnotations =
                    if (annotation.annotationType == directCallAnnotationType)
                        Seq(annotation)
                    else if (annotation.annotationType == directCallsAnnotationType)
                        getAnnotations(annotation, "value")
                    else
                        Seq.empty

                val csAssessment = handleDirectCallAnnotations(
                    computedReachableMethods.getOrElse(annotatedMethod, Set.empty),
                    annotatedMethod,
                    method,
                    callSiteAnnotations,
                    verbose
                )

                if (csAssessment.isUnsound) {
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
                    computedReachableMethods,
                    method,
                    indirectCallAnnotations,
                    verbose
                )

                val finalAssessment = csAssessment.combine(icsAssessment)

                if (!finalAssessment.isSound)
                    return finalAssessment;
            }

        }

        Sound
    }

    /**
     * Checks whether the annotated direct calls are present in the computed call graph and
     * whether the prohibit call targets are not present in the computed call graph.
     */
    private def handleDirectCallAnnotations(
        computedCallSites:     Set[CallSite],
        annotatedMethod:       Method,
        method:                br.Method,
        directCallAnnotations: Seq[Annotation],
        verbose:               Boolean
    )(implicit p: SomeProject): Assessment = {
        var finalAssessment: Assessment = Sound
        for (annotation ← directCallAnnotations) {
            val line = getLineNumber(annotation)
            val name = getName(annotation)

            // here we identify call sites only by name and line number, not regarding types
            verifyCallSite(line, method, name)
            computedCallSites.find { cs ⇒
                cs.line == line && cs.declaredTarget.name == name
            } match {
                case Some(computedCallSite) ⇒

                    val computedTargets = computedCallSite.targets.map(_.declaringClass)

                    for (annotatedTgt ← getResolvedTargets(annotation)) {
                        if (!computedTargets.contains(annotatedTgt)) {
                            if (verbose)
                                println(s"$line:${annotatedMethod.declaringClass}#${annotatedMethod.name}:\t there is no call to $annotatedTgt#$name")
                            return Unsound;
                        } else {
                            if (verbose) println("found it")
                        }
                    }

                    for (prohibitedTgt ← getProhibitedTargets(annotation)) {
                        if (computedTargets.contains(prohibitedTgt)) {
                            if (verbose)
                                println(s"$line:${annotatedMethod.declaringClass}#${annotatedMethod.name}:\t there is a call to prohibited target $prohibitedTgt#$name")
                            finalAssessment = finalAssessment.combine(Imprecise)
                        } else {
                            if (verbose) println("no call to prohibited")
                        }
                    }
                case _ ⇒
                    // there is no matching call site in the computed call graph
                    return Unsound;
            }
        }

        finalAssessment
    }

    /**
     * Checks whether the annotated indirect calls are present in the computed call graph and
     * whether the prohibit call targets are not present in the computed call graph.
     */
    private def handleIndirectCallAnnotations(
        reachableMethods:        Map[Method, Set[CallSite]],
        source:                  br.Method,
        indirectCallAnnotations: Seq[Annotation],
        verbose:                 Boolean
    )(implicit p: SomeProject): Assessment = {
        val annotatedSource = convertMethod(source)
        var finalAssessment: Assessment = Sound
        for (annotation ← indirectCallAnnotations) {
            val line = getLineNumber(annotation)
            verifyCallExistence(line, source)
            val name = getName(annotation)
            val returnType = getReturnType(annotation).toJVMTypeName
            val parameterTypes = getParameterList(annotation).map(_.toJVMTypeName)
            for (declaringClass ← getResolvedTargets(annotation)) {
                val annotatedTarget = Method(name, declaringClass, returnType, parameterTypes)
                if (!callsIndirectly(reachableMethods, annotatedSource, annotatedTarget, verbose))
                    return Unsound;
            }

            for (prohibitedTgt ← getProhibitedTargets(annotation)) {
                val annotatedTarget = Method(name, prohibitedTgt, returnType, parameterTypes)
                if (callsIndirectly(reachableMethods, annotatedSource, annotatedTarget, verbose))
                    finalAssessment = finalAssessment.combine(Imprecise)
            }
        }

        finalAssessment
    }

    /**
     * Does this method has a call annotation?
     */
    private def isAnnotatedMethod(method: br.Method): Boolean = {
        method.annotations.exists { a ⇒
            a.annotationType == directCallAnnotationType ||
                a.annotationType == directCallsAnnotationType ||
                a.annotationType == indirectCallAnnotationType ||
                a.annotationType == indirectCallsAnnotationType
        }
    }

    /**
     * Verifies that for every line in the method, there are at most one calls to a method with
     * the same name.
     * If this is not the case, an exception is thrown.
     */
    private def checkNoAmbiguousCalls(method: br.Method, projectName: String): Unit = {
        val body = method.body.get
        val invocations = body.instructions.zipWithIndex filter {
            case (instr, _) ⇒
                instr != null && instr.isInvocationInstruction
        }
        val lines = invocations.map {
            case (instr, pc) ⇒
                (body.lineNumber(pc), instr.asInvocationInstruction.name)
        }.toSet
        if (lines.size != invocations.length) {
            throw new RuntimeException(s"Multiple call sites with same name in the same line $projectName, ${method.name}")
        }
    }

    /**
     * Checks, whether there is an invocation instruction with the annotated target name in the
     * annotated line.
     * If the check fails, a runtime exception is thrown.
     */
    private def verifyCallSite(annotatedLineNumber: Int, src: br.Method, tgtName: String): Unit = {
        val body = src.body.get
        val existsInstruction = body.instructions.zipWithIndex.exists {
            case (null, _) ⇒ false
            case (instr, pc) if instr.isInvocationInstruction ⇒
                val lineNumber = body.lineNumber(pc)
                lineNumber.isDefined && annotatedLineNumber == lineNumber.get && tgtName == instr.asInvocationInstruction.name
            case _ ⇒ false
        }
        if (!existsInstruction)
            throw new RuntimeException(s"There is no call to $tgtName in line $annotatedLineNumber")
    }

    /**
     * Checks whether the is an invocation instruction in the given line (if specfied).
     * If this is not the case, an exception is thrown.
     */
    private def verifyCallExistence(annotatedLineNumber: Int, method: br.Method): Unit = {
        if (annotatedLineNumber != -1) {
            val body = method.body.get
            val existsCall = body.instructions.zipWithIndex.exists {
                case (instr, pc) ⇒
                    val lineNumber = body.lineNumber(pc)
                    instr != null &&
                        instr.isInvocationInstruction &&
                        lineNumber.isDefined &&
                        lineNumber.get == annotatedLineNumber
            }
            if (!existsCall) {
                throw new RuntimeException(s"There is no call in line $annotatedLineNumber")
            }
        }
    }

    /**
     * Is there a path in the call graph from the `source` to the `annotatedTarget`?
     */
    private def callsIndirectly(
        reachableMethods: Map[Method, Set[CallSite]],
        source:           Method,
        annotatedTarget:  Method,
        verbose:          Boolean
    ): Boolean = {
        var visited: Set[Method] = Set(source)
        var workset: Set[Method] = Set(source)

        while (workset.nonEmpty) {
            val currentSource = workset.head
            workset = workset.tail

            val computedCallSites = reachableMethods.getOrElse(currentSource, Set.empty)

            for (tgt ← computedCallSites.flatMap(_.targets)) {
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

    //
    // UTILITY FUNCTIONS
    //

    private def convertMethod(method: org.opalj.br.Method): Method = {
        val name = method.name
        val declaringClass = method.classFile.thisType.toJVMTypeName
        val returnType = method.returnType.toJVMTypeName
        val parameterTypes = method.parameterTypes.toList.map(_.toJVMTypeName)

        Method(name, declaringClass, returnType, parameterTypes)
    }

    private def getAnnotations(callSites: Annotation, label: String): Seq[Annotation] = { //@DirectCalls -> @DirectCall[]
        val avs = callSites.elementValuePairs collectFirst {
            case ElementValuePair(`label`, ArrayValue(array)) ⇒ array
        }
        avs.getOrElse(IndexedSeq.empty).map { cs ⇒ cs.asInstanceOf[AnnotationValue].annotation }
    }

    private def getName(callSite: Annotation): String = { //@DirectCall -> String
        val sv = callSite.elementValuePairs collectFirst {
            case ElementValuePair("name", StringValue(string)) ⇒ string
        }
        sv.get
    }

    private def getLineNumber(callSite: Annotation): Int = { //@DirectCall -> int
        val iv = callSite.elementValuePairs collectFirst {
            case ElementValuePair("line", IntValue(int)) ⇒ int
        }
        iv.getOrElse(-1)
    }

    private def getType(annotation: Annotation, label: String): Type = { //@DirectCall -> Type
        val cv = annotation.elementValuePairs collectFirst {
            case ElementValuePair(`label`, ClassValue(declaringType)) ⇒ declaringType
        }
        cv.getOrElse(VoidType)
    }

    private def getReturnType(annotation: Annotation): Type = { //@DirectCall -> Type
        getType(annotation, "returnType")
    }

    private def getParameterList(callSite: Annotation): List[Type] = { //@DirectCall -> Seq[FieldType]
        val av = callSite.elementValuePairs collectFirst {
            case ElementValuePair("parameterTypes", ArrayValue(ab)) ⇒
                ab.toIndexedSeq.map(ev ⇒
                    ev.asInstanceOf[ClassValue].value)
        }
        av.getOrElse(List()).toList
    }

    private def getResolvedTargets(annotation: Annotation)(implicit p: SomeProject): List[String] = {
        val av = annotation.elementValuePairs collectFirst {
            case ElementValuePair("resolvedTargets", ArrayValue(ab)) ⇒
                ab.toIndexedSeq.map(_.asInstanceOf[StringValue].value)
        }
        val callTargets = av.getOrElse(List()).toList
        checkJVMTypeString(callTargets)
        callTargets
    }

    private def getProhibitedTargets(annotation: Annotation)(implicit p: SomeProject): List[String] = {
        val av = annotation.elementValuePairs collectFirst {
            case ElementValuePair("prohibitedTargets", ArrayValue(ab)) ⇒
                ab.toIndexedSeq.map(_.asInstanceOf[StringValue].value)
        }
        val callTargets = av.getOrElse(List()).toList
        checkJVMTypeString(callTargets)
        callTargets
    }

    private def checkJVMTypeString(callTargets: List[String])(implicit p: SomeProject): Unit = {
        if (!callTargets.forall { ct ⇒
            val re = "L([^;]*);".r
            re findFirstMatchIn ct match {
                case Some(m) ⇒ p.classHierarchy.isKnown(ObjectType(m.group(1)))
                case None    ⇒ false
            }
        }) {
            if(!callTargets.exists(_.startsWith("Llib/IntComp")))
                throw new RuntimeException("Call targets must be given in JVM notation and the type must exist")
        }
    }
}
