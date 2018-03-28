package lib;

import static lib.annotations.callgraph.AnalysisMode.CPA;
import static lib.annotations.callgraph.AnalysisMode.OPA;

import lib.annotations.callgraph.CallSite;
import lib.annotations.callgraph.ResolvedMethod;
import lib.annotations.properties.EntryPoint;

/**
 * This class defines an alternative implementation of the native expression
 * class implemented mostly with native expressions written in C.
 *
 * @author Mario Trageser
 */

public class NativeAddExpression extends BinaryExpression {

    private static final long serialVersionUID = 1010267344516507320L;

    public static final String FQN = "Llib/NativeAddExpression;";

    private final Object left;

    private final Object right;

    static {
        System.loadLibrary("arithmetic_operations");
    }

    @EntryPoint(value = {OPA, CPA})
    public NativeAddExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    @CallSite(name = "left", returnType = Object.class, resolvedTargets = NativeAddExpression.FQN)
    @CallSite(name = "right", returnType = Object.class, resolvedTargets = NativeAddExpression.FQN)
    @CallSite(name = "eval", returnType = Constant.class, parameterTypes = {Map.class}, resolvedTargets = {
            IncrementExpression.FQN,
            MultExpression.FQN,
            DecrementExpression.FQN,
            AddExpression.FQN,
            Constant.FQN,
            IdentityExpression.FQN,
            SquareExpression.FQN,
            SubExpression.FQN,
            Variable.FQN,
            NativeAddExpression.FQN})
    @CallSite(name = "getValue", returnType = int.class, resolvedTargets = Constant.FQN)
    @CallSite(name = "<init>", returnType = Constant.class, resolvedTargets = Constant.FQN)
    @CallSite(name = "<init>", returnType = NullPointerException.class, resolvedTargets = "java/lang/NullPointerException")
    @EntryPoint(value = {OPA, CPA})
    /**
     * This method is implemented in native/lib/ArithmeticOperations.c which is to be compiled into
     * the arithmetic_operations binary.
     */
    public native Constant eval(Map<String, Constant> values);

    @Override
    @EntryPoint(value = {OPA, CPA})
    protected Expression left() {
        return (Expression) left;
    }

    @Override
    @EntryPoint(value = {OPA, CPA})
    protected Expression right() {
        return (Expression) right;
    }

    @Override
    @EntryPoint(value = {OPA, CPA})
    public String operator() {
        return "+";
    }
}
