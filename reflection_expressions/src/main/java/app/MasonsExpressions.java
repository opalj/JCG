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

import lib.annotations.callgraph.*;
import lib.annotations.properties.EntryPoint;

import static lib.annotations.callgraph.AnalysisMode.*;
import static lib.UnaryOperator.*;

import lib.*;

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
 * 
 * -->
 *
 *
 * @author Michael Eichberg
 * @author Michael Reif
 * @author Roberts Kolosovs
 */
public class MasonsExpressions {

    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
	@CallSite(name = "createBinaryExpression", returnType = BinaryExpression.class,
            resolvedMethods = {@ResolvedMethod(receiverType = BinaryExpression.FQN)},
            resolution = TargetResolution.REFLECTIVE,
            parameterTypes = {String.class, Expression.class, Expression.class},
            line = 76)
	@InvokedConstructor(receiverType = "lib/Constant", line = 77)
	@InvokedConstructor(receiverType = "lib/Constant", line = 78)
	public static void main(final String[] args) throws Exception {
		BinaryExpression masonsAngle = BinaryExpression.createBinaryExpression("Plus", 
				(Expression) UnaryExpression.createUnaryExpressions(SQUARE, new Constant(3)), 
				(Expression) UnaryExpression.createUnaryExpressions(SQUARE, new Constant(4)));
	}
}
