#JVMCalls
System Callbacks must be treated as (on-the-fly) entry points, i.e. when certain operations are performed
like creating an object or adding an ShutdownHook. 

Please note that Java's Serialization feature is a similar mechanism. However, Serialization is
substantial feature and is thus handled as own category.

##SC1
[//]: # (MAIN: sc.Dmeo)

```java
// sc/Demo.java
package sc;

import java.lang.System;
import java.lang.Runtime;

import lib.annotations.callgraph.IndirectCall;

public class Demo {

    public static void callback(){};

    @CallSite(name = "callback", line = 16, resolvedTargets = "Lsc/Demo;")
	public static void main(String[] args){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run(){
                callbackTest();           
                System.out.println("Good bye! Was nice to have you.");
            }
        });	  
	}
}
```
[//]: # (END)

##SC2
[//]: # (MAIN: sc.Dmeo)

```java
// sc/Demo.java
package sc;


import lib.annotations.callgraph.CallSite;

public class Demo {

    public static void callback(){};

	public static void main(String[] args){
          for(int i = -1; i < args.length; i++){
              new Demo();
          }
	}
	
	@CallSite(name="callback", line=18, resolvedTargets = "Lsc/Demo;")
    public void finalize(){
        callbackTest();
        super.finalize();
    }	
}
```
[//]: # (END)