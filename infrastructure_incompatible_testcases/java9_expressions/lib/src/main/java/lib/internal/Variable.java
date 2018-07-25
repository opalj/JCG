package lib.internal;

import lib.Expression;
import lib.UnboundVariableException;
import lib.VarBinding;

public class Variable implements Expression {

    char varName;

    public Variable(char varName){
        this.varName = varName;
    }

    public int eval(VarBinding[] env) {
        int i = 0;
        while(i < env.length){
            VarBinding cur = env[i++];
            if(cur.getVarName() == varName){
                return cur.getValue();
            }
        }

        throw new UnboundVariableException("Unbound variable: " + varName + "!");
    }
}
