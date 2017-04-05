This JCG sub-project highlights the java feature of static initialization. It implements a library modeling simple arithmetic expressions. The use of static initialization of classes and interfaces turns several private and otherwise dead methods into entry points.
Contains following test cases:

 1. Static initializer of an interface with a default method
 
 ..* Expression
 2. Static initializer of an interface without a default method
 
 ..* ArithmeticExpression
 3. Static initializer block of a class (TODO: Move entry point annotation from method to static block in bytecode)
 
 ..* Negation
 ..* UnaryExpression
 4. Static initializer method call in declaration of a class
 
 ..* Constant
 5. App instantiates a class extending a chain of classes and interfaces with static initializations. An interesting chain of static initializer and constructor calls follows.
 
 ..* Expression
 ..* ArithmeticExpression
 ..* UnaryExpression
 ..* Negation
 ..* Constant