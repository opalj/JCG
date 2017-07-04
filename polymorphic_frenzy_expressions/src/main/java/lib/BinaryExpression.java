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
import lib.annotations.callgraph.InvokedConstructor;
import lib.annotations.callgraph.ResolvedMethod;
import lib.annotations.properties.EntryPoint;

import static lib.annotations.callgraph.AnalysisMode.*;

/**
 * This abstract class models a binary mathematical expression.
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
 *
 *
 * -->
 *
 * @author Michael Eichberg
 * @author Michael Reif
 */
public abstract class BinaryExpression implements Expression {

    public static final String FQN = "lib/BinaryExpression";

    abstract protected Expression left();

    abstract protected Expression right();

    abstract protected Operator operator();

    @EntryPoint(value = {OPA, CPA})
    public <T> T accept(ExpressionVisitor <T> visitor){
        return visitor.visit(this);
    }

    @InvokedConstructor(receiverType = BinaryExpression.FQN, line = 97)
    @EntryPoint(value = {OPA, CPA})
    public static BinaryExpression createBasicBinaryExpression(
            Operator operator,
            final Expression left,
            final Expression right) throws Exception{
        final Operator op = operator;

        return new BinaryExpression(){

            @Override 
            @EntryPoint(value = {OPA, CPA})
            public Constant eval(Map<String, Constant> values) {
                throw new UnsupportedOperationException();
            }

            @Override 
            @EntryPoint(value = {OPA, CPA})
            protected Expression left() {
                return left;
            }

            @Override
            @EntryPoint(value = {OPA, CPA})
            protected Expression right() {
                return right;
            }

            @Override
            @EntryPoint(value = {OPA, CPA})
            protected Operator operator() {
                return op;
            }
        };
    }

    @CallSite(name = "createBinaryExpression", 
    		resolvedMethods = {@ResolvedMethod(receiverType = "lib/MultOperator"), 
    				@ResolvedMethod(receiverType = "lib/PlusOperator"), 
    				@ResolvedMethod(receiverType = "lib/SubOperator")}, 
    	    parameterTypes = {Expression.class, Expression.class},
    		line = 136) 
    @EntryPoint(value = {OPA, CPA})
    public static BinaryExpression createBinaryExpression(
            Operator operator,
            final Expression left,
            final Expression right) throws Exception{
        return operator.createBinaryExpression(left, right);
    }
}

