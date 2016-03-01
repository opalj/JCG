package fancy_expressions;

import expressions.*;

/**
 * Created by eichberg on 27.01.16.
 */
public class MultOperator extends Operator {

    public final static Operator instance = new MultOperator();

    public static class MultExpression extends BinaryExpression {

        public static final String FQN = "expressions/MultOperator$MultExpression";

        private final Expression right;
        private final Expression left;

        public MultExpression(Expression left , Expression right) {
         this.left = left;
            this.right = right;
        }
        public Expression left(){return this.left;}

        public Expression right(){return this.right;}

        public Operator operator(){return MultOperator.instance;}

        @Override public Constant eval(Map<String, Constant> values) {
            return new Constant( left.eval(values).getValue() * right.eval(values).getValue() );
        }
    }

    static BinaryExpression createBinaryExpression(Expression left,Expression right ) {
        return new MultExpression(left,right);
    }

    public String toString(){
        return "*";
    }
}

