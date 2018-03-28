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

import lib.*;
import lib.annotations.callgraph.IndirectCall;
import lib.annotations.documentation.CGFeature;
import lib.annotations.properties.EntryPoint;

import java.lang.reflect.Method;

import static lib.UnaryOperator.IDENTITY;
import static lib.UnaryOperator.SQUARE;
import static lib.annotations.callgraph.AnalysisMode.*;

/**
 * This class defines an application use case of the expression library featuring reflection.
 * It just creates a binary expression representing the mason's angle (3²+4²=5²) and does nothing with it.
 * <!--
 * <p>
 * <p>
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
 * -->
 *
 * @author Michael Eichberg
 * @author Michael Reif
 * @author Roberts Kolosovs
 * @author Florian Kuebler
 */
public class MasonsExpressions {

    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    @IndirectCall(
            name = "eval",
            returnType = Constant.class,
            parameterTypes = Map.class,
            resolvedTargets = MultOperator.MultExpression.FQN
    )
    @IndirectCall(
            name = "eval",
            returnType = Constant.class,
            parameterTypes = Map.class,
            resolvedTargets = PlusOperator.AddExpression.FQN
    )
    @IndirectCall(
            name = "eval",
            returnType = Constant.class,
            parameterTypes = Map.class,
            resolvedTargets = Constant.FQN
    )
    public static void main(final String[] args) throws Exception {
        Constant c1 = new Constant(3);
        Constant c2 = new Constant(4);

        Expression left = UnaryExpression.createUnaryExpressions(SQUARE, c1);
        Expression right = UnaryExpression.createIdentityExpression(c1);
        UnaryExpression.createUnaryExpressions("lib.NegationExpression", c2);


        MultOperator.MultExpression mult = BinaryExpression.createMultExpression(left, right);
        PlusOperator.AddExpression add = (PlusOperator.AddExpression) PlusOperator.createBinaryExpression(left, right);

        BinaryExpression.createBinaryExpression("lib.PlusOperator", left, right);
        BinaryExpression.createRandomBinaryExpression(left, right);

        BinaryExpression.createBasicBinaryExpression("lib.PlusOperator", left, right);
        BinaryExpression.createBasicMultExpression(left, right);

        Class<?> multClass = Class.forName("lib.MultOperator$MultExpression");
        Method evalMultMethod = multClass.getMethod("eval", Map.class);
        evalMultMethod.invoke(mult, new Map<>());

        Class<?> plusClass = Class.forName("lib.PlusOperator$AddExpression");
        Method evalPlusMethod = plusClass.getMethod("eval", Map.class);
        evalPlusMethod.invoke(add, new Map<>());

        Class<?> constantClass = Class.forName("lib.Constant");
        Method evalConstantMethod = constantClass.getMethod("eval", Map.class);
        evalConstantMethod.invoke(c1, new Map<>());


    }
}
