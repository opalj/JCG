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
import lib.annotations.properties.EntryPoint;

/**
 * 
 * <p>
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
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 * <p>
 * <p>
 * 
 * -->
 *
 * @author Roberts Kolosovs
 */
public class ExpressionPrinter {
	
	private ExpressionPrinter(){}
	
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    public static void main(final String[] args) {
    	Expression expr = new IdentityExpression(new SquareExpression(new IncrementExpression(new Constant(1))));
    	Supplier<ExpressionPrinter> instance = ExpressionPrinter::instance;
    	ExpressionStringifier stringifier = instance.get().new ExpressionStringifier();
    	System.out.println(expr.accept(stringifier::visit));
    }
    
    static ExpressionPrinter instance() {
    	Supplier<ExpressionPrinter> printerConstructor = ExpressionPrinter::new;
    	return printerConstructor.get();
    }
    
    private class ExpressionStringifier extends ExpressionVisitor<String> {

		@Override
		public String visit(Expression e) {
			if (e instanceof DecrementExpression) {
				return "(" + recursiveAccept(e, Expression::accept) + ")--";
			} else if (e instanceof IncrementExpression) {
				return "(" + recursiveAccept(e, Expression::accept) + ")++";
			} else if (e instanceof IdentityExpression) {
				return "Id(" + recursiveAccept(e, Expression::accept) + ")";
			} else if (e instanceof SquareExpression) {
				return "(" + recursiveAccept(e, Expression::accept) + ")²";
			} else if (e instanceof Constant) {
				return String.valueOf(((Constant)e).getValue());
			} else {
				return "unknown expression";
			}
		}
		
		public String recursiveAccept(Expression e, BiFunction<Expression, Function<Expression, String>, String> func) {
			return func.apply(e, this::visit);
		}
    }
}
