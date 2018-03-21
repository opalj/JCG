import impl.InfixExprParser;
import impl.PostfixExprParser;
import parser.IExpressionParser;

module parserimpl {
    requires expressions;
    requires exprparserdefinition;
    provides IExpressionParser with PostfixExprParser, InfixExprParser;
}