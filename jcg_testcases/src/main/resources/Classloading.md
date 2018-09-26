#Classloading

Tests related to the usage of `java.lang.ClassLoader`s. The following test cases are available and
listed in the `infrastructure incompatible testcases`

- __CL1__ - TODO
- __CL2__ - TODO
- __CL3__ - TODO

##CL4
[//]: # (MAIN: cl4.Demo)
This test case defines a custom classloader, that loads the class from a byte array.
```java
// cl4/Demo.java
package cl4;

import lib.annotations.callgraph.IndirectCall;
import java.util.Comparator;

public class Demo {

	@IndirectCall(name = "compare", line = 14, resolvedTargets = "Llib/IntComparator;")
    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ClassLoader cl = new ByteClassLoader(ClassLoader.getSystemClassLoader());
        Class<?> cls = cl.loadClass("lib.IntComparator");
        Comparator<Integer> comparator = (Comparator<Integer>) cls.newInstance();
        Integer one = 1;
        comparator.compare(one, one);
    }
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

    private final byte[] bytes = new byte[] {
            -54,-2,-70,-66,0,0,0,52,0,34,10,0,6,0,25,10,0,3,0,26,7,0,27,10,
            0,5,0,28,7,0,29,7,0,30,7,0,31,1,0,6,60,105,110,105,116,62,1,0,
            3,40,41,86,1,0,4,67,111,100,101,1,0,15,76,105,110,101,78,117,109,98,101,114,
            84,97,98,108,101,1,0,18,76,111,99,97,108,86,97,114,105,97,98,108,101,84,97,98,
            108,101,1,0,4,116,104,105,115,1,0,19,76,108,105,98,47,73,110,116,67,111,109,112,
            97,114,97,116,111,114,59,1,0,7,99,111,109,112,97,114,101,1,0,41,40,76,106,97,
            118,97,47,108,97,110,103,47,73,110,116,101,103,101,114,59,76,106,97,118,97,47,108,97,
            110,103,47,73,110,116,101,103,101,114,59,41,73,1,0,2,111,49,1,0,19,76,106,97,
            118,97,47,108,97,110,103,47,73,110,116,101,103,101,114,59,1,0,2,111,50,1,0,39,
            40,76,106,97,118,97,47,108,97,110,103,47,79,98,106,101,99,116,59,76,106,97,118,97,
            47,108,97,110,103,47,79,98,106,101,99,116,59,41,73,1,0,9,83,105,103,110,97,116,
            117,114,101,1,0,61,76,106,97,118,97,47,108,97,110,103,47,79,98,106,101,99,116,59,
            76,106,97,118,97,47,117,116,105,108,47,67,111,109,112,97,114,97,116,111,114,60,76,106,
            97,118,97,47,108,97,110,103,47,73,110,116,101,103,101,114,59,62,59,1,0,10,83,111,
            117,114,99,101,70,105,108,101,1,0,18,73,110,116,67,111,109,112,97,114,97,116,111,114,
            46,106,97,118,97,12,0,8,0,9,12,0,32,0,33,1,0,17,106,97,118,97,47,108,
            97,110,103,47,73,110,116,101,103,101,114,12,0,15,0,16,1,0,17,108,105,98,47,73,
            110,116,67,111,109,112,97,114,97,116,111,114,1,0,16,106,97,118,97,47,108,97,110,103,
            47,79,98,106,101,99,116,1,0,20,106,97,118,97,47,117,116,105,108,47,67,111,109,112,
            97,114,97,116,111,114,1,0,9,99,111,109,112,97,114,101,84,111,1,0,22,40,76,106,
            97,118,97,47,108,97,110,103,47,73,110,116,101,103,101,114,59,41,73,0,33,0,5,0,
            6,0,1,0,7,0,0,0,3,0,1,0,8,0,9,0,1,0,10,0,0,0,47,0,
            1,0,1,0,0,0,5,42,-73,0,1,-79,0,0,0,2,0,11,0,0,0,6,0,1,
            0,0,0,5,0,12,0,0,0,12,0,1,0,0,0,5,0,13,0,14,0,0,0,1,
            0,15,0,16,0,1,0,10,0,0,0,68,0,2,0,3,0,0,0,6,43,44,-74,0,
            2,-84,0,0,0,2,0,11,0,0,0,6,0,1,0,0,0,8,0,12,0,0,0,32,
            0,3,0,0,0,6,0,13,0,14,0,0,0,0,0,6,0,17,0,18,0,1,0,0,
            0,6,0,19,0,18,0,2,16,65,0,15,0,20,0,1,0,10,0,0,0,55,0,3,
            0,3,0,0,0,13,42,43,-64,0,3,44,-64,0,3,-74,0,4,-84,0,0,0,2,0,
            11,0,0,0,6,0,1,0,0,0,5,0,12,0,0,0,12,0,1,0,0,0,13,0,
            13,0,14,0,0,0,2,0,21,0,0,0,2,0,22,0,23,0,0,0,2,0,24};
}
```
[//]: # (END)