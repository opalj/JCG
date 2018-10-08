package tmr

import java.nio.file.Files
import java.nio.file.Paths

import org.opalj.ba._
import org.opalj.br._
import org.opalj.br.instructions._
import org.opalj.collection.immutable.RefArray;

/**
 *
 * @author Dominik Helm
 */
object EngineerBytecode {

    def main(args: Array[String]): Unit = {

        val demo = generateDemoClass()

        val file_demo = "out/test/tmr/Demo.class"

        writeClassFile(demo, file_demo)
    }

    private def writeClassFile(demo: CLASS[_], filename: String): Unit = {
        val (daClassFile, _) = demo.toDA()
        val rawClassFile: Array[Byte] = org.opalj.bc.Assembler(daClassFile)
        val filePath = Paths.get(filename)
        Files.write(filePath, rawClassFile)
    }

    private def generateDemoClass(): CLASS[_] = {
        CLASS(
            accessModifiers = PUBLIC SUPER,
            thisType = "tmr/Demo",
            methods = METHODS(
                METHOD(PUBLIC, "<init>", "()V", CODE(
                    ALOAD_0,
                    INVOKESPECIAL("java/lang/Object", false, "<init>", "()V"),
                    RETURN
                )),
                METHOD(PUBLIC STATIC, "main", "([Ljava/lang/String;)V",
                    CODE(
                        GETSTATIC("java/lang/System", "out", "Ljava/io/PrintStream;"),
                        LoadMethodHandle(
                            InvokeStaticMethodHandle(
                                ObjectType("tmr/Demo"),
                                isInterface = false,
                                "target",
                                MethodDescriptor.JustReturnsInteger
                            )
                        ),
                        LINENUMBER(16),
                        INVOKEVIRTUAL(
                            "java/lang/invoke/MethodHandle",
                            "invokeExact",
                            "()I"
                        ),
                        LINENUMBER(17),
                        INVOKEVIRTUAL("java/io/PrintStream", "println", "(I)V"),
                        RETURN
                    ),
                    RefArray[MethodAttributeBuilder](
                        RuntimeVisibleAnnotationTable(RefArray(
                            Annotation(
                                FieldType("Llib/annotations/callgraph/DirectCall;"),
                                ElementValuePairs(
                                    ElementValuePair("name", StringValue("target")),
                                    ElementValuePair("line", IntValue(16)),
                                    ElementValuePair("returnType", ClassValue(IntegerType)),
                                    ElementValuePair("resolvedTarget", ArrayValue(
                                        RefArray(StringValue("Ltmr/Demo;"))
                                    ))
                                )
                            )
                        )),
                        ExceptionTable(RefArray(ObjectType.Throwable))
                    )),
                METHOD(PUBLIC STATIC, "target", "()I", CODE(
                    ICONST_3,
                    IRETURN
                ))
            )
        )
    }
}
