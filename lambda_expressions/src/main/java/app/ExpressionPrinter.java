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

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import lib.Constant;
import lib.DecrementExpression;
import lib.Expression;
import lib.ExpressionVisitor;
import lib.IdentityExpression;
import lib.IncrementExpression;
import lib.Map;
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
	
	private static int[] values = {0, 1, 2, 3, 4}; 
	
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    @InvokedConstructor(receiverType = "lib/IdentityExpression", line = 115)
    @InvokedConstructor(receiverType = "lib/SquareExpression", line = 115)
    @InvokedConstructor(receiverType = "lib/IncrementExpression", line = 115)
    @InvokedConstructor(receiverType = "lib/Constant", line = 115)
    @InvokedConstructor(receiverType = "app/ExpressionPrinter$ExpressionStringifier", line = 117)
    @CallSite(name = "accept",
    	resolvedMethods = {@ResolvedMethod(receiverType = "lib/SquareExpression"),
    			@ResolvedMethod(receiverType = "lib/IdentityExpression"),
    			@ResolvedMethod(receiverType = "lib/IncrementExpression")},
    	returnType = String.class,
        parameterTypes = {Function.class},
    	line = 118)
    @CallSite(name = "clone",
    	resolvedMethods = {@ResolvedMethod(receiverType = "java/lang/int[]")},
    	returnType = int[].class,
    	line = 120)
    @CallSite(name = "incrementAll",
    	resolvedMethods = {@ResolvedMethod(receiverType = "app/ExpressionPrinter")},
    	parameterTypes = int[].class,
    	returnType = Expression[].class,
    	line = 122)
    @CallSite(name = "asList",
		resolvedMethods = {@ResolvedMethod(receiverType = "java/util/Arrays")},
		parameterTypes = Expression[].class,
		line = 124)
    @CallSite(name = "toConstant",
		resolvedMethods = {@ResolvedMethod(receiverType = "app/ExpressionPrinter$ZeroConstant")},
		parameterTypes = int[].class,
		returnType = Expression[].class,
		line = 126)
    public static void main(final String[] args) {
    	Expression expr = new IdentityExpression(new SquareExpression(new IncrementExpression(new Constant(1))));
    	Supplier<ExpressionPrinter> instance = ExpressionPrinter::instance;
    	ExpressionStringifier stringifier = instance.get().new ExpressionStringifier();
    	System.out.println(expr.accept(stringifier::visit));
    	Supplier<int[]> valuesProvider = values::clone;
    	int[] clonedValues = valuesProvider.get();
    	Function<int[], Expression[]> incrementArrayFunc = ExpressionPrinter::incrementAll;
    	Expression[] incrementArray = incrementArrayFunc.apply(clonedValues);
    	Consumer<Expression[]> toList = Arrays::<Expression[]>asList;
    	toList.accept(incrementArray);
    	Supplier<Constant> constantSupplier = new OneConstant().getSuperToConstant();
    	Constant zero = constantSupplier.get();
    }
    
    @InvokedConstructor(receiverType = "app/ExpressionPrinter", line = 132)
    static ExpressionPrinter instance() {
    	Supplier<ExpressionPrinter> printerConstructor = ExpressionPrinter::new;
    	return printerConstructor.get();
    }
    
    private static Expression[] incrementAll(int[] vals){
    	if (vals.length == 0){
    		Expression[] res = {new Constant(0)};
    		return res;
    	} else {
    		Expression[] res = new Expression[vals.length];
    		for(int i = 0; i<vals.length; i++){
    			res[i] = new IncrementExpression(new Constant(vals[i]));
    		}
    		return res;
    	}
    }

    private static Expression[] incrementAll(double[] vals){
    	if (vals.length == 0){
    		Expression[] res = {new Constant(0)};
    		return res;
    	} else {
    		Expression[] res = new Expression[vals.length];
    		for(int i = 0; i<vals.length; i++){
    			res[i] = new IncrementExpression(new Constant((int) vals[i]));
    		}
    		return res;
    	}
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
	        parameterTypes = {Function.class},
	        line = 188)
		public String recursiveAccept(Expression e, BiFunction<Expression, Function<Expression, String>, String> func) {
			return func.apply(e, this::visit);
		}
    }
    
    private static class ZeroConstant {
    	public Constant toConstant() {
    		return new Constant(0);
		}
	}

	private static class OneConstant extends ZeroConstant {
    	public java.util.function.Supplier<Constant> getSuperToConstant() {
    		return super::toConstant;
		}

		@Override
		public Constant toConstant() {
    		return new Constant(1);
		}
	}
}
