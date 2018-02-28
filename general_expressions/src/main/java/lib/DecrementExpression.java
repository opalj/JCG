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

package lib;

import lib.annotations.callgraph.CallSite;
import lib.annotations.callgraph.ResolvedMethod;
import lib.annotations.properties.EntryPoint;

import static lib.annotations.callgraph.AnalysisMode.CPA;
import static lib.annotations.callgraph.AnalysisMode.OPA;

/**
 * An unary expression which represents the decrement function.
 *
 * THIS CLASS IS INTENTIONALLY UNUSED WITHIN THE APPLICATION SCENARIO. (CLASS IS NEVER INSTANTIATED)
 *
 * 
 * <!--
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * -->
 *
 * @author Michael Reif
 * @author Roberts Kolosovs
 */
public class DecrementExpression extends UnaryExpression {

	public static final String FQN = "lib/DecrementExpression"; 
	
	@EntryPoint(value = { OPA, CPA })
    public DecrementExpression(Expression expr){
        super(expr);
    }

    @EntryPoint(value = {OPA, CPA})
    public String toString() {
        return "Dec("+expr.toString()+")";
    }

    @EntryPoint(value = {OPA, CPA})
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

	@Override
    @EntryPoint(value = {OPA, CPA})
    @CallSite(name = "checkIfDecrement", resolvedMethods = {
    		@ResolvedMethod(receiverType = DecrementExpression.FQN)},
    		parameterTypes = {Expression.class}, line = 99)
    @CallSite(name = "eval", returnType = Constant.class, resolvedMethods =
    		@ResolvedMethod(receiverType = Constant.FQN),
    		parameterTypes = {Map.class}, line = 101)
	public Constant eval(Map<String, Constant> values) {
		try {
			checkIfDecrement(this.expr);
		} catch (Exception e) {
			return new Constant(expr.eval(values).getValue() - 1);
		}
		return new Constant( ((UnaryExpression) expr).expr.eval(values).getValue() - 2);
	}
	
	private void checkIfDecrement(Object vals) throws Exception{
		if(!(vals instanceof DecrementExpression)){
			//do nothing
		} else {
			throw new Exception();
		}
	}
}
