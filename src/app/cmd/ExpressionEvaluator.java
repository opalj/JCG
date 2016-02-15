/*
 * BSD 2-Clause License:
 * Copyright (c) 2009 - 2016
 * Software Technology Group
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package cmd;

import static annotations.documentation.CGCategory.*;
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
    @InvokedConstructor(receiverType = "expressions/Stack", line = 140)
    @CallSite(name = "clone", resolvedMethods = {@ResolvedMethod(receiverType = "java.lang.String")}, line = 103)
    @CallSite(name = "push", resolvedMethods = {@ResolvedMethod(receiverType = "expressions.Stack")}, line = 148)
    @CallSite(name = "createBinaryExpression",
            resolvedMethods = {@ResolvedMethod( receiverType = BinaryExpression.FQN)},
            parameterTypes = {String.class, Expression.class, Expression.class},
            line = 151)
    @CallSite(name = "callback", resolvedMethods = {@ResolvedMethod(receiverType = "cmd.ExpressionEvaluator$CALLBACK")}, line = 110)
    @CallSite(name = "callback", resolvedMethods = {@ResolvedMethod(receiverType = "cmd.ExpressionEvaluator$CALLBACK")}, line = 122)
    @CallSite(name = "callback", resolvedMethods = {@ResolvedMethod(receiverType = "cmd.ExpressionEvaluator$CALLBACK")}, line = 174)
    public static void main(final String[] args) {

        @CGNote(value = ARRAY_HANDLING, description = "") // TODO Why is this special?
        String[] expressions = args.clone();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @CGNote(value = JVM_CALLBACK, description="invisible callback because no native code is involved; the call graph seems to be complete")
            @CGNote(value= NOTE,description="the related method <Thread>.dispatchUncaughtException is not dead")
            @Override public void uncaughtException(Thread t, Throwable e) {
                CALLBACK.callback();
                String msg = "unexpected error while processing "+ Arrays.deepToString(args);
                System.out.println(msg);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(){

            // This is an entry point!
            @CGNote(value = JVM_CALLBACK, description="invisible callback because no native code is involved; the call graph seems to be complete")
            @CGNote(value = NOTE, description="the related method<Thread>.run is called by the jvm")
            @Override public void run() {
                CALLBACK.callback();
                System.out.println("It was a pleasure to evaluate your expression!");
                super.run();
            }
        });

        synchronized (ExpressionEvaluator.class) {
            // all methods of the class object of ExpressionEvaluation may be called...
            // unless we analyze the "JVM" internal implementation
            @CGNote(value = NATIVE_CALLBACK, description = "potential callback because native code is involved; the call graph seems to be complete")
            @CGNote(value = NOTE, description = "the native code may call any of the methods declared at the class object of ExpressionEvaluation")
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

    /*
     * !!!!! THIS METHOD IS NOT INTENDED TO BE CALLED DIRECTLY !!!!
     * The ExpressionEvaluator.class is passed to a native method with an ´Object´ type
     * as parameter. The native method can (potentially) call any visible method on the passed object, i.e. toString().
     */
    public String toString(){
        CALLBACK.callback();
        return "ExpressionEvaluater v0.1";
    }

    /*
     * We need this class to annotate callbacks. We have no other opportunity to annotate the this call back edges.
     */
    private static class CALLBACK { static void callback() {/* do nothing*/}}
}
