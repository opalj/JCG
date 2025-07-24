import com.ibm.wala.ipa.callgraph.AnalysisCache
import com.ibm.wala.ipa.callgraph.AnalysisOptions
import com.ibm.wala.ipa.callgraph.AnalysisScope
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.ipa.callgraph.Entrypoint
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.util.NullProgressMonitor
import edu.rit.se.design.callgraph.analysis.PointerAnalysisPolicy
import edu.rit.se.design.callgraph.analysis.seneca.SenecaNCFACallGraphBuilder
import edu.rit.se.design.callgraph.analysis.seneca.SenecaZeroXCallGraphBuilder
import edu.rit.se.design.callgraph.dispatcher.SerializationDispatcher

object SenecaJCGAdapter extends WalaBasedJCGAdapter {

    val frameworkName: String = "Seneca"

    val possibleAlgorithms: Array[String] = Array("0-CFA", "1-CFA")

    def computeCG(algorithm: String, scope: AnalysisScope, classHierarchy: ClassHierarchy, cache: AnalysisCache, options: AnalysisOptions, entrypoints: java.lang.Iterable[Entrypoint]): CallGraph = {
        val secondaryPolicy: PointerAnalysisPolicy = new PointerAnalysisPolicy(PointerAnalysisPolicy.PolicyType.nCFA, 1);

        val dispatcher = new SerializationDispatcher(classHierarchy);

        val cgBuilder = if (algorithm.contains("0-CFA"))
            SenecaZeroXCallGraphBuilder.make(scope, options, cache, classHierarchy, 1, secondaryPolicy, dispatcher)
        else if (algorithm.contains("1-CFA"))
            SenecaNCFACallGraphBuilder.make(scope, options, cache, classHierarchy, 1, secondaryPolicy, dispatcher)
        else
            throw new IllegalArgumentException(s"unknown algorithm $algorithm")

        cgBuilder.makeCallGraph(options, new NullProgressMonitor)
    }

}
