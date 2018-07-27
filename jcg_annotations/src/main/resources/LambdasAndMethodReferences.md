#MethodReferences
Test cases in the presence of method references.

##MR1
[//]: # (MAIN: mr1.Class)
Tests method reference that deals with interface default methods (Java 8 or higher).

```java
// mr1/Class.java
package mr1;

import lib.annotations.callgraph.IndirectCall;

class Class implements Interface {
    
    @FunctionalInterface public interface FIBoolean {
        boolean get();
    }
    
    @IndirectCall(
           name = "method", returnType = boolean.class, line = 18,
           resolvedTargets = "Lmr1/Interface;"
    )
    public static void main(String[] args){
        Class cls = new Class();
        FIBoolean bc = cls::method;
        bc.get();
    }
}

interface Interface { 
    default boolean method() {
        return true;
    }
}
```
[//]: # (END)

##MR2
[//]: # (MAIN: mr2.Class)
Tests method reference that result in an *INVOKESPECIAL* call issued by calling a private method.

```java
// mr2/Class.java
package mr2;

import lib.annotations.callgraph.IndirectCall;

class Class {
    
    private String getTypeName() { return "Lmr2/Class;";}
    
    @IndirectCall(
       name = "getTypeName", returnType = String.class, line = 14,
       resolvedTargets = "Lmr2/Class;")
    public void callViaMethodReference(){
        java.util.function.Supplier<String> stringSupplier = this::getTypeName;
        stringSupplier.get();
    }
    
    public static void main(String[] args){
        Class cls = new Class();
        cls.callViaMethodReference();
    }
}
```
[//]: # (END)

##MR3
[//]: # (MAIN: mr3.Class)
Tests method reference that result in an *INVOKESPECIAL* call issued by calling a protected method
from a super class.

```java
// mr3/Class.java
package mr3;

import lib.annotations.callgraph.IndirectCall;

class Class extends SuperClass {
    
    @IndirectCall(
       name = "getTypeName", returnType = String.class, line = 12,
       resolvedTargets = "Lmr3/SuperClass;")
    public void callViaMethodReference(){
        java.util.function.Supplier<String> stringSupplier = super::getTypeName;
        stringSupplier.get();
    }
    
    public static void main(String[] args){
        Class cls = new Class();
        cls.callViaMethodReference();
    }
}

class SuperClass{ 
    protected String getTypeName() { return "Lmr3/SuperClass;";}
}
```
[//]: # (END)

##MR4
[//]: # (MAIN: mr.Class)
Tests method reference that result in an *INVOKESPECIAL* call issued by calling a static method
from a super class.

```java
// mr/Class.java
package mr;

import java.util.function.Supplier;
import lib.annotations.callgraph.IndirectCall;

class Class {
    
    @IndirectCall(
       name = "getTypeName", returnType = String.class, line = 13,
       resolvedTargets = "Lmr/Class;")
    public static void main(String[] args){     
        Supplier<String> stringSupplier = Class::getTypeName;
        stringSupplier.get();
    }
    
    static String getTypeName() { return "Lmr/Class"; }
}
```
[//]: # (END)

##MR5
[//]: # (MAIN: mr.Class)
Tests method reference dealing with primitive type parameters.
from a super class.

```java
// mr/Class.java
package mr;

import java.util.function.Supplier;
import lib.annotations.callgraph.IndirectCall;

class Class {
    
    public static double sum(double a, double b) { return a + b; }
    
    @FunctionalInterface public interface FIDoubleDouble {
        double apply(double a, double b);
    }
    
    @IndirectCall(
       name = "sum", returnType = double.class, line = 19,
       resolvedTargets = "Lmr/Class;")
    public static void main(String[] args){     
        FIDoubleDouble fidd = Class::sum;
        fidd.apply(1d,2d);
    }
}
```
[//]: # (END)

##MR6
[//]: # (MAIN: mr.Class)
Tests method reference that result in a constructor call.

```java
// mr/Class.java
package mr;

import java.util.function.Supplier;
import lib.annotations.callgraph.IndirectCall;

class Class {
    
    public Class(){}
    
    @IndirectCall(
       name = "<init>", line = 14, resolvedTargets = "Lmr/Class;")
    public static void main(String[] args){     
        Supplier<Class> classSupplier = Class::new;
        classSupplier.get();
    }
}
```
[//]: # (END)

##MR7
[//]: # (MAIN: mr.Class)
Tests method reference that result in a method invocation where the method is defined in a super class.

```java
// mr/Class.java
package mr;

import lib.annotations.callgraph.IndirectCall;

class Class extends SuperClass{
    
    @IndirectCall(
       name = "version", returnType = String.class, line = 13,
       resolvedTargets = "Lmr/SuperClass;")
    public static void main(String[] args){
        Class cls = new Class();
        java.util.function.Supplier<String> classSupplier = cls::version;
        classSupplier.get();
    }
}

class SuperClass {
    public String version() { return "1.0"; }
}
```
[//]: # (END)

#Lambdas
Test cases in the presence of lambdas.

##Lambda1
[//]: # (MAIN: lambda.Class)
Tests the invocation of a lambda with a integer boxing.

```java
// lambda/Class.java
package lambda;

import lib.annotations.callgraph.IndirectCall;
import java.util.function.Function;

class Class {
    @IndirectCall(
       name = "doSomething", line = 11, resolvedTargets = "Llambda/Class;")
    public static void main(String[] args){
        Function<Integer, Boolean> isEven = (Integer a) -> {
            doSomething();
            return a % 2 == 0;
        };
        isEven.apply(2);
    }
    
    private static void doSomething(){
        // call in lambda
    }
}
```
[//]: # (END)

##Lambda2
[//]: # (MAIN: lambda.Class)
Tests the invocation on an object receiver captured in a lambda.

```java
// lambda/Class.java
package lambda;

import lib.annotations.callgraph.IndirectCall;

class Class {
    
    public static void doSomething(){ }
    
    @IndirectCall(name = "doSomething", line = 17, resolvedTargets = "Llambda/LambdaProvider;")
    public static void main(String[] args) {
        Runnable lambda = LambdaProvider.getRunnable();
        
        lambda.run();
    }
}

class LambdaProvider {
        
    public static void doSomething(){
        /* do something */
    }
    
    public static lambda.Runnable getRunnable(){
        return () -> LambdaProvider.doSomething(); 
    }
}
```
```java
// lambda/Runnable.java
package lambda;

@FunctionalInterface interface Runnable {
    void run();
}
```
[//]: # (END)

##Lambda3
[//]: # (MAIN: lambda.Class)
Tests the invocation of a lambda when it was written to an array and later retrieved and applied.

```java
// lambda/Class.java
package lambda;

import lib.annotations.callgraph.IndirectCall;

class Class {
    
     @FunctionalInterface interface Runnable {
        void run();
    }
    
    public static void doSomething(){
        /* do something */
    }

    public static Runnable[] lambdaArray = new Runnable[10];

    @IndirectCall(name = "doSomething", line = 25, resolvedTargets = "Llambda/Class;")       
    public static void main(String[] args) {
        Runnable r1 = () -> doSomething();
        lambdaArray[0] = r1;
        Runnable same = lambdaArray[0];
        
        same.run();
    }
}

final class Math {
    public static int PI(){
        return 3;
    }
}
```
[//]: # (END)

##Lambda4
[//]: # (MAIN: lambda.Class)
Tests the invocation of an intersection type lambda. 

```java
// lambda/Class.java
package lambda;

import lib.annotations.callgraph.IndirectCall;

class Class {
    
    public interface MyMarkerInterface1 {}
    public interface MyMarkerInterface2 {}
    
    public @FunctionalInterface interface Runnable {
        void run();
    }
    
    public static void doSomething(){
        /* do something */
    }

    @IndirectCall(name = "doSomething", line = 21, resolvedTargets = "Llambda/Class;")       
    public static void main(String[] args) {
        Runnable run = (Runnable & MyMarkerInterface1 & MyMarkerInterface2) () -> doSomething();
        run.run();
    }
}
```
[//]: # (END)