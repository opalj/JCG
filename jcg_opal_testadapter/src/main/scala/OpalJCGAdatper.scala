import java.io.File
import java.io.FileWriter
import java.net.URL

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import org.opalj.br.DeclaredMethod
import org.opalj.br.Code
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.Project.JavaClassFileReader
import org.opalj.br.instructions.MethodInvocationInstruction
import org.opalj.fpcf.PropertyStoreKey
import org.opalj.fpcf.FPCFAnalysesManagerKey
import org.opalj.fpcf.FinalEP
import org.opalj.fpcf.analyses.cg.EagerRTACallGraphAnalysisScheduler
import org.opalj.fpcf.analyses.cg.EagerSerializationRelatedCallsAnalysis
import org.opalj.fpcf.analyses.cg.EagerThreadRelatedCallsAnalysis
import org.opalj.fpcf.analyses.cg.EagerLoadedClassesAnalysis
import org.opalj.fpcf.analyses.cg.EagerFinalizerAnalysisScheduler
import org.opalj.fpcf.analyses.cg.LazyCalleesAnalysis
import org.opalj.fpcf.properties.ThreadRelatedCallees
import org.opalj.fpcf.properties.StandardInvokeCallees
import org.opalj.fpcf.properties.Callees
import org.opalj.fpcf.properties.SerializationRelatedCallees
import org.opalj.fpcf.properties.NoCallees
import org.opalj.fpcf.properties.NoCalleesDueToNotReachableMethod
import play.api.libs.json.Json
import scala.collection.JavaConverters._

class OpalJCGAdatper extends JCGTestAdapter {

    def possibleAlgorithms(): Array[String] = Array[String]("RTA")

    def frameworkName(): String = "OPAL"

    def serializeCG(
        algorithm:  String,
        target:     String,
        mainClass:  String,
        classPath:  Array[String],
        outputFile: String
    ): Unit = {
        val baseConfig: Config = ConfigFactory.load()

        implicit val config: Config =
            if (mainClass eq null) {
                baseConfig.withValue(
                    "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
                    ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryEntryPointsFinder")
                )
            } else {
                baseConfig.withValue(
                    "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
                    ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.ConfigurationEntryPointsFinder")
                ).withValue(
                    "org.opalj.br.analyses.cg.InitialEntryPointsKey.entryPoints",
                    ConfigValueFactory.fromIterable(Seq(ConfigValueFactory.fromMap(Map(
                        "declaringClass" -> mainClass.replace('.', '/'), "name" -> "main"
                    ).asJava)).asJava)
                )
            }

        val targetClassFiles = JavaClassFileReader().ClassFiles(new File(target)).toIterator
        val cpClassFiles = JavaClassFileReader().AllClassFiles(classPath.map(new File(_))).toIterator
        val allClassFiles = targetClassFiles ++ cpClassFiles
        val project: Project[URL] = Project(allClassFiles.toTraversable, Seq.empty, true, Seq.empty)

        val ps = project.get(PropertyStoreKey)

        val manager = project.get(FPCFAnalysesManagerKey)
        manager.runAll(
            EagerRTACallGraphAnalysisScheduler,
            EagerLoadedClassesAnalysis,
            EagerFinalizerAnalysisScheduler,
            EagerThreadRelatedCallsAnalysis,
            EagerSerializationRelatedCallsAnalysis,
            new LazyCalleesAnalysis(
                Set(StandardInvokeCallees, ThreadRelatedCallees, SerializationRelatedCallees)
            )
        )

        implicit val declaredMethods = project.get(DeclaredMethodsKey)
        for (dm ← declaredMethods.declaredMethods) {
            ps.force(dm, Callees.key)
        }

        ps.waitOnPhaseCompletion()

        var callSites: Set[CallSite] = Set.empty

        for (dm ← declaredMethods.declaredMethods) {
            ps(dm, Callees.key) match {
                case FinalEP(_, NoCallees | NoCalleesDueToNotReachableMethod) ⇒ // TODO the callsite is needed anyway!
                case FinalEP(_, cs: Callees) ⇒
                    val body = dm.definedMethod.body.get
                    for ((pc, callees) ← cs.callees) {
                        callSites ++= createCallSites(dm, body, pc, callees)
                    }
            }
        }

        ps.shutdown()

        val file: FileWriter = new FileWriter(outputFile)
        file.write(Json.prettyPrint(Json.toJson(CallSites(callSites))))
        file.flush()
        file.close()
    }

    private def createCallSites(
        caller:  DeclaredMethod,
        body:    Code,
        pc:      Int,
        callees: Set[DeclaredMethod]
    ): Seq[CallSite] = {
        val declaredO = body.instructions(pc) match {
            case MethodInvocationInstruction(dc, _, name, desc) ⇒ Some(dc, name, desc)
            case _                                              ⇒ None
        }

        val line = body.lineNumber(pc).getOrElse(-1)

        if (declaredO.isDefined) {
            val (dc, name, desc) = declaredO.get
            val declaredTarget =
                Method(
                    name,
                    dc.toJVMTypeName,
                    desc.returnType.toJVMTypeName,
                    desc.parameterTypes.iterator.map(_.toJVMTypeName).toList
                )

            val (directCallees, indirectCallees) = callees.partition { callee =>
                callee.name == name && // TODO check descriptor correctly for refinement
                    callee.descriptor.parametersCount == desc.parametersCount
            }

            indirectCallees.iterator.map(createIndividualCallSite(_, caller, line)).toSeq :+
                CallSite(
                    declaredTarget,
                    line,
                    createMethodObject(caller),
                    directCallees.iterator.map(createMethodObject).toSet
                )
        } else {
            callees.iterator.map(createIndividualCallSite(_, caller, line)).toSeq
        }
    }

    def createIndividualCallSite(
        method: DeclaredMethod,
        caller: DeclaredMethod,
        line:   Int
    ): CallSite = {
        CallSite(
            createMethodObject(method),
            line,
            createMethodObject(caller),
            Set(createMethodObject(method))
        )
    }

    private def createMethodObject(method: DeclaredMethod): Method = {
        Method(
            method.name,
            method.declaringClassType.toJVMTypeName,
            method.descriptor.returnType.toJVMTypeName,
            method.descriptor.parameterTypes.iterator.map(_.toJVMTypeName).toList
        )
    }
}
