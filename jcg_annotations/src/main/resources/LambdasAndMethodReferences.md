#MethodReferences
Test cases in the presence of method references.

##MR1
[//]: # (MAIN: mr1/Class)
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
           name = "method", returnType = boolean.class, line = 17,
           resolvedTargets = "Lmr1/Class;"
    )
    public static boolean callWithMethodHandle(Interface i) {
        FIBoolean bc = i::method;
        return bc.get();
    }
    
    public static void main(String[] args){
        Class cls = new Class();
        callWithMethodHandle(cls);
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
[//]: # (MAIN: mr2/Class)
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
    public void callMethodHandle(){
        java.util.function.Supplier<String> stringSupplier = this::getTypeName;
        stringSupplier.get();
    }
    
    public static void main(String[] args){
        Class cls = new Class();
        cls.callMethodHandle();
    }
}
```
[//]: # (END)

##MR3
[//]: # (MAIN: mr3/Class)
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
    public void callMethodHandle(){
        java.util.function.Supplier<String> stringSupplier = super::getTypeName;
        stringSupplier.get();
    }
    
    public static void main(String[] args){
        Class cls = new Class();
        cls.callMethodHandle();
    }
}

class SuperClass{ 
    protected String getTypeName() { return "Lmr3/SuperClass;";}
}
```
[//]: # (END)

##MR4
[//]: # (MAIN: mr4/Class)
Tests method reference that result in an *INVOKESPECIAL* call issued by calling a static method
from a super class.
```java
// mr4/Class.java
package mr4;

import java.util.function.Supplier;
import lib.annotations.callgraph.IndirectCall;

class Class {
    
    @IndirectCall(
       name = "getTypeName", returnType = String.class, line = 13,
       resolvedTargets = "Lmr4/Class;")
    public static void main(String[] args){     
        Supplier<String> stringSupplier = Class::getTypeName;
        stringSupplier.get();
    }
    
    static String getTypeName() { return "Lmr4/Class"; }
}
```
[//]: # (END)

##MR5
[//]: # (MAIN: mr5/Class)
Tests method reference dealing with primitive type parameters.
from a super class.
```java
// mr5/Class.java
package mr5;

import java.util.function.Supplier;
import lib.annotations.callgraph.IndirectCall;

class Class {
    
    public static double sum(double a, double b) { return a + b; }
    
    @FunctionalInterface public interface FIDoubleDouble {
        double apply(double a, double b);
    }
    
    @IndirectCall(
       name = "sum", returnType = String.class, line = 18,
       resolvedTargets = "Lmr5/Class;")
    public static void main(String[] args){     
        FIDoubleDouble fidd = Class::sum;
        fidd.apply(1d,2d);
    }
}
```
[//]: # (END)

##MR6
[//]: # (MAIN: mr6/Class)
Tests method reference that result in a constructor call.
```java
// mr6/Class.java
package mr6;

import java.util.function.Supplier;
import lib.annotations.callgraph.IndirectCall;

class Class {
    
    public Class(){}
    
    @IndirectCall(
       name = "<init>", returnType = String.class, line = 15,
       resolvedTargets = "Lmr6/Class;")
    public static void main(String[] args){     
        Supplier<Class> classSupplier = Class::new;
        classSupplier.get();
    }
}
```
[//]: # (END)

##MR7
[//]: # (MAIN: mr7/Class)
Tests method reference that result in a method invocation where the method is defined in a super class.
```java
// mr7/Class.java
package mr7;

import lib.annotations.callgraph.IndirectCall;

class Class extends SuperClass{
    
    @IndirectCall(
       name = "version", returnType = String.class, line = 13,
       resolvedTargets = "Lmr7/SuperClass;")
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

##MR8
[//]: # (MAIN: mr8/Class)
Tests method reference where the method is defined in a super interface.
```java
// mr8/Class.java
package mr8;

import lib.annotations.callgraph.IndirectCall;

class Class implements Interface {
    
    @IndirectCall(
       name = "version", returnType = String.class, line = 13,
       resolvedTargets = "Lmr8/Interface;")
    public static void main(String[] args){
        Class cls = new Class();
        java.util.function.Supplier<String> classSupplier = cls::version;
        classSupplier.get();
    }
}

interface Interface{
    default String version() { return "0.2"; }
}
```
[//]: # (END)

#Lambdas
Test cases in the presence of lambdas.

##Lambda1
[//]: # (MAIN: lambda1/Class)
Tests the invocation of a lambda with a integer boxing.
```java
// lambda1/Class.java
package lambda1;

import lib.annotations.callgraph.IndirectCall;
import java.util.function.Function;

class Class {
    @IndirectCall(
       name = "lambda$main$0", returnType = String.class, line = 13,
       resolvedTargets = "Llambda1/Class;")
    public static void main(String[] args){
        Function<Integer, Boolean> isEven = (Integer a) -> a % 2 == 0;
        isEven.apply(2);
    }
}
```
[//]: # (END)

##Lambda2
[//]: # (MAIN: lambda2/Class)
Tests the invocation of a higher-order lambda predicate.
```java
// lambda2/Class.java
package lambda2;

import java.util.function.Predicate;
import lib.annotations.callgraph.IndirectCall;
import lib.annotations.callgraph.IndirectCalls;

class Class {
    @IndirectCalls({
        @IndirectCall(
           name = "lambda$main$0", returnType = String.class, line = 16, resolvedTargets = "Llambda2/Class;"),
       @IndirectCall(
           name = "lambda$main$1", returnType = String.class, line = 16, resolvedTargets = "Llambda2/Class;")
       })
    public static void main(String[] args){
        Predicate<Predicate<String>> acceptsEmptyString = (Predicate<String> p) -> p.test("");
        acceptsEmptyString.test((String s) -> s.isEmpty());
    }
}
```
[//]: # (END)

##Lambda3
[//]: # (MAIN: lambda3/Class)
Tests the invocation of nested lambda.
```java
// lambda3/Class.java
package lambda3;

import java.util.function.Predicate;
import lib.annotations.callgraph.IndirectCall;
import lib.annotations.callgraph.IndirectCalls;

class Class {
    @IndirectCalls({
        @IndirectCall(
           name = "lambda$main$0", returnType = String.class, line = 18, resolvedTargets = "Llambda3/Class;"),
       @IndirectCall(
           name = "lambda$main$1", returnType = String.class, line = 23, resolvedTargets = "Llambda3/Class;")
       })
    public static void main(String[] args){
        Predicate<String> outer = (String s) -> {
            Predicate<Character> inner = (Character c) -> c > 31;
            for(char c : s.toCharArray()){
                boolean test = inner.test(c);
                if(!test) return false;
            }
            return true;
        };
        outer.test("NestedLambdas");
    }
}
```
[//]: # (END)

##Lambda4
[//]: # (MAIN: lambda4/Class)
Tests the invocation on an object receiver captured in a lambda.
```java
// lambda4/Class.java
package lambda4;

import lib.annotations.callgraph.IndirectCalls;
import lib.annotations.callgraph.IndirectCall;

class Class {
    
    @FunctionalInterface interface Runnable {
        void run();
    }
    
    public static final Class cls = new SubClass();
    public static Class pCls = new SubClass();
    
    @IndirectCalls({
        @IndirectCall(name = "lambda$main$0", line = 26, resolvedTargets = "Llambda4/Class;"),
        @IndirectCall(name = "lambda$main$1", line = 27, resolvedTargets = "Llambda4/Class;"),
        @IndirectCall(name = "lambda$main$2", line = 28, resolvedTargets = "Llambda4/Class;")
    })
    public static void main(String[] args){
        Class lCls = new SubClass();
        Runnable psfField = () -> cls.doSomething();
        Runnable psField = () -> pCls.doSomething();
        Runnable localVar = () -> lCls.doSomething();
        
        psfField.run();
        psField.run();
        localVar.run();
    }
    
    public void doSomething(){ }
}

class SubClass extends Class {
    public void doSomething(){ }
}

class SubSubClass extends SubClass {
    public void doSomething(){ }
}
```
[//]: # (END)

##Lambda5
[//]: # (MAIN: lambda5/Class)
Tests the invocation on an object receiver captured in a lambda.
```java
// lambda5/Class.java
package lambda5;

import lib.annotations.callgraph.CallSites;
import lib.annotations.callgraph.CallSite;

class Class {
    
    @FunctionalInterface interface Runnable {
        void run();
    }
    
    public static void doSomething(){ }
    
    @CallSites({
        @CallSite(name = "equals", line = 26, resolvedTargets = "Ljava/lang/Object;"),
        @CallSite(name = "getClass", line = 27, resolvedTargets = "Ljava/lang/Object;"),
        @CallSite(name = "hashCode", line = 28, resolvedTargets = "Ljava/lang/Object;"),
        @CallSite(name = "notify", line = 29, resolvedTargets = "Ljava/lang/Object;"),
        @CallSite(name = "notifyAll", line = 30, resolvedTargets = "Ljava/lang/Object;"),
        @CallSite(name = "toString", line = 31, resolvedTargets = "Ljava/lang/Object;"),
        @CallSite(name = "wait", line = 32, resolvedTargets = "Ljava/lang/Object;")
    })
    public static void main(String[] args) throws InterruptedException {
        Runnable lambda = () -> doSomething();
        Object o = new Object();
        lambda.equals(o);
        lambda.getClass();
        lambda.hashCode();
        lambda.notify();
        lambda.notifyAll();
        lambda.toString();
        lambda.wait();
    }
}
```
[//]: # (END)