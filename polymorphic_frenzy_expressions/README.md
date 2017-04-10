This JCG sub-project highlights the effects of java polymorphism on call graphs. It contains no corner cases and "special" features like serialization or reflection.
Contains following test cases:  

1. Static inner class.  
  - DecrementExpression$DecrementOperator  
  - MultOperator$MultExpression  
  - PlusOperator$AddExpression  
  - SubOperator$SubExpression  
2. Private inner class escapes local scope indirectly via another inner class.  
  - Map$LinkedEntry  
3. Private inner class escapes local scope via a method of containing class.  
  - Map$MapIterator  
4. Package private class escapes local scope only under the open package assumption.  
  - Stack$StackIterator  
5. Private inner class escapes local scope due to deserialization.  
  - TODO  
6. Private inner class looks like it escapes due to deserialization but locks itself in via readResolve.  
  - TODO  