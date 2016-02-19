/* BSD 2-Clause License:
 * Copyright (c) 2009 - 2014
 * Software Technology Group
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package callgraph.highPrecision;

import static org.opalj.annotations.callgraph.CallGraphAlgorithm.BasicVTA;
import static org.opalj.annotations.callgraph.CallGraphAlgorithm.CHA;

import org.opalj.annotations.callgraph.*;

/**
 * This class was used to create a class file with some well defined attributes. The
 * created class is subsequently used by several tests.
 * <p>
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 * <p>
 * <!--
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 * <p>
 * <p>
 * -->
 *
 * @author Michael Reif
 */
public class ExecutionClass {

    private IBase innerClass = new InnerClass();

    class InnerClass implements IBase {

        public IBase interfaceMethod() {
            return this;
        }
    }

    @CallSite(name = "interfaceMethod", line = 73, resolvedMethods = {
            @ResolvedMethod(receiverType = "callgraph/highPrecision/ExecutionClass$InnerClass"),
            @ResolvedMethod(receiverType = "callgraph/highPrecision/ConcreteClass", iff = {@ResolvingCondition(containedInMax = BasicVTA)})})
    public void testInnerClass() {
        innerClass.interfaceMethod();
    }

    @CallSite(name = "interfaceMethod", line = 89, resolvedMethods = {
            @ResolvedMethod(receiverType = "callgraph/highPrecision/ExecutionClass$1"),
            @ResolvedMethod(receiverType = "callgraph/highPrecision/ConcreteClass", iff = {@ResolvingCondition(containedInMax = CHA)}),
            @ResolvedMethod(receiverType = "callgraph/highPrecision/ExecutionClass$InnerClass", iff = {@ResolvingCondition(containedInMax = CHA)})})
    public void testAnonClass() {
        IBase anon = new IBase() {

            @Override
            public IBase interfaceMethod() {
                return this;
            }

        };
        anon.interfaceMethod();
    }
}
