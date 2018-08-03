#JVMCalls
JVM calls or callbacks must be treated as (on-the-fly) entry points and explicitly modelled for correct
call-graph construction, i.e. when certain operations are performed like creating an object or 
adding an ShutdownHook. 

Please note that Java's Serialization feature is a similar mechanism. However, Serialization is
substantial feature and is thus handled as own category.

##JVMC1
[//]: # (MAIN: jvmc.Demo)
This tests covers a callback that can be introduced to the program, namely ```Runtime.addShutdownHook```.
It allows the program to pass a customizable thread to the JVM that is called by the JVM when it
shuts down. 
```java
// jvmc/Demo.java
package jvmc;

import java.lang.System;
import java.lang.Runtime;

import lib.annotations.callgraph.CallSite;

public class Demo {

    public static void callback(){ /* do something */ }

	public static void main(String[] args){
        Runnable r = new TargetRunnable();
        Runtime.getRuntime().addShutdownHook(new Thread(r));
	}
}

class TargetRunnable implements Runnable {
    
    @CallSite(name = "callback", line = 22, resolvedTargets = "Ljvmc/Demo;")
    public void run(){
        Demo.callback();
    }
}
```
[//]: # (END)

##JVMC2
[//]: # (MAIN: jvmc.Demo)
This test case covers the ```finalize``` method, which __can__ be called by the JVM during
garbage collection.
```java
// jvmc/Demo.java
package jvmc;


import lib.annotations.callgraph.CallSite;

public class Demo {

    public static void callback(){};

	public static void main(String[] args){
          for(int i = -1; i < args.length; i++){
              new Demo();
          }
	}
	
	@CallSite(name="callback", line=18, resolvedTargets = "Ljvmc/Demo;")
    public void finalize() throws java.lang.Throwable {
        callback();
        super.finalize();
    }	
}
```
[//]: # (END)

##JVMC3
[//]: # (MAIN: jvmc.Demo)
This cases tests the implicitly introduced call edge from ```Thread.start``` to ```Thread.run```.
Please note that this test tests this feature indirectly by validating that the run method of
```TargetRunnable``` is transitively reachable.
```java
// jvmc/Demo.java
package jvmc;

import lib.annotations.callgraph.IndirectCall;

public class Demo {

    @IndirectCall(name="run", line = 11, resolvedTargets = "Ljvmc/TargetRunnable;")
	public static void main(String[] args) throws InterruptedException {
        Runnable r = new TargetRunnable();
        Thread t = new Thread(r);
        t.start();
        t.join();
	}
}

class TargetRunnable implements Runnable {
    
    public void run(){
        /* Do the hard work */
    }   
}
```
[//]: # (END)