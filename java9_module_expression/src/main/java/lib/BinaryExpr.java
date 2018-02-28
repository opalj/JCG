package lib;

public abstract class BinaryExpr implements Expression {
    Expression left;
    Expression right;

    public BinaryExpr(Expression left, Expression right){
        this.left = left;
        this.right = right;
    }
}
