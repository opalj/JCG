#Java8StaticInterfaceMethods

Tests the correct method resolution in the presence of Java 8 interfaces, i.e. default methods.

##J8PC6
[//]: # (MAIN: j8pc6.Class)
Tests the resolution of static interface methods.

```java
// j8pc6/Class.java
package j8pc6;

import lib.annotations.callgraph.DirectCall;

class Class {

    @DirectCall(name = "method", line = 9, resolvedTargets = "Lj8pc6/Interface;")
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
