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

