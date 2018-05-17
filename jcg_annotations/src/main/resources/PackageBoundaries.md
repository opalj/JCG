#Package Boundaries

Visibility modifiers influence the call resolution as other language features (e.g. virtual dispatch).
Especially package visibility sometimes permits the resolution to some target method even if they are
public. The following test cases target mostly the method resolution of inter-package method calls.

##PB1
[//]: # (MAIN: pb1/a/Main)
Tests the resolution of a call site with a public receiver type (```pb1.a.A```) but a package visible method where the
actual receiver type is a class defined in another package (```pb1.b```). The receiver type's class
(```pb1.b.B```) also declares a package visible method with the exact same signature. However,
due to the access modifier ```package visible```, the call is resolved to the declared type of the
variable. It's prohibited to resolve the call to the package visible method in package ```pb1.b```. 
```java
// pb1/a/Main.java
package pb1.a;

import lib.annotations.callgraph.CallSite;

public class Main {
    
    @CallSite(name = "method", line = 10, resolvedTargets = "Lpb1/a/A;", prohibitedTargets = "Lpb1/b/B;")
    public static void main(String[] args){
        A a = new pb1.b.B();
        a.method();
    }   
}
```
```java
// pb1/a/A.java
package pb1.a;

public class A {
    
    void method(){
        /* do something */
    }
}
```
```java
// pb1/b/B.java
package pb1.b;

public class B extends pb1.a.A {
    
    void method(){
        /* do something */
    }
}
```
[//]: # (END)

##PB2
[//]: # (MAIN: pb2/a/A)
Tests the resolution of a call site with a public receiver type (```pb2.a.A```) but a package visible method where the
actual receiver type is a class defined in same package (```pb2.a```). The receiver type's class
(```pb2.a.C```) does not declare a method with the same name and inherits from a public type from another
package that declares it. However, due to the access modifier ```package visible```, the call is resolved
to the declared type of the variable and not to the supertype's implementation. 
It's prohibited to resolve the call to the package visible method in package ```pb2.b```. 
```java
// pb2/a/A.java
package pb2.a;

import lib.annotations.callgraph.CallSite;

public class A {
    
    @CallSite(name = "method", line = 10, resolvedTargets = "Lpb2/a/A;", prohibitedTargets = "Lpb2/b/B;")
    public static void main(String[] args){
        A a = new C();
        a.method();
    }
    
    void method(){
        /* do something */
    }
}
```
```java
// pb2/a/C.java
package pb2.a;

public class C extends pb2.b.B {
    
}
```
```java
// pb2/b/B.java
package pb2.b;

public class B extends pb2.a.A {
    
    void method(){
        /* do something */
    }
}
```
[//]: # (END)