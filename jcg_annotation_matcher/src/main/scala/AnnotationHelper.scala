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
import org.opalj.br.analyses.SomeProject

object AnnotationHelper {

    val DirectCallAnnotationType =
        ObjectType(classOf[DirectCall].getName.replace(".", "/"))
    val DirectCallsAnnotationType =
        ObjectType(classOf[DirectCalls].getName.replace(".", "/"))
    val IndirectCallAnnotationType =
        ObjectType(classOf[IndirectCall].getName.replace(".", "/"))
    val IndirectCallsAnnotationType =
        ObjectType(classOf[IndirectCalls].getName.replace(".", "/"))

    def directCallAnnotations(annotation: Annotation): Seq[Annotation] = {
        if (annotation.annotationType == AnnotationHelper.DirectCallAnnotationType)
            Seq(annotation)
        else if (annotation.annotationType == AnnotationHelper.DirectCallsAnnotationType)
            AnnotationHelper.getAnnotations(annotation, "value")
        else
            Seq.empty
    }

    def indirectCallAnnotations(annotation: Annotation): Seq[Annotation] = {
        if (annotation.annotationType == AnnotationHelper.IndirectCallAnnotationType)
            Seq(annotation)
        else if (annotation.annotationType == AnnotationHelper.IndirectCallsAnnotationType)
            AnnotationHelper.getAnnotations(annotation, "value")
        else
            Seq.empty
    }

    /**
     * Does this method has a call annotation?
     */
    def isAnnotatedMethod(method: br.Method): Boolean = {
        method.annotations.exists { a ⇒
            a.annotationType == DirectCallAnnotationType ||
                a.annotationType == DirectCallsAnnotationType ||
                a.annotationType == IndirectCallAnnotationType ||
                a.annotationType == IndirectCallsAnnotationType
        }
    }

    def getAnnotations(annotation: Annotation, label: String): Seq[Annotation] = { //@DirectCalls -> @DirectCall[]
        val avs = annotation.elementValuePairs collectFirst {
            case ElementValuePair(`label`, ArrayValue(array)) ⇒ array
        }
        avs.getOrElse(IndexedSeq.empty).map { cs ⇒ cs.asInstanceOf[AnnotationValue].annotation }
    }

    def getName(annotation: Annotation): String = { //@DirectCall -> String
        val sv = annotation.elementValuePairs collectFirst {
            case ElementValuePair("name", StringValue(string)) ⇒ string
        }
        sv.get
    }

    def getLineNumber(annotation: Annotation): Int = { //@DirectCall -> int
        val iv = annotation.elementValuePairs collectFirst {
            case ElementValuePair("line", IntValue(int)) ⇒ int
        }
        iv.getOrElse(-1)
    }

    def getType(annotation: Annotation, label: String): Type = { //@DirectCall -> Type
        val cv = annotation.elementValuePairs collectFirst {
            case ElementValuePair(`label`, ClassValue(declaringType)) ⇒ declaringType
        }
        cv.getOrElse(VoidType)
    }

    def getReturnType(annotation: Annotation): Type = { //@DirectCall -> Type
        getType(annotation, "returnType")
    }

    def getParameterList(annotation: Annotation): List[Type] = { //@DirectCall -> Seq[FieldType]
        val av = annotation.elementValuePairs collectFirst {
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

        av.getOrElse(List()).toList
    }

    def getProhibitedTargets(annotation: Annotation)(implicit p: SomeProject): List[String] = {
        val av = annotation.elementValuePairs collectFirst {
            case ElementValuePair("prohibitedTargets", ArrayValue(ab)) ⇒
                ab.toIndexedSeq.map(_.asInstanceOf[StringValue].value)
        }

        av.getOrElse(List()).toList
    }
}
