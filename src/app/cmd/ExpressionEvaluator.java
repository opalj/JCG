package cmd;

import static java.lang.Integer.parseInt;

import annotations.CGNote;
import expressions.*;

import java.util.Arrays;

import static expressions.BinaryExpression.createBinaryExpression;
import static expressions.PlusOperator.AddExpression;


public class ExpressionEvaluator {

    private static final Map<String,Constant> NO_VARIABLES = (Map<String,Constant>)Map.EMPTY;


    // 2 34 + 23 Plus == 59
    // 2 3 + 5 Plus 2 fancy_expressions.MultOperator
    // 2 3 + 5 Plus 2 fancy_expressions.MultOperator Crash
    public static void main(final String[] args) {

        String[] expressions = args.clone();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @CGNote(value = "[INV_CALLBACK]", description="invisible callback because no native code is involved; the call graph seems to be complete")
            @CGNote(value="[NOTE]",description="the related method <Thread>.dispatchUncaughtException is not dead")
            @Override public void uncaughtException(Thread t, Throwable e) {
                String msg = "unexpected error while processing "+ Arrays.deepToString(args);
                System.out.println(msg);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(){

            // This is an entry point!
            @Override public void run() {
                System.out.println("It was a pleasure to evaluate your expression!");
                super.run();
            }
        });

        synchronized (ExpressionEvaluator.class) {
            // all methods of the class object of ExpressionEvaluation may be called...
            // unless we analyze the "JVM" internal implementation
            if (!Thread.holdsLock(ExpressionEvaluator.class)) throw new UnknownError();

        if(args.length == 0) {
            throw new IllegalArgumentException("no expression");
        }

        Stack<Constant> values = new Stack();

        for (String expr: expressions) {
            try {
                values.push(new Constant(parseInt(expr)));
            } catch (NumberFormatException nfe) {
                // so it is not a number...
                if(expr.equals( "+")){
                    values.push(new AddExpression(values.pop(),values.pop()).eval(NO_VARIABLES));
                } else {
                    try {
                        values.push(createBinaryExpression(expr, values.pop(), values.pop()).eval(NO_VARIABLES));
                    } catch (Throwable t){
                        throw new IllegalArgumentException("unsupported symbol "+expr,t);
                    }
                }
            } finally {
                System.out.println("processed the symbol "+expr);
            }
        }

        if(values.size() > 1) {
            throw new IllegalArgumentException("the expression is not valid missing operator");
        }

        System.out.println("result "+values.pop().getValue());
        }
    }

}
