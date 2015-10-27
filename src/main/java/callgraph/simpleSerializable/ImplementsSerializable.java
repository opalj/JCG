package callgraph.simpleSerializable;

import java.io.Serializable;
import org.opalj.test.annotations.InvokedConstructor;
import org.opalj.test.annotations.InvokedMethod;

/**
 * This class was used to create a class file with some well defined attributes. The
 * created class is subsequently used by several tests.
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
public class ImplementsSerializable extends Base implements Serializable{

	private static final long serialVersionUID = 1L;

	/*Entry point via de-serialization*/
	@InvokedConstructor(receiverType = "callgraph/simpleSerializable/Base", line = 36)
	private Object readResolve(){
		return new Base();
	}
}
