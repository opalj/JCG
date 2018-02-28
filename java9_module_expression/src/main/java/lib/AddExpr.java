package lib;

public class AddExpr extends BinaryExpr {

    public AddExpr(Expression left, Expression right) {
        super(left, right);
    }

    public int eval(VarBinding[] env) throws UnboundVariableException {
        return left.eval(env) + right.eval(env);
    }
}
