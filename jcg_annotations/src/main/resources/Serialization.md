#SerializableClasses

##ss1
```java
// tr1/Foo.java
package tr1;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import lib.annotations.callgraph.IndirectCall;
public class Foo implements Serializable {
    public static String callback() { return ""; }



    public static void main(String[] args) throws Exception {

    }
}
```
[//]: # (END)

##Deserialization1

