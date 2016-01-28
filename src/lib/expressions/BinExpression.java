package expressions;

import annotations.CGNote;

import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;

// This class is no longer used, but if we deserialize an old expression it replaces itself
// with an instance of BinaryExpression
public class BinExpression implements Expression {

    private Expression left;
    private Expression right;
    private Operator operator;

    // This class can no longer be instantiated by regular code... (the JVM can still do it)
    private BinExpression() {
    }

    protected Expression left() {
        throw new UnknownError();
    }

    protected Expression right() {
        throw new UnknownError();
    }

    protected Operator operator() {
        throw new UnknownError();
    }

    @Override public Constant eval(Map<String, Constant> values) {
        throw new UnknownError();
    }

    @Override public <T> T accept(ExpressionVisitor<T> visitor) {
        throw new UnknownError();
    }

    @CGNote(
            value = "[SER]",
            description = "This method is called if an old instance of this class is read from some stream.")
    private Object readResolve() throws ObjectStreamException {
        if(operator == PlusOperator.instance)
            return new PlusOperator.AddExpression(left,right);
        else
            throw new StreamCorruptedException("unexpected binary expression");
    }
}

