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
package callgraph.publicReadResolveInSuperclass;

import java.io.IOException;

/**
 * This class was used to create a class file with some well defined attributes.
 * The created class is subsequently used by several tests.
 * 
 * This class contains a nested serializable class extending another serializable class. No new 
 * instances of the nested class can be created but old versions can be de-serialized. However the 
 * superclass implements a public readResolve method which replaces all de-serialized instances of 
 * this class at the end of de-serialization process. This class also has two private methods:
 * One is alive because it is called during de-serialization, other is dead because it is not.
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
public class Superclass{

	private class ExtendsSerializable extends SerializableWithPublicReadResolve{ 
		//no new instances of this class can be created 
		//but serialized instances of older versions can be de-serialized
		
		private static final long serialVersionUID = 2320664358117848370L;

		private void readObject(java.io.ObjectInputStream in) 
				throws ClassNotFoundException, IOException{ //entry point via de-serialization
															//public readResolve of superclass called immediately after
			in.defaultReadObject(); //default implementation
			livingCode();
		}
		
		private void writeObject(java.io.ObjectOutputStream out) 
				throws IOException{ //entry point via serialization
			out.defaultWriteObject(); //default implementation
		}
		
		private void livingCode(){ //living code; called by readObject during de-serialization
								   //shortly before the object of this class is replaced
			System.out.println("Still alive!");
		}
		
		public void deadMethod(){ //dead code; no instance of this class survives de-serialization
			System.out.println("I feel dead inside.");
		}
	}
}
