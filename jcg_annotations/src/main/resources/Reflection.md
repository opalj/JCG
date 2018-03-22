#TrivialReflection
The strings are directly available. No control- or data-flow analysis is required.
##TR1
Test reflection with respect to static methods.
```java
// tr1/Foo.jar
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

##TR2
Test reflection with respect to instance methods.
```java
// tr2/Foo.jar
package tr2;

import lib.annotations.callgraph.IndirectCall;
class Foo { 
    String toString() { return "Foo"; }
    
    @IndirectCall(name = "toString", declaringClass = "Ltr2/Foo;", returnType = String.class)
    void m() throws Exception {
        Class.forName("Foo").getDeclaredMethod("toString").invoke(this);
    }
    
    public static void main(String[] args) throws Exception { new Foo().m(); }
}
```

##TR3
Test reflection with respect to constructors.
```java
// tr3/Foo.jar
package tr3;

import lib.annotations.callgraph.IndirectCall;
class Foo { 
    public Foo(String s) {    }
    
    @IndirectCall(name = "<init>", declaringClass = "Ltr3/Foo;", parameterTypes = String.class)
    public static void main(String[] args) throws Exception {
        Class.forName("tr3.Foo").getConstructor(String.class).invoke(); 
    }
}
```

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


TODO
Here newInstance could call every default constructor
```java
class Foo { 
    String toString() { return ""; }
    
    static void m(String s) {
        Foo f = (Foo) Class.forName(s).newInstance();
        f.toString();
    }
}
```


```java
package tr1;
class Foo { 
    String toString() { return ""; }
    
    static void m(String s) {
        Object o = Class.forName(s).newInstance();
        if (o instanceof Foo) 
            o.toString();
    }
}
```

