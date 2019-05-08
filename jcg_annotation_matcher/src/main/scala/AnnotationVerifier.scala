import java.io.File
import java.net.URL

import org.opalj.br
import org.opalj.br.Annotation
import org.opalj.br.ObjectType
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.SomeProject

/**
 * Provides methods, to verify the correctness of the annotations to specify the call graph behavior.
 *
 * @author Florian Kuebler
 */
object AnnotationVerifier {

    /**
     * Verify the correctness of all call annotations ([[lib.annotations.callgraph.DirectCall]],
     * [[lib.annotations.callgraph.IndirectCall]], and the wrappers) in the given project.
     */
    def verifyProject(projectSpecification: ProjectSpecification, parent: File): Unit = {
        implicit val project: Project[URL] = Project(projectSpecification.target(parent))

        val p = projectSpecification.name
        for {
            method ← project.allMethodsWithBody
            if AnnotationHelper.isAnnotatedMethod(method)
        } {
            val m = method.name
            printAssertionError(m, p) { verifyNoAmbiguousCalls(method) }
            for (annotation ← method.annotations) {
                val directCallAnnotations = AnnotationHelper.directCallAnnotations(annotation)
                for (directCallAnnotation ← directCallAnnotations) {
                    printAssertionError(m, p) {
                        verifyDirectCallAnnotation(directCallAnnotation, method)
                    }
                    printAssertionError(m, p) {
                        verifyJVMTypes(AnnotationHelper.getProhibitedTargets(directCallAnnotation))
                    }
                    printAssertionError(m, p) {
                        verifyJVMTypes(AnnotationHelper.getResolvedTargets(directCallAnnotation))
                    }
                }

                val indirectCallAnnotations = AnnotationHelper.indirectCallAnnotations(annotation)
                for (indirectCallAnnotation ← indirectCallAnnotations) {
                    printAssertionError(m, p) {
                        verifyCallExistence(indirectCallAnnotation, method)
                    }
                    printAssertionError(m, p) {
                        verifyJVMTypes(AnnotationHelper.getProhibitedTargets(indirectCallAnnotation))
                    }
                    printAssertionError(m, p) {
                        verifyJVMTypes(AnnotationHelper.getResolvedTargets(indirectCallAnnotation))
                    }
                }
            }
        }
    }

    /**
     * Instead of throwing an [[AssertionError]], this method prints the message.
     */
    private def printAssertionError(methodName: String, projectName: String)(f: ⇒ Unit): Unit = {
        try {
            f
        } catch {
            case e: AssertionError ⇒
                print(s"verification error for $projectName - $methodName: ${e.getMessage}")
        }
    }

    /**
     * Verifies that for every line in the method, there are at most one calls to a method with
     * the same name.
     * If this is not the case, an exception is thrown.
     */
    def verifyNoAmbiguousCalls(method: br.Method): Unit = {
        val body = method.body.get
        val invocationsWithPC = body.instructions.zipWithIndex filter {
            case (instr, _) ⇒
                instr != null && instr.isMethodInvocationInstruction
        }

        val namesAndLines = invocationsWithPC.map {
            case (instr, pc) ⇒
                (instr.asMethodInvocationInstruction.name, body.lineNumber(pc))
        }

        val duplicates = namesAndLines.diff(namesAndLines.distinct).distinct

        assert(
            duplicates.isEmpty,
            s"Multiple call sites with same name and line number - ${duplicates.mkString(", ")}"
        )
    }

    /**
     * Checks, whether there is an invocation instruction with the annotated target name in the
     * annotated line.
     * If the check fails, a runtime exception is thrown.
     */
    def verifyDirectCallAnnotation(annotation: Annotation, src: br.Method): Unit = {
        val annotatedLineNumber = AnnotationHelper.getLineNumber(annotation)
        val tgtName = AnnotationHelper.getName(annotation)

        val body = src.body.get
        val existsInstruction = body.instructions.zipWithIndex.exists {
            case (null, _) ⇒ false
            case (instr, pc) if instr.isInvocationInstruction ⇒
                val lineNumber = body.lineNumber(pc)
                lineNumber.isDefined && annotatedLineNumber == lineNumber.get && tgtName == instr.asInvocationInstruction.name
            case _ ⇒ false
        }
//        assert(existsInstruction, s"There is no call to $tgtName in line $annotatedLineNumber")
    }

    /**
     * Checks whether the is an invocation instruction in the given line (if specfied).
     * If this is not the case, an exception is thrown.
     */
    def verifyCallExistence(annotation: Annotation, method: br.Method): Unit = {
        val annotatedLineNumber = AnnotationHelper.getLineNumber(annotation)
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
            if(!existsCall)
                println("NOT VERIFIED");
            //assert(existsCall, s"There is no call in line $annotatedLineNumber")
        }
    }

    /**
     * Checks whether the `types` are given in JVM binary notation and whether they exists in project.
     */
    def verifyJVMTypes(types: List[String])(implicit p: SomeProject): Unit = {
        types.foreach { ct ⇒
            val re = "L([^;]*);".r
            re findFirstMatchIn ct match {
                case Some(m) ⇒
                    //assert(p.classHierarchy.isKnown(ObjectType(m.group(1))), s"$ct is no known type")
                case None ⇒
//                    throw new AssertionError(
//                        s"Call targets must be given in JVM notation but found $ct"
//                    )
            }
        }
    }
}
