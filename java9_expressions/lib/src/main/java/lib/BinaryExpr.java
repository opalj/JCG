package lib;

public class BinaryExpr implements Expression {
    private Expression left;
    private Expression right;
    private Operator op;

    public BinaryExpr(Expression left, Expression right, Operator op){
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public int eval(VarBinding[] env) throws UnboundVariableException {
        return op.compute(left, right, env);
    }

    public static class OperatorFactory {

        public static Operator create(char c){
            switch(c) {
                case '/': return new DivOperator();
                default: return null;
            }
        }
    }

    public interface Operator {
        int compute(Expression left, Expression right, VarBinding[] env);
    }

    private static class HiddenOperator implements Operator {

        public int compute(Expression left, Expression right, VarBinding[] env) {
            return (left.eval(env) + right.eval(env)) * 2;
        }
    }

    static class MultOperator implements Operator {
        public int compute(Expression left, Expression right, VarBinding[] env) {
            return left.eval(env) * right.eval(env);
        }
    }

    static protected class SubOperator implements Operator {
        public int compute(Expression left, Expression right, VarBinding[] env) {
            return left.eval(env) - right.eval(env);
        }
    }

    static public class DivOperator implements Operator {
        public int compute(Expression left, Expression right, VarBinding[] env) {
            int rightRes = right.eval(env);
            if(rightRes == 0)
                throw new ArithmeticException("Div by Zero");
            else
                return left.eval(env) / right.eval(env);
        }
    }
}
