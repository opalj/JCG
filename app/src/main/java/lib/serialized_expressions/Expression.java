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

package serialized_expressions;

import static annotations.callgraph.AnalysisMode.*;

import java.io.Serializable;
import annotations.callgraph.CallSite;
import annotations.callgraph.ResolvedMethod;

import annotations.properties.EntryPoint;
import expressions.ExpressionVisitor;

/**
 * This class defines an application use case of the expression library and has some well defined properties
 * wrt. call graph construction. It covers primarily serialization and externalization.
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
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
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
 * @author Roberts Kolosovs
 * @param <T>
 */
public interface Expression<T> extends Serializable {

    static final int MajorVersion = 1;
    static final int MinorVersion = 0;

    Constant eval(Map<String,Constant> values);

    T accept(ExpressionVisitor <T> visitor);

    @EntryPoint(value = {OPA, CPA})
    @CallSite(name= "makeVersionName", resolvedMethods = {
    		@ResolvedMethod(receiverType = "serialized_expressions/Expression")
    }, line = 84)
    public static String getVersion(){
    	return makeVersionName();
    }
    
    @EntryPoint(value = {OPA})
    static String makeVersionName(){
    	return MajorVersion + "." + MinorVersion;
    }
}

