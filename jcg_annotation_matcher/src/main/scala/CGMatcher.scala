import java.io.File
import java.io.FileInputStream

import org.opalj.br
import org.opalj.br.Annotation
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
            if AnnotationHelper.isAnnotatedMethod(method)
        } {
            // check if the call site might not be ambiguous
            AnnotationVerifier.verifyNoAmbiguousCalls(method)

            val annotatedMethod = convertMethod(method)

            for (annotation ← method.annotations) {

                val directCallAnnotations = AnnotationHelper.directCallAnnotations(annotation)

                val csAssessment = handleDirectCallAnnotations(
                    computedReachableMethods.getOrElse(annotatedMethod, Set.empty),
                    annotatedMethod,
                    method,
                    directCallAnnotations,
                    verbose
                )

                if (csAssessment.isUnsound) {
                    return Unsound;
                }

                val indirectCallAnnotations = AnnotationHelper.indirectCallAnnotations(annotation)

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
            // here we identify call sites only by name and line number, not regarding types
            AnnotationVerifier.verifyDirectCallAnnotation(annotation, method)

            val line = AnnotationHelper.getLineNumber(annotation)
            val name = AnnotationHelper.getName(annotation)

            computedCallSites.find { cs ⇒
                cs.line == line && cs.declaredTarget.name == name
            } match {
                case Some(computedCallSite) ⇒

                    val computedTargets = computedCallSite.targets.map(_.declaringClass)

                    val resolvedTargets = AnnotationHelper.getResolvedTargets(annotation)
                    AnnotationVerifier.verifyJVMTypes(resolvedTargets)
                    for (annotatedTgt ← resolvedTargets) {
                        if (!computedTargets.contains(annotatedTgt)) {
                            if (verbose)
                                println(s"$line:${annotatedMethod.declaringClass}#${annotatedMethod.name}:\t there is no call to $annotatedTgt#$name")
                            return Unsound;
                        } else {
                            if (verbose) println("found it")
                        }
                    }

                    val prohibitedTargets = AnnotationHelper.getProhibitedTargets(annotation)
                    AnnotationVerifier.verifyJVMTypes(prohibitedTargets)
                    for (prohibitedTgt ← prohibitedTargets) {
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
            AnnotationVerifier.verifyCallExistence(annotation, source)

            val name = AnnotationHelper.getName(annotation)
            val returnType = AnnotationHelper.getReturnType(annotation).toJVMTypeName
            val parameterTypes = AnnotationHelper.getParameterList(annotation).map(_.toJVMTypeName)

            val resolvedTargets = AnnotationHelper.getResolvedTargets(annotation)
            AnnotationVerifier.verifyJVMTypes(resolvedTargets)
            for (declaringClass ← resolvedTargets) {
                val annotatedTarget = Method(name, declaringClass, returnType, parameterTypes)
                if (!callsIndirectly(reachableMethods, annotatedSource, annotatedTarget, verbose))
                    return Unsound;
            }

            val prohibitedTargets = AnnotationHelper.getProhibitedTargets(annotation)
            AnnotationVerifier.verifyJVMTypes(prohibitedTargets)
            for (prohibitedTgt ← prohibitedTargets) {
                val annotatedTarget = Method(name, prohibitedTgt, returnType, parameterTypes)
                if (callsIndirectly(reachableMethods, annotatedSource, annotatedTarget, verbose))
                    finalAssessment = finalAssessment.combine(Imprecise)
            }
        }

        finalAssessment
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
}
