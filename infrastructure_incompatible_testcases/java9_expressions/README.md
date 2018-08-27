This JCG sub-project highlights language and modularity concepts that have been
introduced with Java 9 and their modules.

## Tested Module Features

Java 9's module system provides several constructs that describe the interface between
multiple modules. Before we start to define concrete test case that are relevant to
call-graph construction, we give a short overview over the tested features. [1]

#### The ___export___ directive
```java
module <modulename> {
    exports <packagename>;
    exports <packagename> to <another module>(,<another moule>)*;
}
```
It allows other modules to read all public types, incl. their nested public and protected types, in the specified package
(excluding sub packages) Those would require an extra export. Whereas this makes
the package readable to all modules that depend on *<modulenname>*, the *export ... to*
construct can restrict this access to modules with the name of <another packagename>.

| Testcase | Condition|
|:--------:|----------|
|1| M1 exports package x; M2 requires x and can't call subpackage types|
|2| M1 exports package x; M2 has only access to a protected/public nested types of public outer types|
|3| M1 exports package x to MY; MZ has no access to MY|

#### The ___requires___ directive
```java
module <modulename> {
    requires <packagename>;
    requires static <packagename>
    requires transitive <packagename>
}
```
A __requires__ module directive specifies depender modules dependency. Those must always be explicitly specified.
In case a module is only required at compile time, the __requires static__ directive can be used. It states compile-time
dependencies which are optional at runtime. ***TODO: GET MORE INFO***
Since requires is by default not transitive, the __requires transitive__ directive is syntactic sugar for listing all
required dependencies of the required module explicitly.

#### The ___uses___ directive

A uses module directive specifies a service used by this module. A service is an object of a class that implements
the interface or extends the abstract class specified in the uses directive.

#### The ___provides ... with___ directive

The __provides…with__ module directive specifies that a module provides a service (interface or abstract type) implementation.
The provides part of the directive specifies an interface or abstract class listed in a module’s uses directive and
the with part of the directive specifies the name of the service provider class that implements the interface or extends the abstract class.

| Testcase | Condition|
|:--------:|----------|
|1| M1 defines service, M2 provides implementations. M1's Serviceloader should only find service implementations that are provided.|
|2| M1 defines service with default method, M2 provides impl which uses the default method, call should be correctly resolved.|

#### The ___open, opens, and opens ... to___ directives aka. reflection

Those directives define which modules are accessible or can be accessed by reflection. By default, reflection is disabled.
It's possible to define the modules that can access a given module by reflection, and, hence, more fine grained control
is possible.



[1] https://www.oracle.com/corporate/features/understanding-java-9-modules.html
