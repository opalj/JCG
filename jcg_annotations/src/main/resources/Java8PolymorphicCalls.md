#Java8PolymorphicCalls
Tests the correct method resolution in the presence of Java 8
interfaces, i.e. default methods.
##J8PC1
[//]: # (MAIN: j8pc/Class)
Tests the resolution of a polymorphic calls when a class implements an interface (with default method) and 
inherits the method from the inherited interface.
```java
// j8pc/Class.java
package j8pc;

import lib.annotations.callgraph.CallSite;

class Class implements Interface {
    
    @CallSite(name = "method", line = 16, resolvedTargets = "Lj8pc/Interface;")
    public static void main(String[] args){ 
        Interface i = new Class();
        i.method();
    }
}

class Interface { 
    default void method() {
        // do something
    }
}
```
[//]: # (END)

##J8PC2
[//]: # (MAIN: j8pc/SuperClass)
Tests the resolution of a polymorphic calls when a class implements an interface (with default method) and extends a class
where the interface and the class define a method with the same signature. The subclass - inheriting from both - does not
define a method with that signature, hence, the method call on that class must be dispatched to the superclass's method. 
```java
// j8pc/SuperClass.java
package j8pc;

import lib.annotations.callgraph.CallSites;
import lib.annotations.callgraph.CallSite;
import lib.annotations.callgraph.ProhibitedMethod;

class SuperClass {
    
    public void method(){
        // do something
    }
    
    @CallSites({
        @CallSite(
                name = "method",
                line = 67,
                resolvedTargets = "Lj8pc/SuperClass;",
                prohibitedTargets = { 
                    @ProhibitedMethod(receiverType = "Lj8pc/Interface;")}),
        @CallSite(
                name = "method",
                line = 68,
                resolvedTargets = "Lj8pc/SuperClass;",
                prohibitedTargets = { 
                    @ProhibitedMethod(receiverType = "Lj8pc/Interface;")})
    })
    public static void main(String[] args){ 
        Interface i = new SubClass();
        SubClass subClass = new SubClass();
        i.method();
        subClass.method();
    }
}

class Interface { 
    default void method() {
        // do something
    }
}

class SubClass extends SuperClass implements Interface {
    
}
```
[//]: # (END)

// TODO - Test the case of the JVM specification. That case requires engineering of Java bytecode. The computation of
// maximally specific methods in the case, that there is a class that inherits two independent interfaces that define a
// method with an equivalent signature. The resolution of that construct is w.r.t. to the JVM arbitrary.