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
import static lib.annotations.callgraph.TargetResolution.DYNAMIC;
import static lib.annotations.documentation.CGCategory.INVOKEDYNAMIC;

import java.util.function.Function;

import lib.annotations.callgraph.CallSite;
import lib.annotations.callgraph.IndirectCall;
import lib.annotations.callgraph.ResolvedMethod;
import lib.annotations.documentation.CGNote;
import lib.annotations.properties.EntryPoint;

/**
 * A SquareExpression represents an unary operation that squares an expression.
 * <p>
 * Has a method returning an instance of a FunctionalInterface with a lambda expression.
 * <p>
 * <p>
 * <!--
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
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
 * -->
 *
 * @author Michael Reif
 */
public final class SquareExpression extends UnaryExpression {

    public static final String FQN = "Llib/SquareExpression;";

    private Expression operand;

    @EntryPoint(value = {OPA, CPA})
    public SquareExpression(Expression expr) {
        super(expr);
        operand = expr;
    }

    @CGNote(value = INVOKEDYNAMIC, description = "The following lambda expression is compiled to an invokedynamic instruction.")
    @CallSite(resolution = DYNAMIC,
            name = "lambda$operator$0",
            returnType = Constant.class,
            parameterTypes = {Constant.class},
            resolvedTargets = "Llib/IncrementExpression;",
            line = 91)
    @EntryPoint(value = {OPA, CPA})
    public IUnaryOperator operator() {
        return (Constant c) -> new Constant(c.getValue() * c.getValue());
    }

    @EntryPoint(value = {OPA, CPA})
    public String toString() {
        return expr.toString() + "²";
    }

    @EntryPoint(value = {OPA, CPA})
    public Constant eval(Map<String, Constant> values) {
        int opVal = operand.eval(values).getValue();
        return new Constant(opVal * opVal);
    }

    @Override
    public <T> T accept(Function<Expression, T> visit) {
        return visit.apply(this);
    }
}
