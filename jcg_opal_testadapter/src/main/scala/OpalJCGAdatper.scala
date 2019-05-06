import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.net.URL

import scala.collection.JavaConverters._

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory

import org.opalj.fpcf.FinalEP
import org.opalj.fpcf.PropertyStore
import org.opalj.br.DeclaredMethod
import org.opalj.br.analyses.DeclaredMethods
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.Project.JavaClassFileReader
import org.opalj.br.fpcf.FPCFAnalysesManagerKey
import org.opalj.br.fpcf.PropertyStoreKey
import org.opalj.br.fpcf.cg.properties.Callees
import org.opalj.br.fpcf.cg.properties.NoCallees
import org.opalj.br.fpcf.cg.properties.NoCalleesDueToNotReachableMethod
import org.opalj.br.instructions.MethodInvocationInstruction
import org.opalj.br.ObjectType
import org.opalj.ai.domain.l1.DefaultDomainWithCFGAndDefUse
import org.opalj.ai.fpcf.properties.AIDomainFactoryKey
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
            LazyTACAIProvider
        )

        // start the computation of the call graph
        implicit val declaredMethods: DeclaredMethods = project.get(DeclaredMethodsKey)
        for (dm ← declaredMethods.declaredMethods) {
            ps.force(dm, Callees.key)
        }

        ps.waitOnPhaseCompletion()

        val after = System.nanoTime()

        val out = new BufferedWriter(new FileWriter(outputFile))
        out.write(s"""{"reachableMethods":[""")
        var firstRM = true
        for {
            dm ← declaredMethods.declaredMethods if (!dm.hasSingleDefinedMethod && !dm.hasMultipleDefinedMethods) ||
                (dm.hasSingleDefinedMethod && dm.definedMethod.classFile.thisType == dm.declaringClassType)
            calleeEOptP = ps(dm, Callees.key)
            if calleeEOptP.ub ne NoCalleesDueToNotReachableMethod
        } {
            if (firstRM) {
                firstRM = false
            } else {
                out.write(",")
            }
            out.write("{\"method\":")
            writeMethodObject(dm, out)
            out.write(",\"callSites\":[")
            calleeEOptP match {
                case FinalEP(_, NoCallees) ⇒
                case FinalEP(_, callees: Callees) ⇒
                    writeCallSites(dm, callees, out)
            }
            out.write("]}")
        }
        out.write("]}")
        out.flush()
        out.close()

        ps.shutdown()

        after - before
    }

    private def writeCallSites(
        method:  DeclaredMethod,
        callees: Callees,
        out:     Writer
    )(implicit ps: PropertyStore, declaredMethods: DeclaredMethods): Unit = {
        val bodyO = if (method.hasSingleDefinedMethod) method.definedMethod.body else None
        var first = true
        for ((pc, targets) ← callees.callSites()) {
            bodyO match {
                case None ⇒
                    for (tgt ← targets) {
                        if (first) first = false
                        else out.write(",")
                        writeCallSite(tgt, -1, pc, Iterator(tgt), out)
                    }

                case Some(body) ⇒
                    val declaredTgtO = body.instructions(pc) match {
                        case MethodInvocationInstruction(dc, _, name, desc) ⇒ Some(dc, name, desc)
                        case _                                              ⇒ None
                    }

                    val line = body.lineNumber(pc).getOrElse(-1)

                    if (declaredTgtO.isDefined) {
                        val (dc, name, desc) = declaredTgtO.get
                        val declaredType =
                            if (dc.isArrayType)
                                ObjectType.Object
                            else
                                dc.asObjectType

                        val declaredTarget = declaredMethods(
                            declaredType, declaredType.packageName, declaredType, name, desc
                        )

                        val (directCallees, indirectCallees) = targets.partition { callee ⇒
                            callee.name == name && // TODO check descriptor correctly for refinement
                                callee.descriptor.parametersCount == desc.parametersCount
                        }

                        for (tgt ← indirectCallees) {
                            if (first) first = false
                            else out.write(",")
                            writeCallSite(tgt, line, pc, Iterator(tgt), out)
                        }
                        if (directCallees.nonEmpty) {
                            if (first) first = false
                            else out.write(",")
                            writeCallSite(declaredTarget, line, pc, directCallees, out)
                        }

                    } else {
                        for (tgt ← targets) {
                            if (first) first = false
                            else out.write(",")
                            writeCallSite(tgt, line, pc, Iterator(tgt), out)
                        }
                    }
            }
        }
    }

    private def writeCallSite(
        declaredTarget: DeclaredMethod,
        line:           Int,
        pc:             Int,
        targets:        Iterator[DeclaredMethod],
        out:            Writer
    ): Unit = {
        out.write("{\"declaredTarget\":")
        writeMethodObject(declaredTarget, out)
        out.write(",\"line\":")
        out.write(line.toString)
        out.write(",\"pc\":")
        out.write(pc.toString)
        out.write(",\"targets\":[")
        var first = true
        for (tgt ← targets) {
            if (first) first = false
            else out.write(",")
            writeMethodObject(tgt, out)
        }
        out.write("]}")
    }

    private def writeMethodObject(
        method: DeclaredMethod,
        out:    Writer
    ): Unit = {
        out.write("{\"name\":\"")
        out.write(method.name)
        out.write("\",\"declaringClass\":\"")
        out.write(method.declaringClassType.toJVMTypeName)
        out.write("\",\"returnType\":\"")
        out.write(method.descriptor.returnType.toJVMTypeName)
        out.write("\",\"parameterTypes\":[")
        if (method.descriptor.parametersCount > 0)
            out.write(method.descriptor.parameterTypes.iterator.map[String](_.toJVMTypeName).mkString("\"", "\",\"", "\""))
        out.write("]}")
    }
}
