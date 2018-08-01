import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.config.AnalysisScopeReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

public class WalaJCGAdapter implements JCGTestAdapter {

    @Override
    public String[] possibleAlgorithms() {
        return new String[]{
                "RTA", "0-CFA", "1-CFA", "0-1-CFA", "Lib0-CFA", "Lib1-CFA", "Lib0-1-CFA"
        };
    }

    @Override
    public String frameworkName() {
        return "WALA";
    }

    public static void main(String[] args) throws Exception {
        String cgAlgorithm = args[0];
        String targetJar = args[1];
        String mainClass = args[2];
        String outputPath = args[3];
        String[] cp = Arrays.copyOfRange(args, 4, args.length);

        new WalaJCGAdapter().serializeCG(cgAlgorithm, targetJar,mainClass, cp, outputPath);
    }

    @Override
    public void serializeCG(String algorithm, String target, String mainClass, String[] classPath, String outputFile) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        String cp = Arrays.stream(classPath).collect(Collectors.joining(File.pathSeparator));
        cp = target + File.pathSeparator + cp;

        File ex = new File(cl.getResource("exclusions.txt").getFile());
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(cp, ex);

        IClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope);

        Iterable<Entrypoint> entrypoints;
        if (mainClass == null) {
            entrypoints = new AllSubtypesOfApplicationEntrypoints(scope, classHierarchy);
        } else {
            entrypoints = Util.makeMainEntrypoints(scope, classHierarchy, mainClass);
        }

        AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);


        CallGraph callGraph = null;
        AnalysisCache cache = new AnalysisCacheImpl();

        if (algorithm.contains("0-CFA")) {
            SSAPropagationCallGraphBuilder ncfaBuilder = Util.makeZeroCFABuilder(options, cache, classHierarchy, scope);
            callGraph = ncfaBuilder.makeCallGraph(options);
        } else if (algorithm.contains("0-1-CFA")) {
            SSAPropagationCallGraphBuilder cfaBuilder = Util.makeZeroOneCFABuilder(options, cache, classHierarchy, scope);
            callGraph = cfaBuilder.makeCallGraph(options);
        } else if (algorithm.contains("1-CFA")) {
            SSAPropagationCallGraphBuilder cfaBuilder = Util.makeNCFABuilder(1, options, cache, classHierarchy, scope);
            callGraph = cfaBuilder.makeCallGraph(options);
        } else if (algorithm.contains("RTA")) {
            CallGraphBuilder<?> rtaBuilder = Util.makeRTABuilder(options, cache, classHierarchy, scope);
            callGraph = rtaBuilder.makeCallGraph(options, new NullProgressMonitor());
        } else {
            throw new IllegalArgumentException();
        }

        JSONArray callSites = new JSONArray();
        JSONObject callSitesObject = new JSONObject();

        for (IClass clazz : classHierarchy) {
            //String pck = clazz.getName().getPackage().toString();
            /*if (clazz.getClassLoader().getName().toString().equals("Primordial"))
                continue;*/
            for (IMethod method : clazz.getDeclaredMethods()) {

                Iterator<CallSiteReference> callSiteIter;
                for (CGNode cgNode : callGraph.getNodes(method.getReference())) {

                    if (cgNode == null)
                        continue;

                    callSiteIter = cgNode.iterateCallSites();


                    while (callSiteIter.hasNext()) {

                        CallSiteReference csr = callSiteIter.next();


                        JSONObject callSite = new JSONObject();
                        callSite.put("declaredTarget", createMethodObject(csr.getDeclaredTarget()));
                        callSite.put("line", method.getLineNumber(csr.getProgramCounter()));
                        callSite.put("method", createMethodObject(method.getReference()));

                        JSONArray callTargets = new JSONArray();


                        for (CGNode tgt : callGraph.getPossibleTargets(cgNode, csr)) {
                            callTargets.add(createMethodObject(tgt.getMethod().getReference()));
                        }


                        callSite.put("targets", callTargets);

                        callSites.add(callSite);

                    }
                }
                //}
            }
        }


        callSitesObject.put("callSites", callSites);

        try (FileWriter file = new FileWriter(outputFile)) {
            file.write(callSitesObject.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject createMethodObject(MethodReference method) {
        JSONObject jMethod = new JSONObject();
        jMethod.put("name", method.getName().toString());
        jMethod.put("declaringClass", toJVMString(method.getDeclaringClass()));
        jMethod.put("returnType", toJVMString(method.getReturnType()));

        JSONArray params = new JSONArray();
        for (int i = 0; i < method.getNumberOfParameters(); i++) {
            params.add(toJVMString(method.getParameterType(i)));
        }
        jMethod.put("parameterTypes", params);
        return jMethod;
    }

    //TODO add ; in case of L - test this
    private static String toJVMString(TypeReference type) {
        if (type.isClassType() || (type.isArrayType() && type.getArrayElementType().isClassType())) {
            return type.getName().toString() + ";";
        } else {
            return type.getName().toString();
        }
    }
}
