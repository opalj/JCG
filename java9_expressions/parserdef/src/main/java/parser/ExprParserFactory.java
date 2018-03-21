package parser;

import lib.Expression;
import lib.VarBinding;
import lib.annotations.callgraph.CallSite;
import lib.annotations.callgraph.CallSites;
import lib.annotations.callgraph.ProhibitedMethod;
import lib.annotations.callgraph.ResolvedMethod;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class ExprParserFactory {
    @CallSites({
        @CallSite(name = "getSupportedExpressions", returnType = Class[].class,
            resolvedMethods = {
                @ResolvedMethod(receiverType = "parser/impl/InfixExprParser"),
                @ResolvedMethod(receiverType = "parser/IExpressionParser")}, line = 33),
        @CallSite(name = "parsingNotation", returnType = String.class,
            resolvedMethods = {
                @ResolvedMethod(receiverType = "parser/impl/InfixExprParser"),
                @ResolvedMethod(receiverType = "parser/impl/PostfixExprParser")},
            prohibitedMethods = {
                @ProhibitedMethod(receiverType = "parser/impl/PrefixExprParser")}, line =34)})
    public static IExpressionParser create() {
        ServiceLoader<IExpressionParser> sl = ServiceLoader.load(IExpressionParser.class);
        Iterator<IExpressionParser> iter = sl.iterator();
        if (!iter.hasNext()) {
            throw new RuntimeException("No service providers found!");
        }

        IExpressionParser parser = null;
        while (iter.hasNext()) {
            parser = iter.next();
            Class[] supportedExpressions = parser.getSupportedExpressions();
            System.out.println("Found " + parser.parsingNotation() + " parser.");
        }

        return parser;
    }


    @CallSite(name = "eval", returnType = int.class,
            resolvedMethods = {
                    @ResolvedMethod(receiverType = "lib/Constant"),
                    @ResolvedMethod(receiverType = "lib/BinaryExpr")},
            prohibitedMethods = {
                    @ProhibitedMethod(receiverType = "lib/internal/Variable")}, line = 62)
    public static int[] getServiceResults(String expr) {
        ServiceLoader<IExpressionParser> sl = ServiceLoader.load(IExpressionParser.class);
        Iterator<IExpressionParser> iter = sl.iterator();
        if (!iter.hasNext()) {
            throw new RuntimeException("No service providers found!");
        }

        IExpressionParser parser = null;
        while (iter.hasNext()) {
            parser = iter.next();
            Expression pExpr = parser.parseExpression(expr);
            if(pExpr != null){
                pExpr.eval(new VarBinding[0]);
            }
        }

        return new int[0];
    }
}