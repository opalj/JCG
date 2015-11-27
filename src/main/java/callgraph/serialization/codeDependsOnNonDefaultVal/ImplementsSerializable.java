package callgraph.serialization.codeDependsOnNonDefaultVal;

import java.io.Serializable;

import javax.management.RuntimeErrorException;

import org.opalj.annotations.callgraph.CallSite;
import org.opalj.annotations.callgraph.ResolvedMethod;
import org.opalj.annotations.callgraph.properties.EntryPointKeys;
import org.opalj.annotations.callgraph.properties.EntryPointProperty;

public class ImplementsSerializable implements Serializable {

	private int number = 42; //a field always set to 42 per default
	
	public ImplementsSerializable(){
		number = 42; //doesn't matter what the field starts at 42
	}
	
	@CallSite(resolvedMethods = { @ResolvedMethod(receiverType = "java/io/ObjectInputStream") }, name = "defaultReadObject", isStatic = false, line = 25)
	@CallSite(resolvedMethods = { @ResolvedMethod(receiverType = "callgraph/serialization/ImplementsSerializable") }, name = "deadCode", isStatic = false, line = 26)
	@EntryPointProperty(cpa=EntryPointKeys.IsEntryPoint)
	private void readObject(java.io.ObjectInputStream in) throws Exception{ //entry point via de-serialization
		if(number != 42){ //number always == 0 immediately after de-serialization 
			throw new Exception();
		}
		in.defaultReadObject(); //default implementation
		deadCode(); //call never executed
	}
	
	@EntryPointProperty(cpa=EntryPointKeys.NoEntryPoint)
	private Object readResolve(){ //no entry point; readObject terminates de-serialization with an exception
		return this; //default implementation
	}
	
	private void deadCode(){ //dead code; calling method terminated by an exception before call
		System.out.println("I feel dead inside.");
	}
}
