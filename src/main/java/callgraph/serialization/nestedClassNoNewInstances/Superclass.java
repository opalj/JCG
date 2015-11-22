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
package callgraph.serialization.nestedClassNoNewInstances;

import java.io.Serializable;
import org.opalj.test.annotations.InvokedConstructor;
import org.opalj.test.annotations.InvokedMethod;

/**
 * This class was used to create a class file with some well defined attributes. The
 * created class is subsequently used by several tests.
 * 
 * The superclass contains a nested private class not intended for use. No instances of it can be 
 * created but it has to be kept around for legacy reasons as serialized instances of the class can be 
 * de-serialized. The constructor and the readResolve methods are entry points due to de-serialization
 * but other methods of the class (someMethod) are dead code. 
 * 
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 * 
 * <!--
 * 
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 * 
 * -->
 * 
 * @author Roberts Kolosovs
 */
public class Superclass implements Serializable {

	private static final long serialVersionUID = -8714932542828113368L;

	public Superclass() {
		nestedClassValue = new NewClass(); //only instances of NewClass are created
	}
	
	private class OldClass implements ExtendsSerializable { //kept for backwards compatibility, no new instances can be created
		private static final long serialVersionUID = 1L;

		private OldClass() {} //entry point via de-serialization
		
		@InvokedConstructor(receiverType = "callgraph/nestedClassSerializable/Superclass$newClass", line = 72)
		private Object readResolve() { //entry point via de-serialization
			return new NewClass(); //create instance of new version of the class instead of an instance of old version
		}
		
		public void someMethod() { //dead code, no instances of OldClass escape this scope
			System.out.println("Executing someMethod of OldClass.");
		} 
	}
	
	private class NewClass implements ExtendsSerializable {
		private static final long serialVersionUID = 1L;

		NewClass() {} //entry point
		
		public void someMethod() { //living code
			System.out.println("Executing someMethod of NewClass.");
		}
	}
	
	private ExtendsSerializable nestedClassValue; //place where OldClass was used / NewClass is used
}
