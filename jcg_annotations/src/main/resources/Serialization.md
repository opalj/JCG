#SerializableClasses
Callbacks related to java.io.Serializable classes.

##SC1
[//]: # (MAIN: sc1.Foo)
Tests the writeObject/readObject callback methhods.

```java
// sc1/Foo.java
package sc1;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable {
    public static String callback() { return ""; }

    @CallSite(name = "defaultWriteObject", resolvedTargets = "Ljava/io/ObjectOutputStream;", line = 15)
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();
    }
    
    @CallSite(name = "defaultReadObject", resolvedTargets = "Ljava/io/ObjectOutputStream;", line = 20)
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    }

    public static void main(String[] args) throws Exception {
    	Foo f = new Foo();
    	FileOutputStream fos = new FileOutputStream("test.ser");
    	ObjectOutputStream out = new ObjectOutputStream(fos);
    	out.writeObject(f);
    	out.close();

    	FileInputStream fis = new FileInputStream("test.ser");
    	ObjectInputStream in = new ObjectInputStream(fis);
    	Object obj = in.readObject();
    	in.close();
    }
}
```
[//]: # (END)

##SC2
[//]: # (MAIN: sc2.Foo)
Tests the writeReplace/readResolve/validateObject methods.

```java
// sc2/Foo.java
package sc2;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.ObjectInputValidation;
import java.io.InvalidObjectException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable, ObjectInputValidation {
    public Object replace() { return this; }
    public void callback() { }

	@CallSite(name = "replace", returnType = Object.class, resolvedTargets = "Lsc2/Foo;", line = 19)
    private Object writeReplace() throws ObjectStreamException {
    	return replace();
    }

	@CallSite(name = "callback", resolvedTargets = "Lsc2/Foo;", line = 24)
    public void validateObject() throws InvalidObjectException {
    	callback();
    }

	@CallSite(name = "replace", returnType = Object.class, resolvedTargets = "Lsc2/Foo;", line = 29)
    private Object readResolve() throws ObjectStreamException {
    	return replace();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    }

    public static void main(String[] args) throws Exception {
    	Foo f = new Foo();
    	FileOutputStream fos = new FileOutputStream("test.ser");
    	ObjectOutputStream out = new ObjectOutputStream(fos);
    	out.writeObject(f);
    	out.close();

    	FileInputStream fis = new FileInputStream("test.ser");
    	ObjectInputStream in = new ObjectInputStream(fis);
    	Object obj = in.readObject();
    	in.close();
    }
}
```
[//]: # (END)

#ExternalizableClasses
Callback methods related to java.io.Externalizable classes.
##EC1
[//]: # (MAIN: ec1.Foo)
Tests the writeExternal/readExternal methods.

```java
// ec1/Foo.java
package ec1;

import java.io.Externalizable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Externalizable {
    public static String callback() { return ""; }

    @CallSite(name = "defaultWriteObject", resolvedTargets = "Ljava/io/ObjectOutputStream;", line = 15)
    public void writeExternal(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    @CallSite(name = "defaultReadObject", resolvedTargets = "Ljava/io/ObjectOutputStream;", line = 20)
    public void readExternal(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public static void main(String[] args) throws Exception {
        Foo f = new Foo();
        FileOutputStream fos = new FileOutputStream("test.ser");
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(f);
        out.close();

        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Object obj = in.readObject();
        in.close();
    }
}
```
[//]: # (END)

#Serialization and Lambdas
Tests Java's serialization mechanism when Lambdas are (de)serialized, i.e., de(serialization) of Lambdas
causes the JVM to use ```java.lang.invoke.SerializedLambda```
##SerLam1
[//]: # (MAIN: serlam.DoSerialization)
Tests whether the serialization of lambdas that implement a functional interface is modelled correctly.
```java
// serlam/DoSerialization.java
package serlam;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Function;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import lib.annotations.callgraph.IndirectCall;

public class DoSerialization {

    @FunctionalInterface interface Test extends Serializable{
        String concat(Integer seconds);
    }
    
    @IndirectCall(
            name = "writeReplace",
            line = 33,
            resolvedTargets = "Ljava/lang/invoke/SerializedLambda;")
    public static void main(String[] args) throws Exception {
        float y = 3.13f;
        String s = "bar";
        
        Test lambda = (Integer x) -> "Hello World " + x + y + s;
        
        FileOutputStream fos = new FileOutputStream("serlam1.ser");
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(lambda);
        out.close();
        
        
    }
}
```
[//]: # (END)

##SerLam2
[//]: # (MAIN: serlam.DoDeserialization)
Tests whether the deserialization of lambdas that implement a functional interface is modelled correctly.
```java
// serlam/DoSerialization.java
package serlam;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class DoSerialization {

    public static void main(String[] args) throws Exception {
        float y = 3.14f;
        String s = "foo";
        
        Test lambda = (Integer x) -> "Hello World " + x + y + s;
        
        FileOutputStream fos = new FileOutputStream("serlam2.ser");
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(lambda);
        out.close();
    }
}
```
```java
// serlam/Test.java
package serlam;

import java.io.Serializable;

public @FunctionalInterface interface Test extends Serializable{
    String concat(Integer seconds);
}
```
```java
// serlam/DoDeserialization.java
package serlam;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import lib.annotations.callgraph.IndirectCall;

public class DoDeserialization {

    @IndirectCall(
            name = "readResolve",
            line = 28,
            resolvedTargets = "Ljava/lang/invoke/SerializedLambda;")
    public static void main(String[] args) throws Exception {
        DoSerialization.main(args);
        FileInputStream fis = new FileInputStream("serlam2.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Object obj = in.readObject();
        in.close();
    }
}
```
[//]: # (END)