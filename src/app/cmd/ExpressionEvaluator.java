package cmd;

import static java.lang.Integer.parseInt;

import annotations.callgraph.CallSite;
import annotations.callgraph.InvokedConstructor;
import annotations.callgraph.ResolvedMethod;
import annotations.documentation.CGNote;
import annotations.properties.EntryPoint;
import static annotations.callgraph.AnalysisMode.*;
import expressions.*;

import java.util.Arrays;

import static expressions.BinaryExpression.createBinaryExpression;
import static expressions.PlusOperator.AddExpression;

/**
 * This class defines an application use case of the expression library and has some well defined properties
 * wrt. call graph construction. It covers ( inlc. the library) serveral Java language features to test whether
 * a given call graph implementation can handle these features.
 *
 * <p>
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 * <p>
 * <!--
 * <p>
 * <p>
 * <p>
 * <p>
 *
 *
 *
 *
 *
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 *
 *
 *
 *
 *
 *
 *
 * <p>
 * <p>
 * <p>
 * <p>
 * -->
 *
 * @author Michael Eichberg
 * @author Micahel Reif
 */
public class ExpressionEvaluator {

    private static final Map<String,Constant> NO_VARIABLES = (Map<String,Constant>)Map.EMPTY;

    // 2 34 + 23 Plus == 59
    // 2 3 + 5 Plus 2 fancy_expressions.MultOperator
    // 2 3 + 5 Plus 2 fancy_expressions.MultOperator Crash
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    @InvokedConstructor(receiverType = "expressions/Stack", line = 109)
    @CallSite(name = "clone", resolvedMethods = {@ResolvedMethod(receiverType = "java.lang.String")}, line = 74)
    @CallSite(name = "push", resolvedMethods = {@ResolvedMethod(receiverType = "expressions.Stack")}, line = 117)
    @CallSite(name = "createBinaryExpression",
            resolvedMethods = {@ResolvedMethod( receiverType = BinaryExpression.fqn)},
            parameterTypes = {String.class, Expression.class, Expression.class},
            line = 120)
    public static void main(final String[] args) {

        @CGNote(value = "[ARR_HANLDE]", description = "") // TODO Why is this special?
        String[] expressions = args.clone();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @CGNote(value = "[INV_CALLBACK]", description="invisible callback because no native code is involved; the call graph seems to be complete")
            @CGNote(value="[NOTE]",description="the related method <Thread>.dispatchUncaughtException is not dead")
            @Override public void uncaughtException(Thread t, Throwable e) {
                String msg = "unexpected error while processing "+ Arrays.deepToString(args);
                System.out.println(msg);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(){

            // This is an entry point!
            @CGNote(value = "[INV_CALLBACK]", description="invisible callback because no native code is involved; the call graph seems to be complete")
            @CGNote(value = "[NOTE]", description="the related method<Thread>.run is called by the jvm")
            @Override public void run() {
                System.out.println("It was a pleasure to evaluate your expression!");
                super.run();
            }
        });

        synchronized (ExpressionEvaluator.class) {
            // all methods of the class object of ExpressionEvaluation may be called...
            // unless we analyze the "JVM" internal implementation
            @CGNote(value ="[POT_CALLBACK]", description = "potential callback because native code is involved; the call graph seems to be complete")
            @CGNote(value = "[NOTE]", description = "the native code may call any of the methods declared at the class object of ExpressionEvaluation")
            boolean holdsLock = !Thread.holdsLock(ExpressionEvaluator.class);
            if (holdsLock) throw new UnknownError();

        if(args.length == 0) {
            throw new IllegalArgumentException("no expression");
        }

        Stack<Constant> values = new Stack();

        for (String expr: expressions) {
            try {
                values.push(new Constant(parseInt(expr)));
            } catch (NumberFormatException nfe) {
                // so it is not a number...
                if(expr.equals( "+")){
                    values.push(new AddExpression(values.pop(),values.pop()).eval(NO_VARIABLES));
                } else {
                    try {
                        values.push(createBinaryExpression(expr, values.pop(), values.pop()).eval(NO_VARIABLES));
                    } catch (Throwable t){
                        throw new IllegalArgumentException("unsupported symbol "+expr,t);
                    }
                }
            } finally {
                System.out.println("processed the symbol "+expr);
            }
        }

        if(values.size() > 1) {
            throw new IllegalArgumentException("the expression is not valid missing operator");
        }

        System.out.println("result "+values.pop().getValue());
        }
    }
}
