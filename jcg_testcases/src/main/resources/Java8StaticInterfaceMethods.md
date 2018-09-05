#Java8StaticInterfaceMethods

Tests the correct method resolution of static interface methods.

Please note that the ```infrastructure_incompatible_testcases``` are more test cases w.r.t. to 
static interface methods pertaining to Java 9 and higher versions.
 
##J8SIM1
[//]: # (MAIN: j8sim.Class)
Tests the invocation of a static interface method ```j8sim.Interface``` in ```j8sim.Class```'s main
method.
```java
// j8sim/Class.java
package j8sim;

import lib.annotations.callgraph.DirectCall;

class Class {

    @DirectCall(name = "method", line = 9, resolvedTargets = "Lj8sim/Interface;")
    public static void main(String[] args){
        Interface.method();
    }
}

interface Interface {
    static void method() {
        // do something
    }
}
```
[//]: # (END)
