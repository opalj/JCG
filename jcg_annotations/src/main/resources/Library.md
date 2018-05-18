# Library
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
1) a ```public class DismissedSuperclass``` that cannot be inherited and, therefore, canå't be target,
1) a ```public interface``` that can be inherited,
1) all of the previous mentioned classes/interfaces declare the method ```public void method()```. 
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

public interface Interface {
    
    void method();
}
```
[//]: # (END)

##LIB2
[//]: # (LIBRARY)
Tests library interface invocation for CBS edges under the following circumstances:
1) a ```package visible class PotentialSuperclass``` in package ```lib2.collude``` that can be
inherited from a class within the same package, i.e. when a new class is added to the same package,
2) a ```package visible class InternalClass``` in package ```lib2.internal``` that can be inherited 
(analogously to 1) ),
3) a ```package visible interface``` in package ```lib2.collude``` that can be inherited from classes in the same package,
4) all of the previous mentioned classes/interfaces declare the method ```public void method()```. 
```java
// lib2/collude/Demo.java
package lib2.collude;

import lib.annotations.callgraph.CallSite;

public class Demo {
    
    @CallSite(name = "method", line = 10, resolvedTargets = "Llib2/collude/PotentialSuperclass;", 
    prohibitedTargets = "Llib2/internal/InternalClass;")
    public static void interfaceCallSite(PotentialInterface pi){
        pi.method();
    }
}
```
```java
// lib2/collude/PotentialSuperclass.java
package lib2.collude;

class PotentialSuperclass {
    
    public void method(){
        /* do something */
    }
}
```
```java
// lib2/collude/PotentialInterface.java
package lib2.collude;

interface PotentialInterface {
    
    void method();
}
```
```java
// lib2/internal/InternalClass.java
package lib2.internal;

class InternalClass {
    
    public void method(){
        /* do something */
    }
}
```
[//]: # (END)

##LIB3
[//]: # (LIBRARY)
Tests library interface invocation for CBS edges under the following circumstances:
1) a ```public class PotentialSuperclass``` in package ```lib3.internal``` that can be
inherited from and, therefore, provides the method ```public void method()``` from its superclass,
2) a ```package visible class InternalClass``` in package ```lib3.internal``` that can be inherited 
(analogously to 1) ),
3) a ```package visible interface``` in package ```lib3.collude``` that can be inherited from classes in the same package,
4) all of the previous mentioned classes/interfaces declare the method ```public void method()```. 
```java
// lib3/collude/Demo.java
package lib3.collude;

import lib.annotations.callgraph.CallSite;

public class Demo {
    
    @CallSite(name = "method", line = 9, resolvedTargets = "Llib3/internal/InternalClass;")
    public static void interfaceCallSite(PotentialInterface pi){
        pi.method();
    }
}

interface PotentialInterface {
    
    void method();
}
```
```java
// lib3/internal/PotentialSuperclass.java
package lib3.internal;

public class PotentialSuperclass extends InternalClass {
    
}

class InternalClass {
    
    public void method(){
        /* do something */
    }
}
```
[//]: # (END)

##LIB4
[//]: # (LIBRARY)
Tests virtual call resolution in the context of libraries where the calling context is unknown. 
The circumstances of the virtual call are as follows:
1) We have a method ```public void libraryEntryPoint(Type type)``` which calls a method on the passed
parameter,
2) A type ```public class Type``` which declares a method ```public void method()```,
3) Another type ```public class Subtype extends Type``` which also declares a method ```public void method()```,
4) An additional type ```public class SomeType``` which also delcares a method ```public void method()```.
Since the calling context of ```Type.method()``` in ```Demo.entrypoint(Type t)``` is unknown. The call
graph construction must assume that the call all possible subtypes of ```Type``` can be passed over
the parameter.
```java
// lib4/Demo.java
package lib4;

import lib.annotations.callgraph.CallSite;

public class Demo {
    
    @CallSite(name = "method", line = 10, resolvedTargets = {"Llib4/Type;", "Llib4/Subtype;"}, 
    prohibitedTargets = "Llib4/SomeType;")
    public void libraryEntryPoint(Type type){
        type.method();
    }
}
```
```java
// lib4/Type.java
package lib4;

public class Type {
    
    public void method(){
        /* do something */
    }
}
```
```java
// lib4/Subtype.java
package lib4;

public class Subtype extends Type {
    
    public void method(){
        /* do something */
    }
}
```
```java
// lib4/SomeType.java
package lib4;

public class SomeType {
    
    public void method(){
        /* do something */
    }
}
```
[//]: # (END)