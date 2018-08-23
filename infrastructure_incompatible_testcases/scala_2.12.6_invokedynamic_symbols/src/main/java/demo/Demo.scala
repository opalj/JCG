package demo

import lib.annotations.callgraph.IndirectCall;

import java.io._

/**
 * This class executes the code that is supposed to trigger the test case.
 *
 * @author Michael Eichberg
 */
class Demo {

    def m(m : AnyRef) : Unit = {
        println(m.toString)
    }

    @IndirectCall(name = "<init>", line = 20, resolvedTargets = Array("Lscala/Symbol;"),
	parameterTypes = Array(classOf[String]))
    def main() : Unit = {
        m('mySymbol)
    }

}

object Demo {
	def main(args : Array[String]) : Unit = {
        new Demo().main()
    }
}
