import parser.IExpressionParser;

module exprparserdefinition {
    exports parser;
    requires expressions;
    uses IExpressionParser;
}