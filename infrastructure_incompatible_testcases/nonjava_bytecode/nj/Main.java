package nj;

import lib.annotations.callgraph.DirectCall;

interface SuperIntf {
    /* METHOD 1 */ default void m(){ Helper.println("SuperIntf.m"); };
}

interface Intf extends SuperIntf {
    // In Java it is not possible to have a subclass that defines a
    // method with the same name and signature, but which is static.
    // This is, however, possible at the bytecode level and the JVM
    // will call the default method.
/* METHOD 2 */    //static void m(){ Helper.println("Intf.m"); };
}

interface SubIntf extends Intf {}

class C implements SubIntf {}

class Helper {
    public static void println(java.lang.String s) {
        System.out.println(s);
    }
}

public class Main {
    public static void main(String[] args) {
        run(new C());
    }

    @DirectCall(name = "m", line = 35, resolvedTargets = "Lnj/SuperIntf;", prohibitedTargets = "Lnj/Intf;")
    public static void run(SubIntf c) {
        // This invokes the default method from SuperIntf
    	c.m();
    }

}