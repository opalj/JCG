import java.io.Writer;

/**
 * A interface to adapt a static analysis framework to be used in the JCG context.
 *
 * @author Florian Kubler
 */
public interface JCGTestAdapter {

    /**
     * Constructs a call graph for the given target and saves the serialized version
     * ({@link ReachableMethods}) into the specified output file and return the elapsed time in
     * nanoseconds
     *
     * @param mainClass the main-class to be analysed (x.y.Foo) or `null` in case of a library
     * @return the elapsed nanoseconds
     */
    long serializeCG(
            String algorithm,
            String target,
            String mainClass,
            String[] classPath,
            String JDKPath,
            boolean analyzeJDK,
            Writer output,
            String[] jvm_args,
            String[] program_args) throws Exception;

    String[] possibleAlgorithms();

    String frameworkName();
}
