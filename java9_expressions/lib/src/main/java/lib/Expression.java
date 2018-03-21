package lib;

/**
 * @author Michael Reif
 */
public interface Expression {

    int eval(VarBinding[] env) throws UnboundVariableException;
}
