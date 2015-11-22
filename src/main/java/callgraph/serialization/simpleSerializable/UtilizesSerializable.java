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
package callgraph.serialization.simpleSerializable;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class was used to create a class file with some well defined attributes. The
 * created class is subsequently used by several tests.
 * 
 * This class utilizes a serializable class and performs basic (de-)serialization routine.
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
public class UtilizesSerializable {

	public ImplementsSerializable serializableField = new ImplementsSerializable(); //serializable field to be (de-)serialized
	
	public void performSerialization() throws IOException{ //basic serialization routine
		FileOutputStream fileOut = new FileOutputStream("/tmp/serializableField.ser");
		ObjectOutputStream out = new ObjectOutputStream(fileOut);
		out.writeObject(serializableField); //call writeReplace and writeObject of serializable class
		out.close();
		fileOut.close();
	}
	
	public void performDeserialization() 
			throws IOException, ClassNotFoundException{ //basic de-serialization routine
		FileInputStream fileIn = new FileInputStream("/tmp/serializableField.ser");
		ObjectInputStream in = new ObjectInputStream(fileIn);
		serializableField = (ImplementsSerializable) in.readObject(); //call readObject and readResolve of serializable class
		in.close();
		fileIn.close();
	}
}
