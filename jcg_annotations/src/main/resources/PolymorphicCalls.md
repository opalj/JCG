#BasicPolymorphicCalls
This tests aim to test basic virtual calls.

##BPC1
[//]: # (MAIN: bpc1/Class)
Tests a virtually dispatched method call which is in fact monomorphic.

```java
// bpc1/Class.java
package bpc1;

import lib.annotations.callgraph.CallSite;

class Class {
    
    public void method(){ }
    
    @CallSite(name = "method", line = 12, resolvedTargets = "Lbpc1/Class;")
    public static void main(String[] args){ 
        Class cls = new Class();
        cls.method();
    }
}
```
[//]: # (END)

##BPC2
[//]: # (MAIN: bpc2/Class)
Tests a virtually dispatched method call when a simple type hierarchy is present.

```java
// bpc2/Class.java
package bpc2;

import lib.annotations.callgraph.CallSite;

class Class {
    
    public void method(){ }
    
    @CallSite(name = "method", line = 12, resolvedTargets = "Lbpc2/SubClass;")
    public static void main(String[] args){ 
        Class cls = new SubClass();
        cls.method();
    }
}

class SubClass extends Class {
    
    public void method() { }
}
```
[//]: # (END)

##BPC3
[//]: # (MAIN: bpc3/Class)
Tests a virtually dispatched method call when the receiver is an interface type.

```java
// bpc3/Class.java
package bpc3;

import lib.annotations.callgraph.CallSite;

interface Interface {
    void method();
}

class Class implements Interface {
    
    public void method(){ }
 
    @CallSite(name = "method", line = 15, resolvedTargets = "Lbpc3/ClassImpl;")
    public static void callOnInterface(Interface i){
        i.method();
    }
    
    public static void main(String[] args){
        callOnInterface(new ClassImpl());
    }
}

class ClassImpl implements Interface {
    public void method(){ }
}
```
[//]: # (END)

##BPC4
[//]: # (MAIN: bpc4/Class)
Tests a virtually dispatched method call when the receiver is loaded from an array.

```java
// bpc4/Class.java
package bpc4;

import lib.annotations.callgraph.CallSite;

interface Interface {
    void method();
}

class Class implements Interface {
    
    public static Interface[] types = new Interface[]{new Class(), new ClassImpl()};
    
    public void method(){ }
 
    @CallSite(name = "method", line = 18, resolvedTargets = "Lbpc4/Class;")
    public static void main(String[] args){
        Interface i = types[0];
        i.method();
    }
}

class ClassImpl implements Interface {
    public void method(){ }
}
```
[//]: # (END)

##BPC5
[//]: # (MAIN: bpc5/Class)
Tests a virtually dispatched method call should not be dispatched to methods with the same signature
when the types are in no relation.
from the super class.

```java
// bpc5/Class.java
package bpc5;

import lib.annotations.callgraph.CallSite;

class Class {
    
    public void method(){ }
 
    @CallSite(name = "method", line = 12, resolvedTargets = "Lbpc5/Class;", prohibitedTargets = "Lbpc5/DifferentClass;")
    public static void main(String[] args){
        Class cls = new Class();
        cls.method();
    }
}

class DifferentClass {
    public void method(){ }
}
```
[//]: # (END)