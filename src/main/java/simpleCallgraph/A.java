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
package simpleCallgraph;

import org.opalj.test.annotations.InvokedConstructor;
import org.opalj.test.annotations.CallSite;
import org.opalj.test.annotations.ResolvedMethod;
import org.opalj.test.annotations.CallSites;

/**
 * This class was used to create a class file with some well defined properties. The
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
 * INTENTIONALLY LEFT EMPTY (THIS AREA CAN BE EXTENDED/REDUCED TO MAKE SURE THAT THE
 * SPECIFIED LINE NUMBERS ARE STABLE.
 * <p>
 * <p>
 * <p>
 * <p>
 * -->
 *
 * @author Marco Jacobasch
 * @author Michael Reif
 */
public class A implements Base {

    Base b = new B();

    @Override
    @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "simpleCallgraph/B")}, name = "callOnInstanceField", line = 65)
    public String callOnInstanceField() {
        return b.callOnInstanceField();
    }

    @Override
    @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "simpleCallgraph/B")}, name = "callOnConstructor", line = 72)
    @InvokedConstructor(receiverType = "simpleCallgraph/B", line = 72)
    public void callOnConstructor() {
        new B().callOnConstructor();
    }

    @Override
    @CallSites({
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "simpleCallgraph/A")}, name = "callOnMethodParameter", line = 83),
            @CallSite(name = "callOnConstructor", line = 84, resolvedMethods = {@ResolvedMethod(receiverType = "simpleCallgraph/A"),
                    @ResolvedMethod(receiverType = "simpleCallgraph/B")})})
    public void callOnMethodParameter(Base b) {
        if (b != null) {
            this.callOnMethodParameter(null);
            b.callOnConstructor();
        }
    }

    @CallSites({
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "simpleCallgraph/A")}, name = "callOnConstructor", line = 92),
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "simpleCallgraph/B")}, name = "callOnConstructor", line = 93)})
    public void directCallOnConstructor() {
        new A().callOnConstructor();
        new B().callOnConstructor();
    }

}
