Scope: Complete Java Code Is Available (The Project is Valid (w.r.t. JVM))

#Entry Points
- Application
app.ExpressionEvaluator.main

- JEE Web Application
- OSGI Bundle
(- Android)

#Use of Generics

#Call Back Methods (source: native methods)
(**Deferred** Entry Points)

#Virtual (Classes/Interfaces) Method Calls
## Overridden Methods  
## Java 8 Default Methods 

### Anonymous Classes, Local Classes, Inner Classes, Nested Classes (in Method Scope) (JLS 8.1.3)
###(private) "Inner Classes" (Static, Virtual, ...)

#Static (Special) Method Call

#Points to/Escape Analysis required
###Calls on uninitialized values
class Y { this(){f()} def f()}
class X extends Y {
	val a = "fooo"
	override def f(){a.toString()} // a is here null
}

## Java 8 Static Interface Methods
	
#Static Initializers (Static Initialization Order)

#Serializability Related Methods (read... / write...)	

#Reflection Related

##Instance Creation (Constructor Calls)
##(static) Method Calls

##Class Loading

#Invokedynamic (Java 8/General) 
(Lambda Expressions)

#Signature Polymorphic Methods (JVM 8)

#DynamicProxy
	
#Array Handling
in particular int[] a= ...; a.clone() bzw. a.toString()...

#JVM Event Listeners (without any direct native code)
Thread.UncaughtExceptionHandler
Runtime.addShutdownHook

#Threads

#System.exit and Hooks???

# Legal JVM Stuff that cannot be created using Java 
## (e.g. package visibility related)
## Inheriting from Java Level incompatible classes/interfaces w.r.t. the method return type
## Inheriting from Java Level incompatible classes/interfaces w.r.t. declared exceptions