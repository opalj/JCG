package org.jcg.taieadapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Tai_e_TestAdapterImpl {
    // ---- JCG Adapter: TOOL-INDEPENDENT FORMAT ----

    /**
     * Representation of a method in the JCG format.
     * Contains method name, declaring class (in JVM format), return type and
     * parameter types (all JVM-formatted).
     */
    public static class Method {
        public String name, declaringClass, returnType;
        public List<String> parameterTypes;

        public Method(String name, String declaringClass, String returnType,
                List<String> parameterTypes) {
            this.name = name;
            this.declaringClass = declaringClass;
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Method))
                return false;
            Method m = (Method) o;
            return Objects.equals(name, m.name) &&
                    Objects.equals(declaringClass, m.declaringClass) &&
                    Objects.equals(returnType, m.returnType) &&
                    Objects.equals(parameterTypes, m.parameterTypes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, declaringClass, returnType, parameterTypes);
        }
    }

    /**
     * Representation of a call site in the JCG format.
     * Contains the declared target, source line, bytecode offset (if available),
     * and the set of possible targets.
     */
    public static class CallSite {
        public Method declaredTarget;
        public int line;
        public Integer pc;
        public Set<Method> targets;

        public CallSite(Method declaredTarget, int line, Integer pc, Set<Method> targets) {
            this.declaredTarget = declaredTarget;
            this.line = line;
            this.pc = pc;
            this.targets = targets;
        }
    }

    /**
     * Representation of a reachable method and its outgoing call sites in the JCG
     * format.
     */
    public static class ReachableMethod {
        public Method method;
        public Set<CallSite> callSites;

        public ReachableMethod(Method method, Set<CallSite> callSites) {
            this.method = method;
            this.callSites = callSites;
        }
    }

    /**
     * Container for all reachable methods, as required by the JCG format.
     */
    public static class ReachableMethods {
        public Set<ReachableMethod> reachableMethods;

        public ReachableMethods(Set<ReachableMethod> reachableMethods) {
            this.reachableMethods = reachableMethods;
        }
    }

    // ---- JCG Adapter Entry Point ----

    /**
     * Entry point: Converts a Tai-e call graph into the JCG ReachableMethods JSON
     * format and writes it to the given output Writer.
     * 
     * @return The runtime in nanoseconds.
     */
    public long serializeCG(
            String algorithm,
            String inputDirPath,
            Writer output,
            String mainClass,
            String[] classPath,
            String JDKPath,
            boolean analyzeJDK) throws Exception {

        System.out.println("algorithm=" + algorithm + ", inputDirPath=" + inputDirPath + ", output=" + output
                + ", mainClass=" + mainClass + ", classPath=" + java.util.Arrays.toString(classPath) + ", JDKPath="
                + JDKPath + ", analyzeJDK=" + analyzeJDK);

        long start = System.nanoTime();

        String runnerDir = System.getenv("TAIE_RUNNER_DIR");
        if (runnerDir == null) {
            throw new IllegalStateException("TAIE_RUNNER_DIR env variable not set");
        }

        // Generate callgraph
        long processed = 0;
        File inputFile = new File(inputDirPath); // inputDirPath is the single .apk or .jar file that we want to
                                                 // generate the CG for
        String testCaseName = readTestCaseName(inputFile);
        Path cgDir = Paths.get(runnerDir, "output-cgs", algorithm, testCaseName); // where to write the intermediate
                                                                                  // results from Taie before reading
                                                                                  // and parsing them
        processed += generateCGforFile(
                inputFile,
                algorithm,
                runnerDir,
                cgDir,
                mainClass,
                classPath,
                JDKPath,
                analyzeJDK);
        System.out.printf("------ Wrote %d callgraphs ------\n", processed);

        // Read reachable methods
        Path reachableMethodsPath = cgDir.resolve("reachable-methods.txt");
        Set<Method> allMethods = Files.readAllLines(reachableMethodsPath).stream()
                .map(this::parseMethodSignature)
                .collect(Collectors.toSet());

        // Parse call-graph.dot
        Map<String, String> nodeMap = parseDotNodes(cgDir.resolve("call-graph.dot"));
        Map<Method, Map<CallSiteKey, Set<Method>>> callSitesMap = parseDotEdges(cgDir.resolve("call-graph.dot"),
                nodeMap);

        // Build ReachableMethods structure
        Set<ReachableMethod> reachableMethods = new HashSet<>();
        for (Method method : allMethods) {
            Set<CallSite> sites = new HashSet<>();
            if (callSitesMap.containsKey(method)) {
                for (var entry : callSitesMap.get(method).entrySet()) {
                    sites.add(new CallSite(
                            entry.getKey().declaredTarget,
                            entry.getKey().line,
                            null, // pc not required in final format
                            entry.getValue()));
                }
            }
            reachableMethods.add(new ReachableMethod(method, sites));
        }

        // Serialize the ReachableMethods object to JSON
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        output.write(gson.toJson(new ReachableMethods(reachableMethods)));
        output.flush();

        return System.nanoTime() - start;
    }

    private String readTestCaseName(File inputFile) {
        String name = inputFile.getName();
        if (name.endsWith(".jar") || name.endsWith(".apk")) {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }

    private long generateCGforFile(
            File inputFile,
            String algorithm,
            String runnerDir,
            Path outDir,
            String mainClass,
            String[] classPath,
            String jdkPath,
            boolean analyzeJdk) throws Exception {

        // Create output directory for this file
        String testCaseName = readTestCaseName(inputFile);
        outDir = outDir.toAbsolutePath();
        Files.createDirectories(outDir);

        // Validate runnerDir contains Tai-e JAR
        Path jarPath = Paths.get(runnerDir, "tai-e-all-0.5.1.jar");
        if (!Files.exists(jarPath)) {
            throw new RuntimeException("Tai-e JAR not found at: " + jarPath);
        }

        // Generate configuration file from template
        String algoTaieName = null;
        switch (algorithm.toUpperCase()) {
            case "CHA":
                algoTaieName = "cha";
                break;
            case "PTA":
                algoTaieName = "pta";
                break;
            default:
                throw new RuntimeException("Invalid algorithm: " + algorithm);
        }

        // Execute analysis process
        List<String> command = new ArrayList<>(Arrays.asList(
                "java",
                "-jar", "tai-e-all-0.5.1.jar",
                "--class-path", inputFile.getAbsolutePath(),
                "--main-class", mainClass,
                "-java", "8",
                "-scope", "ALL",
                "-a", "cg=algorithm:" + algoTaieName + ";dump:true;dump-methods:true",
                "--output-dir", outDir.toString()));
        for (String cp : classPath) {
            command.add("--class-path");
            command.add(cp);
        }

        // path to <...>/JCG/jcg_annotations/src/main/java
        // otherwise we get:
        // java.lang.RuntimeException: couldn't find class:
        // lib.annotations.callgraph.IndirectCalls
        String jcgPath = System.getenv("JCG_ANNOTATIONS_PATH");
        if (jcgPath == null || jcgPath.isEmpty()) {
            throw new RuntimeException("JCG_ANNOTATIONS_PATH env variable not set");
        }
        command.add("--class-path");
        command.add(jcgPath);

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(runnerDir));
        pb.redirectErrorStream(true);

        // debug
        System.out.println("Executing command: " + String.join(" ", pb.command()));

        Process process = pb.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(System.out::println);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Analysis failed with exit code: " + exitCode);
        }

        System.out.printf("------ Finished generating CG for input file: %s ------\n", testCaseName);
        System.out.printf("------ Files written: ------\n");
        Files.list(outDir)
                .filter(path -> (path.toString().endsWith(".dot")))
                .forEach(e -> System.out.println(e.toString()));

        // Count generated callgraph files
        return Files.list(outDir)
                .filter(path -> (path.toString().endsWith(".dot")))
                .count();
    }

    // ---- Helper methods for converting Tai-e output format to JCG format ----

    private static class CallSiteKey {
        final int line;
        final Method declaredTarget;
        final String sourceStatement;

        CallSiteKey(int line, Method declaredTarget, String sourceStatement) {
            this.line = line;
            this.declaredTarget = declaredTarget;
            this.sourceStatement = sourceStatement;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            CallSiteKey that = (CallSiteKey) o;
            return line == that.line
                    && Objects.equals(declaredTarget, that.declaredTarget)
                    && Objects.equals(sourceStatement, that.sourceStatement);
        }

        @Override
        public int hashCode() {
            return Objects.hash(line, declaredTarget, sourceStatement);
        }
    }

    /**
     * Convert method signature from Tai-e format to the JVM format used by JCG
     * 
     * @param sig Tai-e method signature as string (e.g. `<cfne.Demo: void
     *            main(java.lang.String[])>`)
     * @return Method object similar to other JCG adapters
     */
    private Method parseMethodSignature(String sig) {
        // Remove angle brackets
        sig = sig.substring(1, sig.length() - 1);

        // Split into class and method parts
        int colonIdx = sig.indexOf(':');
        String className = sig.substring(0, colonIdx).trim();
        String methodPart = sig.substring(colonIdx + 1).trim();

        // Extract return type
        int lastSpace = methodPart.lastIndexOf(' ');
        String returnType = methodPart.substring(0, lastSpace).trim();
        String rest = methodPart.substring(lastSpace + 1).trim();

        // Extract method name and parameters
        int parenIdx = rest.indexOf('(');
        String methodName = rest.substring(0, parenIdx).trim();
        String paramsStr = rest.substring(parenIdx + 1, rest.length() - 1).trim();

        // Parse parameter types
        List<String> paramTypes = new ArrayList<>();
        if (!paramsStr.isEmpty()) {
            for (String param : paramsStr.split(",")) {
                paramTypes.add(toJVMType(param.trim()));
            }
        }

        return new Method(
                methodName,
                toJVMType(className),
                toJVMType(returnType),
                paramTypes);
    }

    /**
     * Convert Type string (e.g. `java.lang.String[]`) to NVM internal format used
     * by JCG (e.g. `[Ljava.lang.String;`)
     * Also used for class names (e.g. `cfne.Demo` becomes `Lcfne/Demo;`)
     * 
     * @param javaType
     * @return
     */
    private String toJVMType(String javaType) {
        int dims = 0;
        while (javaType.endsWith("[]")) {
            dims++;
            javaType = javaType.substring(0, javaType.length() - 2);
        }

        String base;
        switch (javaType) {
            case "byte":
                base = "B";
                break;
            case "char":
                base = "C";
                break;
            case "double":
                base = "D";
                break;
            case "float":
                base = "F";
                break;
            case "int":
                base = "I";
                break;
            case "long":
                base = "J";
                break;
            case "short":
                base = "S";
                break;
            case "boolean":
                base = "Z";
                break;
            case "void":
                base = "V";
                break;
            default:
                base = "L" + javaType.replace('.', '/') + ";";
        }

        return "[".repeat(dims) + base;
    }

    private Map<String, String> parseDotNodes(Path dotPath) throws IOException {
        Map<String, String> nodeMap = new HashMap<>();
        Pattern nodePattern = Pattern.compile("\"(\\d+)\" \\[label=\"(<[^>]+>)\"");

        for (String line : Files.readAllLines(dotPath)) {
            Matcher m = nodePattern.matcher(line);
            if (m.find())
                nodeMap.put(m.group(1), m.group(2));
        }
        return nodeMap;
    }

    private Map<Method, Map<CallSiteKey, Set<Method>>> parseDotEdges(
            Path dotPath, Map<String, String> nodeMap) throws IOException {

        // example edge from DOT file:
        // "3" -> "20786" [label="[0@L228] $r1 = invokevirtual
        // %this.<java.util.stream.FindOps$FindSink$OfDouble: java.util.OptionalDouble
        // get()>();",];

        Map<Method, Map<CallSiteKey, Set<Method>>> result = new HashMap<>();
        Pattern edgePattern = Pattern.compile(
                "\\s*\"(\\d+)\"\\s*->\\s*\"(\\d+)\"\\s*\\[label=\\\"\\[(\\d+)@L(-?\\d+)\\].*?<([^>]*)>.*?\\\"");
        System.out.println("=== START PARSING DOT EDGES ===");
        System.out.println("Node map size: " + nodeMap.size());
        int total = 0, matched = 0, skipped = 0;

        for (String line : Files.readAllLines(dotPath)) {
            if (!line.contains("->"))
                continue;

            total++;

            Matcher m = edgePattern.matcher(line);
            if (!m.find()) {
                skipped++;
                System.out.println("SKIPPED: " + line);
                continue;
            }

            matched++;
            String srcId = m.group(1);
            String tgtId = m.group(2);
            int pc = Integer.parseInt(m.group(3));
            int lineNum = Integer.parseInt(m.group(4));
            String declaredSig = "<" + m.group(5) + ">";

            // System.out.printf("MATCHED: src=%s, tgt=%s, pc=%d, line=%d, sig=%s%n",
            // srcId, tgtId, pc, lineNum, declaredSig);

            if (!nodeMap.containsKey(srcId) || !nodeMap.containsKey(tgtId))
                continue;

            Method caller = parseMethodSignature(nodeMap.get(srcId));
            Method declared = parseMethodSignature(declaredSig);
            Method target = parseMethodSignature(nodeMap.get(tgtId));

            CallSiteKey key = new CallSiteKey(lineNum, declared, line);
            result
                    .computeIfAbsent(caller, k -> new HashMap<>())
                    .computeIfAbsent(key, k -> new HashSet<>())
                    .add(target);
        }
        System.out.printf("DOT STATS: total=%d, matched=%d, skipped=%d%n", total, matched, skipped);
        return result;
    }
}
