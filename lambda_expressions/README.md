This JCG sub-project features java8 lambda functions. It implements a library modeling simple arithmetic expressions while heavily relying on lambda functions. Test cases featuring method references and generics do not feature the use of wildcards in the generic typing as wildcards are not permitted in this context (JLS8 page 78).
Contains following test cases:

1. Lambda function call.  
  - IncrementExpression  
  - SquareExpression  
2. Lambda function returned by a functional interface and invoked via apply.  
  - UnaryExpression  
  - IUnaryOperator  
3. Method accepting a method reference by accepting a Function<T,R> as input.  
  - Constant  
  - Expression  
  - DecrementExpression  
  - IdentityExpression  
  - IncrementExpression  
  - SquareExpression  
4. Method reference to a method of a particular object used as input.  
  - ExpressionPrinter$ExpressionStringifier  
  - ExpressionPrinter  
5. Method reference to a method of an arbitrary object of particular type used as input.  
  - ExpressionPrinter$ExpressionStringifier  
6. Method reference of a static method of a class  
  - ExpressionPrinter  
7. Method reference of a constructor  
  - ExpressionPrinter  