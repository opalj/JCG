package nj

import java.nio.file.Files
import java.nio.file.Paths

import org.opalj.ba._
import org.opalj.br._
import org.opalj.br.instructions._
import org.opalj.collection.immutable.RefArray;

/**
 *
 * @author Michael Reif
 */
object EngineerBytecode {

    def main(args: Array[String]) : Unit = {

        val demo = generateDemoClass()
        val target = generateTargetClass()

        val file_demo = "out/test/nj/Demo.class"
        val file_target = "out/test/nj/Target.class"


        writeClassFile(demo, file_demo)
        writeClassFile(target, file_target)
    }

    private def writeClassFile(demo: CLASS[_], filename: String) : Unit = {
        val (daClassFile, _) = demo.toDA()
        val rawClassFile: Array[Byte] = org.opalj.bc.Assembler(daClassFile)
        val filePath = Paths.get(filename)
        Files.write(filePath, rawClassFile)
    }

    private def generateDemoClass() : CLASS[_] = {
        CLASS(
            accessModifiers = PUBLIC SUPER,
            thisType = "nj/Demo",
            methods = METHODS(
                METHOD(PUBLIC, "<init>", "()V", CODE(
                    ALOAD_0,
                    INVOKESPECIAL("java/lang/Object", false, "<init>", "()V"),
                    RETURN
                )),
                METHOD(PUBLIC STATIC, "main", "([Ljava/lang/String;)V",
                    CODE(
                        NEW("nj/Target"),
                        DUP,
                        INVOKESPECIAL("nj/Target", false, "<init>", "()V"),
                        ASTORE_1,
                        ALOAD_1,
                        LoadString("ResolvedToObject"),
                        LINENUMBER(5),
                        INVOKEVIRTUAL("nj/Target", "method", "(Ljava/lang/String;)Ljava/lang/Object;"),
                        POP,
                        ALOAD_1,
                        LoadString("ResolvedToString"),
                        LINENUMBER(6),
                        INVOKEVIRTUAL("nj/Target", "method", "(Ljava/lang/String;)Ljava/lang/String;"),
                        POP,
                        RETURN
                    ),
                    RefArray[MethodAttributeBuilder](
                        RuntimeVisibleAnnotationTable(RefArray(
                            Annotation(
                                FieldType("Llib/annotations/callgraph/DirectCalls;"),
                                ElementValuePair("value",
                                    ArrayValue(RefArray(
                                        AnnotationValue(Annotation(
                                            FieldType("Llib/annotations/callgraph/DirectCall;"),
                                            ElementValuePairs(
                                                ElementValuePair("name", StringValue("method")),
                                                ElementValuePair("line", IntValue(5)),
                                                ElementValuePair("returnType", ClassValue(ObjectType("java/lang/Object"))),
                                                ElementValuePair("resolvedTarget",
                                                    ArrayValue(RefArray(StringValue("Lnj/Target;"))))
                                            )
                                        )),
                                        AnnotationValue(Annotation(
                                            FieldType("Llib/annotations/callgraph/DirectCall;"),
                                            ElementValuePairs(
                                                ElementValuePair("name", StringValue("method")),
                                                ElementValuePair("line", IntValue(6)),
                                                ElementValuePair("returnType", ClassValue(ObjectType("java/lang/String"))),
                                                ElementValuePair("resolvedTarget",
                                                    ArrayValue(RefArray(StringValue("Lnj/Target;"))))
                                            )
                                        ))
                                    )))
                            )
                        )
                        )
                    )
                )))
    }

    //s Annotation(
    //        annotationType:    FieldType,
    //        elementValuePairs: ElementValuePairs = NoElementValuePairs

    private def generateTargetClass() : CLASS[_] = {
        CLASS(
            accessModifiers = PUBLIC SUPER,
            thisType = "nj/Target",
            methods = METHODS(
                METHOD(PUBLIC, "<init>", "()V", CODE(
                    ALOAD_0,
                    INVOKESPECIAL("java/lang/Object", false, "<init>", "()V"),
                    RETURN
                )),
                METHOD(PUBLIC, "method", "(Ljava/lang/String;)Ljava/lang/Object;", CODE(
                    GETSTATIC("java/lang/System", "out", "Ljava/io/PrintStream;"),
                    LoadString(" -- Object --"),
                    INVOKEVIRTUAL("java/io/PrintStream", "println", "(Ljava/lang/String;)V"),
                    GETSTATIC("java/lang/System", "out", "Ljava/io/PrintStream;"),
                    ALOAD_1,
                    INVOKEVIRTUAL("java/io/PrintStream", "println", "(Ljava/lang/String;)V"),
                    ALOAD_1,
                    ARETURN
                )),
                METHOD(PUBLIC, "method", "(Ljava/lang/String;)Ljava/lang/String;", CODE(
                    GETSTATIC("java/lang/System", "out", "Ljava/io/PrintStream;"),
                    LoadString(" -- String --"),
                    INVOKEVIRTUAL("java/io/PrintStream", "println", "(Ljava/lang/String;)V"),
                    GETSTATIC("java/lang/System", "out", "Ljava/io/PrintStream;"),
                    ALOAD_1,
                    INVOKEVIRTUAL("java/io/PrintStream", "println", "(Ljava/lang/String;)V"),
                    ALOAD_1,
                    ARETURN
                ))
            )
        )
    }
}
