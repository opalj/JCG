This JCG sub-project contains all cases not warranting their own sub-project. The project implements a library modeling simple arithmetic expressions and an app using said library to process arithmetic expressions. 
Contains following test cases:  

1. Class instantiable only via a factory method.  
  - ExpressionPrinter  
2. Calls to native method arrayCopy affecting the data flow and thus call targets.  
  - ExpressionEvaluator  
3. Declaration of native methods.  
  - Constant  
4. Native method implemented in present C code and called.  
  - NativeAddExpression  
  - ExpressionEvaluator  
5. New NullPointerException with 'throw null' statement.   
  - ExpressionEvaluator  
6. Exceptions affecting call graph.  
  - DecrementExpression  
7. Generics using wildcard typing and type erasures stemming from that.  
  - ExpressionEvaluator$ParameterizedEvaluator  
8. Arrays break type security and prevent further execution.  
  - ExpressionEvaluator  
9. Classes implementing Serializable without serialization or deserialization in the application class.  
  - Constant   
10. Classes implementing Externalizable without serialization or deserialization in the application class.  
  - AltConstant  