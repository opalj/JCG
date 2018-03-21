package impl;

import lib.AddExpr;
import lib.BinaryExpr;
import lib.Constant;
import lib.Expression;
import lib.annotations.callgraph.CallSite;
import lib.annotations.callgraph.ProhibitedMethod;
import lib.annotations.callgraph.ResolvedMethod;
import parser.IExpressionParser;

import java.util.Stack;

public class PostfixExprParser implements IExpressionParser{

    public static final boolean DEBUG = false;

    public String parsingNotation() {
        return "postfix";
    }

    @CallSite(name = "compute", returnType = int.class,
            resolvedMethods = {
                    @ResolvedMethod(receiverType = "lib/BinaryExpr$DivOperator")},
            prohibitedMethods = {
                    @ProhibitedMethod(receiverType = "lib/BinaryExpr$HiddenOperator"),
                    @ProhibitedMethod(receiverType = "lib/BinaryExpr$MultOperator"),
                    @ProhibitedMethod(receiverType = "lib/BinaryExpr$SubOperator")}, line =46)
    public Expression parseExpression(String expressionString) {
        Stack<Expression> exprStack = new Stack<Expression>();
        int i = 0;
        while(i < expressionString.length()){
            char cur = expressionString.charAt(i);
            if(cur == '+'){
                if(exprStack.size() > 1){
                    exprStack.push(new AddExpr(exprStack.pop(), exprStack.pop()));
                }
            } else if(cur > 47 && cur < 58){
                exprStack.push(new Constant(cur));
            } else {
                BinaryExpr.Operator op = BinaryExpr.OperatorFactory.create(cur);
                if(op != null && exprStack.size() > 1){
                    Expression left = exprStack.pop();
                    Expression right = exprStack.pop();
                    if(DEBUG){
                        int tmp = op.compute(left, right, null);
                        System.out.println(tmp);
                    }
                    exprStack.push(new BinaryExpr(left, right, op));
                } else {
                    return null;
                }
            }
            i++;
        }

        return (exprStack.size() == 1)? exprStack.pop() : null;
    }
}