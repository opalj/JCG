package lib;

public class Constant implements Expression {

    private int value;

    public Constant(int value){
        this.value = value;
    }

    public int eval(VarBinding[] env) throws UnboundVariableException {
        return value;
    }
}
