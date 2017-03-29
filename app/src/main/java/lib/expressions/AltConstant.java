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

import static annotations.callgraph.AnalysisMode.*;

import annotations.callgraph.CallSite;
import annotations.callgraph.ResolvedMethod;
import annotations.properties.EntryPoint;
import testutils.CallbackTest;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;

/**
 * This class simply wraps an integer value. Defines methods to be called during (de-)serialization.
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
 *
 *
 *
 * -->
 *
 * @author Michael Eichberg
 * @author Micahel Reif
 * @author Roberts Kolosovs
 */
public class AltConstant implements Externalizable{
	
	public static final String FQN = "expressions/AltConstant";
	public static final String ObjectOutputStreamReceiverType = "java/io/ObjectOutputStream";
	public static final String ObjectInputStreamReceiverType = "java/io/ObjectInputStream";

    private int value;

    public AltConstant(int value) {
        this.value = value;
    }

    @EntryPoint(value = {OPA, CPA})
    public int getValue() {
        return value;
    }

    @EntryPoint(value = {OPA, CPA})
    public AltConstant eval(Map<String,Constant> values) {
        return this;
    }

    @EntryPoint(value = {OPA, CPA})
    public native float toFloat();

    @EntryPoint(value = {OPA, CPA})
    @CallSite(name = "readInt", resolvedMethods = {@ResolvedMethod(receiverType = ObjectInputStreamReceiverType)}, line = 107)
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	value = in.readInt();
    }

    @EntryPoint(value = {OPA, CPA})
    @CallSite(name = "writeInt", resolvedMethods = {@ResolvedMethod(receiverType = ObjectOutputStreamReceiverType)}, line = 113)
    public void writeExternal(ObjectOutput out) throws IOException {
    	out.writeInt(value);
    }
    
    @EntryPoint(value = {OPA, CPA})
    private Object writeReplace() throws ObjectStreamException {
    	return this;
    }
    
    @EntryPoint(value = {OPA, CPA})
    private Object readResolve() throws ObjectStreamException {
    	return this;
    }
    
    @EntryPoint(value = {OPA, CPA})
	@CallSite(name = "garbageCollectorCall", resolvedMethods = @ResolvedMethod(receiverType = CallbackTest.FQN), line = 129)
    public void finalize () {
		CallbackTest.garbageCollectorCall();
    	System.out.println("AltConstant object destroyed.");
    }
}
