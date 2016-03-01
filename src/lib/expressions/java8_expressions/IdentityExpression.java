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

import annotations.callgraph.CallSite;
import annotations.callgraph.ResolvedMethod;
import annotations.documentation.CGNote;
import annotations.properties.EntryPoint;
import expressions.Expression;
import expressions.ExpressionVisitor;
import testutils.CallbackTest;
import testutils.StaticInitializerTest;

import static annotations.documentation.CGCategory.NOTE;

/**
 * An unary expression which represents the identity function. Hence, the encapsulated expression
 * is mapped to itself.
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
 *
 * <p>
 * <p>
 * <p>
 * <p>
 * -->
 *
 * @author Micahel Reif
 */
public class IdentityExpression extends UnaryExpression {

    public static final String FQN = "expressions/java8_expressions/IdentityExpression";

    private static /* final */ IUnaryOperator _IDENTITY;

    @CallSite(name = "staticCall", resolvedMethods = {@ResolvedMethod(receiverType = StaticInitializerTest.FQN)}, isStatic = true, line = 83)
    @CGNote(value = NOTE, description = "The call on UnaryOperator is a call on an interface default method.")
    @CallSite(name = "identity", resolvedMethods = @ResolvedMethod(receiverType = IUnaryOperator.FQN), isStatic = true, line = 84)
    private static void clinit(){
        StaticInitializerTest.staticCall();
        _IDENTITY = IUnaryOperator.identity();
    }

    static {
        clinit();
    }

    public IdentityExpression(Expression expr){
        super(expr);
    }

    public IUnaryOperator operator() {
        return _IDENTITY;
    }

    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public String toString(){
        return "Id("+expr.toString()+")";
    }

    @EntryPoint
    @CGNote(value = NOTE, description = "This method is called during the garbage collection process if no references to this object are hold. It therefore becomes an entry point")
    @CallSite(name = "garbageCollectorCall", resolvedMethods = @ResolvedMethod(receiverType = CallbackTest.FQN))
    @Override public void finalize() throws Throwable{
        CallbackTest.garbageCollectorCall();
        super.finalize();
    }
}
