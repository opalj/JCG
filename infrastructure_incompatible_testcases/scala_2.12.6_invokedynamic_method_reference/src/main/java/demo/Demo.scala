package demo

import lib.annotations.callgraph.IndirectCall;

import java.io._

/**
 * This class executes the code that is supposed to trigger the test case.
 *
 * @author Michael Eichberg
 */
class Demo {

    @IndirectCall(name = "twice", line = 17, resolvedTargets = Array("Ldemo/Demo$;"), 
    	returnType= classOf[Int], parameterTypes = Array(classOf[Int]))
    def m(m : Int => Int) : Unit = {
        println(m(2))
    }
}

object Demo {	
    def twice(i : Int) : Int = i * 2

    def main(args : Array[String]) : Unit = {
        new Demo().m(twice)
    }
}
