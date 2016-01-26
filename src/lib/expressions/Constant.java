package expressions;

import java.io.Serializable;

/**
 * Simply wraps an integer value.
 */
public class Constant implements Expression, Serializable{

    private final int value;

    public Constant(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Constant eval(Map<String,Constant> values) {
        return this;
    }

    public <T> T accept(ExpressionVisitor <T> visitor) {
        return visitor.visit(this);
    }

    public native float toFloat();

}
