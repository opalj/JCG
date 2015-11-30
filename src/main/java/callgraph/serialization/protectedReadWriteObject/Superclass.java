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
package callgraph.serialization.protectedReadWriteObject;

import static org.opalj.annotations.callgraph.properties.AnalysisMode.OPA;

import java.io.IOException;
import java.io.Serializable;

import org.opalj.annotations.callgraph.CallSite;
import org.opalj.annotations.callgraph.ResolvedMethod;
import org.opalj.annotations.callgraph.properties.EntryPoint;

/**
 * This class was used to create a class file with some well defined attributes.
 * The created class is subsequently used by several tests.
 * 
 * Serializable class with protected read/writeObject methods nested in another class.
 * No instances of the class are created but it still can be de-serialized, accessed 
 * via public superclass or by extending the package. However during (de-)serialization 
 * the protected read/writeObject methods are not called as they need to be private to 
 * be called during (de-)serialization.
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
public class Superclass {

	protected class ProtectedReadAndWriteObject extends ImplementsSerializable implements Serializable{

		private static final long serialVersionUID = -8192945638933075918L;

		@CallSite(resolvedMethods = { 
				@ResolvedMethod(receiverType = "java/io/ObjectInputStream") }, 
				name = "defaultReadObject", isStatic = false, line = 88)
		@EntryPoint(OPA)
		protected void readObject(java.io.ObjectInputStream in) //no entry point via de-serialization;
																//this method must be private
																//de-serialization still completes
				throws ClassNotFoundException, IOException{
			in.defaultReadObject(); //default implementation
		}
		
		@CallSite(resolvedMethods = { 
				@ResolvedMethod(receiverType = "java/io/ObjectOutputStream") }, 
				name = "defaultWriteObject", isStatic = false, line = 88)
		@EntryPoint(OPA)
		protected void writeObject(java.io.ObjectOutputStream out) //no entry point via serialization;
																   //this method must be private
																   //serialization still completes
				throws IOException{
			out.defaultWriteObject(); //default implementation
		}
	}
}
