import java.io.File
import java.io.Writer
import scala.collection.JavaConverters._
import scala.collection.mutable
import play.api.libs.json.Json

import qilin.driver.PTAFactory
import qilin.driver.PTAPattern
import qilin.pta.PTAConfig
import sootup.callgraph.CallGraph
import sootup.callgraph.CallGraphAlgorithm
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm
import sootup.callgraph.RapidTypeAnalysisAlgorithm
import sootup.core.inputlocation.AnalysisInputLocation
import sootup.core.signatures.MethodSignature
import sootup.core.types.ArrayType
import sootup.core.types.ClassType
import sootup.core.types.PrimitiveType.BooleanType
import sootup.core.types.PrimitiveType.ByteType
import sootup.core.types.PrimitiveType.CharType
import sootup.core.types.PrimitiveType.DoubleType
import sootup.core.types.PrimitiveType.FloatType
import sootup.core.types.PrimitiveType.IntType
import sootup.core.types.PrimitiveType.LongType
import sootup.core.types.PrimitiveType.ShortType
import sootup.core.types.Type
import sootup.core.types.VoidType
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation
import sootup.java.core.views.JavaView

object SootUpJCGAdapter extends JavaTestAdapter {

    private val CHA = "CHA"
    private val RTA = "RTA"
    private val CFA0 = "0-CFA"
    private val CFA1 = "1-CFA"

    val possibleAlgorithms: Array[String] = Array(CHA, RTA, CFA0, CFA1)

    val frameworkName: String = "SootUp"
    def serializeCG(
        algorithm:      String,
        inputDirPath:   String,
        output:         Writer,
        adapterOptions: AdapterOptions
    ): Long = {
        val mainClass = adapterOptions.getString("mainClass")
        val classPath = adapterOptions.getStringArray("classPath")
        val JDKPath = adapterOptions.getString("JDKPath")
        val analyzeJDK = adapterOptions.getBoolean("analyzeJDK")

        val classPathString = if(classPath.isEmpty) "" else classPath.mkString(File.pathSeparator, File.pathSeparator, "")

        val cp =
            if(analyzeJDK || !Seq(CHA, RTA).contains(algorithm)){
                val jreJars = JRELocation.getAllJREJars(JDKPath).map(_.getCanonicalPath)
                val jreJarString = if(jreJars.isEmpty) "" else jreJars.mkString(File.pathSeparator, File.pathSeparator, "")
                inputDirPath + classPathString + jreJarString
            } else {
                inputDirPath + classPathString
            }

        val inputLocations: List[AnalysisInputLocation] = List(new JavaClassPathAnalysisInputLocation(cp))

        val view = new JavaView(inputLocations.asJava)

        // todo no-bodies-for-excluded in case of !analyzeJDK

        val before = System.nanoTime

        def computeCG(cgAlgorithm: CallGraphAlgorithm): (CallGraph, Iterable[MethodSignature]) = {
            val cg =
                if (mainClass == null) {
                cgAlgorithm.initialize()
            } else {
                val idFactory = view.getIdentifierFactory
                val mainClassType = idFactory.getClassType(mainClass)
                val stringArrayType = idFactory.getType("java.lang.String[]")
                val mainMethod = idFactory.getMethodSignature(mainClassType, "main", VoidType.getInstance(), List(stringArrayType).asJava)
                cgAlgorithm.initialize(List(mainMethod).asJava)
            }
            (cg, cg.getEntryMethods.asScala)
        }

        val (cg: CallGraph, entrypoints: Iterable[MethodSignature]) =
            if (algorithm.contains(CHA)) {
                computeCG(new ClassHierarchyAnalysisAlgorithm(view))
        } else if (algorithm.contains(RTA)) {
                computeCG(new RapidTypeAnalysisAlgorithm(view))
        } else {
            val ptaPattern = if(algorithm.contains(CFA0))
                new PTAPattern("insens") // "2o"=>2OBJ, "1c"=>1CFA, etc.
            else if (algorithm.contains(CFA1))
                new PTAPattern("1c") // "2o"=>2OBJ, "1c"=>1CFA, etc.
            else {
                throw new IllegalArgumentException(s"unknown algorithm $algorithm")
            }
            PTAConfig.v()
            val pta = PTAFactory.createPTA(ptaPattern, view, mainClass)
            pta.run()
            (pta.getCallGraph(), pta.getNakedReachableMethods.asScala.map(_.getSignature))
        }

        val after = System.nanoTime

        val worklist = mutable.Queue(entrypoints.toSeq: _*)
        val processed = mutable.Set(worklist.toSeq: _*)

        var reachableMethods = Set.empty[ReachableMethod]

        while (worklist.nonEmpty) {
            val currentMethod = worklist.dequeue()

            var callSitesMap = Map.empty[(MethodSignature, Int), Set[MethodSignature]]
            for (edge ← cg.callsFrom(currentMethod).asScala) {
                val stmt = edge.getInvokableStmt

                // e.g. null for finalize and no invoke for static initializers
                val declaredMethod = if (stmt != null && stmt.containsInvokeExpr()) {
                    stmt.getInvokeExpr.get().getMethodSignature
                } else
                    edge.getTargetMethodSignature

                val lineNumber =
                    if (stmt != null)
                        stmt.getPositionInfo.getStmtPosition.getFirstLine
                    else
                        -1

                val tgt = edge.getTargetMethodSignature
                val key =
                    if (declaredMethod.getName == tgt.getName)
                        declaredMethod → lineNumber
                    else
                        tgt → lineNumber

                val tgts = callSitesMap.getOrElse(key, Set.empty)
                callSitesMap = callSitesMap.updated(key, tgts + tgt)
                if (!processed.contains(tgt)) {
                    worklist += tgt
                    processed += tgt
                }
            }

            val callSites = callSitesMap.map {
                case ((declaredTgt, line), tgts) ⇒
                    // todo: would be good to have the PC
                    CallSite(createMethodObject(declaredTgt), line, None, tgts.map(createMethodObject))
            }.toSet

            val method = createMethodObject(currentMethod)
            reachableMethods += ReachableMethod(method, callSites)
        }

        output.write(Json.prettyPrint(Json.toJson(ReachableMethods(reachableMethods))))

        after - before
    }

    private def createMethodObject(method: MethodSignature): Method = {
        val name = method.getName
        val declaringClass = javaToJVMType(method.getDeclClassType)
        val returnType = javaToJVMType(method.getType)
        val paramTypes = method.getParameterTypes.asScala.map(t => javaToJVMType(t)).toList

        Method(name, declaringClass, returnType, paramTypes)
    }

    private def javaToJVMType(javaType: Type): String = {
        javaType match {
            case t: ClassType => "L" + t.getFullyQualifiedName.replace('.', '/') + ";"
            case t: ArrayType => "[" * t.getDimension + javaToJVMType(t.getBaseType)
            case _: ByteType => "B"
            case _: CharType => "C"
            case _: DoubleType => "D"
            case _: FloatType => "F"
            case _: IntType => "I"
            case _: LongType => "J"
            case _: ShortType => "S"
            case _: BooleanType => "Z"
            case _: VoidType => "V"
            case _   => throw new IllegalArgumentException(s"Unknow type $javaType")
        }
    }
}
