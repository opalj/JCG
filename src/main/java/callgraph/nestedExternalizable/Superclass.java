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
package callgraph.nestedExternalizable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.opalj.test.annotations.InvokedConstructor;

/**
 * This class was used to create a class file with some well defined attributes. The
 * created class is subsequently used by several tests.
 * 
 * Class contains a nested private class never instantiated. Similarly to example in callgraph/nestedClassSerializable 
 * the class can still be de-serialized. However here the de-serialization always leads to an exception 
 * thrown by readExternal before readResolve is ever called.
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
public class Superclass implements Externalizable {

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException { //entry point via de-serialization
		//no fields to read; do nothing
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException { //entry point via serialization
		//no fields to write; do nothing
	}
	
	private class DeadInternalClass implements Externalizable { //no instances are created; can still be de-serialized

		@Override
	    @InvokedConstructor(receiverType = "java/io/InvalidClassException", line = 81)
		public void readExternal(ObjectInput in) throws IOException, //entry point via de-serialization
				ClassNotFoundException { 
			throw new InvalidClassException(null); //throw an exception whenever a de-serialization is attempted
		}

	    @InvokedConstructor(receiverType = "callgraph/nestedExternalizable/DeadInternalClass", line = 86)
		private Object readResolve(){ //entry point via de-serialization; called after readExternal
			return new DeadInternalClass(); //dead code; every de-serialization results in an exception thrown
		}
		
		@Override
		public void writeExternal(ObjectOutput out) throws IOException { //entry point via serialization
			//no fields to write; do nothing
		}
		
	}

}
