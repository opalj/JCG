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

import lib.annotations.callgraph.*;
import lib.annotations.properties.EntryPoint;

import static lib.annotations.callgraph.AnalysisMode.CPA;
import static lib.annotations.callgraph.AnalysisMode.OPA;
import static lib.annotations.callgraph.CallGraphAlgorithm.CHA;

/**
 * An abstract unary Expression where the operation has to be implemented via a
 * lambda function.
 *
 * <p>
 * <!-- <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves
 * documentation purposes.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE
 * STABLE IF THE CODE (E.G. IMPORTS) CHANGE.
 * <p>
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
 */

public abstract class UnaryExpression implements Expression {

	public static final String FQN = "lib/UnaryExpression";

	protected Expression expr;

	public abstract IUnaryOperator operator();
	
	@InvokedConstructor(receiverType = DecrementExpression.FQN, parameterTypes = {Object.class, Object.class}, line = 89)
	@InvokedConstructor(receiverType = IdentityExpression.FQN, parameterTypes = {Object.class, Object.class}, line = 92)
	@InvokedConstructor(receiverType = IncrementExpression.FQN, parameterTypes = {Object.class, Object.class}, line = 95)
	@InvokedConstructor(receiverType = SquareExpression.FQN, parameterTypes = {Object.class, Object.class}, line = 98)
	@InvokedConstructor(receiverType = IdentityExpression.FQN, parameterTypes = {Object.class, Object.class}, line = 106)
	@EntryPoint(value = { OPA, CPA })
	public static UnaryExpression createUnaryExpressions(UnaryOperator operator, final Expression expr) {
		UnaryExpression uExpr = null;
		try {
			switch (operator) {
			case DECREMENT:
				uExpr = new DecrementExpression(expr);
				break;
			case IDENTITY:
				uExpr = new IdentityExpression(expr);
				break;
			case INCREMENT:
				uExpr = new IncrementExpression(expr);
				break;
			case SQUARE:
				uExpr = new SquareExpression(expr);
				break;
			default:
				throw new Exception();
			}
		} catch (Exception e) {
			if (uExpr == null) {
				try {
					uExpr = new IdentityExpression(expr);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}

		return uExpr;
	}

	public UnaryExpression(Expression expr) {
		this.expr = expr;
	}

	public Expression getExpr(){
		return expr;
	}
	
	@EntryPoint(value = { OPA, CPA })
	public abstract String toString();

	@CallSite(name = "eval", returnType = Constant.class, parameterTypes = Map.class, resolvedMethods = {
			@ResolvedMethod(receiverType = UnaryExpression.FQN),
			@ResolvedMethod(receiverType = DecrementExpression.FQN, iff = @ResolvingCondition(containedInMax = CHA)) }, line = 134)
	@CallSite(name = "apply", returnType = Constant.class, parameterTypes = Constant.class, resolvedMethods = {
			@ResolvedMethod(receiverType = DecrementExpression.DecrementOperator.FQN, iff = @ResolvingCondition(containedInMax = CHA)), }, line = 134)
	@EntryPoint(value = { OPA, CPA })
	public Constant eval(Map<String, Constant> values) {
		return operator().apply(expr.eval(values));
	}
}