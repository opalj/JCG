package expressions;

/**
 * Created by eichberg on 27.01.16.
 */
public class PlusOperator extends Operator {

    public final static Operator instance = new PlusOperator();

    public static class AddExpression extends BinaryExpression {

        private final Expression right;
        private final Expression left;

        public AddExpression(Expression left , Expression right) {
         this.left = left;
            this.right = right;
        }
        public Expression left(){return this.left;}

        public Expression right(){return this.right;}

        public Operator operator(){return PlusOperator.instance;}

        @Override public Constant eval(Map<String, Constant> values) {
            return new Constant( left.eval(values).getValue() + right.eval(values).getValue() );
        }
    }

    static BinaryExpression createBinaryExpression(Expression left,Expression right ) {
        return new AddExpression(left,right);
    }

    public String toString(){
        return "+";
    }
}

