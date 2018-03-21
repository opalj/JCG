import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.SubtypesEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;

import java.util.HashSet;

public class AllSubtypesOfApplicationEntrypoints extends HashSet<Entrypoint> {

    public AllSubtypesOfApplicationEntrypoints(AnalysisScope scope, final IClassHierarchy cha) {

        if (cha == null) {
            throw new IllegalArgumentException("cha is null");
        }
        for (IClass klass : cha) {
            if (!klass.isInterface()) {
                if (isApplicationClass(scope, klass)) {
                    for (IMethod method : klass.getDeclaredMethods()) {
                        if (!method.isAbstract()) {
                            add(new SubtypesEntrypoint(method, cha));
                        }
                    }
                }
            }
        }

    }

    private static boolean isApplicationClass(AnalysisScope scope, IClass klass) {
        return scope.getApplicationLoader().equals(klass.getClassLoader().getReference());
    }
}
