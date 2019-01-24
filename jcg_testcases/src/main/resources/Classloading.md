# Classloading

Tests related to the usage of `java.lang.ClassLoader`s. The following test cases are available and
listed in the `infrastructure incompatible testcases`

## CL1
This test case uses an `URLClassLoader` in order to load classes from an external *.jar* file.
That class will be instantiated using `Class<?>.newInstance`.
Afterwards, it calls the `compare` on the `Comparator` interface, which will resolve to the `IntComparator` 
from the given *.jar* at runtime.

```java
package demo;

import lib.annotations.callgraph.IndirectCall;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;

public class Demo {

    private static final String DIR = System.getProperty("user.dir") + "/resources/";
    private static URL CLv1;

    static {
        try {
            CLv1 = new URL("file://" + DIR + "classloading-version-1.jar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static final String CLS_NAME = "lib.IntComparator";

    @IndirectCall(name = "compareTo", returnType = int.class, line = 34, resolvedTargets = "Ljava/lang/Integer;")
    public static void main(String[] args)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        URL[] urls = new URL[] { CLv1 };
        URLClassLoader cl = URLClassLoader.newInstance(urls, parent);
        Class<?> cls = cl.loadClass(CLS_NAME);
        Comparator<Integer> comparator = (Comparator<Integer>) cls.newInstance();
        Integer one = Integer.valueOf(1);
        comparator.compare(one, one);
    }
}

```

## CL2
This test case is basically the same as CL1. In contrast to this the generic type of the class is 
already specified before calling `Class<Comparator<Integer>>.newInstance`.

```java
package demo;

import lib.annotations.callgraph.IndirectCall;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;

public class Main {

    private static final String DIR = System.getProperty("user.dir") + "/resources/";
    private static URL CLv2;

    static {
        try {
            CLv2 = new URL("file://" + DIR + "classloading-version-2.jar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static final String CLS_NAME = "lib.IntComparator";

    @IndirectCall(name = "compareTo", returnType = int.class, line = 35, resolvedTargets = "Ljava/lang/Integer;")
    public static void main(String[] args)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        URL[] urls = new URL[] { CLv2 };
        URLClassLoader cl = URLClassLoader.newInstance(urls, parent);
        Class<Comparator<Integer>> cls = (Class<Comparator<Integer>>) cl.loadClass(CLS_NAME);
        Comparator<Integer> comparator = cls.newInstance();
        Integer one = Integer.valueOf(1);
        comparator.compare(one, one);
    }
}
```


## CL3
In this test case, to different versions of a class are loaded using an `URLClassLoader`.
On both versions a call to `<Comparator<Integer>>.compare` is performed.
After those different versioned classes are loaded, methods are called on the classes which must
be resolved to different targets.

```java
package cl;

import lib.annotations.callgraph.IndirectCall;
import lib.annotations.callgraph.IndirectCalls;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Comparator;

public class Demo {

    //private static final String DIR = System.getProperty("user.dir") + "/resources/";
    private static final String DIR = "/Users/mreif/Programming/git/jcg/infrastructure_incompatible_testcases/CL3/src/resources/";
    private static URL CLv1;
    private static URL CLv2;
    private static final String CLS_NAME = "lib.IntComparator";

    static {
        try {
            CLv1 = new URL("file://" + DIR + "classloading-version-1.jar");
            CLv2 = new URL("file://" + DIR + "classloading-version-2.jar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @IndirectCalls({
        @IndirectCall(name = "compare", line = 53, returnType = int.class, resolvedTargets = "Ljava/lang/Integer;"),
        @IndirectCall(name = "gc", line = 54, resolvedTargets = "Ljava/lang/System;")
    })
    public static void main(String[] args)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader oldParent = Thread.currentThread().getContextClassLoader();
        ClassLoader newParent = Demo.class.getClassLoader();

        URL[] oldResource = new URL[]{CLv1};
        URL[] newResource = new URL[]{CLv2};

        URLClassLoader oldVersionLoader = new URLClassLoader(oldResource, oldParent);
        URLClassLoader newVersionLoader= new URLClassLoader(newResource, newParent);

        System.out.println(CLv1.toExternalForm());

        Class<?> oldClass = oldVersionLoader.loadClass(CLS_NAME);
        Class<?> newClass = newVersionLoader.loadClass(CLS_NAME);

        Comparator<Integer> oldComparator = (Comparator<Integer>) oldClass.newInstance();
        Comparator<Integer> newComparator = (Comparator<Integer>) newClass.newInstance();

        Integer one = new Integer(1);

        oldComparator.compare(one,one);
        newComparator.compare(one,one);
    }
}
```

