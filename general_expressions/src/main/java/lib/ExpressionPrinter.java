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
import static lib.annotations.documentation.CGCategory.STATIC_INITIALIZERS;

import lib.annotations.documentation.CGNote;
import lib.annotations.properties.EntryPoint;

/**
 * This class defines a visitor that prints visited expressions.
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
 * -->
 *
 * @author Michael Eichberg
 * @author Michael Reif
 */
public final class ExpressionPrinter extends ExpressionVisitor<String> {
	
	public static final String FQN = "Llib/ExpressionPrinter;";
	
    private ExpressionPrinter() {
    }

    @EntryPoint(value = {OPA, CPA})
    public ExpressionPrinter expressionPrinterFactory() {
    	return new ExpressionPrinter();
    }

    @EntryPoint(value = {OPA, CPA})
    public String visit(Constant c) {
        return String.valueOf(c.getValue());
    }

    @EntryPoint(value = {OPA, CPA})
    public String visit(Variable v) {
        return v.name;
    }

    @EntryPoint(value = {OPA, CPA})
    public String visit(BinaryExpression b) {
        return "(" + b.left().toString() + b.operator() + b.right().toString() + ")";
    }

    @EntryPoint(value = {OPA, CPA})
    public String visit(UnaryExpression u) {
        return u.toString();
    }

    @EntryPoint(value = {OPA, CPA})
    public void printExpression(Expression e) {
        System.out.print(e.accept(this));
    }
}