#Usage of sun.misc.Unsafe


##UnsafeSwap
[//]: # (MAIN: swap.Foo)

```java
// swap/Foo.java
package swap;

import sun.misc.Unsafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import lib.annotations.callgraph.CallSite;

public class Foo {
    private Object objectVar = null;

	@CallSite(name = "toString", resolvedTargets = "Lswap/Bar;", line = 22)
    public static void main(String[] args) throws Exception {
        Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
        unsafeConstructor.setAccessible(true);
        Unsafe unsafe = unsafeConstructor.newInstance();

        Foo o = new Foo();
        Field objectField = Foo.class.getDeclaredField("objectVar");
        long objectOffset = unsafe.objectFieldOffset(objectField);

        unsafe.compareAndSwapObject(o, objectOffset, null, new Bar());
        o.objectVar.toString();
    }
}

class Bar {
	public String toString() {
		return "Bar";
	}
}
```
[//]: # (END)

##UnsafePut
[//]: # (MAIN: put.Foo)

```java
// put/Foo.java
package put;

import sun.misc.Unsafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import lib.annotations.callgraph.CallSite;

public class Foo {
    private Object objectVar = null;

	@CallSite(name = "toString", resolvedTargets = "Lput/Bar;", line = 22)
    public static void main(String[] args) throws Exception {
        Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
        unsafeConstructor.setAccessible(true);
        Unsafe unsafe = unsafeConstructor.newInstance();

        Foo o = new Foo();
        Field objectField = Foo.class.getDeclaredField("objectVar");
        long objectOffset = unsafe.objectFieldOffset(objectField);

        unsafe.putObject(o, objectOffset, new Bar());
        o.objectVar.toString();
    }
}

class Bar {
	public String toString() {
		return "Bar";
	}
}
```
[//]: # (END)

##UnsafeGet
[//]: # (MAIN: get.Foo)

```java
// get/Foo.java
package get;

import sun.misc.Unsafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import lib.annotations.callgraph.CallSite;

public class Foo {
    private Object objectVar = null;

	@CallSite(name = "toString", resolvedTargets = "Lget/Bar;", line = 23)
    public static void main(String[] args) throws Exception {
        Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
        unsafeConstructor.setAccessible(true);
        Unsafe unsafe = unsafeConstructor.newInstance();

        Foo o = new Foo();
        Field objectField = Foo.class.getDeclaredField("objectVar");
        long objectOffset = unsafe.objectFieldOffset(objectField);

        o.objectVar = new Bar();
        Object f = unsafe.getObject(o, objectOffset);
        f.toString();
    }
}

class Bar {
	public String toString() {
		return "Bar";
	}
}
```
[//]: # (END)



//put/getObject
