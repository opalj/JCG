# Java Call Graph (JCG)
This repository accommodates the Java Call Graph (JCG) project, a collection of annotated test cases
that are relevant for call-graph construction in Java. The test cases specifically target the call
graph's soundness and, therefore, tests the correct support of Java language features, core APIs, and
runtime (JVM) callbacks. Not supporting those features/APIs will render the call graph unsound.

The project's structure and usage are explained in the following.

## The Testcase Project
The subproject `jcg_testcases` is test suite's s core project. It holds the test files along with 
the test cases extractor which is used to parse the test cases and compile them to jars. 

The project depends on `jcg_annotation` which contains the annotations that
are required to express the test case's expectations.

### Annotating Test Expectations

Within the core project, in `lib.annotations`, are three different annotation classes that enable to specify expectations
for a call site's method resolution, i.e., they allow to annotate a method's call sites with expected call targets.
The first annotation, `lib.annotations.CallSite`, enables to annotate a method with expectations for a specific call site,
specified by line number. Please note that the annotation of two call sites within the same source line which call a
equally named method is not supported.

```java
class Class {
    
    @CallSite(name = "toString", line = 6, resolvedTargets = "Ljava/lang/String;")
    public static void main(String[] args){ 
        if(args.length == 1){
            args[0].toString();
        }
    }
}
```

The above example shows how the `CallSite` annotation sets the expectations for the call site on *line 6*. It expresses,
that the call of the method `toString` is expected to be resolved to `java.lang.String.toString`. However, the above
example is only sufficiently annotated as long as no other `toString` method exists on type `String`, e.g., a arbitrary 
`toString` method with another method signature. To overcome this issue, the `CallSite` annotations supports the
specification of the called method's return type as well as parameter types. Additionally, it is supported to specify more
than one expected call receiver which is required by virtually resolved methods. However, the `CallSite` annotation specifies
only direct callee expectations, i.e., the annotation's expectation implies that the annotated method directly calls the
expected call targets. In other words, considering our previous example, it's expected that the call graph contains
a call edge from `Class.main` to `java.lang.String.toString`.

Another annotation, that does not expect a direct but transitive edge within the call graph, is the
`lib.annotations.IndirectCall` annotation. This annotation is specified analogously to the `CallSite` annotation but
is matched differently. It therefore allows a framework specific handling for indirect method invocations such as calls invoked
via `invokedynamics` or Java's reflection API.

### Writing Tests

Furthermore, the set of test cases, that are shipped with JCG, are located in the `src/main/resources`
directory as markdown `.md` files.
There is one markdown file for each language feature to be tested.
For each such file there might be multiple categories of test cases, indicated
by first level headers.
Each category then contains several test cases, that are identified using a second level
header.
Finally, a test case consists of a name, the specification of the main class (each test case must be a full
runnable Java programm) and several Java classes given in a code listing.
Theses listings should be annotated using the provided annotations.

In addition to the specification of the test cases, the `TestCaseExtractor` retrieves, compiles, bundle and
runs the Java code for each test case in each markdown file.

## Serialization of the CallGraph
In `jcg_testadapter_commons` we provide the following interface that should be used in order to apply a
new call graph creation framework:

```java
public interface JCGTestAdapter {
    void serializeCG(String algorithm, String target, String classPath, String outputFile) throws Exception;
    String[] possibleAlgorithms();
    String frameworkName();
}
```

For each framework, e.g. OPAL, WALA, Soot, there should be one test adapter implementation.
A call to `serialize` should execute the specific call graph algorithm and write a JSon file containing
the serialized version of the computed  call graph in the following format:

```json
{ "callSites": [
    { "declaredTarget": {
        "name": "getDeclaredMethod",
        "parameterTypes": ["Ljava/lang/String;","[Ljava/lang/Class;"],
        "returnType": "Ljava/lang/reflect/Method;",
        "declaringClass": "Ljava/lang/Class;" },
      "method": {
        "name": "main",
        "parameterTypes": ["[Ljava/lang/String;"],
        "returnType": "V",
        "declaringClass": "Ltr1/Foo;" },
      "line": 12,
      "targets": [ {
          "name": "getDeclaredMethod",
          "parameterTypes": ["Ljava/lang/String;","[Ljava/lang/Class;"],
          "returnType": "Ljava/lang/reflect/Method;",
          "declaringClass": "Ljava/lang/Class;" } ]
    },
    ... ]
}
```

A serialized call graph contains of an array of the `callSites` that are contained in the target code.
Each call site consists of a JSon object representing the `declaredTarget` method, and the `method`
containing the call site together with the `line` number. Furthermore, the `targets` array specifies the methods
that are identified as call targets in the computed call graph.

Each method object is defined, using a `name`, `returnType`, `parameterTypes` and a `declaringClass`.
All types must be given in JVM binary notation (object types start with `L`, ends with `;` and `/` is used in packages
instead of a `.`).

## Matching Expectations against Call-graph Implementations.
The `CGMatcher` in the `jcg_annotation_matcher` project, is given one `.jar` file of a test case together with the
`.json` file of a serialized call graph and computes whether the call graph matches the expectations.
Furthermore, it does some verification of the test case in order to avoid wrong annotations.


