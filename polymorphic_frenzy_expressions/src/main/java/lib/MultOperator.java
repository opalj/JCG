package lib;

import static lib.annotations.callgraph.AnalysisMode.CPA;
import static lib.annotations.callgraph.AnalysisMode.OPA;

import lib.annotations.properties.EntryPoint;
import lib.*;

/**
 * Created by eichberg on 27.01.16.
 */
public class MultOperator extends Operator {
	
    public static final String FQN = "lib/MultOperator";

    public final static Operator instance = new MultOperator();

    public static class MultExpression extends BinaryExpression {

        public static final String FQN = "lib/MultOperator$MultExpression";

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
        public Operator operator(){return MultOperator.instance;}

        @EntryPoint(value = {OPA, CPA})
        @Override public Constant eval(Map<String, Constant> values) {
            return new Constant( left.eval(values).getValue() * right.eval(values).getValue() );
        }
    }

    @EntryPoint(value = {OPA})
    BinaryExpression createBinaryExpression(Expression left,Expression right ) {
        return new MultExpression(left,right);
    }

    @EntryPoint(value = {OPA, CPA})
    public String toString(){
        return "*";
    }
}

