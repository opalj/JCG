package lib;

public class Constant implements Expression {

    private int value;

    public Constant(int value){
        this.value = value;
    }

    public int eval(VarBinding[] env) throws UnboundVariableException {
        return value;
    }

    public static class ConstantFactory {


    }

    static public class Zero extends Constant{
        public Zero(){
            super(0);
        }
    }

    static protected class One extends Constant{
        public One(){
            super(1);
        }
    }

    static private class FourthyTwo extends Constant{
        public FourthyTwo(){
            super(42);
        }
    }
}
