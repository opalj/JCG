package expressions;

import java.lang.reflect.Method;

/**
 * Created by eichberg on 26.01.16.
 */
public abstract class BinaryExpression implements Expression {

    abstract protected Expression left();

    abstract protected Expression right();

    abstract protected Operator operator();

    public <T> T accept(ExpressionVisitor <T> visitor){
        return visitor.visit(this);
    }

    public static BinaryExpression createBasicBinaryExpression(
            String operator,
            final Expression left,
            final Expression right) throws Exception{
        final Operator op = (Operator) Class.forName("expression."+operator+"Operator").newInstance();

        return new BinaryExpression(){

            @Override public Constant eval(Map<String, Constant> values) {
                throw new UnsupportedOperationException();
            }

            @Override protected Expression left() {
                return left;
            }

            @Override protected Expression right() {
                return right;
            }

            @Override protected Operator operator() {
                return op;
            }
        };
    }

    public static BinaryExpression createBinaryExpression(
            String operator,
            final Expression left,
            final Expression right) throws Exception{
        Class<?> operatorClass = null;
        try {
            operatorClass = Class.forName("expressions." + operator + "Operator");
        } catch (ClassNotFoundException cnfe) {
            operatorClass = Class.forName(operator);
        }
        Method m = operatorClass.getDeclaredMethod("createBinaryExpression",Expression.class,Expression.class);
        m.setAccessible(true);
        return (BinaryExpression) m.invoke(null,left,right);

    }
}
