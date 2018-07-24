package impl;

import lib.Expression;
import parser.IExpressionParser;

public class InfixExprParser implements IExpressionParser {

    public String parsingNotation() {
        return "Infix";
    }

    public Class[] getSupportedExpressions() {
        return new Class[0];
    }

    public Expression parseExpression(String expressionString) {
        return null;
    }
}
