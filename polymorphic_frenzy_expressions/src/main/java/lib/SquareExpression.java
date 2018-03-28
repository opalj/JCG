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

import static lib.annotations.callgraph.AnalysisMode.CPA;
import static lib.annotations.callgraph.AnalysisMode.OPA;

import java.io.ObjectStreamException;

import lib.annotations.callgraph.CallSite;
import lib.annotations.callgraph.ResolvedMethod;
import lib.annotations.properties.EntryPoint;

/**
 * Class modeling a square expression.
 * 
 * This class declares two inner private classes but uses only one of them. The dead inner 
 * class seems like it escapes the local scope via deserialization like in the test case 
 * featured in IdentityExpression$IdentityOperator but always returns an instance of the 
 * other inner class during deserialization.
 *
 * <!--
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 *
 *
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
 * -->
 *
 * @author Roberts Kolosovs
 */

public class SquareExpression extends UnaryExpression {

	private static final long serialVersionUID = 1L;
	
	@EntryPoint(value = { OPA, CPA })
	public SquareExpression(Expression expr) {
		this.expr = expr;
		this.operator = new NewSquareOperator();
	}

    @EntryPoint(value = {OPA, CPA})
    @CallSite(name = "apply", 
    	resolvedTargets = NewSquareOperator.FQN,
    	parameterTypes ={Expression.class, Map.class}, returnType = Expression.class,
    	line = 90)
	@Override
	public Constant eval(Map<String, Constant> values) {
		return (Constant) operator.apply(expr, values);
	}

    @EntryPoint(value = {OPA, CPA})
	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visit(this);
	}
	
	private class SquareOperator extends UnaryOperator {

		public final static String FQN = "Llib/SquareExpression$SquareOperator;";

		private static final long serialVersionUID = 2L;
		
	    @EntryPoint(value = {OPA, CPA})
	    private Object readResolve() throws ObjectStreamException {
	    	return new NewSquareOperator();
	    }
		
		public String toString() {
			return "Sq";
		}
		
		@Override
		public Expression apply(Expression expr, Map<String, Constant> values) {
			return expr;
		}
	}
	
	private class NewSquareOperator extends UnaryOperator {

		public final static String FQN = "Llib/SquareExpression$SquareOperator;";
		
		private static final long serialVersionUID = 1L;
		
	    @EntryPoint(value = {OPA, CPA})
	    private Object readResolve() throws ObjectStreamException {
	    	return this;
	    }

	    @EntryPoint(value = {OPA, CPA})
		public String toString() {
			return "sq";
		}

	    @EntryPoint(value = {OPA, CPA})
		@Override
		public Expression apply(Expression expr, Map<String, Constant> values) {
			return expr.eval(values);
		}
	}

}
