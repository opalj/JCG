#TrivialModernReflection
The strings are directly available. No control- or data-flow analysis is required.
##TMR1
[//]: # (MAIN: tmr1.Foo)
Tests modern reflection with respect to static methods using invokeExact.

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
        name = "staticToString", returnType = String.class, line = 19,
        resolvedTargets = "Ltmr1/Foo;"
    )
    public static void main(String[] args) throws Throwable {
        Class<?> clazz = Class.forName("tmr1.Foo");
        MethodType methodType = MethodType.methodType(String.class);
        MethodHandle handle = MethodHandles.lookup().findStatic(clazz, "staticToString", methodType);
        String s = (String) handle.invokeExact();
    }
}
```
[//]: # (END)

##TMR2
[//]: # (MAIN: tmr2.Foo)
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
        name = "toString", returnType = String.class, line = 19,
        resolvedTargets = "Ltmr2/Foo;"
    )
    public static void main(String[] args) throws Throwable {
        Class<?> clazz = Class.forName("tmr2.Foo");
        MethodType methodType = MethodType.methodType(String.class);
        MethodHandle handle = MethodHandles.lookup().findVirtual(clazz, "toString", methodType);
        handle.invoke(new Foo());
    }
}
```
[//]: # (END)

##TMR3
[//]: # (MAIN: tmr3.Foo)
Tests modern reflection with respect to constructor calls.

```java
// tmr3/Foo.java
package tmr3;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import lib.annotations.callgraph.IndirectCall;
class Foo {

    @IndirectCall(name = "<init>", line = 15, resolvedTargets = "Ltmr3/Foo;")
    public static void main(String[] args) throws Throwable {
        Class<?> clazz = Class.forName("tmr3.Foo");
        MethodType methodType = MethodType.methodType(void.class);
        MethodHandle handle = MethodHandles.lookup().findConstructor(clazz, methodType);
        handle.invoke();
    }
}
```
[//]: # (END)


##TMR4
[//]: # (MAIN: tmr4.Foo)
Tests modern reflection with arguments.

```java
// tmr4/Foo.java
package tmr4;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    public String id(String param) { return param; }

    @IndirectCall(
        name = "id", returnType = String.class, parameterTypes = String.class,
        line = 19, resolvedTargets = "Ltmr4/Foo;"
    )
    public static void main(String[] args) throws Throwable {
        Class<?> clazz = Class.forName("tmr4.Foo");
        MethodType methodType = MethodType.methodType(String.class, String.class);
        MethodHandle handle = MethodHandles.lookup().findVirtual(clazz, "id", methodType);
        handle.invokeWithArguments(new Foo(), "foo");
    }
}

```

[//]: # (END)

##TMR5
[//]: # (MAIN: tmr5.Foo)
Uses modern reflection to retrieve a static field's value.

```java
// tmr5/Foo.java
package tmr5;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import lib.annotations.callgraph.CallSite;
class Foo {
    public String toString() { return "FOO"; }

    public static Foo f = new Foo();

    @CallSite(
        name = "toString", returnType = String.class, 
        line = 19, resolvedTargets = "Ltmr5/Foo;"
    )
    public static void main(String[] args) throws Throwable {
        Class<?> clazz = Class.forName("tmr5.Foo");
        MethodHandle handle = MethodHandles.lookup().findStaticGetter(clazz, "f", clazz);
        handle.invoke().toString();
    }
}

```

[//]: # (END)

##TMR6
[//]: # (MAIN: tmr6.Foo)
Uses modern reflection to retrieve a static field's value.

```java
// tmr6/Foo.java
package tmr6;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import lib.annotations.callgraph.CallSite;
class Foo {
    public String toString() { return "Foo"; }

    public Foo f;

    public Foo() {
        this.f = this;
    }

    @CallSite(
        name = "toString", returnType = String.class, 
        line = 23, resolvedTargets = "Ltmr6/Foo;"
    )
    public static void main(String[] args) throws Throwable {
        Class<?> clazz = Class.forName("tmr6.Foo");
        MethodHandle handle = MethodHandles.lookup().findGetter(clazz, "f", clazz);
        handle.invoke(new Foo()).toString();
    }
}

```

[//]: # (END)

##TMR7
[//]: # (MAIN: tmr7.Foo)
Tests modern reflection with respect to special calls and invoke.

```java
// tmr7/Foo.java
package tmr7;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    private String foo() { return "Foo"; }
    
    @IndirectCall(
        name = "foo", returnType = String.class, line = 19,
        resolvedTargets = "Ltmr7/Foo;"
    )
    public static void main(String[] args) throws Throwable {
        Class<?> clazz = Class.forName("tmr7.Foo");
        MethodType methodType = MethodType.methodType(String.class);
        MethodHandle handle = MethodHandles.lookup().findSpecial(clazz, "foo", methodType, clazz);
        handle.invoke(new Foo());
    }
}
```
[//]: # (END)

