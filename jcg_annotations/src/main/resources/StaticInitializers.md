#StaticInitializers
Static initializers have to be treated as entry points.
##SI1
[//]: # (MAIN: si1.Bar)
Static initializer of an interface without a default method. 
```java
// si1/Foo.java
package si1;

import lib.annotations.callgraph.CallSite;
public interface Foo {

	static String name = init();

    @CallSite(name = "callback", line = 10, resolvedTargets = "Lsi1/Foo;")
	static String init() {
		callback();
		return "Foo";
	}

	static void callback() {}
}
class Bar implements Foo {
	public static void main(String[] args) {
		new Bar();
	}
}
```
[//]: # (END)

##SI2
[//]: # (MAIN: si2.Bar)
Static initializer of an interface with a default method. 
```java
// si2/Foo.java
package si2;

import lib.annotations.callgraph.CallSite;
public interface Foo {

	static String name = init();

    @CallSite(name = "callback", line = 10, resolvedTargets = "Lsi2/Foo;")
	static String init() {
		callback();
		return "Foo";
	}

	default String m() { return "Foo"; }

	static void callback() {}
}
class Bar implements Foo {
	public static void main(String[] args) {
		new Bar();
	}
}
```
[//]: # (END)


##SI3
[//]: # (MAIN: si3.Foo)
Static initializer block of a class.
```java
// si3/Foo.java
package si3;

import lib.annotations.callgraph.CallSite;
public class Foo {

	static {
		init();
	}

    @CallSite(name = "callback", line = 12, resolvedTargets = "Lsi3/Foo;")
	static void init() {
		callback();
	}

	static void callback() {}

	public static void main(String[] args) {
		new Foo();
	}
}
```
[//]: # (END)

##SI4
[//]: # (MAIN: si4.Foo)
Static initializer method call in declaration of a class.  
```java
// si4/Foo.java
package si4;

import lib.annotations.callgraph.CallSite;
public class Foo {

	static String name = init();

    @CallSite(name = "callback", line = 10, resolvedTargets = "Lsi4/Foo;")
	static String init() {
		callback();
		return "Foo";
	}

	static void callback() {}

	public static void main(String[] args) {
		new Foo();
	}
}
```
[//]: # (END)