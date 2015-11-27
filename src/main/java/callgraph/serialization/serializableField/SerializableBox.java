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
package callgraph.serialization.serializableField;

import java.io.IOException;
import java.io.Serializable;

import org.opalj.annotations.callgraph.CallSite;
import org.opalj.annotations.callgraph.ResolvedMethod;
import org.opalj.annotations.callgraph.properties.EntryPoint;

/**
 * This class was used to create a class file with some well defined attributes. The
 * created class is subsequently used by several tests.
 * 
 * This is a serializable string box intended to be used as a field type of another serializable class.
 * 
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 * 
 * <!--
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
 * @author Roberts Kolosovs
 */
public class SerializableBox implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String value; //The content of the box.
	
	@EntryPoint
	private Object writeReplace(){ //entry point via serialization
								   //called after writeObject of containing class if serialized as field
		return this; //default implementation
	}
	
	@CallSite(resolvedMethods = { 
			@ResolvedMethod(receiverType = "java/io/ObjectOutputStream") }, 
			name = "defaultWriteObject", isStatic = false, line = 80)
	@EntryPoint
	private void writeObject(java.io.ObjectOutputStream out) //entry point via serialization; called after writeReplace
			throws IOException { 
		out.defaultWriteObject();
	}
	
	@CallSite(resolvedMethods = { 
			@ResolvedMethod(receiverType = "java/io/ObjectInputStream") }, 
			name = "defaultReadObject", isStatic = false, line = 90)
	@EntryPoint
	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException { //entry point via de-serialization; 
									 //called after readObject of containing class if serialized as field
		in.defaultReadObject();
	}

	@EntryPoint
	private Object readResolve() { // entry point via de-serialization; called after readObject
		return this; //default implementation
	}
}
