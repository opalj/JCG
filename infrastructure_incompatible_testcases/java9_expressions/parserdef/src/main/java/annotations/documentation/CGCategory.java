package lib.annotations.documentation;

/**
 * Created by eichberg on 28.01.16.
 */
public enum CGCategory {

    /**
     * Scenarios:
     *  - polymorphic unless the generic type parameters are taken into account..
     *  - polymorphic unless the concrete type is known..
     *  - truly polymorphic call, the call graph algorithm doesn't matter
     */
    POLYMORPHIC_CALL,

    /**
     * Monomorphic calls at compile time. E.g., constructor, private, static and super calls.
     */
    MONOMORPHIC_CALL,

    INVOKEDYNAMIC,

    /**
     * Scenarios:
     *  - class instantiation by a statically known and unique receiver type
     *  - class instantiation by a fixed set of statically known receiver types
     *  - class instantiation by a(n over method boundaries) composed string.
     *  - method invocation of a statically known method. (class also has been instantiated over reflection)
     */
    REFLECTION,

    SERIALIZABILITY,

    // There is no native code ...
    // e.g., Thread.unhandledException...
    // e.g., Thread.start => Thread.run bzw. Runnable.run
    JVM_CALLBACK,

    /**
     * Some object
     */
    NATIVE_CALLBACK,


    ARRAY_HANDLING,

    /**
     * Scenarios:
     *  - call a static method of another class
     */
    STATIC_INITIALIZERS,

    /**
     * A notice if additional information is needed.
     */
    NOTE
}
