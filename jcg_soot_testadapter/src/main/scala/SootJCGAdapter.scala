import scala.collection.JavaConverters._
import java.io.File
import java.io.FileWriter

import play.api.libs.json.Json
import soot.G
import soot.Main
import soot.Scene
import soot.SootMethod
import soot.util.backend.ASMBackendUtils

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object SootJCGAdapter extends JCGTestAdapter {

    private val CHA = "CHA"
    private val RTA = "RTA"
    private val VTA = "VTA"
    private val Spark = "SPARK"

    override def possibleAlgorithms(): Array[String] = Array(CHA /*, RTA, VTA, Spark*/ )

    override val frameworkName: String = "Soot"

    override def serializeCG(algorithm: String, target: String, mainClass: String, classPath: Array[String], outputFile: String): Long = {
        val options = new ArrayBuffer[String](40)
        options += "-whole-program"
        options += "-keep-line-number"
        options += "-allow-phantom-refs"
        options += "-include-all"
        options += "-no-writeout-body-releasing" //todo we do not want this option here

        addPhaseOptions(options, "cg", Array("safe-forname:true", "safe-newinstance:true", "types-for-invoke:true"))

        addPhaseOptions(options, "jb", Array("enabled:true", "use-original-names:true"))

        if (mainClass == null) addPhaseOptions(options, "cg", Array("library:signature-resolution", "all-reachable:true"))
        else {
            options += "-main-class"
            options += mainClass
        }

        options += "-process-dir"
        options += target

        options += "-cp"
        options += classPath.mkString(File.pathSeparator)

        options += "-output-format"
        options += "n"

        if (algorithm.contains(CHA)) {
            addPhaseOptions(options, "cg.cha", Array[String]("enabled:true"))
        } else if (algorithm.contains(RTA)) {
            addPhaseOptions(options, "cg.spark", Array[String]("enabled:true", "rta:true", "simulate-natives:true", "on-fly-cg:false"))
        } else if (algorithm.contains(VTA)) {
            addPhaseOptions(options, "cg.spark", Array[String]("enabled:true", "vta:true", "simulate-natives:true"))
        } else if (algorithm.contains(Spark)) {
            addPhaseOptions(options, "cg.spark", Array[String]("enabled:true", "simulate-natives:true"))
        } else {
            throw new IllegalArgumentException(s"unknown algorithm $algorithm")
        }

        val before = System.nanoTime
        Main.main(options.toArray)
        val after = System.nanoTime

        val scene = Scene.v()
        val cg = scene.getCallGraph

        val worklist = mutable.Queue(scene.getEntryPoints.asScala: _*)
        val processed = mutable.Set(worklist: _*)

        var reachableMethods = Set.empty[ReachableMethod]

        while (worklist.nonEmpty) {
            val currentMethod = worklist.dequeue()

            var callSitesMap = Map.empty[(SootMethod, Int), Set[SootMethod]]
            for (edge ← cg.edgesOutOf(currentMethod).asScala) {
                val stmt = edge.srcStmt()

                // e.g. null for finalize and no invoke for static initializers
                val declaredMethod = if (stmt != null && stmt.containsInvokeExpr())
                    stmt.getInvokeExpr.getMethod
                else
                    edge.tgt()

                val lineNumber =
                    if (stmt != null)
                        stmt.getJavaSourceStartLineNumber
                    else
                        -1

                val tgt = edge.tgt
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
                    CallSite(createMethodObject(declaredTgt), line, tgts.map(createMethodObject))
            }.toSet

            val method = createMethodObject(currentMethod)
            reachableMethods += ReachableMethod(method, callSites)
        }

        val file: FileWriter = new FileWriter(outputFile)
        file.write(Json.prettyPrint(Json.toJson(ReachableMethods(reachableMethods))))
        file.flush()
        file.close()

        G.reset()

        after - before
    }

    private def addPhaseOptions(options: ArrayBuffer[String], phase: String, phaseOptions: Array[String]): Unit = {
        options += "-p"
        options += phase
        options += phaseOptions.mkString(",")
    }

    def main(args: Array[String]): Unit = {
        val cgAlgorithm: String = args(0)
        val targetJar: String = args(1)
        val mainClass: String = args(2)
        val outputPath: String = args(3)
        val cp: Array[String] = args.slice(4, args.length)
        serializeCG(cgAlgorithm, targetJar, mainClass, cp, outputPath)
    }

    private def createMethodObject(method: SootMethod): Method = {
        val name = method.getName
        val declaringClass = ASMBackendUtils.toTypeDesc(method.getDeclaringClass.getType)
        val returnType = ASMBackendUtils.toTypeDesc(method.getReturnType)
        val paramTypes = method.getParameterTypes.asScala.map(ASMBackendUtils.toTypeDesc).toList

        Method(name, declaringClass, returnType, paramTypes)
    }
}
