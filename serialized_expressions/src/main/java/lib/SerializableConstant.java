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
import java.io.IOException;
import java.io.ObjectStreamException;

import static lib.annotations.callgraph.AnalysisMode.*;

/**
 * This class represents a mathematical constant by simply wrapping an integer value. 
 * It has support for being saved to and loaded from an external file via the 
 * Serializable interface.
 * 
 * Defines methods to be called during (de-)serialization. These are entrypoints in the 
 * application scenario as (de-)serialization is performed in a main method in this project.
 *
 * <!--
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 *
 *
 *
 *
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 *
 *
 *
 * -->
 *
 * @author Michael Eichberg
 * @author Michael Reif
 * @author Roberts Kolosovs
 */
public class SerializableConstant extends Constant implements Serializable {
	
	public static final String SerializableConstantReceiverType = "lib/SerializableConstant";
	public static final String OOSReceiverType = "java/io/ObjectOutputStream";
	public static final String OISReceiverType = "java/io/ObjectInputStream";

    private final int value;

    public SerializableConstant(int value) {
        this.value = value;
    }

    @EntryPoint(value = {OPA, CPA})
    public int getValue() {
        return value;
    }

    @CallSite(name = "defaultWriteObject", resolvedMethods = {@ResolvedMethod(receiverType = OOSReceiverType)}, line = 90)
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();
    }
    
    @CallSite(name = "defaultReadObject", resolvedMethods = {@ResolvedMethod(receiverType = OISReceiverType)}, line = 96)
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    }
    
    @CallSite(name = "replacementFactory", returnType = SerializableConstant.class, 
    		resolvedMethods = {@ResolvedMethod(receiverType = SerializableConstantReceiverType)}, line = 103)
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
    private Object writeReplace() throws ObjectStreamException {
    	return replacementFactory();
    }
    
    @CallSite(name = "replacementFactory", returnType = SerializableConstant.class, 
    		resolvedMethods = {@ResolvedMethod(receiverType = SerializableConstantReceiverType)}, line = 110)
    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
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
