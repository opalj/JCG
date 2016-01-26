package expressions;

/**
 * Created by eichberg on 26.01.16.
 */
public class Variable implements Expression {

    public final String name;

    public Variable(String name) {this.name = name;}

    public Constant eval(Map<String,Constant> values){
        return values.get(name);
    }

    public <T> T accept(ExpressionVisitor <T> visitor) {
        return visitor.visit(this);
    }

}
