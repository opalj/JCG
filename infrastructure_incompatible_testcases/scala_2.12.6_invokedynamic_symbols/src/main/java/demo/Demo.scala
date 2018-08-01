package demo

import lib.annotations.callgraph.IndirectCall;

import java.io._

/**
 * This class executes the code that is supposed to trigger the test case.
 *
 * @author Michael Eichberg
 */
object Demo {

    def m(m : AnyRef) : Unit = {
        println(m.toString)
    }

    @IndirectCall(name = "<init>", line = 14, resolvedTargets = Array("Lscala/Symbol;"), returnType= classOf[scala.Symbol])
    def main(args : Array[String]) : Unit = {
        m('mySymbol)
    }

}
