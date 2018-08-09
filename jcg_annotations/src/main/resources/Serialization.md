#SerializableClasses
Callbacks related to java.io.Serializable classes.

##SC1
[//]: # (MAIN: sc.Foo)
Tests the writeObject callback methhod.
 
```java
// sc/Foo.java
package sc;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable {

    @CallSite(name = "defaultWriteObject", resolvedTargets = "Ljava/io/ObjectOutputStream;", line = 12)
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();
    }

    public static void main(String[] args) throws Exception {
    	Foo f = new Foo();
    	FileOutputStream fos = new FileOutputStream("test.ser");
    	ObjectOutputStream out = new ObjectOutputStream(fos);
    	out.writeObject(f);
    	out.close();
    }
}
```
[//]: # (END)

##SC2
[//]: # (MAIN: sc.Foo)
Tests writeObject with intraprocedurally resolvable parameters.
 
```java
// sc/Foo.java
package sc;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable {

    @CallSite(name = "defaultWriteObject", resolvedTargets = "Ljava/io/ObjectOutputStream;", line = 12)
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    public static void main(String[] args) throws Exception {
        Object f;
        if(args.length == 0)
            f = new Foo();
        else
            f = new Bar();
        FileOutputStream fos = new FileOutputStream("test.ser");
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(f);
        out.close();
    }
}

class Bar implements Serializable {}
```
[//]: # (END)

##SC3
[//]: # (MAIN: sc.Foo)
Tests writeObject with object from param methhod.

```java
// sc/Foo.java
package sc;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable {

    @CallSite(name = "defaultWriteObject", resolvedTargets = "Ljava/io/ObjectOutputStream;", line = 12)
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    public static void serialize(Object f) throws Exception {
        FileOutputStream fos = new FileOutputStream("test.ser");
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(f);
        out.close();
    }

    public static void main(String[] args) throws Exception {
        Foo f = new Foo();
        serialize(f);
    }
}
```
[//]: # (END)

##SC4
[//]: # (MAIN: sc.Foo)
Tests the readObject callback methhod with no cast done.

```java
// sc/Foo.java
package sc;

import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable {
    
    @CallSite(name = "defaultReadObject", resolvedTargets = "Ljava/io/ObjectOutputStream;", line = 12)
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Object obj = in.readObject();
        in.close();
    }
}
```
[//]: # (END)


##SC5
[//]: # (MAIN: sc.Foo)
Tests the readObject callback methhod with a cast done.

```java
// sc/Foo.java
package sc;

import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable {
    
    @CallSite(name = "defaultReadObject", resolvedTargets = "Ljava/io/ObjectOutputStream;", line = 12)
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Foo obj = (Foo) in.readObject();
        in.close();
    }
}
```
[//]: # (END)

##SC6
[//]: # (MAIN: sc.Foo)
Tests the writeReplace method.

```java
// sc/Foo.java
package sc;

import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable {
    public Object replace() { return this; }

	@CallSite(name = "replace", returnType = Object.class, resolvedTargets = "Lsc/Foo;", line = 14)
    private Object writeReplace() throws ObjectStreamException {
    	return replace();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();
    }

    public static void main(String[] args) throws Exception {
    	Foo f = new Foo();
    	FileOutputStream fos = new FileOutputStream("test.ser");
    	ObjectOutputStream out = new ObjectOutputStream(fos);
    	out.writeObject(f);
    	out.close();
    }
}
```
[//]: # (END)

##SC7
[//]: # (MAIN: sc.Foo)
Tests the readResolve method.

```java
// sc/Foo.java
package sc;

import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable {
    public Object replace() { return this; }

    @CallSite(name = "replace", returnType = Object.class, resolvedTargets = "Lsc/Foo;", line = 14)
    private Object readResolve() throws ObjectStreamException {
        return replace();
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Foo obj = (Foo) in.readObject();
        in.close();
    }
}
```
[//]: # (END)

##SC8
[//]: # (MAIN: sc.Foo)
Tests the validateObject method.

```java
// sc/Foo.java
package sc;

import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.ObjectInputValidation;
import java.io.InvalidObjectException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable, ObjectInputValidation {
    public void callback() {  }

    @CallSite(name = "callback", resolvedTargets = "Lsc/Foo;", line = 16)
    public void validateObject() throws InvalidObjectException {
        callback();
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Foo obj = (Foo) in.readObject();
        in.close();
    }
}
```
[//]: # (END)

##SC9
[//]: # (MAIN: sc.Foo)
Tests that the no-arg. constructor of the first super class that is not serializable is called.

```java
// sc/Bar.java
package sc;

import lib.annotations.callgraph.CallSite;

public class Bar {
    public void callback() { }

    @CallSite(name = "callback", resolvedTargets = "Lsc/Bar;", line = 10)
    public Bar() {
        callback();
    }
}
```

```java
// sc/Foo.java
package sc;

import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;

public class Foo extends Bar implements Serializable {
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Foo obj = (Foo) in.readObject();
        in.close();
    }
}
```
[//]: # (END)

#ExternalizableClasses
Callback methods related to java.io.Externalizable classes.
##EC1
[//]: # (MAIN: ec.Foo)
Tests the writeExternal method.

```java
// ec/Foo.java
package ec;

import java.io.Externalizable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutput;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectInput;
import java.io.IOException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Externalizable {

    @CallSite(name = "callback", resolvedTargets = "Lec/Foo;", line = 16)
    public void writeExternal(ObjectOutput out) throws IOException {
        callback();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        callback();
    }

    public void callback() { }

    public static void main(String[] args) throws Exception {
        Foo f = new Foo();
        FileOutputStream fos = new FileOutputStream("test.ser");
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(f);
        out.close();
    }
}
```
[//]: # (END)

##EC2
[//]: # (MAIN: ec.Foo)
Tests the readExternal method.

```java
// ec/Foo.java
package ec;

import java.io.Externalizable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutput;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectInput;
import java.io.IOException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Externalizable {
    
    @CallSite(name = "callback", resolvedTargets = "Lec/Foo;", line = 16)
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        callback();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        callback();
    }

    public void callback() { }

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Foo obj = (Foo) in.readObject();
        in.close();
    }
}
```
[//]: # (END)

##EC3
[//]: # (MAIN: ec.Foo)
Tests that the no-arg constructor is called.

```java
// ec/Foo.java
package ec;

import java.io.Externalizable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutput;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectInput;
import java.io.IOException;
import lib.annotations.callgraph.CallSite;

public class Foo implements Externalizable {
    
    public void callback() { }

    @CallSite(name = "callback", resolvedTargets = "Lec/Foo;", line = 19)
    public Foo() {
        callback();
    }
    
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        callback();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        callback();
    }

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Foo obj = (Foo) in.readObject();
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