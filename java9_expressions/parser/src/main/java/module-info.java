import parser.impl.InfixExprParser;
import parser.impl.PostfixExprParser;
import parser.IExpressionParser;

module exprinfixparser {
    requires expressions;
    requires exprparserdefinition;
    provides IExpressionParser with PostfixExprParser, InfixExprParser;
}