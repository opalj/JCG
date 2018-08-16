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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class SootJCGAdatper implements JCGTestAdapter {

    private static final String CHA = "CHA";
    private static final String RTA = "RTA";
    private static final String VTA = "VTA";
    private static final String Spark = "SPARK";


    @Override
    public String[] possibleAlgorithms() {
        return new String[]{CHA/*, RTA, VTA, Spark*/};
    }

    @Override
    public String frameworkName() {
        return "Soot";
    }

    private void addPhaseOptions(List<String> options, String phase, String[] phaseOptions) {
        options.add("-p");
        options.add(phase);
        options.add(String.join(",", phaseOptions));
    }

    @Override
    public long serializeCG(
            String algorithm,
            String target,
            String mainClass,
            String[] classPath,
            String outputFile
    ) {
        List<String> options = new ArrayList<>(40);
        options.add("-whole-program");
        options.add("-keep-line-number");
        options.add("-allow-phantom-refs");
        options.add("-include-all");
        options.add("-no-writeout-body-releasing"); //todo we do not want this option here

        addPhaseOptions(options, "cg", new String[]{
                "safe-forname:true",
                "safe-newinstance:true",
                "types-for-invoke:true"}
        );

        addPhaseOptions(options, "jb", new String[]{
                "enabled:true", "use-original-names:true"});


        if (mainClass == null) {
            addPhaseOptions(options, "cg", new String[]{
                            "library:signature-resolution",
                            "all-reachable:true"
                    }
            );
        } else {
            options.add("-main-class");
            options.add(mainClass);
        }

        options.add("-process-dir");
        options.add(target);
        options.add("-cp");
        options.add(target + File.pathSeparator + String.join(File.pathSeparator, classPath));
        options.add("-output-format");
        options.add("n");

        switch (algorithm) {
            case CHA:
                addPhaseOptions(options, "cg.cha", new String[]{"enabled:true"});
                break;
            case RTA:
                addPhaseOptions(options, "cg.spark", new String[]{
                                "enabled:true",
                                "rta:true",
                                "simulate-natives:true",
                                "on-fly-cg:false"
                        }
                );
                break;
            case VTA:
                addPhaseOptions(options, "cg.spark", new String[]{
                                "enabled:true",
                                "vta:true",
                                "simulate-natives:true"
                        }
                );
                break;
            case Spark:
                addPhaseOptions(options, "cg.spark", new String[]{
                                "enabled:true",
                                "simulate-natives:true"
                        }
                );
                break;
        }

        long before = System.nanoTime();
        Main.main(options.toArray(new String[0]));
        long after = System.nanoTime();

        Scene scene = Scene.v();
        CallGraph cg = scene.getCallGraph();

        JSONObject callSitesObject = new JSONObject();
        JSONArray callSites = new JSONArray();

        for (SootClass clazz : scene.getClasses()) {
            // all methods defined in that class
            for (SootMethod method : clazz.getMethods()) {
                if (method.getName().equals("main"))
                    System.out.println(method.hasActiveBody());
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

        return after - before;
    }

    public static void main(String[] args) {
        String cgAlgorithm = args[0];
        String targetJar = args[1];
        String mainClass = args[2];
        String outputPath = args[3];

        String[] cp = Arrays.copyOfRange(args, 4, args.length);

        new SootJCGAdatper().serializeCG(cgAlgorithm, targetJar, mainClass, cp, outputPath);
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
