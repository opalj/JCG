package lib;

public class UnboundVariableException extends IllegalArgumentException {

    public UnboundVariableException(String msg){
        super(msg);
    }
}
