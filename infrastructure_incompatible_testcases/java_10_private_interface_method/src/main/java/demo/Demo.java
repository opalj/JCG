package demo;

import lib.annotations.callgraph.CallSite;

/**
 * This test case tests a new language feature from Java 9, namely private interface methods.
 *
 * @author Michael Reif
 */
public class Demo {

    public static void main(String[] args) {
        Interface in = new InterfaceImpl();
        in.method();
    }
}

class InterfaceImpl implements Interface {

}

interface Interface {

    @CallSite(
            name = "doSomething", line = 28,
            resolvedTargets = "Ldemo/Interface;")
    default void method(){
        doSomething();
    }

    private void doSomething(){

    }
}