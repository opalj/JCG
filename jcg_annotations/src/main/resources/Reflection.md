#TrivialReflection
The strings are directly available. No control- or data-flow analysis is required.

##TR1
[//]: # (MAIN: tr2.Foo)
Test reflection with respect to static methods.

```java
// tr2/Foo.java
package tr2;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    static String staticToString() { return "Foo"; }

    @IndirectCall(
        name = "staticToString", returnType = String.class, line = 12,
        resolvedTargets = "Ltr2/Foo;"
    )
    public static void main(String[] args) throws Exception {
        Foo.class.getDeclaredMethod("staticToString").invoke(null);
    }
}
```
[//]: # (END)

##TR2
[//]: # (MAIN: tr3.Foo)
Test reflection with respect to instance methods.

```java
// tr3/Foo.java
package tr3;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    public String n() { return "Foo"; }

    @IndirectCall(
        name = "n", returnType = String.class, line = 12,
        resolvedTargets = "Ltr3/Foo;"
    )
    void m() throws Exception {
        Foo.class.getDeclaredMethod("n").invoke(this);
    }

    public static void main(String[] args) throws Exception { new Foo().m(); }
}
```
[//]: # (END)

##TR3
[//]: # (MAIN: tr4.Foo)
Test reflection with respect to instance methods retrieved via getMethod.

```java
// tr4/Foo.java
package tr4;

import lib.annotations.callgraph.IndirectCall;
public class Foo {
    public String n() { return "Foo"; }

    @IndirectCall(
        name = "n", returnType = String.class, line = 12,
        resolvedTargets = "Ltr4/Foo;"
    )
    void m() throws Exception {
        Foo.class.getMethod("n").invoke(this);
    }

    public static void main(String[] args) throws Exception { new Foo().m(); }
}
```
[//]: # (END)

##TR4
[//]: # (MAIN: tr5.Foo)
Test reflection with respect to methods having parameters.

```java
// tr5/Foo.java
package tr5;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    public static String m(String parameter) { return "Foo" + parameter; }

    @IndirectCall(
        name = "m", returnType = String.class, parameterTypes = String.class, line = 12,
        resolvedTargets = "Ltr5/Foo;"
    )
    public static void main(String[] args) throws Exception {
        Foo.class.getDeclaredMethod("m", String.class).invoke(null, "Bar");
    }
}
```
[//]: # (END)

##TR5
[//]: # (MAIN: tr6.Foo)
Test reflection with respect to constructors.

```java
// tr6/Foo.java
package tr6;

import lib.annotations.callgraph.CallSite;
class Foo {
    public static void verifyCall(){ /* do something */ }
    
    @CallSite(name="verifyCall", line=8, resolvedTargets = "Ltr6/Foo;")
    public Foo(String s) { Foo.verifyCall(); }

    public static void main(String[] args) throws Exception {
        Foo.class.getConstructor(String.class).newInstance("ASD");
    }
}
```
[//]: # (END)


##TR6
[//]: # (MAIN: tr7.Foo)
Test reflection with respect to the default constructor.

```java
// tr7/Foo.java
package tr7;

import lib.annotations.callgraph.CallSite;
class Foo {
    public static void verifyCall(){ /* do something */ }
    
    @CallSite(name="verifyCall", line=8, resolvedTargets = "Ltr7/Foo;")
    public Foo() { Foo.verifyCall(); }

    public static void main(String[] args) throws Exception {
        Foo.class.newInstance();
    }
}
```
[//]: # (END)

##TR7
[//]: # (MAIN: tr8.Foo)
Test reflection used to retrieve a field.

```java
// tr8/Foo.java
package tr8;

import java.lang.reflect.Field;
import lib.annotations.callgraph.IndirectCall;
public class Foo {
    private Object field;

    @IndirectCall(
        name = "toString", returnType = String.class, line = 18,
        resolvedTargets = "Ltr8/Foo;"
    )
    public static void main(String[] args) throws Exception {
        Foo foo = new Foo();
        foo.field = new Foo();

        Field field = Foo.class.getDeclaredField("field");
        Object o = field.get(foo);
        o.toString();
    }

    public String toString() {
        return "Foo";
    }
}
```
[//]: # (END)

##TR8
[//]: # (MAIN: tr9.Foo)
Test reflection used to retrieve a field via getField.

```java
// tr9/Foo.java
package tr9;

import java.lang.reflect.Field;
import lib.annotations.callgraph.IndirectCall;
public class Foo {
    public Object field;

    @IndirectCall(
        name = "toString", returnType = String.class, line = 18,
        resolvedTargets = "Ltr9/Foo;"
    )
    public static void main(String[] args) throws Exception {
        Foo foo = new Foo();
        foo.field = new Foo();

        Field field = Foo.class.getField("field");
        Object o = field.get(foo);
        o.toString();
    }

    public String toString() {
        return "Foo";
    }
}
```
[//]: # (END)

##TR9
[//]: # (MAIN: tr10.Foo)
Test reflection with respect to forName.

```java
// tr10/Foo.java
package tr10;

import lib.annotations.callgraph.CallSite;
class Foo {
    public static void verifyCall(){ /* do something */ }

    public static void main(String[] args) throws Exception {
        Class.forName("tr10.Bar");
    }
}

class Bar {
    static {
        staticInitializerCalled();
    }
    
    @CallSite(name="verifyCall", line=19, resolvedTargets = "Ltr10/Foo;")
    static private void staticInitializerCalled(){
        Foo.verifyCall();
    }
}
```
[//]: # (END)


#LocallyResolvableReflection
The complete information is locally (intra-procedurally) available.
##LRR1
[//]: # (MAIN: lrr1.Foo)
Test reflection where the target class is dynamically decided.

```java
// lrr1/Foo.java
package lrr1;

import lib.annotations.callgraph.CallSite;
class Foo {
    public static void verifyCall(){ /* do something */ }

    public static void main(String[] args) throws Exception {
        m(args.length % 2 == 0);
    }

    static void m(boolean b) throws Exception {
        Class.forName(b ? "lrr1.Bar" : "lrr1.Baz");
    }
}

class Bar {
    static {
        staticInitializerCalled();
    }
    
    @CallSite(name="verifyCall", line=23, resolvedTargets = "Llrr1/Foo;")
    static private void staticInitializerCalled(){
        Foo.verifyCall();
    }
}


class Baz {
    static {
        staticInitializerCalled();
    }
    
    @CallSite(name="verifyCall", line=35, resolvedTargets = "Llrr1/Foo;")
    static private void staticInitializerCalled(){
        Foo.verifyCall();
    }
}
```
[//]: # (END)

##LRR2
[//]: # (MAIN: lrr2.Foo)
Tests reflection where the target class is dynamically decided and the result of a StringBuilder.

```java
// lrr2/Foo.java
package lrr2;

import lib.annotations.callgraph.CallSite;
class Foo {
    public static void verifyCall(){ /* do something */ }
    
    static void m(boolean b) throws Exception {
        StringBuilder className = new StringBuilder("lrr2.Ba");
        if (b)
            className.append("r");
        else
            className.append("z");
        Class.forName(className.toString());
    }

    public static void main(String[] args) throws Exception {
        m(args.length % 2 == 0);
    }
}

class Bar {
     static {
         staticInitializerCalled();
     }
     
     @CallSite(name="verifyCall", line=28, resolvedTargets = "Llrr2/Foo;")
     static private void staticInitializerCalled(){
         Foo.verifyCall();
     }
 }
 
 
 class Baz {
     static {
         staticInitializerCalled();
     }
     
     @CallSite(name="verifyCall", line=40, resolvedTargets = "Llrr2/Foo;")
     static private void staticInitializerCalled(){
         Foo.verifyCall();
     }
 }
```
[//]: # (END)

##LRR3
[//]: # (MAIN: lrr3.Foo)
Test reflection where the target class is dynamically decided from a locally set instance field.

```java
// lrr3/Foo.java
package lrr3;

import lib.annotations.callgraph.CallSite;
class Foo {
    private String className;
    
    public static void verifyCall(){ /* do something */ }

    public static void main(String[] args) throws Exception {
        Foo foo = new Foo();
        foo.className = "lrr3.Bar";
        Class.forName(foo.className);
    }
}

class Bar {
     static {
         staticInitializerCalled();
     }
     
     @CallSite(name="verifyCall", line=23, resolvedTargets = "Llrr3/Foo;")
     static private void staticInitializerCalled(){
         Foo.verifyCall();
     }
 }
```
[//]: # (END)

#ContextSensitiveReflection
The concrete strings require information about the context.

##CSR1
[//]: # (MAIN: csr1.Foo)
The class name is passed as an argument.

```java
// csr1/Foo.java
package csr1;

import lib.annotations.callgraph.CallSite;
class Foo {
    public static void verifyCall(){ /* do something */ }

    static void m(String className) throws Exception {
        Class.forName(className);
    }

    public static void main(String[] args) throws Exception {
        Foo.m("csr1.Bar");
    }
}

class Bar {
     static {
         staticInitializerCalled();
     }
     
     @CallSite(name="verifyCall", line=23, resolvedTargets = "Lcsr1/Foo;")
     static private void staticInitializerCalled(){
         Foo.verifyCall();
     }
 }
```
[//]: # (END)

##CSR2
[//]: # (MAIN: csr2.Foo)
The class name is unknown.

```java
// csr2/Foo.java
package csr2;

import lib.annotations.callgraph.CallSite;
public class Foo {
    public static void verifyCall(){ /* do something */ }

    static void m(String className) throws Exception {
        Class.forName(className);
    }

    public static void main(String[] args) throws Exception {
        Foo.m(args[0]);
    }
}

class Bar {
     static {
         staticInitializerCalled();
     }
     
     @CallSite(name="verifyCall", line=23, resolvedTargets = "Lcsr2/Foo;")
     static private void staticInitializerCalled(){
         Foo.verifyCall();
     }
 }
```
[//]: # (END)

##CSR3
[//]: # (MAIN: csr3.Foo)
Test reflection with respect to a public static field.

```java
// csr3/Foo.java
package csr3;

import lib.annotations.callgraph.CallSite;
class Foo {
    public static String className;
    
    public static void verifyCall(){ /* do something */ }

    static void m() throws Exception {
        Class.forName(Foo.className);
    }

    public static void main(String[] args) throws Exception {
        Foo.className = "csr3.Bar";
        Foo.m();
    }
}

class Bar {
     static {
         staticInitializerCalled();
     }
     
     @CallSite(name="verifyCall", line=22, resolvedTargets = "Lcsr3/Foo;")
     static private void staticInitializerCalled(){
         Foo.verifyCall();
     }
 }
```
[//]: # (END)


##CSR4
[//]: # (MAIN: csr4.Foo)
Test reflection with respect to System properties.

```java
// csr4/Foo.java
package csr4;

import lib.annotations.callgraph.CallSite;
class Foo {
    public static String className;
    
    public static void verifyCall(){ /* do something */ }

    static void m() throws Exception {
    	String className = System.getProperty("className");
        Class.forName(className);
    }

    public static void main(String[] args) throws Exception {
		Properties props = System.getProperties();
		props.put("className", "csr4.Bar");
		System.setProperties(props);      
        Foo.m();
    }
}

class Bar {
     static {
         staticInitializerCalled();
     }
     
     @CallSite(name="verifyCall", line=22, resolvedTargets = "Lcsr4/Foo;")
     static private void staticInitializerCalled(){
         Foo.verifyCall();
     }
 }
```
[//]: # (END)