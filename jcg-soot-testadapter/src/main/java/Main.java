import de.tud.cs.peaks.sootconfig.*;
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

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public class Main {

    private static final String CHA = "CHA";
    private static final String RTA = "RTA";
    private static final String VTA = "VTA";

    public static void main(String[] args) {
        String cp = args[0];
        String targetJar = args[1];
        String cgAlgorithm = args[2];


        FluentOptions options = new FluentOptions();
        options.wholeProgramAnalysis();
        options.keepLineNumbers();
        options.allowPhantomReferences();
        options.prependClasspath(); //TODO

        CallGraphPhaseOptions cgOptions = new CallGraphPhaseOptions();
        cgOptions.processAllReachable();//cgOptions.processOnlyEntryPoints();
        cgOptions.libraryMode(); //TODO

        CallGraphPhaseSubOptions cgModeOption = null;
        switch (cgAlgorithm) {
            case CHA:
                cgModeOption = new CHAOptions().enable();
                break;
            case RTA:
                cgModeOption = new RTAOptions().enableRTA();
                break;
            case VTA:
                cgModeOption = new VTAOptions().enableVTA();
                break;
        }
        cgOptions.addSubOption(cgModeOption);

        AnalysisTarget analysisTarget = new AnalysisTarget();
        analysisTarget.classPath(cp);
        analysisTarget.processPath(targetJar);

        SootRun run = new SootRun(options, analysisTarget);
        SootResult result = run.perform();

        Scene scene = result.getScene();
        CallGraph cg = scene.getCallGraph();

        JSONObject callSitesObject = new JSONObject();
        JSONArray callSites = new JSONArray();

        // all application classes
        for (SootClass clazz : scene.getApplicationClasses()) {
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
                                callTargets.add(ASMBackendUtils.slashify(tgt.getDeclaringClass().toString()));

                                //callTargets.add(createMethodObject(tgt));
                            }

                            callSite.put("targets", callTargets);

                            callSites.add(callSite);
                        }
                    }
                }
            }
        }
        callSitesObject.put("callSites", callSites);


        try (FileWriter file = new FileWriter("test.json")) {
            file.write(callSitesObject.toJSONString());
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
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
