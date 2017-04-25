This JCG sub-project highlights language constructs which cause JVm callbacks.
Contains following test cases:  

1. Finalize method of a class instantiated in the app.  
  - Constant  
2. Finalize method of a class not instantiated in the app.  
  - AltConstant  
3. Method defined in Thread.UncaughtExceptionHandler().  
  - ExpressionEvaluator  
4. Registering a Thread with a run method via addShutdownHook.  
  - ExpressionEvaluator  
5. Run method of a class implementing Runnable used in the app.  
  - Thread in ExpressionEvaluator  
6. Run method of a class implementing Runnable not used in the app.  
  - AltConstant  