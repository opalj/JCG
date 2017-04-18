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

import lib.annotations.callgraph.*;
import lib.annotations.documentation.CGNote;
import lib.annotations.properties.EntryPoint;
import lib.*;

import java.lang.reflect.Constructor;

import static lib.annotations.callgraph.AnalysisMode.CPA;
import static lib.annotations.callgraph.AnalysisMode.OPA;
import static lib.annotations.callgraph.CallGraphAlgorithm.CHA;
import static lib.annotations.documentation.CGCategory.REFLECTION;
import static lib.UnaryOperator.IDENTITY;

/**
 * An abstract unary Expression where the operation has to be implemented
 * via a lambda function.
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

public abstract class UnaryExpression implements Expression {

    public static final String FQN = "lib/UnaryExpression";

    protected Expression expr;

    public abstract IUnaryOperator operator();

    @CGNote(value = REFLECTION, description = "The first reflective String can be varied by an enumeration but all possible call targets can be found.")
    @CallSite(name = "<init>", parameterTypes = {Expression.class},
            resolvedMethods = {
                    @ResolvedMethod(receiverType = IdentityExpression.FQN),
                    @ResolvedMethod(receiverType = DecrementExpression.FQN),
                    @ResolvedMethod(receiverType = IncrementExpression.FQN)},
            resolution = TargetResolution.REFLECTIVE,
            line = 106)
    @CGNote(value = REFLECTION, description = "The second reflective String is known at compile time. The exact call target can be determined.")
    @CallSite(name = "<init>", parameterTypes = {Expression.class},
            resolvedMethods = @ResolvedMethod(receiverType = IdentityExpression.FQN),
            resolution = TargetResolution.REFLECTIVE,
            line = 112)
    @EntryPoint(value = {OPA, CPA})
    public static UnaryExpression createUnaryExpressions(
            UnaryOperator operator,
            final Expression expr) {
        UnaryExpression uExpr = null;
        try {
            Class<?> clazz = Class.forName(operator.toString());
            Constructor<?> constructor = clazz.getConstructor(Expression.class);
            uExpr = (UnaryExpression) constructor.newInstance(expr);
        } catch (Exception e) {
            if (uExpr == null) {
                try {
                    Class<?> clazz = Class.forName(IDENTITY.toString());
                    Constructor<?> constructor = clazz.getConstructor(Expression.class);
                    uExpr = (UnaryExpression) constructor.newInstance(expr);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        return uExpr;
    }

    public UnaryExpression(Expression expr) {
        this.expr = expr;
    }

    @EntryPoint(value = {OPA, CPA})
    public abstract String toString();

    @CallSite(name = "eval", returnType = Constant.class, parameterTypes = Map.class,
    resolvedMethods = {
            @ResolvedMethod(receiverType = UnaryExpression.FQN),
            @ResolvedMethod(receiverType = PlusOperator.AddExpression.FQN),
            @ResolvedMethod(receiverType = MultOperator.MultExpression.FQN),
            @ResolvedMethod(receiverType = SubOperator.SubExpression.FQN, iff = @ResolvingCondition(containedInMax = CHA)),
            @ResolvedMethod(receiverType = DecrementExpression.FQN, iff = @ResolvingCondition(containedInMax = CHA))
    }, line = 143)
    @CallSite(name = "apply", returnType = Constant.class, parameterTypes = Constant.class,
            resolvedMethods = {
                    @ResolvedMethod(receiverType = DecrementExpression.DecrementOperator.FQN, iff = @ResolvingCondition(containedInMax = CHA)),
            }, line = 143)
    @EntryPoint(value = {OPA, CPA})
    public Constant eval(Map<String, Constant> values) {
        return operator().apply(expr.eval(values));
    }
}