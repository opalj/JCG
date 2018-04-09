#SerializableClasses
Callbacks related to java.io.Serializable classes.

##SC1
Tests the writeObject/readObject callback methhods.
[//]: # (MAIN: sc1.Foo)
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
Tests the writeReplace/readResolve/validateObject methods.
[//]: # (MAIN: sc2.Foo)
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
import java.io.InvalidObjectException;
import lib.annotations.callgraph.CallSite;
public class Foo implements Serializable {
    public Object replace() { return this; }
    public void callback() { }

	@CallSite(name = "replace", returnType = Object.class, resolvedTargets = "Lsc2/Foo;", line = 18)
    private Object writeReplace() throws ObjectStreamException {
    	return replace();
    }

	@CallSite(name = "callback", resolvedTargets = "Lsc2/Foo;", line = 23)
    public void validateObject() throws InvalidObjectException {
    	callback();
    }

	@CallSite(name = "replace", returnType = Object.class, resolvedTargets = "Lsc2/Foo;", line = 28)
    private Object readResolve() throws ObjectStreamException {
    	return replace();
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    	out.defaultWriteObject();
    }
    
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

#ExternalizableClasses
Callback methods related to java.io.Externalizable classes.
##EC1
Tests the writeExternal/readExternal methods.
[//]: # (MAIN: ec1.Foo)
```java
// ec1/Foo.java
package ec1;

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
    public void writeExternal(ObjectOutput out) throws IOException {
        out.defaultWriteObject();
    }
    
    @CallSite(name = "defaultReadObject", resolvedTargets = "Ljava/io/ObjectOutputStream;", line = 20)
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
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

TODO test serializable: first call to no-arg constr. of its first non-serilizable super class
TODO test externalizable: call to no-arg constr.

