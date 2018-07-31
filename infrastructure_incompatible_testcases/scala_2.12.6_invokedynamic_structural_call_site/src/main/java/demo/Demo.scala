package demo

import lib.annotations.callgraph.IndirectCall;

/**
 * This class executes the code that is supposed to trigger the test case.
 *
 * @author Michael Eichberg
 */
object Demo {

    @IndirectCall(name = "x", line = 14, resolvedTargets = "Ldemo/X;", returnType= classOf[Int])
    def m(m : AnyRef{ def x() : Int}) : Unit = {
        println(m.x())
    }

    def main(args : Array[String]) : Unit = {
        m(new X())
    }

}

class X {
    def x() : Int = 666
}
