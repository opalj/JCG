package app;

import lib.*;
import lib.annotations.callgraph.InvokedConstructor;
/**
 * This class is the main class of the application. The app simply makes the expression "-1" 
 * and does nothing with it.
 *
 * This class starts an interesting chain of static initializer and constructor calls:
 *  - Static initializer of Expression interface
 *  - (Static initializer of ArithmeticExpression interface if NOT called)
 *  - Static initializer of UnaryExpression class (NOT of Constant class)
 *  - Static initializer of Negation class
 *  - Static initializer of Constant class
 *  - Constructor of Constant class
 *  - Constructor of UnaryExpression class
 *  - Constructor of Negation class
 *
 * <!--
 * <b>NOTE</b><br>
 * This class is not meant to be (automatically) recompiled; it just serves documentation
 * purposes.
 *
 *
 *
 *
 *
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 *
 *
 *
 *
 *
 * -->
 *
 * @author Roberts Kolosovs
 */
public class Main {

	@SuppressWarnings("unused")
	@InvokedConstructor(receiverType = "lib/Negation", line = 45)
	@InvokedConstructor(receiverType = "lib/Constant", line = 45)
	public static void main(String[] args) {
		Negation minusOne = new Negation(new Constant(1));
	}
}
