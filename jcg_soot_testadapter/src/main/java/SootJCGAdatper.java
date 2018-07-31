import de.tud.cs.peaks.sootconfig.*;
import options.CGOptionsWithReflection;
import options.CHAOptions;
import options.RTAOptions;
import options.VTAOptions;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.backend.ASMBackendUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

public class SootJCGAdatper implements JCGTestAdapter {

    private static final String CHA = "CHA";
    private static final String RTA = "RTA";
    private static final String VTA = "VTA";
    private static final String Spark = "SPARK";
    private static final String LibCHA = "LibCHA";
    private static final String LibSpark = "LibSpark";


    @Override
    public String[] possibleAlgorithms() {
        return new String[]{CHA, RTA, VTA, Spark, LibCHA, LibSpark};
    }

    @Override
    public String frameworkName() {
        return "Soot";
    }

    @Override
    public void serializeCG(String algorithm, String target, String[] classPath, String outputFile) {
        FluentOptions options = new FluentOptions();
        options.wholeProgramAnalysis();
        options.keepLineNumbers();
        options.allowPhantomReferences();
        options.noBodiesForExcluded();

        CGOptionsWithReflection cgOptions = new CGOptionsWithReflection();
        options.addPhaseOptions(cgOptions);

        cgOptions.handleForNameSafe().handleNewInstanceSafe().useTypesForInvoke();

        CallGraphPhaseSubOptions cgModeOption = null;
        switch (algorithm) {
            case CHA:
                cgModeOption = new CHAOptions().enable();
                break;
            case RTA:
                cgModeOption = new RTAOptions().enableRTA();
                break;
            case VTA:
                cgModeOption = new VTAOptions().enableVTA();
                break;
            case Spark:
                cgModeOption = new SparkOptions().enable();
                break;
            case LibCHA:
                cgModeOption = new CHAOptions().enable();
                cgOptions.processAllReachable();
                cgOptions.libraryModeSignatureResolution();
                break;
            case LibSpark:
                cgModeOption = new SparkOptions().enable();
                cgOptions.processAllReachable();
                cgOptions.libraryModeSignatureResolution();
                break;
        }
        cgOptions.addSubOption(cgModeOption);


        AnalysisTarget analysisTarget = new AnalysisTarget();
        String cp = Arrays.stream(classPath).collect(Collectors.joining(File.pathSeparator));
        analysisTarget.classPath(cp);
        analysisTarget.processPath(target);

        SootRun run = new SootRun(options, analysisTarget);
        SootResult result = run.perform();

        Scene scene = result.getScene();
        CallGraph cg = scene.getCallGraph();

        JSONObject callSitesObject = new JSONObject();
        JSONArray callSites = new JSONArray();

        // all application classes
        for (SootClass clazz : scene.getClasses()) {
            // all methods defined in that class
            for (SootMethod method : clazz.getMethods()) {
                if (method.hasActiveBody()) {
                    // all stmts in that method that contains invokations
                    for (Unit u : method.getActiveBody().getUnits()) {
                        Stmt stmt = (Stmt) u;
                        if (stmt.containsInvokeExpr()) {
                            Iterator<Edge> edges = cg.edgesOutOf(stmt);

                            JSONObject callSite = new JSONObject();

                            callSite.put("declaredTarget", createMethodObject(stmt.getInvokeExpr().getMethod()));
                            callSite.put("line", stmt.getJavaSourceStartLineNumber());
                            callSite.put("method", createMethodObject(method));

                            JSONArray callTargets = new JSONArray();
                            while (edges.hasNext()) {
                                Edge edge = edges.next();
                                SootMethod tgt = edge.tgt();
                                callTargets.add(createMethodObject(tgt));
                            }

                            callSite.put("targets", callTargets);

                            callSites.add(callSite);
                        }
                    }
                }
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

    public static void main(String[] args) {
        String cgAlgorithm = args[0];
        String targetJar = args[1];
        String outputPath = args[2];
        String[] cp = Arrays.copyOfRange(args, 3, args.length);

        new SootJCGAdatper().serializeCG(cgAlgorithm, targetJar, cp, outputPath);
    }

    private static JSONObject createMethodObject(SootMethod method) {
        JSONObject jMethod = new JSONObject();
        jMethod.put("name", method.getName());
        jMethod.put("declaringClass", ASMBackendUtils.toTypeDesc(method.getDeclaringClass().getType()));
        jMethod.put("returnType", ASMBackendUtils.toTypeDesc(method.getReturnType()));

        JSONArray params = new JSONArray();
        for (Type param : method.getParameterTypes()) {
            params.add(ASMBackendUtils.toTypeDesc(param));
        }

        jMethod.put("parameterTypes", params);
        return jMethod;
    }
}
