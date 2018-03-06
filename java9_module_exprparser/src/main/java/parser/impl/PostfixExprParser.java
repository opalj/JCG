package parser.impl;

import lib.AddExpr;
import lib.Constant;
import lib.Expression;
import parser.IExpressionParser;

import java.util.Stack;

public class PostfixExprParser implements IExpressionParser{

    public String parsingNotation() {
        return "postfix";
    }


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
                return null;
            }
            i++;
        }

        return (exprStack.size() == 1)? exprStack.pop() : null;
    }
}
