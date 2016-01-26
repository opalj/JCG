package expressions;

/**
 * Created by eichberg on 26.01.16.
 */
public abstract class ExpressionVisitor<T> {

    static {
        System.out.println("Expression Visitor Version 1.00.00.00");
    }

    public abstract T visit(Constant c);

    public abstract T visit(Variable v);

    public abstract T visit(BinaryExpression b);
}
