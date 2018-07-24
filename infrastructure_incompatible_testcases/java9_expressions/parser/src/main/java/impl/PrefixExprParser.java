package impl;

import lib.Expression;
import parser.IExpressionParser;

public class PrefixExprParser implements IExpressionParser {

    public String parsingNotation() {
        return "Prefix";
    }

    public Expression parseExpression(String expressionString) {
        return null;
    }
}
