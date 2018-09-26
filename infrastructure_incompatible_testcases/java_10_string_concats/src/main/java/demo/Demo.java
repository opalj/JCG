package demo;

import lib.annotations.callgraph.IndirectCall;

/**
 * This class pertains to a JCG lambda test case with respect to Java 10 String concatenations.
 * When String concatenation is used within Java 10, the constants are not longer combined using
 * StringBuilder.append() but rather by using a invokedynamic-based concatenation function.
 *
 * The respective ID of the test case is **Lambda5**.
 *
 * @author Michael Reif
 */
public class Demo {

    @IndirectCall(
            line = 21, name = "makeConcatWithConstants",
            resolvedTargets = "Ljava/lang/invoke/StringConcatFactory;")
    public static void main(String[] args) {
        String s1 = "Java 10";
        String s2 = s1 + " String Concat";
    }
}