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

import lib.annotations.callgraph.CallSite;
import lib.annotations.callgraph.ResolvedMethod;
import lib.annotations.properties.EntryPoint;

import java.io.Serializable;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;

import static lib.annotations.callgraph.AnalysisMode.*;

/**
 * This class simply wraps an integer value. Defines methods to be called during (de-)externalization 
 * but also contains readObject and writeObject methods.
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
 * @author Roberts Kolosovs
 */
public class ExternalizableConstant extends AltConstant implements Externalizable {

	public static final String ExternalizableConstantReceiverType = "serialized_expressions/ExternalizableConstant";
	public static final String ObjectOutputStreamReceiverType = "java/io/ObjectOutputStream";
	public static final String ObjectInputStreamReceiverType = "java/io/ObjectInputStream";

    private int value;

    public ExternalizableConstant(int value) {
        this.value = value;
    }

    @EntryPoint(value = {OPA, CPA})
    public int getValue() {
        return value;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    }
    
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    @CallSite(name = "readInt", resolvedMethods = {@ResolvedMethod(receiverType = ObjectInputStreamReceiverType)}, line = 106)
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	value = in.readInt();
    }

    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    @CallSite(name = "writeInt", resolvedMethods = {@ResolvedMethod(receiverType = ObjectOutputStreamReceiverType)}, line = 112)
    public void writeExternal(ObjectOutput out) throws IOException {
    	out.writeInt(value);
    }
    
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    @CallSite(name = "replacementFactory", resolvedMethods = {@ResolvedMethod(receiverType = ExternalizableConstantReceiverType)}, line = 118)
    private Object writeReplace() throws ObjectStreamException {
    	return replacementFactory();
    }
    
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    @CallSite(name = "replacementFactory", resolvedMethods = {@ResolvedMethod(receiverType = ExternalizableConstantReceiverType)}, line = 124)
    private Object readResolve() throws ObjectStreamException {
    	return replacementFactory();
    }
    
    private Object replacementFactory() {
    	return this;
    }

	@Override
    @EntryPoint(value = {OPA, CPA})
	public Constant eval(Map values) {
		return null;
	}

	@Override
    @EntryPoint(value = {OPA, CPA})
	public Object accept(ExpressionVisitor visitor) {
		return null;
	}
}
