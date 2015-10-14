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
package callgraph.opal.algorithms;

import org.opalj.test.annotations.*;

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
 * <p>
 * <p>
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * -->
 *
 * @author Michael Reif
 */
public class Superclass {

    private SubclassLevel1 someSubtype;
    private Superclass top;

    public Superclass() {
        someSubtype = new SubclassLevel2();
        top = new SubclassLevel2();
    }

    public void implementedInEachSubclass() {
        this.toString();
    }

    @CallSites({
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/Superclass", iff = {@ResolvingCondition(containedInMax = CallGraphAlgorithm.BasicVTA)})}, name = "implementedInEachSubclass", line = 92),
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/SubclassLevel1", iff = {@ResolvingCondition(containedInMax = CallGraphAlgorithm.BasicVTA)})}, name = "implementedInEachSubclass", line = 92),
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/SubclassLevel2")}, name = "implementedInEachSubclass", line = 92),

            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/SubclassLevel1", iff = {@ResolvingCondition(containedInMax = CallGraphAlgorithm.BasicVTA)})}, name = "implementedInEachSubclass", line = 93),
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/SubclassLevel2")}, name = "implementedInEachSubclass", line = 93),

            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/Superclass", iff = {@ResolvingCondition(containedInMax = CallGraphAlgorithm.BasicVTA)})}, name = "implementedInEachSubclass", line = 94),
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/SubclassLevel1", iff = {@ResolvingCondition(containedInMax = CallGraphAlgorithm.BasicVTA)})}, name = "implementedInEachSubclass", line = 94),
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/SubclassLevel2")}, name = "implementedInEachSubclass", line = 94),})
    public void callMethods() {
        getInstance().implementedInEachSubclass();
        someSubtype.implementedInEachSubclass();
        top.implementedInEachSubclass();
    }

    @CallSites({
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/Superclass", iff = {@ResolvingCondition(containedInMax = CallGraphAlgorithm.CHA)})}, name = "implementedInEachSubclass", line = 111),
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/SubclassLevel1", iff = {@ResolvingCondition(containedInMax = CallGraphAlgorithm.BasicVTA)})}, name = "implementedInEachSubclass", line = 111),
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/AltSubclassLevel2", iff = {@ResolvingCondition(containedInMax = CallGraphAlgorithm.DefaultVTA)})}, name = "implementedInEachSubclass", line = 111),
            @CallSite(resolvedMethods = {@ResolvedMethod(receiverType = "callgraph/opal/algorithms/SubclassLevel2")}, name = "implementedInEachSubclass", line = 111)})
    public void testInstanceOfExtVTABranchElimination() {
        Superclass field = null;
        if (getInstance() instanceof AltSubclassLevel2)
            field = new AltSubclassLevel2();
        else
            field = this.someSubtype;
        field.implementedInEachSubclass();
    }

    private Superclass getInstance() {
        return top;
    }
}
