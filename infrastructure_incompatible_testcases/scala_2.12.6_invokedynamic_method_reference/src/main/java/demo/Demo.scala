package demo

import lib.annotations.callgraph.IndirectCall;

import java.io._

/**
 * This class executes the code that is supposed to trigger the test case.
 *
 * @author Michael Eichberg
 */
object Demo {

    @IndirectCall(name = "twice", line = 14, resolvedTargets = Array("Ldemo/Demo;"), returnType= classOf[Int])
    def m(m : Int => Int) : Unit = {
        println(m(2))
    }

    def main(args : Array[String]) : Unit = {
        m(twice)
    }

    def twice(i : Int) : Int = i * 2

}
