package lib;

public class AddExpr extends BinaryExpr {

    public AddExpr(Expression left, Expression right) {
        super(left, right, new Operator() {
            public int compute(Expression left, Expression right, VarBinding[] env){
                return left.eval(env) + right.eval(env);
            }
        });
    }
}
