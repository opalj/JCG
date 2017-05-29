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

	private final Object left;

	private final Object right;

	static {
		System.loadLibrary("arithmetic_operations");
	}

	public NativeAddExpression(Expression left, Expression right) {
		this.left = left;
		this.right = right;
	}

	@Override
	@CallSite(name = "left", resolvedMethods = { @ResolvedMethod(receiverType = "expressions/AddExpression"),
			@ResolvedMethod(receiverType = "expressions/SubExpression"),
			@ResolvedMethod(receiverType = "fancy_expressions/MultExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/AddExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/SubExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/MulExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/DivExpression") })
	@CallSite(name = "right", resolvedMethods = { @ResolvedMethod(receiverType = "expressions/AddExpression"),
			@ResolvedMethod(receiverType = "expressions/SubExpression"),
			@ResolvedMethod(receiverType = "fancy_expressions/MultExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/AddExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/SubExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/MulExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/DivExpression") })
	@CallSite(name = "eval", resolvedMethods = { @ResolvedMethod(receiverType = "expressions/AddExpression"),
			@ResolvedMethod(receiverType = "expressions/SubExpression"),
			@ResolvedMethod(receiverType = "fancy_expressions/MultExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/AddExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/SubExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/MulExpression"),
			@ResolvedMethod(receiverType = "expressions/jni/DivExpression") })
	@CallSite(name = "getValue", resolvedMethods = { @ResolvedMethod(receiverType = "expressions/Constant") })
	@CallSite(name = "<init>", resolvedMethods = { @ResolvedMethod(receiverType = "expressions/Constant") })
	@CallSite(name = "<init>", resolvedMethods = { @ResolvedMethod(receiverType = "java/lang/NullPointerException") })
	@EntryPoint(value = {OPA, CPA})
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
