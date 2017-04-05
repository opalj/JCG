This JCG sub-project highlights the java Serializable and Externalizable interfaces. It implements a library modeling simple arithmetic expressions and an app saving an expression to an external file via the serialization API. The use of the serialization API turns several private and otherwise dead methods into entry points and provides a way to instantiate several otherwise dead classes. 
Contains following test cases:

1. A class implementing Serializable and methods relevant to (de-)serialization.  
  - SerializableConstant  
2. A class implementing Externalizable and methods relevant to (de-)externalization.  
  - ExternalizableConstant  
3. A class implementing Externalizable but also methods relevant to (de-)serialization (readObject and writeObject).  
  - ExternalizableConstant  
4. Application performing (de-)serialization.  
  - ExpressionSaver  
  - Expression  
  - Constant  
  - AltConstant  
  - SerializableConstant  
  - ExternalizableConstant