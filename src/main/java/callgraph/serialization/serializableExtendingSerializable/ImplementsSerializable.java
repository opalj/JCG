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
package callgraph.serialization.serializableExtendingSerializable;

import java.io.IOException;
import java.io.Serializable;

import org.opalj.annotations.callgraph.CallSite;
import org.opalj.annotations.callgraph.ResolvedMethod;
import org.opalj.annotations.callgraph.properties.EntryPoint;

/**
 * This class was used to create a class file with some well defined attributes.
 * The created class is subsequently used by several tests.
 * 
 * Serializable class with private no-args constructor. Used as a superclass for another serializable 
 * class. Implements readResolve and writeReplace, which are only called if this class is 
 * (de-)serialized directly. Also contains the readObjectNoData method, which is called if a
 * subclass which was serialized before the inheritance was added is de-serialized.
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
public class ImplementsSerializable implements Serializable {

	private static final long serialVersionUID = -6839681032530961303L;
	
	public String label; //String field to enable sensible constructor with arguments
	
	private ImplementsSerializable(){} //private no-args constructor; dead code
	
	public ImplementsSerializable(String arg){ //public constructor to enable valid subclasses
		label = arg;
	}
	
	@EntryPoint
	private void readObject(java.io.ObjectInputStream in) 
			throws ClassNotFoundException, IOException{ //entry point via de-serialization
		in.defaultReadObject(); //default implementation
	}

	@EntryPoint
	private Object readResolve(){ //entry point via de-serialization; not called if a subclass is de-serialized
		return this; //default implementation
	}
	
	@EntryPoint
	private void writeObject(java.io.ObjectOutputStream out) 
			throws IOException{ //entry point via serialization
		out.defaultWriteObject(); //default implementation
	}

	@EntryPoint
	private Object writeReplace(){ //entry point via serialization; not called if a subclass is serialized
		return this; //default implementation
	}

	@EntryPoint
	private void readObjectNoData(){ //entry point via de-serialization of subclass; 
									 //only called if serialization was done before the inheritance was added
		//do nothing;
	}
}
