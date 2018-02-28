package parser;

import lib.Expression;

public interface IExpressionParser {

    String parsingNotation();

    /**
     * This method parses a String, given in a format that is specified by the actual parser instance and
     * return an Expression.
     *
     * @param expressionString A string in a format the actual parser specifies.
     * @return null if the given expression can't be parsed, otherwise the parsed Expression object.
     */
    Expression parseExpression(String expressionString);
}
