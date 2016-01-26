package expressions;

import java.io.Serializable;

public interface Expression extends Serializable {

    static final int MajorVersion = 1;
    static final int MinorVersion = 0;

    Constant eval(Map<String,Constant> values);

    <T> T accept(ExpressionVisitor <T> visitor);

}

