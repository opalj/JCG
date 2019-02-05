import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL

import scala.collection.JavaConverters._
import scala.collection.mutable

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import play.api.libs.json.Json

import org.opalj.fpcf.FinalEP
import org.opalj.fpcf.PropertyStore
import org.opalj.br.Code
import org.opalj.br.DeclaredMethod
import org.opalj.br.analyses.DeclaredMethods
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.Project.JavaClassFileReader
import org.opalj.br.fpcf.cg.properties.ReflectionRelatedCallees
import org.opalj.br.fpcf.cg.properties.SerializationRelatedCallees
import org.opalj.br.fpcf.cg.properties.StandardInvokeCallees
import org.opalj.br.fpcf.cg.properties.ThreadRelatedIncompleteCallSites
import org.opalj.br.fpcf.FPCFAnalysesManagerKey
import org.opalj.br.fpcf.PropertyStoreKey
import org.opalj.br.fpcf.cg.properties.Callees
import org.opalj.br.fpcf.cg.properties.NoCallees
import org.opalj.br.fpcf.cg.properties.NoCalleesDueToNotReachableMethod
import org.opalj.br.instructions.MethodInvocationInstruction
import org.opalj.ai.domain.l1.DefaultDomainWithCFGAndDefUse
import org.opalj.ai.fpcf.properties.AIDomainFactoryKey
import org.opalj.tac.fpcf.analyses.cg.LazyCalleesAnalysis
import org.opalj.tac.fpcf.analyses.TriggeredSystemPropertiesAnalysis
import org.opalj.tac.fpcf.analyses.cg.reflection.TriggeredReflectionRelatedCallsAnalysis
import org.opalj.tac.fpcf.analyses.cg.TriggeredLoadedClassesAnalysis
import org.opalj.tac.fpcf.analyses.cg.TriggeredSerializationRelatedCallsAnalysis
import org.opalj.tac.fpcf.analyses.cg.TriggeredThreadRelatedCallsAnalysis
import org.opalj.tac.fpcf.analyses.LazyTACAIProvider
import org.opalj.tac.fpcf.analyses.cg.RTACallGraphAnalysisScheduler
import org.opalj.tac.fpcf.analyses.cg.TriggeredConfiguredNativeMethodsAnalysis
import org.opalj.tac.fpcf.analyses.cg.TriggeredFinalizerAnalysisScheduler
import org.opalj.tac.fpcf.analyses.cg.TriggeredInstantiatedTypesAnalysis
import org.opalj.tac.fpcf.analyses.cg.TriggeredStaticInitializerAnalysis

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
            } else baseConfig.withValue(
                "org.opalj.br.analyses.cg.InitialEntryPointsKey.analysis",
                ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.ConfigurationEntryPointsFinder")
            ).withValue(
                    "org.opalj.br.analyses.cg.InitialEntryPointsKey.entryPoints",
                    ConfigValueFactory.fromIterable(
                        Seq(
                            ConfigValueFactory.fromMap(Map(
                                "declaringClass" → mainClass.replace('.', '/'), "name" → "main"
                            ).asJava)
                        ).asJava
                    )
                ).withValue(
                        "org.opalj.br.analyses.cg.InitialInstantiatedTypesKey.analysis",
                        ConfigValueFactory.fromAnyRef("org.opalj.br.analyses.cg.ApplicationInstantiatedTypesFinder")
                    )

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

        project.updateProjectInformationKeyInitializationData(
            AIDomainFactoryKey,
            (i: Option[Set[Class[_ <: AnyRef]]]) ⇒ (i match {
                case None               ⇒ Set(classOf[DefaultDomainWithCFGAndDefUse[_]])
                case Some(requirements) ⇒ requirements + classOf[DefaultDomainWithCFGAndDefUse[_]]
            }): Set[Class[_ <: AnyRef]]
        )

        implicit val ps: PropertyStore = project.get(PropertyStoreKey)

        // run RTA call graph, along with extra analyses e.g. for reflection
        val manager = project.get(FPCFAnalysesManagerKey)
        manager.runAll(
            RTACallGraphAnalysisScheduler,
            TriggeredStaticInitializerAnalysis,
            TriggeredLoadedClassesAnalysis,
            TriggeredFinalizerAnalysisScheduler,
            TriggeredThreadRelatedCallsAnalysis,
            TriggeredSerializationRelatedCallsAnalysis,
            TriggeredReflectionRelatedCallsAnalysis,
            TriggeredInstantiatedTypesAnalysis,
            TriggeredConfiguredNativeMethodsAnalysis,
            TriggeredSystemPropertiesAnalysis,
            // LazyL0BaseAIAnalysis,
            // TACAITransformer,
            LazyTACAIProvider,
            LazyCalleesAnalysis(
                Set(
                    StandardInvokeCallees,
                    SerializationRelatedCallees,
                    ReflectionRelatedCallees,
                    ThreadRelatedIncompleteCallSites
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
        val methodCache = mutable.Map.empty[Method, Method]
        val stringCache = mutable.Map.empty[String, String]

        for (
            dm ← declaredMethods.declaredMethods if (!dm.hasSingleDefinedMethod && !dm.hasMultipleDefinedMethods) ||
                (dm.hasSingleDefinedMethod && dm.definedMethod.classFile.thisType == dm.declaringClassType)
        ) {
            val m = createMethodObject(dm, methodCache, stringCache)
            ps(dm, Callees.key) match {
                case FinalEP(_, NoCalleesDueToNotReachableMethod) ⇒
                case FinalEP(_, NoCallees) ⇒
                    reachableMethods += ReachableMethod(m, Set.empty)
                case FinalEP(_, cs: Callees) ⇒
                    val body = dm.definedMethod.body
                    val callSites = cs.callSites().flatMap {
                        case (pc, callees) ⇒
                            createCallSites(body, pc, callees, methodCache, stringCache)
                    }.toSet
                    reachableMethods += ReachableMethod(m, callSites)
            }
        }

        ps.shutdown()

        val file = new BufferedOutputStream(new FileOutputStream(outputFile))
        file.write(Json.toBytes(Json.toJson(ReachableMethods(reachableMethods))))
        file.flush()
        file.close()

        after - before
    }

    private def createCallSites(
        bodyO:       Option[Code],
        pc:          Int,
        callees:     Iterator[DeclaredMethod],
        methodCache: mutable.Map[Method, Method],
        stringCache: mutable.Map[String, String]
    ): Seq[CallSite] = bodyO match {
        case None ⇒ callees.map(createIndividualCallSite(_, -1, methodCache, stringCache)).toSeq
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

                indirectCallees.map(createIndividualCallSite(_, line, methodCache, stringCache)).toSeq :+
                    CallSite(
                        declaredTarget,
                        line,
                        directCallees.map(createMethodObject(_, methodCache, stringCache)).toSet
                    )
            } else {
                callees.map(createIndividualCallSite(_, line, methodCache, stringCache)).toSeq
            }
    }

    def createIndividualCallSite(
        method:      DeclaredMethod,
        line:        Int,
        methodCache: mutable.Map[Method, Method],
        stringCache: mutable.Map[String, String]
    ): CallSite = {
        CallSite(
            createMethodObject(method, methodCache, stringCache),
            line,
            Set(createMethodObject(method, methodCache, stringCache))
        )
    }

    private def createMethodObject(
        method:      DeclaredMethod,
        methodCache: mutable.Map[Method, Method],
        stringCache: mutable.Map[String, String]
    ): Method = {
        val m = Method(
            uniqueString(method.name, stringCache),
            uniqueString(method.declaringClassType.toJVMTypeName, stringCache),
            uniqueString(method.descriptor.returnType.toJVMTypeName, stringCache),
            method.descriptor.parameterTypes.map[String] {
                pt ⇒ uniqueString(pt.toJVMTypeName, stringCache)
            }.toList
        )
        if (methodCache.contains(m)) {
            methodCache(m)
        } else {
            methodCache += (m → m)
            m
        }
    }

    private def uniqueString(s: String, stringCache: mutable.Map[String, String]): String = {
        if (stringCache.contains(s))
            stringCache(s)
        else {
            stringCache += (s → s)
            s
        }
    }
}
