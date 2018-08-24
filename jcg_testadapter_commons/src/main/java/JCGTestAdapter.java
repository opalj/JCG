public interface JCGTestAdapter {
    // main class: x.y.Foo or null if library
    long serializeCG(
            String algorithm,
            String target,
            String mainClass,
            String[] classPath,
            String jreLocations,
            int jreVersion,
            String outputFile) throws Exception;

    String[] possibleAlgorithms();

    String frameworkName();
}
