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
package callgraph.serialization.serializableAndExternalizable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.opalj.test.annotations.CallSite;
import org.opalj.test.annotations.ResolvedMethod;

/**
 * This class was used to create a class file with some well defined attributes. The
 * created class is subsequently used by several tests.
 * 
 * This class implements both Externalizable and Serializable interfaces. Apart from being redundant 
 * (as Externalizable is a sub-interface of Serializable) this doesn't generate any strange behavior. 
 * The readExternal defines the manner of reading the class data from the stream and afterwards the 
 * readResolve method is called to reconstruct the class from the data read.
 * 
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 * 
 * <!--
 * 
 * 
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 * 
 * 
 * -->
 * 
 * @author Roberts Kolosovs
 */
public class SerializableAndExternalizable implements Serializable,
		Externalizable {

	@Override
	public void readExternal(ObjectInput arg0) throws IOException,
			ClassNotFoundException { //entry point via de-serialization
		//no fields to read; do nothing
	}
	
	@CallSite(resolvedMethods = { @ResolvedMethod(receiverType = "callgraph/serializableAndExternalizable/SerializableAndExternalizable") }, name = "deadMethod", isStatic = false, line = 75)
	private void readObject(java.io.ObjectInputStream in) throws IOException { //dead code; superseded by readExternal
		deadMethod();
	}
	
	private Object readResolve(){ //entry point via de-serialization
		return this; //living code; called during de-serialization after readExternal
	}

	private Object writeReplace(){ //entry point via serialization
		return this; //living code; called during serialization before writeExternal
	}
	
	@Override
	public void writeExternal(ObjectOutput arg0) throws IOException { //entry point via serialization
		//no fields to write; do nothing
	}

	@CallSite(resolvedMethods = { @ResolvedMethod(receiverType = "callgraph/serializableAndExternalizable/SerializableAndExternalizable") }, name = "deadMethod", isStatic = false, line = 93)
	private void writeObject(java.io.ObjectOutputStream out) throws IOException{ //dead code; superseded by writeExternal
		deadMethod();
	}
	
	private void deadMethod(){ //dead code; all callers are dead
		System.out.println("I feel dead inside.");
	}

}
