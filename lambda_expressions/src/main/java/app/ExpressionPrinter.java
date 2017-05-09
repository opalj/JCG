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

import static lib.annotations.callgraph.AnalysisMode.*;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import lib.Constant;
import lib.DecrementExpression;
import lib.Expression;
import lib.ExpressionVisitor;
import lib.IdentityExpression;
import lib.IncrementExpression;
import lib.SquareExpression;
import lib.annotations.callgraph.CallSite;
import lib.annotations.callgraph.InvokedConstructor;
import lib.annotations.callgraph.ResolvedMethod;
import lib.annotations.properties.EntryPoint;

/**
 * 
 * <!--
 * 
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
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
 * -->
 *
 * @author Roberts Kolosovs
 */
public class ExpressionPrinter {
	
	private ExpressionPrinter(){}
	
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    @InvokedConstructor(receiverType = "lib/IdentityExpression", line = 91)
    @InvokedConstructor(receiverType = "lib/SquareExpression", line = 91)
    @InvokedConstructor(receiverType = "lib/IncrementExpression", line = 91)
    @InvokedConstructor(receiverType = "lib/Constant", line = 91)
    @InvokedConstructor(receiverType = "app/ExpressionPrinter$ExpressionStringifier", line = 93)
    @CallSite(name = "accept",
    	resolvedMethods = {@ResolvedMethod(receiverType = "lib/SquareExpression"),
    			@ResolvedMethod(receiverType = "lib/IdentityExpression"),
    			@ResolvedMethod(receiverType = "lib/IncrementExpression")},
    	returnType = Object.class,
    	line = 94)
    public static void main(final String[] args) {
    	Expression expr = new IdentityExpression(new SquareExpression(new IncrementExpression(new Constant(1))));
    	Supplier<ExpressionPrinter> instance = ExpressionPrinter::instance;
    	ExpressionStringifier stringifier = instance.get().new ExpressionStringifier();
    	System.out.println(expr.accept(stringifier::visit));
    }
    
    @InvokedConstructor(receiverType = "app/ExpressionPrinter", line = 100)
    static ExpressionPrinter instance() {
    	Supplier<ExpressionPrinter> printerConstructor = ExpressionPrinter::new;
    	return printerConstructor.get();
    }
    
    private class ExpressionStringifier extends ExpressionVisitor<String> {

		@Override
		public String visit(Expression e) {
			if (e instanceof DecrementExpression) {
				return "(" + recursiveAccept(((DecrementExpression)e).getExpr(), Expression::accept) + ")--";
			} else if (e instanceof IncrementExpression) {
				return "(" + recursiveAccept(((IncrementExpression)e).getExpr(), Expression::accept) + ")++";
			} else if (e instanceof IdentityExpression) {
				return "Id(" + recursiveAccept(((IdentityExpression)e).getExpr(), Expression::accept) + ")";
			} else if (e instanceof SquareExpression) {
				return "(" + recursiveAccept(((SquareExpression)e).getExpr(), Expression::accept) + ")²";
			} else if (e instanceof Constant) {
				return String.valueOf(((Constant)e).getValue());
			} else {
				return "unknown expression";
			}
		}

	    @CallSite(name = "accept",
	        	resolvedMethods = {@ResolvedMethod(receiverType = "lib/SquareExpression"),
	        			@ResolvedMethod(receiverType = "lib/IdentityExpression"),
	        			@ResolvedMethod(receiverType = "lib/IncrementExpression")},
	        	returnType = Object.class,
	    line = 129)
		public String recursiveAccept(Expression e, BiFunction<Expression, Function<Expression, String>, String> func) {
			return func.apply(e, this::visit);
		}
    }
}
