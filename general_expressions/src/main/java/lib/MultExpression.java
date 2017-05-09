package lib;

import static lib.annotations.callgraph.AnalysisMode.CPA;
import static lib.annotations.callgraph.AnalysisMode.OPA;

import lib.annotations.properties.EntryPoint;

/**
 * Mathematical expression multiplying two numbers.
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
 *
 * INTENTIONALLY LEFT EMPTY TO MAKE SURE THAT THE SPECIFIED LINE NUMBERS ARE STABLE IF THE
 * CODE (E.G. IMPORTS) CHANGE.
 *
 *
 *
 *
 *
 *
 *
 * -->
 *
 * @author Roberts Kolosovs
 */
public class MultExpression extends BinaryExpression {

        public static final String FQN = "lib/MultExpression";

        private final Expression right;
        private final Expression left;

        public MultExpression(Expression left , Expression right) {
        	this.left = left;
        	this.right = right;
        }

        @EntryPoint(value = {OPA, CPA})
        public Expression left(){return this.left;}

        @EntryPoint(value = {OPA, CPA})
        public Expression right(){return this.right;}

        @EntryPoint(value = {OPA, CPA})
        @Override public Constant eval(Map<String, Constant> values) {
            return new Constant( left.eval(values).getValue() * right.eval(values).getValue() );
        }

		@Override
        @EntryPoint(value = {OPA, CPA})
		public String operator() {
			return "*";
		}
}

