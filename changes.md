# Changes

In the following, we describe the changes to the JCG test suite w.r.t. to version published in the paper.

## August 27th, 2018

 - moved the current `Class.forName` **Classloading** tests to **Reflection**; the tests are basically about *Reflection and Type/Method Inference*.
 - renamed **DirectCalls** to **Non-virtualCalls**.
 - renamed **PolymorphicCalls** to **VirtualCalls**
 - added an additional expected call target to the test case for **DynamicProxies**
 - split up **Java8PolymorphicCalls** in those related to default methods and those related to default methods
