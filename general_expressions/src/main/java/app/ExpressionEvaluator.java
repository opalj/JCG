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
package app;

import lib.*;
import lib.annotations.callgraph.CallSite;
import lib.annotations.callgraph.ResolvedMethod;
import lib.annotations.documentation.CGNote;
import lib.annotations.properties.EntryPoint;
import lib.testutils.CallbackTest;

import static lib.annotations.callgraph.AnalysisMode.*;
import static lib.annotations.documentation.CGCategory.NATIVE_CALLBACK;
import static lib.testutils.CallbackTest.callback;

/**
 * This class defines an application use case of the expression library. It uses native
 * arraycopy method extensively.
 * <p>
 * NOTE
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 * <p>
 * <!--
 * <p>
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 * <p>
 * -->
 *
 *
 *
 *
 *
 *
 *
 *
 * @author Michael Eichberg
 * @author Michael Reif
 * @author Roberts Kolosovs
 */
public class ExpressionEvaluator {
    public final static String FQN = "Lapp/ExpressionEvaluator;";

    private IncrementExpression[] coutToThree = {new IncrementExpression(new Constant(0)),
            new IncrementExpression(new Constant(1)),
            new IncrementExpression(new Constant(2))};

    private Expression[] expressionArray = {new SubExpression(new Constant(2), new Constant(1)),
            new MultExpression(new Constant(2), new Constant(1)), null};

    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    @CallSite(name = "evalFirstEntry", returnType = int.class, resolvedMethods = {@ResolvedMethod(receiverType = FQN)}, line = 89)
    @CallSite(name = "evalSecondEntry", returnType = int.class, resolvedMethods = {@ResolvedMethod(receiverType = FQN)}, line = 93)
    @CallSite(name = "evalAll", returnType = int[].class, resolvedMethods = {@ResolvedMethod(receiverType = FQN)}, line = 99)
    @CallSite(name = "eval", returnType = lib.Constant.class, parameterTypes = {lib.Map.class}, resolvedMethods = {@ResolvedMethod(receiverType = NativeAddExpression.FQN)}, line = 102)
    @CallSite(name = "runAltEvaluation", resolvedMethods = {@ResolvedMethod(receiverType = FQN)}, line = 106)
    @CallSite(name = "execute", returnType = lib.Constant.class, parameterTypes = {lib.Expression.class}, resolvedMethods = {@ResolvedMethod(receiverType = ParameterizedEvaluator.FQN)}, line = 101)
    @CallSite(name = "getNullPointerException", resolvedMethods = {@ResolvedMethod(receiverType = FQN)}, line = 115)
    public static void main(final String[] args) {
        ExpressionEvaluator evaluatorOne = new ExpressionEvaluator();
        evaluatorOne.evalFirstEntry();

        ExpressionEvaluator evaluatorTwo = new ExpressionEvaluator();
        evaluatorTwo.copyPrivateArrays();
        evaluatorTwo.evalSecondEntry();

        ExpressionEvaluator evaluatorThree = new ExpressionEvaluator();
        evaluatorThree.copyPrivateArrays();
        evaluatorThree.expressionArray[0] = new SquareExpression(new Constant(1));
        evaluatorThree.expressionArray[1] = new AddExpression(new Constant(1), new Constant(1));
        evaluatorThree.evalAll();

        NativeAddExpression nativeTwo = new NativeAddExpression(new Constant(1), new Constant(1));
        nativeTwo.eval(null); //call to a native method with native c code present

        try {
            ExpressionEvaluator evaluatorFour = new ExpressionEvaluator();
            evaluatorFour.runAltEvaluation();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ParameterizedEvaluator evaluatorFive = new ExpressionEvaluator().makeParamEvaluator();
        evaluatorFive.execute(new AddExpression(new Constant(1), new DecrementExpression(new Constant(2))));

        try {
            ExpressionEvaluator evaluatorSix = new ExpressionEvaluator();
            evaluatorSix.getNullPointerException();
        } catch (Exception e) {
            e.printStackTrace();
        }

        evaluatorThree.toString();

    }

    @CGNote(value = NATIVE_CALLBACK, description = "monomorph, intraprocedural case of arraycopy test case.")
    @CallSite(name = "arraycopy", isStatic = true, resolvedMethods = {
            @ResolvedMethod(receiverType = "Ljava/lang/System;")},
            parameterTypes = {lib.IncrementExpression[].class, int.class, lib.Expression[].class, int.class, int.class}, line = 133)
    @CallSite(name = "eval", returnType = lib.Constant.class, resolvedMethods = {
            @ResolvedMethod(receiverType = IncrementExpression.FQN)},
            parameterTypes = {lib.Map.class}, line = 134)
    private int evalFirstEntry() {
        Expression[] tempArray = new Expression[3];
        System.arraycopy(coutToThree, 0, tempArray, 0, 3);
        return tempArray[0].eval(new Map<String, Constant>()).getValue();
    }

    @CGNote(value = NATIVE_CALLBACK, description = "monomorph, interprocedural case of arraycopy test case.")
    @CallSite(name = "eval", resolvedMethods = {@ResolvedMethod(receiverType = IncrementExpression.FQN)},
            parameterTypes = {lib.Map.class}, returnType = lib.Constant.class, line = 141)
    private int evalSecondEntry() {
        return expressionArray[1].eval(new Map<String, Constant>()).getValue();
    }

    @CGNote(value = NATIVE_CALLBACK, description = "polymorph, interprocedural case of arraycopy test case.")
    @CallSite(name = "eval", resolvedMethods = {@ResolvedMethod(receiverType = IncrementExpression.FQN),
            @ResolvedMethod(receiverType = SquareExpression.FQN),
            @ResolvedMethod(receiverType = AddExpression.FQN)},
            parameterTypes = {lib.Map.class}, returnType = lib.Constant.class, line = 152)
    private int[] evalAll() { //expressionArray manipulated before this method is called (lines 94, 95 and 96)
        int[] result = new int[3];
        for (int i = 0; i < expressionArray.length; i++) {
            result[i] = expressionArray[i].eval(new Map<String, Constant>()).getValue();
        }
        return result;
    }

    @CallSite(name = "arraycopy", isStatic = true,
            resolvedMethods = {@ResolvedMethod(receiverType = "Ljava/lang/System;")},
            parameterTypes = {lib.IncrementExpression[].class, int.class, lib.Expression[].class, int.class, int.class}, line = 162)
    @CGNote(value = NATIVE_CALLBACK, description = "array with well known types is copied into other array.")
    private void copyPrivateArrays() {
        System.arraycopy(coutToThree, 0, expressionArray, 0, 3);
    }

    /*
     * !!!!! THIS METHOD IS NOT INTENDED TO BE CALLED DIRECTLY !!!!
     * The ExpressionEvaluator.class is passed to a native method with an ´Object´ type
     * as parameter. The native method can (potentially) call any visible method on the passed object, i.e. toString().
     */
    @CallSite(name = "callback", resolvedMethods = {@ResolvedMethod(receiverType = CallbackTest.FQN)}, line = 173)
    @EntryPoint(value = {OPA, CPA})
    public String toString() {
        callback();
        return "ExpressionEvaluater v0.1";
    }

    @EntryPoint(value = {OPA, CPA})
    @CallSite(name = "<init>", resolvedMethods = @ResolvedMethod(receiverType = "Ljava/lang/ArrayStoreException;"), line = 182)
    public Constant altEvaluateFirst() {
        this.expressionArray = this.coutToThree; //deliberate misuse of array co-variance
        IdentityExpression i = new IdentityExpression(expressionArray[0]);
        expressionArray[1] = i; //crashes the program with an ArrayStoreException
        copyPrivateArrays(); //never executed
        return null;
    }

    @EntryPoint(value = {OPA, CPA})
    public void runAltEvaluation() {
        altEvaluateFirst(); //crashes the program
        copyPrivateArrays(); //never executed
    }

    /*
     * This is the only way to obtain an instance of ParameterizedEvaluator.
     */
    @EntryPoint(value = {OPA, CPA})
    @CallSite(name = "<init>", resolvedMethods = @ResolvedMethod(receiverType = ExpressionEvaluator.ParameterizedEvaluator.FQN), line = 199)
    public ParameterizedEvaluator<? extends BinaryExpression> makeParamEvaluator() {
        return new ParameterizedEvaluator<>();
    }

    class ParameterizedEvaluator<T extends Expression> { //should be made private in the future
        public static final String FQN = "Lapp/ExpressionEvaluator$ParameterizedEvaluator;";

        @CallSite(name = "eval", returnType = Constant.class, resolvedMethods = {
                @ResolvedMethod(receiverType = AddExpression.FQN)},
                parameterTypes = {Map.class}, line = 214)
        /*
         * Due to the way this is instantiated only BinaryExpressions ever make it this far
         * (once the class is made private).
         */
        @EntryPoint(value = {OPA, CPA})
        public Constant execute(T expression) {
            return expression.eval(new Map<String, Constant>());
        }
    }

    @CallSite(name = "<init>", resolvedMethods = @ResolvedMethod(receiverType = "Ljava/lang/NullPointerException;"), line = 221)
    @EntryPoint(value = {OPA, CPA})
    public void getNullPointerException() throws Exception {
        throw null;
    }
}
