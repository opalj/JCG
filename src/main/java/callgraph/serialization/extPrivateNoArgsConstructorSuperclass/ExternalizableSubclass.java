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
package callgraph.serialization.extPrivateNoArgsConstructorSuperclass;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.opalj.annotations.callgraph.AccessedField;
import org.opalj.annotations.callgraph.properties.EntryPointKeys;
import org.opalj.annotations.callgraph.properties.EntryPointProperty;

/**
 * This class was used to create a class file with some well defined attributes.
 * The created class is subsequently used by several tests.
 * 
 * This class implements externalizable and extends a class with private no-args constructor. 
 * Unlike with plain Serializable this is not a problem. The field of the superclass is not 
 * explicitly read or written by read/writeExternal and thus set to the default value after
 * de-serialization.
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
public class ExternalizableSubclass extends Superclass implements
		Externalizable {

	public String label; //field to read and write during (de-)serialization
	
	public ExternalizableSubclass(){ //explicit no-args constructor to accommodate the superclass
		super(42);
	}
	
	@Override
	@EntryPointProperty(cpa=EntryPointKeys.IsEntryPoint)
	@AccessedField(declaringType = ExternalizableSubclass.class, fieldType = String.class, name = "label", line = 81)
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException { //called during de-serialization
		label = in.readUTF(); //read label previously written as UTF8
	}

	@Override
	@EntryPointProperty(cpa=EntryPointKeys.IsEntryPoint)
	@AccessedField(declaringType = ExternalizableSubclass.class, fieldType = String.class, name = "label", line = 88)
	public void writeExternal(ObjectOutput out) throws IOException { //called during serialization
		out.writeUTF(label); //write label as UTF8 into file
	}

}
