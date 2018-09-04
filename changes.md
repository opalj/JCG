# Changes

In the following, we describe the changes to the JCG test suite w.r.t. to version published in the paper.

## August 27th, 2018

 - moved the current `Class.forName` **Classloading** tests to **Reflection**; the tests are basically about *Reflection and Type/Method Inference*.
 - renamed **DirectCalls** to **Non-virtualCalls**.
 - renamed **PolymorphicCalls** to **VirtualCalls**
 - added an additional expected call target to the test case for **DynamicProxies**
 - split up **Java8PolymorphicCalls** in those related to default methods and those related to static interface methods
 - renamed **(LambdasAnd)MethodReferences** to **Java8Invokedynamics** to reflect that the tests cases are about the invokedynamic instructions created by Java 8+ method references and lambda expressions. Please note that the Scala compiler hijacks Java's infrastructure and analyses which support Java 8's invokedynamics (i.e., those using `(Alt)LambdaMetaFactory`) will also support Scala to a reasonable amount.
 - removed **PackageBoundaries** (at least for the time being), because the tests are not related to unsoundness (commit: #bc967bb3363bfd85449937126f63ad6e666875cd)
