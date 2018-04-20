#TypeNarrowing
Using local information to get better type information
##SimpleCast
[//]: # (MAIN: simplecast.Foo)
Type narrowing due to previous cast.
```java
// simplecast/Foo.java
package simplecast;

import lib.annotations.callgraph.CallSite;
class Foo {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) 
          m(new Bar());
        else 
          m(new Foo());
    }

    @CallSite(
        name = "toString", returnType = String.class, line = 18,
        resolvedTargets = "Lsimplecast/Bar;"
    )
    static void m(Object o) {
        Bar b = (Bar) o;
        b.toString();
    }

    public String toString() { return "Foo"; }
}
class Bar {
  public String toString() { return "Bar"; }
}

```
[//]: # (END)

##CastClassAPI
[//]: # (MAIN: castclassapi.Foo)
Type narrowing due to previous cast using java class API.
```java
// castclassapi/Foo.java
package castclassapi;

import lib.annotations.callgraph.CallSite;
class Foo {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) 
          m(new Bar());
        else 
          m(new Foo());
    }

    @CallSite(
        name = "toString", returnType = String.class, line = 18,
        resolvedTargets = "Lcastclassapi/Bar;"
    )
    static void m(Object o) {
        Bar b = Bar.class.cast(o);
        b.toString();
    }

    public String toString() { return "Foo"; }
}
class Bar {
  public String toString() { return "Bar"; }
}

```
[//]: # (END)

##ClassEQ
[//]: # (MAIN: classeq.Foo)
Type narrowing due to class equallity check.
```java
// classeq/Foo.java
package classeq;

import lib.annotations.callgraph.CallSite;
class Foo{ 
    public static void main(String[] args) throws Exception {
        if (args.length == 0) 
          m(new Bar());
        else 
          m(new Foo());
    }

    @CallSite(
        name = "toString", returnType = String.class, line = 18,
        resolvedTargets = "Lclasseq/Bar;"
    )
    static void m(Object o) {
      if (o.getClass() == Bar.class)
        o.toString();
    }

    public String toString() { return "Foo"; }
}
class Bar {
  public String toString() { return "Bar"; }
}

```
[//]: # (END)


##InstanceOf
[//]: # (MAIN: instanceofcheck.Foo)
Type narrowing due to previous instance of check.
```java
// instanceofcheck/Foo.java
package instanceofcheck;

import lib.annotations.callgraph.CallSite;
class Foo{ 
    public static void main(String[] args) throws Exception {
        if (args.length == 0) 
          m(new Bar());
        else 
          m(new Foo());
    }

    @CallSite(
        name = "toString", returnType = String.class, line = 18,
        resolvedTargets = "Linstanceofcheck/Bar;"
    )
    static void m(Object o) {
      if (o instanceof Bar)
        o.toString();
    }

    public String toString() { return "Foo"; }
}
class Bar {
  public String toString() { return "Bar"; }
}

```
[//]: # (END)

##InstanceOfClassAPI
[//]: # (MAIN: instanceofclassapi.Foo)
Type narrowing due to previous instance of check.
```java
// instanceofclassapi/Foo.java
package instanceofclassapi;

import lib.annotations.callgraph.CallSite;
class Foo{ 
    public static void main(String[] args) throws Exception {
        if (args.length == 0) 
          m(new Bar());
        else 
          m(new Foo());
    }

    @CallSite(
        name = "toString", returnType = String.class, line = 18,
        resolvedTargets = "Linstanceofclassapi/Bar;"
    )
    static void m(Object o) {
      if (Bar.class.isInstance(o))
        o.toString();
    }

    public String toString() { return "Foo"; }
}
class Bar {
  public String toString() { return "Bar"; }
}

```
[//]: # (END)


##IsAssignable
[//]: # (MAIN: isssignable.Foo)
Type narrowing due to previous is assignable.
```java
// isssignable/Foo.java
package isssignable;

import lib.annotations.callgraph.CallSite;
class Foo{ 
    public static void main(String[] args) throws Exception {
        if (args.length == 0) 
          m(new Bar());
        else 
          m(new Foo());
    }

    @CallSite(
        name = "toString", returnType = String.class, line = 18,
        resolvedTargets = "Lisssignable/Bar;"
    )
    static void m(Object o) {
      if (Bar.class.isAssignableFrom(o.getClass()))
        o.toString();
    }

    public String toString() { return "Foo"; }
}
class Bar {
  public String toString() { return "Bar"; }
}

```
[//]: # (END)

//TODO GENERICS and intersection types