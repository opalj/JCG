# Java Call Graph (JCG)
This repository accommodates the Java Call Graph (JCG) project, a collection of annotated test cases
that are relevant for call-graph construction in Java. The test cases specifically target the call
graph's soundness and, therefore, tests the correct support of Java language features, core APIs, and
runtime (JVM) callbacks. Not supporting those features/APIs will render the call graph unsound.

The project's structure and usage are explained in the following.

## The repositories project

This repository contains multiple projects which provide different building blocks of the JCG Pipeline:

- **jcg_testcases** is the test suite's core project. It holds the test files along with 
the test cases extractor which is used to parse the test cases and to compile them to the test-case jars. 
> please note that all test cases that do belong to a newer Java version than Java 8 are stored and
> compiled outside of the pipeline. Those test cases can be found in the `infrastructure_incompatible_testcases`.

- **jcg_annotations** is a small project that contains the annotations that are required to express
a test case's expectations. 

- **jcg_annotation_matcher** provides a matcher that matches a given call graph against the expected
annotations of a particular test case. 

- **jcg_test_adapter_commons** provides an interface that must be implemented when a custom adapter
is implemented. Adapters that implement this interface are fully compatible with all JCG features and,
hence, can be used everywhere.

- **jcg_soot_adapter** accommodates an adapter that allows to test Soot's call-graph algorithms
against the test suite. Currently, the adapter supports Soot's CHA, RTA, VTA, and SPARK algorithms.
Besides testing Soot's algorithms, the adapter also allows to generate call graph's for an arbitrary
project. The adapter can also serialize the generated call graph in a unified format
(see Section Call-graph Serialization).

- **jcg_wala_adapter** accommodates an adapter that allows to test WALA's call-graph algorithms
against the test suite. Currently, the adapter supports WALA's RTA, 0-CFA, N-CFA, and 0-1-CFA algorithms.
Besides testing WALA's algorithms, the adapter also allows to generate call graph's for an arbitrary
project. The adapter can also serialize the generated call graph in a unified format
(see Section Call-graph Serialization).

- **jcg_opal_adapter** accommodates an adapter that allows to test OPAL's call-graph algorithm
against the test suite. Currently, the adapter supports OPAL's RTA.
Besides testing OPAL's algorithms, the adapter also allows to generate call graph's for an arbitrary
project. The adapter can also serialize the generated call graph in a unified format
(see Section Call-graph Serialization).

- **jcg_doop_adapter** accommodates an adapter that allows to test Doop's context-insensitive call graph
against the test suite. Besides testing DOOP, the adapter also allows to generate call graph's for an arbitrary
project. The adapter can also serialize the generated call graph in a unified format
(see Section Call-graph Serialization).

- **jcg_data_format** holds the data structure which are used to represent the call graphs internally.
And data classes for project specifications.

- **jcg_evaluation** provides a several call-graph evaluation an understanding tools. The usage

## Annotating Test Expectations

Within the core project, in `lib.annotations.callgraph`, are three different annotation classes that enable to specify expectations
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

## Use Static Analysis Framework Adapters

Whereas it is straigth forward to use the WALA and SOOT adapter, we have to do a little bit of work
to setup DOOP and OPAL.

### DOOP
To use the DOOP adapter, you need to setup DOOP. To do so, please have a look at DOOP's install instructions:

https://bitbucket.org/yanniss/doop/src/master/

### OPAL
The current OPAL adapter builds on an development build of OPAL, to set it up do the following:

First make sure to fulfill OPALs dependencies: (see [here](https://github.com/opalj/opal/src/master/))

Afterwards, clone the opal project, checkout the `develop` branch and publish the project to the local
Ivy repository:
```
$ git clone https://github.com/opalj/opal.git
$ git checkout develop
$ sbt publishLocal
```

``````

## Usage-ready Docker Container


