package demo

import lib.annotations.callgraph.IndirectCall;

import java.io._

/**
 * This class executes the code that is supposed to trigger the test case.
 *
 * @author Michael Eichberg
 */
class Demo {

    @IndirectCall(name = "twice", line = 21, resolvedTargets = Array("Ldemo/Demo$;"),
        returnType= classOf[Int], parameterTypes = Array(classOf[Int]))
    def m2(d : Array[Byte] , i : Int) : Unit = {
        val bin = new ByteArrayInputStream(d)
        val oin = new ObjectInputStream(bin)
        val f: Int => Int = oin.readObject().asInstanceOf[Int => Int]
        oin.close()
        println(f(i))
    }
}

object Demo {

    def m1() : Array[Byte] = {
        val bout = new ByteArrayOutputStream()
        val oout = new ObjectOutputStream(bout)
        oout.writeObject((i : Int) => twice(i))
        oout.close()
        bout.toByteArray
    }

    def twice(i : Int) : Int = i * 2

    def main(args : Array[String]) : Unit = {
        new Demo().m2(
            m1(),
            5
        )
    }
}