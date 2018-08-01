public interface JCGTestAdapter {
    // main class: x.y.Foo or null if library
    void serializeCG(String algorithm, String target, String mainClass, String[] classPath, String outputFile) throws Exception;
    String[] possibleAlgorithms();
    String frameworkName();
}
