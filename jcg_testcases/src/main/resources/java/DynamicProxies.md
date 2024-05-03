# Dynamic Proxies
Using `java.lang.reflect.InvocationHandler` type-safe proxy classes can be generated at runtime.
These proxy classes use reflection to forward the calls to a previously configured handler class.

## DP1
[//]: # (MAIN: dp.Main)
Tests the dynamic proxy API by implementing the ```dp.DebugProxy``` class that implements ```java.lang.reflect.Invocationhandler```
and provides a ```newInstance``` method that can than be used to instantiate a dynamic proxy object.
```dp.DebugProxy``` is then used in ```dp.Main```'s main method to instantiate a proxy object of the
```dp.FooImpl``` class and then calls a method on it.
```java
// dp/Foo.java
package dp;

public interface Foo { Object bar(Object obj); }
```
```java
// dp/FooImpl.java
package dp;

public class FooImpl implements Foo {
	public Object bar(Object obj) {
		return obj;
	}
}
```
```java
// dp/DebugProxy.java
package dp;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;

public class DebugProxy implements InvocationHandler {
    private Object obj;

    public static Object newInstance(Object obj) {
        return Proxy.newProxyInstance(
        obj.getClass().getClassLoader(),obj.getClass().getInterfaces(),
        new DebugProxy(obj));
    }

    private DebugProxy(Object obj) { this.obj = obj; }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        System.out.println("before method " + m.getName());
        return m.invoke(obj, args);
    }
}
```
```java
// dp/Main.java
package dp;

import java.lang.reflect.Method;
import lib.annotations.callgraph.IndirectCall;

public class Main {
	@IndirectCall(
        name = "bar", returnType = Object.class, parameterTypes = Object.class, line = 17,
        resolvedTargets = "Ldp/FooImpl;"
    )
    @IndirectCall(
        name = "invoke", returnType = Object.class, parameterTypes = {Object.class, Method.class, Object[].class}, line = 17,
        resolvedTargets = "Ldp/DebugProxy;"
    )
	public static void main(String[] args) {
		Foo foo = (Foo) DebugProxy.newInstance(new FooImpl());
		foo.bar(null);
	}
}
```
[//]: # (END)
