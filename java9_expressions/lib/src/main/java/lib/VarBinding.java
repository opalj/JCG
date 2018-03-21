package lib;

public class VarBinding {
    private char varName;
    private int value;

    public VarBinding(char varName, int value){
        this.varName = varName;
        this.value = value;
    }

    public char getVarName(){ return varName;}
    public int getValue(){return value;}
}
