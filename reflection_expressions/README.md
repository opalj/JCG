This JCG sub-project highlights the java reflection API including the java8 addition of the Executable class.
Contains following test cases:

1. Class instantiated with the Class.newInstance() method.  
  - BinaryExpression (non-trivial case)  
  - UnaryExpression (trivial case)  
2. Calling a method via Method.invoke().  
  - BinaryExpression  
3. Application class instantiating classes employing reflection  
  - MasonsExpressions  
  - BinaryExpression  
  - UnaryExpression  