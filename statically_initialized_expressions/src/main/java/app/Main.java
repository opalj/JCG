package app;

import static lib.annotations.callgraph.AnalysisMode.CPA;
import static lib.annotations.callgraph.AnalysisMode.DESKTOP_APP;
import static lib.annotations.callgraph.AnalysisMode.OPA;

import lib.*;
import lib.annotations.properties.EntryPoint;
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

    @EntryPoint(value = {DESKTOP_APP, OPA, CPA})
	public static void main(String[] args) {
    	Constant c = new Constant(1);
    	Negation n = new Negation(c);
    	n.printName();
    	n.eval(new Map<>());
	}
}
