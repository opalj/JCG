public interface JCGTestAdapter {
    void serializeCG(String algorithm, String target, String[] classPath, String outputFile) throws Exception;
    String[] possibleAlgorithms();
    String frameworkName();
}
