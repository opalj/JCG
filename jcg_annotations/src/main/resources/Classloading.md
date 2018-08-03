#Classloading
This test cases w.r.t. to classloading over ```Class.forName(cls)```.

##CL3
[//]: # (MAIN: cl.Demo)
This test cases targets a common try catch pattern when classes are loaded. An existing class is loaded
over ```Class.forName(...)```, instantiated and then casted to another class. Unfortunately, the class
that is instantiated is __incompatible__ with the cast such that the operation results in a 
```ClassCastException```.
```java
// cl/Demo.java
package cl;

import lib.annotations.callgraph.CallSite;

public class Demo {

    public static void verifyCall(){ /* do something */ }

    @CallSite(name="verifyCall", line = 15, resolvedTargets = "Lcl/Demo;")
	public static void main(String[] args){
	    try {
	        Class cls = Class.forName("cl.DeceptiveClass");
	        LoadedClass lCls = (LoadedClass) cls.newInstance(); 
	    } catch(ClassCastException cce){
	        verifyCall();
	    } catch(ClassNotFoundException cnfe){
	        // DEAD CODE
	    } catch(Exception rest){
            // DEAD CODE
        }
	}
}

class DeceptiveClass {
    
}

class LoadedClass {
    
}
```
[//]: # (END)

##CL4
[//]: # (MAIN: cl.Demo)
This test cases targets a common try catch pattern when classes are loaded. An absent class is loaded
over ```Class.forName(...)```. Since the class __can't be found__ the operation results in a ```ClassNotFoundException```
which is handled in one of the catch blocks.
```java
// cl/Demo.java
package cl;

import lib.annotations.callgraph.CallSite;

public class Demo {

    public static void verifyCall(){ /* do something */ }

    @CallSite(name="verifyCall", line = 18, resolvedTargets = "Lcl/Demo;")
	public static void main(String[] args){
	    try {
	        Class cls = Class.forName("cl.CatchMeIfYouCan");
	        // DEAD CODE
	        LoadedClass lCls = (LoadedClass) cls.newInstance(); 
	    } catch(ClassCastException cce){
	        /* DEAD CODE */
	    } catch(ClassNotFoundException cnfe){
	        verifyCall();
	    } catch(Exception rest){
	        //DEAD CODE
	    }
	}
}

class LoadedClass {
    
}
```
[//]: # (END)

##CL5
[//]: # (MAIN: cl.Demo)
This case targets a concerns not only loading of classes but also the execution of their 
static initializer. When a class is loaded, its static initializer must be called.
```java
// cl/Demo.java
package cl;

import lib.annotations.callgraph.CallSite;

public class Demo {

    public static void verifyCall(){ /* do something */ }

	public static void main(String[] args){
	    try {
	        Class cls = Class.forName("cl.LoadedClass");
	        LoadedClass lCls = (LoadedClass) cls.newInstance();
	    } catch(ClassCastException cce){
	        // DEAD CODE
	    } catch(ClassNotFoundException cnfe){
	        // DEAD CODE
	    } catch(Exception rest){
            //DEAD CODE
        }
	}
}

class LoadedClass {

    static {
        staticInitializerCalled();
    }
    
    @CallSite(name="verifyCall", line=31, resolvedTargets = "Lcl/Demo;")
    static private void staticInitializerCalled(){
        Demo.verifyCall();
    }
}
```
[//]: # (END)

##CL6
[//]: # (MAIN: cl.Demo)
This case targets a concerns not only loading of classes but also the execution of their 
static initializer. When a class is loaded, its static initializer must be called. Also the static
initializers of potential super classes.
```java
// cl/Demo.java
package cl;

import lib.annotations.callgraph.CallSite;

public class Demo {

    public static void verifyCall(){ /* do something */ }

	public static void main(String[] args){
	    try {
	        Class cls = Class.forName("cl.LoadedClass");
	        LoadedClass lCls = (LoadedClass) cls.newInstance();
	    } catch(ClassCastException cce){
	        // DEAD CODE
	    } catch(ClassNotFoundException cnfe){
	        // DEAD CODE
	    } catch(Exception rest){
            //DEAD CODE
        }
	}
}

class LoadedClass extends RootClass {

}

class RootClass {
    
    static {
        staticInitializerCalled();
    }
    
    @CallSite(name="verifyCall", line=35, resolvedTargets = "Lcl/Demo;")
    static private void staticInitializerCalled(){
        Demo.verifyCall();
    }
}
```
[//]: # (END)