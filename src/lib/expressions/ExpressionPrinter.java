package expressions;

import annotations.CGNote;

public final class ExpressionPrinter extends ExpressionVisitor<String> {

    @CGNote(value = "[CINIT]",
            description = "static initializers are called by the jvm;" +
                    " called when the class is somehow referenced the first time." +
                    "I.e. invoking a constructor of a subclass or calling a static method.")
    private static ExpressionPrinter instance;

    static {
        instance = new ExpressionPrinter();
    }

    private ExpressionPrinter() {
    }

    public String visit(Constant c) {
        return String.valueOf(c.getValue());
    }

    public String visit(Variable v) {
        return v.name;
    }

    public String visit(BinaryExpression b) {
        return "(" + b.left().toString() + b.operator().toString() + b.right().toString() + ")";
    }

    public synchronized static void printExpression(Expression e) {
        System.out.print(e.accept(instance));
    }

}
