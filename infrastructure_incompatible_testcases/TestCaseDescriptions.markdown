#TestCaseDescriptions
Documents all test cases that contained within the ```infrastructure_incompatible_testcases``` project
that comprises all test cases that cannot be build with the automated test pipeline.

#ClassLoading
Please see the descriptions from the ```classloading.md``` file which can be found in the ```testcases```
project.

#Java10TestCases
This category comprises all test cases that must be compiled with Java 10.

##NVC6 (java_10_private_interface_method)
This test case tests a new language feature from Java 10, namely private interface methods.
```Interface``` declares a private method ```doSomething()V``` which is called in the interface's
default method ```method()V```. Hence, the special invocation in ```method()V``` must thus be resolved
to ```doSomething()V```.
```java
package demo;

import lib.annotations.callgraph.DirectCall;

public class Demo {

    public static void main(String[] args) {
        Interface in = new InterfaceImpl();
        in.method();
    }
}

class InterfaceImpl implements Interface {

}

interface Interface {

    @DirectCall(
            name = "doSomething", line = 23,
            resolvedTargets = "Ldemo/Interface;")
    default void method(){
        doSomething();
    }

    private void doSomething(){

    }
}
```
##J10SIM2 (java_10_private_static_interface_method)
This test case tests a new language feature from Java 10, namely private static interface methods.
```Interface``` declares a private static method ```privateTarget()V``` which is called in the interface's
static method ```publicTarget()V```. Hence, the static invocation in ```publicTarget()V``` must thus be resolved
to ```privateTarget()V```.
```java
package demo;

import lib.annotations.callgraph.DirectCall;

public class Demo {

    @DirectCall(name = "publicTarget", line = 9, resolvedTargets = "Ldemo/Interface;")
    public static void main(String[] args) {
        Interface.publicTarget();
    }
}

interface Interface {

    @DirectCall(name = "privateTarget", line = 17, resolvedTargets = "Ldemo/Interface;")
    static void publicTarget(){
        privateTarget();
    }

    private static void privateTarget(){}
}
```

##Lambda5 (java_10_string_concats)
This class pertains to a JCG lambda test case with respect to Java 10 String concatenations.
When String concatenation is used within Java 10, the constants are not longer combined using
```StringBuilder.append()``` but rather by using a invokedynamic-based concatenation function.

Compiled with Java 10, the concatenation of the local variable ```s1``` and the String
```"String Concat"``` will result in a use the invokedynamic-based string concatenation mechanism.
The concatenated String will then be stored in ```s2```.
```java
package demo;

import lib.annotations.callgraph.IndirectCall;

public class Demo {

    @IndirectCall(
            line = 12, name = "makeConcatWithConstants",
            resolvedTargets = "Ljava/lang/invoke/StringConcatFactory;")
    public static void main(String[] args) {
        String s1 = "Java 10";
        String s2 = s1 + " String Concat";
    }
}
```

##TMR9 (load_method_handle)
This test case tests the proper handling of a method handle that is saved within a class file's
constant pool via ```CONSTANT_MethodHandle_info``` attribute. Hence, no API calls are required to
retrieve the method handle, instead it is just loaded over the ```ldc``` bytecode instruction.

#NonJavaBytecode
This category groups test cases that must be created manually and cannot be created from compiling
valid Java code. However, the resulting bytecode is still valid and does occur in real-world code.

All test cases within this category **cannot** be compiled using our pipeline. 

##NJ1
[//]: # (MAIN: nj.Main)
In Java it is not possible to have a subclass that defines a method with the same name and
signature, but which is static. This is, however, possible at the bytecode level and the JVM
will call the default method.
 
This test case pertains to an evolution scenario and cannot be compiled out of the box. Please
proceed with the following steps to obtain all class files required by the test case:
 
Step one:
Compile the Main.java as is with the method with the markers *METHOD 1* (LINE 33) and *METHOD 3*
(LINE 58) uncommented and the method with the marker *METHOD 2* (LINE 37) commented. Then take the
resulting class files of ```C```, ```Helper```, ```SuperIntf```, ```SubIntf```, and "Main" and put tom into a
folder named ```nj```.
 
Step two:
Now comment the lines with the markers *METHOD 1* and *METHOD 3* and uncomment the line with the
marker *METHOD 2*. Then compile "Main.java" again and copy ```Intf``` class' class file to the folder
named ```nj```.
 
Step three:
Next to the folder ```nj``` add a folder named "META-INF" with the respective "MANIFEST.MF" file which
declares ```nj/Main``` as main class.
 *
Step four:
Compress both folders and create an executeable ".jar" file.
 */
```java
// nj/Main.java  
package nj;

import lib.annotations.callgraph.DirectCall;

interface SuperIntf {
/* METHOD 1 */ default void m(){ Helper.println("SuperIntf.m"); };
}

interface Intf extends SuperIntf {
/* METHOD 2 */  //  static void m(){ Helper.println("Intf.m"); };
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

    @DirectCall(name = "m", line = 58, resolvedTargets = "Lnj/SuperIntf;", prohibitedTargets = "Lnj/Intf;")
    public static void run(SubIntf c) {
        // This invokes the default method from SuperIntf
/* METHOD 3 */ c.m();
    }

}
```
[//]: # (END)

##NJ2
[//]: # (MAIN: nj.Demo)
On Java's source level it is impossible to define two methods with matching names and signatures that
only vary in the method's return type. However, this can be done by crafting bytecode which is then
still valid.

>Please note: The project ```nonjava_bytecode2```, contained in the
```infrastructure_incompatible_testcases```, provides in ```scala/nj/EngineerBytecode``` a class that
is meant to engineer an instance of this case using OPAL's bytecode engineering DSL.  
```java
// nj/Demo.java
package nj;

import lib.annotations.callgraph.DirectCalls;
import lib.annotations.callgraph.DirectCall;

/**
 * @author Michael Reif
 */
public class Demo {

    @DirectCalls({
        @DirectCall(name="method", line=17, returnType = Object.class, resolvedTargets = "Lnj/Target;"),
        @DirectCall(name="method", line=18, returnType = String.class, resolvedTargets = "Lnj/Target;")
    })
    public static void main(String[] args) {
        Target t = new Target();
        t.method("42");
        t.method("42");
    }
}

class Target {

    public Object method(String s){
        System.out.println(s);
        System.out.println("Object");
        return s;
    }

    public String method(String s){
        return s;
    }
}

```
[//]: # (END)

#NonJavaLambdas
This category contains test cases w.r.t. ```invokedynamic```'s that aren't generated using Java but
other JVM-hosted programming languages as Scala.

##Lambda6 
##Lambda7
##Lambda8
##Lambda9
Does not exist yet. Should contain an example of an ```invokedynamic``` created by Groovy. Mostly,
the Groovy compiler uses ```InvokeStaticMethodHandles``` with some receiver from the following
package: ```"org/codehaus/groovy"```.

Please note that even if there is no test case yet, the respective Hermes query does check for Groovy
invokedynamics with this particular pattern.
##Lambda10 (scala_2.12.6_invokedynamic_method_reference)
This test cases triggers a standard Scala invokedynamic 
           
```scala
package demo

import java.io._

class Demo {

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

```