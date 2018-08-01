package demo

import lib.annotations.callgraph.IndirectCall;

import java.io._

/**
 * This class executes the code that is supposed to trigger the test case.
 *
 * @author Michael Eichberg
 */
object Demo {


    def m1() : Array[Byte] = {
        val bout = new ByteArrayOutputStream()
        val oout = new ObjectOutputStream(bout)
        oout.writeObject((i : Int) => twice(i))
        oout.close()
        bout.toByteArray
    }

    @IndirectCall(name = "twice", line = 28, resolvedTargets = Array("Ldemo/Demo;"), returnType= classOf[Int])
    def m2(d : Array[Byte] , i : Int) : Unit = {
        val oin = new ObjectInputStream(new ByteArrayInputStream(d))
        val f: Int => Int = oin.readObject().asInstanceOf[Int => Int]
        oin.close()
        println(f(i))
    }

    def main(args : Array[String]) : Unit = {
        m2(
            m1(),
            5
        )
    }

    def twice(i : Int) : Int = i * 2

}