## CL4
[//]: # (MAIN: cl4.Demo)
This test case defines a custom classloader, that loads the class `lib.IntComparator` from a byte array.
The array *bytes* contains the bytes of the following class:
```java
package lib;

import java.util.Comparator;
import cl4.Demo;

public class IntComparator implements Comparator<Integer> {
    @Override
    public int compare(Integer o1, Integer o2) {
        Demo.callback;
        return o1.compareTo(o2);
    }
}

```
In order to extract the bytes, we used `java.nio.file.Files.readAllBytes()`.

```java
// cl4/Demo.java
package cl4;

import lib.annotations.callgraph.IndirectCall;
import java.util.Comparator;

public class Demo {

	@IndirectCall(name = "callback", line = 14, resolvedTargets = "Lcl4/Demo;")
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader cl = new ByteClassLoader(ClassLoader.getSystemClassLoader());
        Class<?> cls = cl.loadClass("lib.IntComparator");
        Comparator<Integer> comparator = (Comparator<Integer>) cls.newInstance();
        Integer one = 1;
        comparator.compare(one, one);
    }

    public static void callback() { }
}
```
```java
// cl4/ByteClassLoader.java
package cl4;

import java.security.SecureClassLoader;

public class ByteClassLoader extends SecureClassLoader {

    public ByteClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.equals("lib.IntComparator")) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        return super.findClass(name);
    }

    private final byte[] bytes = new byte[]{
            -54, -2, -70, -66, 0, 0, 0, 52, 0, 39, 10, 0, 7, 0, 26, 10, 0, 27, 0, 28, 10, 0, 4, 0,
            29, 7, 0, 30, 10, 0, 6, 0, 31, 7, 0, 32, 7, 0, 33, 7, 0, 34, 1, 0, 6, 60, 105, 110,
            105, 116, 62, 1, 0, 3, 40, 41, 86, 1, 0, 4, 67, 111, 100, 101, 1, 0, 15, 76, 105, 110, 101, 78,
            117, 109, 98, 101, 114, 84, 97, 98, 108, 101, 1, 0, 18, 76, 111, 99, 97, 108, 86, 97, 114, 105, 97, 98,
            108, 101, 84, 97, 98, 108, 101, 1, 0, 4, 116, 104, 105, 115, 1, 0, 19, 76, 108, 105, 98, 47, 73, 110,
            116, 67, 111, 109, 112, 97, 114, 97, 116, 111, 114, 59, 1, 0, 7, 99, 111, 109, 112, 97, 114, 101, 1, 0,
            41, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 73, 110, 116, 101, 103, 101, 114, 59, 76, 106, 97,
            118, 97, 47, 108, 97, 110, 103, 47, 73, 110, 116, 101, 103, 101, 114, 59, 41, 73, 1, 0, 2, 111, 49, 1,
            0, 19, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 73, 110, 116, 101, 103, 101, 114, 59, 1, 0, 2,
            111, 50, 1, 0, 39, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59,
            76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 59, 41, 73, 1, 0, 9, 83,
            105, 103, 110, 97, 116, 117, 114, 101, 1, 0, 61, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98,
            106, 101, 99, 116, 59, 76, 106, 97, 118, 97, 47, 117, 116, 105, 108, 47, 67, 111, 109, 112, 97, 114, 97, 116,
            111, 114, 60, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 73, 110, 116, 101, 103, 101, 114, 59, 62, 59,
            1, 0, 10, 83, 111, 117, 114, 99, 101, 70, 105, 108, 101, 1, 0, 18, 73, 110, 116, 67, 111, 109, 112, 97,
            114, 97, 116, 111, 114, 46, 106, 97, 118, 97, 12, 0, 9, 0, 10, 7, 0, 35, 12, 0, 36, 0, 10, 12,
            0, 37, 0, 38, 1, 0, 17, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 73, 110, 116, 101, 103, 101, 114,
            12, 0, 16, 0, 17, 1, 0, 17, 108, 105, 98, 47, 73, 110, 116, 67, 111, 109, 112, 97, 114, 97, 116, 111,
            114, 1, 0, 16, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 79, 98, 106, 101, 99, 116, 1, 0, 20, 106,
            97, 118, 97, 47, 117, 116, 105, 108, 47, 67, 111, 109, 112, 97, 114, 97, 116, 111, 114, 1, 0, 8, 99, 108,
            52, 47, 68, 101, 109, 111, 1, 0, 8, 99, 97, 108, 108, 98, 97, 99, 107, 1, 0, 9, 99, 111, 109, 112,
            97, 114, 101, 84, 111, 1, 0, 22, 40, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 73, 110, 116, 101,
            103, 101, 114, 59, 41, 73, 0, 33, 0, 6, 0, 7, 0, 1, 0, 8, 0, 0, 0, 3, 0, 1, 0, 9,
            0, 10, 0, 1, 0, 11, 0, 0, 0, 47, 0, 1, 0, 1, 0, 0, 0, 5, 42, -73, 0, 1, -79, 0,
            0, 0, 2, 0, 12, 0, 0, 0, 6, 0, 1, 0, 0, 0, 7, 0, 13, 0, 0, 0, 12, 0, 1, 0,
            0, 0, 5, 0, 14, 0, 15, 0, 0, 0, 1, 0, 16, 0, 17, 0, 1, 0, 11, 0, 0, 0, 75, 0,
            2, 0, 3, 0, 0, 0, 9, -72, 0, 2, 43, 44, -74, 0, 3, -84, 0, 0, 0, 2, 0, 12, 0, 0,
            0, 10, 0, 2, 0, 0, 0, 10, 0, 3, 0, 11, 0, 13, 0, 0, 0, 32, 0, 3, 0, 0, 0, 9,
            0, 14, 0, 15, 0, 0, 0, 0, 0, 9, 0, 18, 0, 19, 0, 1, 0, 0, 0, 9, 0, 20, 0, 19,
            0, 2, 16, 65, 0, 16, 0, 21, 0, 1, 0, 11, 0, 0, 0, 55, 0, 3, 0, 3, 0, 0, 0, 13,
            42, 43, -64, 0, 4, 44, -64, 0, 4, -74, 0, 5, -84, 0, 0, 0, 2, 0, 12, 0, 0, 0, 6, 0,
            1, 0, 0, 0, 7, 0, 13, 0, 0, 0, 12, 0, 1, 0, 0, 0, 13, 0, 14, 0, 15, 0, 0, 0,
            2, 0, 22, 0, 0, 0, 2, 0, 23, 0, 24, 0, 0, 0, 2, 0, 25
    };
}
```
[//]: # (END)