#StaticInitializers
Static initializers have to be treated as entry points.

##SI1
[//]: # (MAIN: si.Bar)
A static initializer should be triggered when a non-constant field is referenced.

```java
// si/Demo.java
package si;

import lib.annotations.callgraph.CallSite;
public interface Demo {

	static String name = init();

    @CallSite(name = "callback", line = 10, resolvedTargets = "Lsi/Demo;")
	static String init() {
		callback();
		return "Demo";
	}

	static void callback() {}
}
class Bar {
	public static void main(String[] args) {
		Demo.name.toString();
	}
}
```
[//]: # (END)

##SI2
[//]: # (MAIN: si.Demo)
A static initializer should be triggered when a static interface method is invoked.
```java
// si/Interface.java
package si;

import lib.annotations.callgraph.CallSite;
public interface Interface {

	static String name = init();

    @CallSite(name = "callback", line = 10, resolvedTargets = "Lsi/Interface;")
	static String init() {
		callback();
		return "Demo";
	}

	static void callback() {}
}
class Demo {
	public static void main(String[] args) {
		Interface.callback();
	}
}
```
[//]: # (END)

##SI3
[//]: # (MAIN: si.Demo)
Static initializer of an interface with a default method.

```java
// si/Interface.java
package si;

import lib.annotations.callgraph.CallSite;
public interface Interface {

	static String name = init();

    @CallSite(name = "callback", line = 10, resolvedTargets = "Lsi/Interface;")
	static String init() {
		callback();
		return "Demo";
	}

	default String m() { return "Demo"; }

	static void callback() {}
}
class Demo implements Interface {
	public static void main(String[] args) {
		new Demo();
	}
}
```
[//]: # (END)

##SI4
[//]: # (MAIN: si.Demo)
An interface static initializer should be triggered when a final static field with a non-primitive type
and non-String type is referenced.

```java
// si/Demo.java
package si;

import lib.annotations.callgraph.CallSite;
public class Demo {
	public static void main(String[] args) {
		Interface.referenceMe.toString();
	}
}

interface Interface {

    static String testHook = init();
    static final Demo referenceMe = new Demo();

    @CallSite(name = "callback", line = 17, resolvedTargets = "Lsi/Interface;")
    static String init() {
        callback();
        return "Interface";
    }

    static void callback(){}
}
```
[//]: # (END)

##SI5
[//]: # (MAIN: si.Demo)
Static initializer block of a class.

```java
// si/Demo.java
package si;

import lib.annotations.callgraph.CallSite;
public class Demo {

	public static void main(String[] args) {
		new Foo();
	}
}

class Foo {

	static {
		init();
	}

    @CallSite(name = "callback", line = 19, resolvedTargets = "Lsi/Foo;")
	static void init() {
		callback();
	}

	static void callback() {}
}
```
[//]: # (END)

##SI6
[//]: # (MAIN: si.Demo)
Static initializer method call in declaration of a class.

```java
// si/Demo.java
package si;

import lib.annotations.callgraph.CallSite;
public class Demo {

	public static void main(String[] args) {
		Foo.callback();
	}
}

class Foo {
	static String name = init();

    @CallSite(name = "callback", line = 16, resolvedTargets = "Lsi/Foo;")
	static String init() {
		callback();
		return "Demo";
	}

	static void callback() {}
}
```
[//]: # (END)

##SI7
[//]: # (MAIN: si.Demo)
Static initializer method call in declaration of a class.

```java
// si/Demo.java
package si;

import lib.annotations.callgraph.CallSite;
public class Demo {

	public static void main(String[] args) {
		Foo.assignMe = 42;
	}
}

class Foo {
	static String name = init();

    static int assignMe;

    @CallSite(name = "callback", line = 18, resolvedTargets = "Lsi/Foo;")
	static String init() {
		callback();
		return "Foo";
	}

	static void callback() {}
}
```
[//]: # (END)

##SI8
[//]: # (MAIN: si.Class)
When a class is initialized, its super classes are also initialized.

```java
// si/Class.java
package si;

import lib.annotations.callgraph.CallSite;
public class Class{

	public static void main(String[] args) {
		new SubClass();
	}
}

class SubClass extends SuperClass {
	static String name = init();

    @CallSite(name = "callback", line = 16, resolvedTargets = "Lsi/SubClass;")
	static String init() {
		callback();
		return "SubClass";
	}

	static void callback() {}
}

class SuperClass extends RootClass {

    static {
        superInit();
    }

    @CallSite(name = "callback", line = 31, resolvedTargets = "Lsi/SuperClass;")
    static void superInit(){
        callback();
    }

    static void callback() {}
}

class RootClass {

    static {
        rootInit();
    }

    @CallSite(name = "callback", line = 45, resolvedTargets = "Lsi/RootClass;")
    static void rootInit(){
      callback();
    }

    static void callback() {}
}
```
[//]: # (END)
