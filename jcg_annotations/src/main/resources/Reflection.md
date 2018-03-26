#TrivialReflection
The strings are directly available. No control- or data-flow analysis is required.
##TR1
Test reflection with respect to static methods.
```java
// tr1/Foo.java
package tr1;

import lib.annotations.callgraph.IndirectCall;
class Foo { 
    static String staticToString() { return "Foo"; }
    
    @IndirectCall(name = "staticToString", declaringClass = "Ltr1/Foo;", returnType = String.class)
    public static void main(String[] args) throws Exception { 
        Class.forName("tr1.Foo").getDeclaredMethod("staticToString").invoke(null); 
    }
}
```
[//]: # (END)

##TR2
Test reflection with respect to instance methods.
```java
// tr2/Foo.java
package tr2;

import lib.annotations.callgraph.IndirectCall;
class Foo { 
    public String toString() { return "Foo"; }
    
    @IndirectCall(name = "toString", declaringClass = "Ltr2/Foo;", returnType = String.class)
    void m() throws Exception {
        Class.forName("Foo").getDeclaredMethod("toString").invoke(this);
    }
    
    public static void main(String[] args) throws Exception { new Foo().m(); }
}
```
[//]: # (END)

##TR3
Test reflection with respect to constructors.
```java
// tr3/Foo.java
package tr3;

import lib.annotations.callgraph.IndirectCall;
class Foo { 
    public Foo(String s) {    }
    
    @IndirectCall(name = "<init>", declaringClass = "Ltr3/Foo;", parameterTypes = String.class)
    public static void main(String[] args) throws Exception {
        Class.forName("tr3.Foo").getConstructor(String.class).newInstance("ASD");
    }
}
```
[//]: # (END)

##TR4
Test reflection with respect to the default constructor
```java
// tr4/Foo.java
package tr4;

import lib.annotations.callgraph.IndirectCall;
class Foo { 
    public Foo() {    }
    
    @IndirectCall(name = "<init>", declaringClass = "Ltr3/Foo;")
    static void main(String[] args) throws Exception { 
        Class.forName("tr4.Foo").newInstance(); 
    }
}
```
[//]: # (END)

#LocallyResolvableReflection
The complete information is locally (intra-procedurally) available.
##LRR1
Test reflection with respect to static methods where the target class is dynamically decided
```java
// lrr1/Foo.java
package lrr1;

import lib.annotations.callgraph.IndirectCall;
class Bar {
    static String staticToString() { return "bar"; }
}
class Foo { 
    static String staticToString() { return "foo"; }
    
    public static void main(String[] args) throws Exception {
        m(args.length % 2 == 0);
    }
    
    @IndirectCall(name = "staticToString", declaringClass = "Llrr1/Foo;", returnType = String.class)
    static void m(boolean b) throws Exception { 
        Class.forName(b ? "lrr1.Foo" : "lrr1.Bar").getDeclaredMethod("staticToString").invoke(null); 
    }
}
```
[//]: # (END)

##LRR2
```java
// lrr2/Foo1.java
package lrr2;

import lib.annotations.callgraph.IndirectCall;
class Foo1 { 
    static String staticToString() { return "1"; }
    
    @IndirectCall(name = "staticToString", declaringClass = "Llrr2/Foo1;", returnType = String.class)
    @IndirectCall(name = "staticToString", declaringClass = "Llrr2/Foo2;", returnType = String.class)       
    static void m(boolean b) throws Exception {
        Class.forName("lrr2.Foo" + (b ? 1 : 2)).getDeclaredMethod("staticToString").invoke(null); 
    }
    
    public static void main(String[] args) throws Exception { 
        m(args.length % 2 == 0);
    }
}
class Foo2 { 
    static String staticToString() { return "2"; }
}
```
[//]: # (END)

##LRR3
```java
// lrr3/Foo.java
package lrr3;

import lib.annotations.callgraph.IndirectCall;
class Foo { 
    static String staticToString1() { return "1"; }
    static String staticToString2() { return "2"; }
    
    @IndirectCall(name = "staticToString1", declaringClass = "Llrr2/Foo;", returnType = String.class)  
    @IndirectCall(name = "staticToString2", declaringClass = "Llrr2/Foo;", returnType = String.class)  
    static void m(boolean b) throws Exception {
        Class.forName("lrr3.Foo").getDeclaredMethod(b ? "staticToString1" : "staticToString2").invoke(null); 
    }
    
    public static void main(String[] args) throws Exception { 
        m(args.length % 2 == 0);
    }
}
```
[//]: # (END)

#ContextSensitiveReflection
The concrete strings require information about the context.

## CSR1
The method name is passed as an argument.
```java
// csr1/Foo.java
package csr1;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    static String staticToString1() { return "1"; }
    static String staticToString2() { return "2"; }
    
    
    @IndirectCall(name = "staticToString1", declaringClass = "Lcsr1/Foo;", returnType = String.class) 
    static void m(String methodName) throws Exception {
        Class.forName("csr1.Foo").getDeclaredMethod(methodName).invoke(null);
    }
    
    public static void main(String[] args) throws Exception {
        m("staticToString1");
    }
}
```
[//]: # (END)

## CSR2
The class name is passed as an argument.
```java
// csr2/Foo.java
package csr2;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    static String staticToString() { return "Foo"; }
    
    
    @IndirectCall(name = "staticToString", declaringClass = "Lcsr2/Foo;", returnType = String.class) 
    static void m(String className) throws Exception {
        Class.forName(className).getDeclaredMethod("staticToString").invoke(null);
    }
    
    public static void main(String[] args) throws Exception {
        m("Foo");
    }
}

class Bar {
    static String staticToString() { return "Bar"; }
}
```
[//]: # (END)

## CSR3
The method name is unknown.
```java
// csr3/Foo.java
package csr3;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    static String staticToString1() { return "1"; }
    static String staticToString2() { return "2"; }
    
    
    @IndirectCall(name = "staticToString1", declaringClass = "Lcsr3/Foo;", returnType = String.class)
    @IndirectCall(name = "staticToString2", declaringClass = "Lcsr3/Foo;", returnType = String.class)
    static void m(String methodName) throws Exception {
        Class.forName("csr1.Foo").getDeclaredMethod(methodName).invoke(null);
    }
    
    public static void main(String[] args) throws Exception {
        m(args[0]);
    }
}
```
[//]: # (END)

## CSR4
The class name is passed as an argument.
```java
// csr4/Foo.java
package csr4;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    static String staticToString() { return "Foo"; }
    
    
    @IndirectCall(name = "staticToString", declaringClass = "Lcsr4/Foo;", returnType = String.class) 
    @IndirectCall(name = "staticToString", declaringClass = "Lcsr4/Bar;", returnType = String.class) 
    static void m(String className) throws Exception {
        Class.forName(className).getDeclaredMethod("staticToString").invoke(null);
    }
    
    public static void main(String[] args) throws Exception {
        m(args[0]);
    }
}

class Bar {
    static String staticToString() { return "Bar"; }
}
```
[//]: # (END)

TODO
Here newInstance could call every default constructor
```java
// todo1/Foo.java
package todo1;

import lib.annotations.callgraph.IndirectCall;
class Foo {
    @IndirectCall(name = "<init>", declaringClass = "Ltodo1/Foo;")
    @IndirectCall(name = "<init>", declaringClass = "Ltodo1/Bar;")
    static void main(String[] args) throws Exception {
        Foo f = (Foo) Class.forName(args[0]).newInstance();
        f.toString();
    }
}
class Bar {
    
}

```
[//]: # (END)


```java
// todo2/Foo.java
package todo2;

import lib.annotations.callgraph.IndirectCall;
class Foo { 
    public String toString() { return ""; }
    
    static void m(String s) throws Exception {
        Object o = Class.forName(s).newInstance();
        if (o instanceof Foo) 
            o.toString();
    }
}
```
[//]: # (END)

