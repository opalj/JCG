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
package callgraph.serialization.publicWriteReplaceInSuperclass;

import java.io.IOException;

import org.opalj.annotations.callgraph.CallSite;
import org.opalj.annotations.callgraph.ResolvedMethod;

/**
 * This class was used to create a class file with some well defined attributes.
 * The created class is subsequently used by several tests.
 * 
 * Serializable class extending another serializable class. The superclass implements public 
 * writeReplace method which is called whenever this class is serialized. Thus (assuming this 
 * state of code is the only one being run) no serialized instances of this class exist. Whenever 
 * this happens, writeReplace of the superclass is called twice.
 * 
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves
 * documentation purposes.
 * 
 * <!--
 * 
 * 
 * 
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE
 * STABLE IF THE CODE (E.G. IMPORTS) CHANGE.
 * 
 * 
 * 
 * 
 * -->
 * 
 * @author Roberts Kolosovs
 */
public class ExtendsSerializable extends SerializableWithPublicWriteReplace {

	private static final long serialVersionUID = 5471170331658694098L;

	@CallSite(resolvedMethods = { 
			@ResolvedMethod(receiverType = "java/io/ObjectInputStream") }, 
			name = "defaultReadObject", isStatic = false, line = 74)
	private void readObject(java.io.ObjectInputStream in) 
			throws ClassNotFoundException, IOException{ //dead code; 
														//no serialized instances of this class exist 
														//(provided the code has always been in this state)
		in.defaultReadObject(); //default implementation
	}
	
	@CallSite(resolvedMethods = { 
			@ResolvedMethod(receiverType = "java/io/ObjectOutputStream") }, 
			name = "defaultWriteObject", isStatic = false, line = 82)
	private void writeObject(java.io.ObjectOutputStream out) 
			throws IOException{ //dead code; call graph is redirected to superclass by writeReplace during serialization
		out.defaultWriteObject(); //default implementation
	}
}
