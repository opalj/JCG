## Library
Tests the call-graph handling for the analysis of software libraries, i.e. partial programs. The main
difference between applications and library software is, that libraries are intended to be used, and,
therefore, to be extended. Library extensions can cause call-graph edges within the library that can
already be detected without a concrete application scenario. For instance, when the library contains yet
independent as well as by client code accessible and inheritable classes and interfaces that declare
a method with exactly the same method signature.
 
When the previous conditions are meet, a client can extend such a class as well as implement the
interface respectively without overriding the respective method which leads to interface call sites
that must be additionally resolved to the class' method. We refer to those edges as call-by-signature
(CBS) edges. 

##LIB1
[//]: # (LIBRARY)
Tests library interface invocation for CBS edges under the following circumstances:
1) a ```public class PotentialSuperclass``` that can be inherited,
2) a ```public class DismissedSuperclass``` that cannot be inherited and, therefore, can't be target,
3) a ```public interface``` that can be inherited,
4) all of the previous mentioned classes/interfaces declare the method ```public void method()```. 

```java
// lib1/Demo.java
package lib1;

import lib.annotations.callgraph.CallSite;

public class Demo {
    
    @CallSite(name = "method", line = 10, resolvedTargets = "Llib1/PotentialSuperclass;",
    prohibitedTargets = "Llib1/DismissedSuperlass;")
    public static void libraryCallSite(Interface i){
        i.method();
    }
}
```
```java
// lib/PotentialSuperclass.java
package lib1;

public class PotentialSuperclass {
    
    public void method() {
        
    }
}
```
```java
// lib/DismissedSuperlass.java
package lib1;

public final class DismissedSuperlass {
    
    public void method() {
        
    }
}
```
```java
// lib/Interface.java
package lib1;

public class Interface {
    
    void method();
}
```
[//]: # (END)