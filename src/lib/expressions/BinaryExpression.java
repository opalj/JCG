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
package expressions;

import annotations.callgraph.CallSite;
import annotations.callgraph.ResolvedMethod;
import annotations.callgraph.ResolvingCondition;
import annotations.documentation.CGNote;

import static annotations.callgraph.AnalysisMode.*;
import static annotations.documentation.CGCategory.*;

import java.lang.reflect.Method;

/**
 * This class defines an application use case of the expression library and has some well defined properties
 * wrt. call graph construction. It covers ( inlc. the library) serveral Java language features to test whether
 * a given call graph implementation can handle these features.
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
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 *
 *
 *
 *
 *
 *
 *
 * -->
 *
 * @author Michael Eichberg
 * @author Micahel Reif
 */
public abstract class BinaryExpression implements Expression {

    public static final String fqn = "expressions.BinaryExpression";

    abstract protected Expression left();

    abstract protected Expression right();

    abstract protected Operator operator();

    public <T> T accept(ExpressionVisitor <T> visitor){
        return visitor.visit(this);
    }

    @CGNote( value = REFLECTION,description = "a new instance is created by Java Reflection")
    @CallSite(name = "<init>",
            resolvedMethods = {
                    @ResolvedMethod(receiverType = PlusOperator.fqn),
                    @ResolvedMethod(receiverType = SubOperator.fqn, iff = {@ResolvingCondition(mode = {OPA, CPA})})
            },
            isReflective = true,
            line = 98
    )
    public static BinaryExpression createBasicBinaryExpression(
            String operator,
            final Expression left,
            final Expression right) throws Exception{
        final Operator op = (Operator) Class.forName("expression."+operator+"Operator").newInstance();

        return new BinaryExpression(){

            @Override public Constant eval(Map<String, Constant> values) {
                throw new UnsupportedOperationException();
            }

            @Override protected Expression left() {
                return left;
            }

            @Override protected Expression right() {
                return right;
            }

            @Override protected Operator operator() {
                return op;
            }
        };
    }

    @CGNote(value = REFLECTION, description = "a (static) method is invoked by Java's reflection mechanism; the call graph has to handle reflection")
    public static BinaryExpression createBinaryExpression(
            String operator,
            final Expression left,
            final Expression right) throws Exception{
        Class<?> operatorClass = null;
        try {
            operatorClass = Class.forName("expressions." + operator + "Operator");
        } catch (ClassNotFoundException cnfe) {
            operatorClass = Class.forName(operator);
        }
        Method m = operatorClass.getDeclaredMethod("createBinaryExpression",Expression.class,Expression.class);
        m.setAccessible(true);
        return (BinaryExpression) m.invoke(null,left,right);
    }
}

