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

import static annotations.callgraph.AnalysisMode.CPA;
import static annotations.callgraph.AnalysisMode.OPA;

import annotations.properties.EntryPoint;

/**
 * A plus operator that creates a binary add expression.
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
 * @author Michael Reif
 */
public class PlusOperator extends Operator {

    public static final String FQN = "expressions/PlusOperator";

    protected PlusOperator() {}

    public final static Operator instance = new PlusOperator();

    public static class AddExpression extends BinaryExpression {

    	public static final String FQN = "expressions/PlusOperator$AddExpression";
    	
        private final Expression right;
        private final Expression left;

        public AddExpression(Expression left, Expression right) {
         this.left = left;
            this.right = right;
        }

        @EntryPoint(value = {OPA, CPA})
        public Expression left(){return this.left;}

        @EntryPoint(value = {OPA, CPA})
        public Expression right(){return this.right;}

        @EntryPoint(value = {OPA, CPA})
        public Operator operator(){return PlusOperator.instance;}

        @Override public Constant eval(Map<String, Constant> values) {
            return new Constant( left.eval(values).getValue() + right.eval(values).getValue() );
        }
    }

    @EntryPoint(value = {OPA})
    static BinaryExpression createBinaryExpression(Expression left, Expression right ) {
        return new AddExpression(left, right);
    }

    @EntryPoint(value = {OPA, CPA})
    public String toString(){
        return "+";
    }
}

