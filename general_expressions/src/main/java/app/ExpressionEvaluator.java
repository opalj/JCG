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

import lib.annotations.callgraph.*;
import lib.annotations.documentation.CGNote;
import lib.annotations.properties.EntryPoint;

import static lib.annotations.callgraph.AnalysisMode.*;
import static lib.annotations.documentation.CGCategory.NATIVE_CALLBACK;
import static lib.testutils.CallbackTest.callback;

import lib.AddExpression;
import lib.Constant;
import lib.Expression;
import lib.IncrementExpression;
import lib.Map;
import lib.SquareExpression;

/**
 * This class defines an application use case of the expression library and has some well defined properties
 * wrt. call graph construction. It covers ( inlc. the library) serveral Java language features to test whether
 * a given call graph implementation can handle these features.
 * <p>
 * <p>
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 * <p>
 * <!--
 * <p>
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 * <p>
 * <p>
 * -->
 *
 * @author Michael Eichberg
 * @author Michael Reif
 * @author Roberts Kolosovs
 */
public class ExpressionEvaluator {
	private IncrementExpression[] coutToThree = {new IncrementExpression(new Constant(0)), 
			new IncrementExpression(new Constant(1)), 
			new IncrementExpression(new Constant(2))};
	
	private Expression[] expressionArray = new Expression[3];

    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    @CallSite(name = "evalFirstEntry", resolvedMethods = {@ResolvedMethod(receiverType = "app/ExpressionEvaluator")}, line = 101)
    @CallSite(name = "evalSecondEntry", resolvedMethods = {@ResolvedMethod(receiverType = "app/ExpressionEvaluator")}, line = 101)
    @CallSite(name = "evalAll", resolvedMethods = {@ResolvedMethod(receiverType = "app/ExpressionEvaluator")}, line = 101)
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
    }

    @CGNote( value = NATIVE_CALLBACK,description = "monomorph, intraprocedural case of arraycopy test case.")
    @CallSite(name = "eval", resolvedMethods = {@ResolvedMethod(receiverType = IncrementExpression.FQN)}, line = 101)
    @CallSite(name = "arraycopy", isStatic = true, resolvedMethods = {@ResolvedMethod(receiverType = "java/lang/System")}, line = 100)
    private int evalFirstEntry(){
    	Expression[] tempArray = new Expression[3];
    	System.arraycopy(coutToThree, 0, tempArray, 0, 3);
    	return tempArray[0].eval(new Map<String,Constant>()).getValue();
    }

    @CGNote( value = NATIVE_CALLBACK,description = "monomorph, interprocedural case of arraycopy test case.")
    @CallSite(name = "eval", resolvedMethods = {@ResolvedMethod(receiverType = IncrementExpression.FQN)}, line = 107)
    private int evalSecondEntry(){
    	return expressionArray[1].eval(new Map<String,Constant>()).getValue();
    }

    @CGNote( value = NATIVE_CALLBACK,description = "polymorph, interprocedural case of arraycopy test case.")
    @CallSite(name = "eval", resolvedMethods = {@ResolvedMethod(receiverType = IncrementExpression.FQN),
    		@ResolvedMethod(receiverType = SquareExpression.FQN),
    		@ResolvedMethod(receiverType = AddExpression.FQN)}, line = 117)
    private int[] evalAll(){
    	int[] result = new int[3];
    	for(int i = 0; i<expressionArray.length; i++){
    		result[i] = expressionArray[i].eval(new Map<String,Constant>()).getValue();
    	}
    	return result;
    }

    @CallSite(name = "arraycopy", isStatic = true, resolvedMethods = {@ResolvedMethod(receiverType = "java/lang/System")}, line = 125)
    @CGNote( value = NATIVE_CALLBACK,description = "array with well known types is copied into other array.")
    private void copyPrivateArrays() {
    	System.arraycopy(coutToThree, 0, expressionArray, 0, 3);
    }
    
    /*
     * !!!!! THIS METHOD IS NOT INTENDED TO BE CALLED DIRECTLY !!!!
     * The ExpressionEvaluator.class is passed to a native method with an ´Object´ type
     * as parameter. The native method can (potentially) call any visible method on the passed object, i.e. toString().
     */
    @CallSite(name = "callback", resolvedMethods = {@ResolvedMethod(receiverType = "lib/testutils/CallbackTest")}, line = 201)
    @EntryPoint(value = {OPA, CPA})
    public String toString() {
        callback();
        return "ExpressionEvaluater v0.1";
    }
}
