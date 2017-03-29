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

import annotations.documentation.CGNote;
import annotations.properties.EntryPoint;

import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;

import static annotations.callgraph.AnalysisMode.CPA;
import static annotations.callgraph.AnalysisMode.OPA;
import static annotations.documentation.CGCategory.SERIALIZABILITY;

/**
 * This class is no longer used, but if we deserialize an old expression it replaces itself
 * with an instance of BinaryExpressio
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
public class BinExpression implements Expression {
	
	public static final String FQN = "expressions/BinExpression"; 

    private Expression left;
    private Expression right;
    private Operator operator;

    // This class can no longer be instantiated by regular code... (the JVM can still do it)
    private BinExpression() {
    }

    @EntryPoint(value = {CPA, OPA})
    protected Expression left() {
        throw new UnknownError();
    }

    @EntryPoint(value = {CPA, OPA})
    protected Expression right() {
        throw new UnknownError();
    }

    @EntryPoint(value = {CPA, OPA})
    protected Operator operator() {
        throw new UnknownError();
    }

    @EntryPoint(value = {OPA, CPA})
    @Override public Constant eval(Map<String, Constant> values) {
        throw new UnknownError();
    }

    @EntryPoint(value = {OPA, CPA})
    @Override public <T> T accept(ExpressionVisitor<T> visitor) {
        throw new UnknownError();
    }

    @CGNote(
            value = SERIALIZABILITY,
            description = "This method is called if an old instance of this class is read from some stream.")
    @EntryPoint(value = {OPA, CPA})
    private Object readResolve() throws ObjectStreamException {
        if(operator == PlusOperator.instance)
            return new PlusOperator.AddExpression(left,right);
        else
            throw new StreamCorruptedException("unexpected binary expression");
    }
}

