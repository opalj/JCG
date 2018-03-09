import com.ibm.wala.analysis.reflection.ReflectionContextInterpreter;
import com.ibm.wala.analysis.reflection.ReflectionContextSelector;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.examples.drivers.PDFTypeHierarchy;
import com.ibm.wala.examples.properties.WalaExamplesProperties;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.properties.WalaProperties;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.NullProgressMonitor;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.viz.DotUtil;
import com.ibm.wala.viz.PDFViewUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Properties;

public class WalaJCGAdapter {

    public static void main(String[] args) throws IOException, CancelException, WalaException {
        String cgAlgorithm = args[0];
        String testfile = args[1];
        String outputPath = args[2];

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        File ex = new File(cl.getResource("exclusions.txt").getFile());
        AnalysisScope scope = AnalysisScopeReader.makeJavaBinaryAnalysisScope(testfile, ex);

        IClassHierarchy classHierarchy = ClassHierarchyFactory.make(scope);
        //Iterable<Entrypoint> entrypoints = new AllSubtypesOfApplicationEntrypoints(scope, classHierarchy);
        //Iterable<Entrypoint> entrypoints = new AllApplicationEntrypoints(scope, classHierarchy);
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, classHierarchy);
        AnalysisOptions options = new AnalysisOptions(scope, entrypoints);
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);


        CallGraph callGraph = null;
        AnalysisCache cache = new AnalysisCacheImpl();

        if (cgAlgorithm.equals("0-CFA")) {
            SSAPropagationCallGraphBuilder ncfaBuilder = Util.makeZeroCFABuilder(options, cache, classHierarchy, scope);
            callGraph = ncfaBuilder.makeCallGraph(options);
        } else if (cgAlgorithm.equals("0-1-CFA")) {
            SSAPropagationCallGraphBuilder cfaBuilder = Util.makeZeroOneCFABuilder(options, cache, classHierarchy, scope);
            callGraph = cfaBuilder.makeCallGraph(options);
        } else if (cgAlgorithm.equals("1-CFA")) {
            // TODO
            SSAPropagationCallGraphBuilder cfaBuilder = Util.makeNCFABuilder(1, options, cache, classHierarchy, scope);
            callGraph = cfaBuilder.makeCallGraph(options);
        } else if (cgAlgorithm.equals("RTA")) {
            //TODO
            CallGraphBuilder<?> rtaBuilder = Util.makeRTABuilder(options, cache, classHierarchy, scope);
            callGraph = rtaBuilder.makeCallGraph(options, new NullProgressMonitor());
        }

        JSONArray callSites = new JSONArray();
        JSONObject callSitesObject = new JSONObject();

        for (IClass clazz : classHierarchy) {
            //String pck = clazz.getName().getPackage().toString();
            if (clazz.getClassLoader().getName().toString().equals("Primordial"))
                continue;
            for (IMethod method : clazz.getDeclaredMethods()) {

                Iterator<CallSiteReference> callSiteIter;
                CGNode cgNode = null;
                if (cgAlgorithm.equals("CHA")) {
                    IR ir = cache.getIR(method);
                    if (ir == null)
                        continue;
                    callSiteIter = ir.iterateCallSites();
                } else {
                    cgNode = callGraph.getNode(method, Everywhere.EVERYWHERE);

                    if (cgNode == null)
                        continue;

                    callSiteIter = cgNode.iterateCallSites();
                }

                while (callSiteIter.hasNext()) {

                    CallSiteReference csr = callSiteIter.next();


                    JSONObject callSite = new JSONObject();
                    callSite.put("declaredTarget", createMethodObject(csr.getDeclaredTarget()));
                    callSite.put("line", method.getLineNumber(csr.getProgramCounter()));
                    callSite.put("method", createMethodObject(method.getReference()));

                    JSONArray callTargets = new JSONArray();

                    if (cgAlgorithm.equals("CHA")) {
                        for (IMethod tgt : classHierarchy.getPossibleTargets(clazz, csr.getDeclaredTarget())) {
                            callTargets.add(tgt.getDeclaringClass().getName().toString().substring(1));
                        }
                    } else {
                        for (CGNode tgt : callGraph.getPossibleTargets(cgNode, csr)) {
                            callTargets.add(tgt.getMethod().getDeclaringClass().getName().toString().substring(1));


                            // handle reflective calls that occur one stage later
                            String tgtM = tgt.getMethod().getName().toString();
                            String tgtC = tgt.getMethod().getDeclaringClass().getName().toString();
                            if ((tgtC.equals("Ljava/lang/Class") && tgtM.equals("newInstance")) ||
                                    (tgtC.equals("Ljava/lang/reflect/Method") && tgtM.equals("invoke"))) {
                                Iterator<CallSiteReference> csi = tgt.iterateCallSites();
                                while (csi.hasNext()) {
                                    CallSiteReference reflectiveCsR = csi.next();

                                    JSONObject reflectiveCallSite = new JSONObject();
                                    reflectiveCallSite.put("declaredTarget", createMethodObject(reflectiveCsR.getDeclaredTarget()));
                                    reflectiveCallSite.put("line", method.getLineNumber(csr.getProgramCounter())); // use line of invoke/newInstance
                                    reflectiveCallSite.put("method", createMethodObject(method.getReference()));


                                    JSONArray reflectiveCallTargets = new JSONArray();
                                    for (CGNode reflectiveTgt : callGraph.getPossibleTargets(tgt, reflectiveCsR)) {
                                        reflectiveCallTargets.add(reflectiveTgt.getMethod().getDeclaringClass().getName().toString().substring(1));
                                    }

                                    reflectiveCallSite.put("targets", reflectiveCallTargets);
                                    callSites.add(reflectiveCallSite);
                                }
                            }
                        }
                    }

                    callSite.put("targets", callTargets);

                    callSites.add(callSite);

                }

                //}
            }
        }


        callSitesObject.put("callSites", callSites);

        try (FileWriter file = new FileWriter(outputPath)) {
            file.write(callSitesObject.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Properties p = null;
        try {
            p = WalaExamplesProperties.loadProperties();
            p.putAll(WalaProperties.loadProperties());
        } catch (WalaException e) {
            e.printStackTrace();
            Assertions.UNREACHABLE();
        }
        String pdfFile = "cg.pdf";

        String dotExe = p.getProperty(WalaExamplesProperties.DOT_EXE);
        DotUtil.dotify(callGraph, null, PDFTypeHierarchy.DOT_FILE, pdfFile, dotExe);
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
