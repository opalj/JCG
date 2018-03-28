#TrivialModernReflection
The strings are directly available. No control- or data-flow analysis is required.
##TMR1
Tests modern reflection with respect to static methods using invokeExact and .
```java
// tmr1/Foo.java
package tmr1;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import lib.annotations.callgraph.IndirectCall;
class Foo { 
    static String staticToString() { return "Foo"; }
    
    @IndirectCall(
        name = "staticToString", returnType = String.class, line = 23,
        resolvedTargets = "Ltmr1/Foo;"
    )
    public static void main(String[] args) throws Throwable {
        Class<?> clazz = Class.forName("tmr1.Foo");
        MethodType methodType = MethodType.methodType(String.class);
        MethodHandle handle = MethodHandles.lookup().findStatic(clazz, "staticToString", methodType);
        handle.invokeExact();
    }
}
```
[//]: # (END)

##TMR2
Tests modern reflection with respect to virtual calls and invoke.
```java
// tmr2/Foo.java
package tmr2;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    public String toString() { return "Foo"; }
    
    @IndirectCall(
        name = "toString", returnType = String.class, line = 23,
        resolvedTargets = "Ltmr2/Foo;"
    )
    public static void main(String[] args) throws Throwable {
        Class<?> clazz = Class.forName("tmr2.Foo");
        MethodType methodType = java.lang.invoke.MethodType.methodType(String.class);
        MethodHandle handle = MethodHandles.lookup().findVirtual(clazz, "toString", methodType);
        handle.invoke(new Foo());
    }
}
```
[//]: # (END)

##TMR3
Tests modern reflection with respect to virtual calls and invoke.
```java
// tmr3/Foo.java
package tmr2;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    public String toString() { return "Foo"; }

    @IndirectCall(
        name = "<init>", returnType = String.class, line = 23,
        resolvedTargets = "Ltmr3/Foo;"
    )
    public static void main(String[] args) throws Throwable {
        Class<?> clazz = Class.forName("Foo");
        MethodType methodType = java.lang.invoke.MethodType.methodType(void.class);
        MethodHandle handle = MethodHandles.lookup().findConstructor(clazz, methodType);
        handle.invoke().toString();
    }
}
```
[//]: # (END)
