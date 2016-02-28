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

package expressions.java8_expressions;

import annotations.documentation.CGCategory;
import annotations.documentation.CGNote;
import expressions.Constant;
import expressions.Expression;
import expressions.ExpressionVisitor;

import java.util.function.*;
import java.util.function.UnaryOperator;

import static annotations.documentation.CGCategory.NOTE;

/**
 * An unary expression which represents the decrement function.
 *
 * THIS CLASS IS INTENTIONALLY UNUSED WITHIN THE APPLICATION SCENARIO. (CLASS IS NEVER INSTANTIATED)
 *
 * <p>
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
 * <p>
 * -->
 *
 * @author Micahel Reif
 */
public class DecrementExpression extends UnaryExpression {

    public DecrementExpression(Expression expr){
        super(expr);
    }

    @CGNote(value = NOTE, description = "")
    public UnaryOperator<Constant> operator() { return DecrementOperator.newInstance(); }

    public String toString() {
        return "Dec("+expr.toString()+")";
    }

    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    static class DecrementOperator implements UnaryOperator<Constant>{

        public static final String FQN = "expressions/java8_expressions/DecrementExpression/DecrementOperator";

        private static DecrementOperator _INSTANCE;

        private DecrementOperator(){
        }

        public static DecrementOperator newInstance() {
           if(_INSTANCE == null){
               _INSTANCE = new DecrementOperator();
           }

            return _INSTANCE;
        }
        
        public Constant apply(Constant constant) {
            return new Constant(constant.getValue() - 1);
        }
    }
}
