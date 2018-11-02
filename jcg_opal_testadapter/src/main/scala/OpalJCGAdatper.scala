import java.io.File
import java.io.FileWriter
import java.net.URL

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import org.opalj.br.Code
import org.opalj.br.DeclaredMethod
import org.opalj.br.analyses.DeclaredMethods
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.Project.JavaClassFileReader
import org.opalj.br.instructions.MethodInvocationInstruction
import org.opalj.fpcf.FPCFAnalysesManagerKey
import org.opalj.fpcf.FinalEP
import org.opalj.fpcf.PropertyStore
import org.opalj.fpcf.PropertyStoreKey
import org.opalj.fpcf.analyses.SystemPropertiesAnalysis
import org.opalj.fpcf.analyses.cg.EagerFinalizerAnalysisScheduler
import org.opalj.fpcf.analyses.cg.EagerLoadedClassesAnalysis
import org.opalj.fpcf.analyses.cg.EagerRTACallGraphAnalysisScheduler
import org.opalj.fpcf.analyses.cg.EagerReflectionRelatedCallsAnalysis
import org.opalj.fpcf.analyses.cg.EagerSerializationRelatedCallsAnalysis
import org.opalj.fpcf.analyses.cg.EagerThreadRelatedCallsAnalysis
import org.opalj.fpcf.analyses.cg.LazyCalleesAnalysis
import org.opalj.fpcf.cg.properties.Callees
import org.opalj.fpcf.cg.properties.NoCallees
import org.opalj.fpcf.cg.properties.NoCalleesDueToNotReachableMethod
import org.opalj.fpcf.cg.properties.ReflectionRelatedCallees
import org.opalj.fpcf.cg.properties.SerializationRelatedCallees
import org.opalj.fpcf.cg.properties.StandardInvokeCallees
import org.opalj.tac.fpcf.analyses.LazyL0TACAIAnalysis
import play.api.libs.json.Json

import scala.collection.JavaConverters._

/**
 * A [[JCGTestAdapter]] for the FPCF based call graph analyses of OPAL.
 *
 * @author Dominik Helm
 * @author Florian Kuebler
 */
object OpalJCGAdatper extends JCGTestAdapter {

    def possibleAlgorithms(): Array[String] = Array[String]("RTA")

    def frameworkName(): String = "OPAL"

