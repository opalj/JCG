package parser;

import lib.AddExpr;
import lib.Constant;
import lib.Expression;

public interface IExpressionParser {

    Class[] supportedExpressions = {
        AddExpr.class, Constant.class
    };

    String parsingNotation();

    default Class[] getSupportedExpressions(){
        return supportedExpressions;
    }

    /**
     * This method parses a String, given in a format that is specified by the actual parser instance and
     * return an Expression.
     *
     * @param expressionString A string in a format the actual parser specifies.
     * @return null if the given expression can't be parsed, otherwise the parsed Expression object.
     */
    Expression parseExpression(String expressionString);
}