    def serializeCG(
        algorithm:  String,
        target:     String,
        mainClass:  String,
        classPath:  Array[String],
        JDKPath:    String,
        analyzeJDK: Boolean,
        outputFile: String
    ): Long = {
        val before = System.nanoTime()
        val baseConfig: Config = ConfigFactory.load().withValue(
            "org.opalj.br.reader.ClassFileReader.Invokedynamic.rewrite",
            ConfigValueFactory.fromAnyRef(true)
        )

        // configure the initial entry points
        implicit val config: Config =
            if (mainClass eq null) {
                baseConfig.withValue(
                    "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
                    ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryEntryPointsFinder")
                ).withValue(
                        "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
                        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.LibraryInstantiatedTypesFinder")
                    )
            } else {
                baseConfig.withValue(
                    "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
                    ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.ConfigurationEntryPointsFinder")
                ).withValue(
                        "org.opalj.br.analyses.cg.InitialEntryPointsKey.entryPoints",
                        ConfigValueFactory.fromIterable(Seq(ConfigValueFactory.fromMap(Map(
                            "declaringClass" → mainClass.replace('.', '/'), "name" → "main"
                        ).asJava)).asJava)
                    ).withValue(
                            "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
                            ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.ApplicationInstantiatedTypesFinder")
                        )
            }

        // gather the class files to be loaded
        val cfReader = JavaClassFileReader(theConfig = config)
        val targetClassFiles = cfReader.ClassFiles(new File(target))
        val cpClassFiles = cfReader.AllClassFiles(classPath.map(new File(_)))
        val jreJars = JRELocation.getAllJREJars(JDKPath)
        val jre = cfReader.AllClassFiles(jreJars)
        val allClassFiles = targetClassFiles ++ cpClassFiles ++ (if (analyzeJDK) jre else Seq.empty)

        val libClassFiles =
            if (analyzeJDK)
                Seq.empty
            else
                Project.JavaLibraryClassFileReader.AllClassFiles(jreJars)

        val project: Project[URL] = Project(
            allClassFiles,
            libClassFiles,
            libraryClassFilesAreInterfacesOnly = true,
            Seq.empty
        )

        implicit val ps: PropertyStore = project.get(PropertyStoreKey)

        // run RTA call graph, along with extra analyses e.g. for reflection
        val manager = project.get(FPCFAnalysesManagerKey)
        manager.runAll(
            EagerRTACallGraphAnalysisScheduler,
            EagerLoadedClassesAnalysis,
            EagerFinalizerAnalysisScheduler,
            EagerThreadRelatedCallsAnalysis,
            EagerSerializationRelatedCallsAnalysis,
            EagerReflectionRelatedCallsAnalysis,
            SystemPropertiesAnalysis,
            LazyL0TACAIAnalysis,
            new LazyCalleesAnalysis(
                Set(
                    StandardInvokeCallees,
                    SerializationRelatedCallees,
                    ReflectionRelatedCallees
                )
            )
        )

        // start the computation of the call graph
        implicit val declaredMethods: DeclaredMethods = project.get(DeclaredMethodsKey)
        for (dm ← declaredMethods.declaredMethods) {
            ps.force(dm, Callees.key)
        }

        ps.waitOnPhaseCompletion()

        val after = System.nanoTime()

        var reachableMethods = Set.empty[ReachableMethod]

        for (
            dm ← declaredMethods.declaredMethods if (!dm.hasSingleDefinedMethod && !dm.hasMultipleDefinedMethods) ||
                (dm.hasSingleDefinedMethod && dm.definedMethod.classFile.thisType == dm.declaringClassType)
        ) {
            val m = createMethodObject(dm)
            ps(dm, Callees.key) match {
                case FinalEP(_, NoCalleesDueToNotReachableMethod) ⇒
                case FinalEP(_, NoCallees) ⇒
                    reachableMethods += ReachableMethod(m, Set.empty)
                case FinalEP(_, cs: Callees) ⇒
                    val body = dm.definedMethod.body
                    val callSites = cs.callSites().flatMap {
                        case (pc, callees) ⇒
                            createCallSites(body, pc, callees)
                    }.toSet
                    reachableMethods += ReachableMethod(m, callSites)
            }
        }

        ps.shutdown()

        val file: FileWriter = new FileWriter(outputFile)
        file.write(Json.prettyPrint(Json.toJson(ReachableMethods(reachableMethods))))
        file.flush()
        file.close()

        after - before
    }

    private def createCallSites(
        bodyO:   Option[Code],
        pc:      Int,
        callees: Iterator[DeclaredMethod]
    ): Seq[CallSite] = bodyO match {
        case None ⇒ callees.map(createIndividualCallSite(_, -1)).toSeq
        case Some(body) ⇒
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
                        desc.parameterTypes.map[String](_.toJVMTypeName).toList
                    )

                val (directCallees, indirectCallees) = callees.partition { callee ⇒
                    callee.name == name && // TODO check descriptor correctly for refinement
                        callee.descriptor.parametersCount == desc.parametersCount
                }

                indirectCallees.map(createIndividualCallSite(_, line)).toSeq :+
                    CallSite(
                        declaredTarget,
                        line,
                        directCallees.map(createMethodObject).toSet
                    )
            } else {
                callees.map(createIndividualCallSite(_, line)).toSeq
            }
    }

    def createIndividualCallSite(
        method: DeclaredMethod,
        line:   Int
    ): CallSite = {
        CallSite(
            createMethodObject(method),
            line,
            Set(createMethodObject(method))
        )
    }

    private def createMethodObject(method: DeclaredMethod): Method = {
        Method(
            method.name,
            method.declaringClassType.toJVMTypeName,
            method.descriptor.returnType.toJVMTypeName,
            method.descriptor.parameterTypes.map[String](_.toJVMTypeName).toList
        )
    }
}
