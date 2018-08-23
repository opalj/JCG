import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileWriter
import java.io.PrintStream

import play.api.libs.json.Json
import soot.G
import soot.PackManager
import soot.Scene
import soot.SootMethod
import soot.options.Options
import soot.util.backend.ASMBackendUtils

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object SootJCGAdapter extends JCGTestAdapter {

    private val CHA = "CHA"
    private val RTA = "RTA"
    private val VTA = "VTA"
    private val Spark = "SPARK"

    override def possibleAlgorithms(): Array[String] = Array(/*CHA,*/ RTA/*, VTA, Spark */)

    override def frameworkName(): String = "Soot"

    override def serializeCG(algorithm: String, target: String, mainClass: String, classPath: Array[String], outputFile: String): Long = {

        val o = G.v().soot_options_Options()
        o.set_whole_program(true)
        o.set_keep_line_number(true)
        o.set_allow_phantom_refs(true)
        o.set_include_all(true)

        o.set_process_dir(List(target).asJava)
        o.set_soot_classpath(classPath.mkString(File.pathSeparator))
        o.set_output_format(Options.output_format_none)

        o.setPhaseOption("jb", "use-original-names:true")

        o.setPhaseOption("cg", "safe-forname:true")
        o.setPhaseOption("cg", "safe-newinstance:true")
        //o.setPhaseOption("cg", "types-for-invoke:true")

        if (mainClass == null) {
            o.setPhaseOption("cg", "library:signature-resolution")
            o.setPhaseOption("cg", "all-reachable:true")
        } else {
            o.set_main_class(mainClass)
        }

        if (algorithm.contains(CHA)) {
            o.setPhaseOption("cg.cha", "enabled:true")
        } else if (algorithm.contains(RTA)) {
            o.setPhaseOption("cg.spark", "enabled:true")
            o.setPhaseOption("cg.spark", "rta:true")
            o.setPhaseOption("cg.spark", "on-fly-cg:false")
            o.setPhaseOption("cg.spark", "simulate-natives:true")
        } else if (algorithm.contains(VTA)) {
            o.setPhaseOption("cg.spark", "enabled:true")
            o.setPhaseOption("cg.spark", "vta:true")
            o.setPhaseOption("cg.spark", "simulate-natives:true")
        } else if (algorithm.contains(Spark)) {
            o.setPhaseOption("cg.spark", "enabled:true")
            o.setPhaseOption("cg.spark", "simulate-natives:true")
        } else {
            throw new IllegalArgumentException(s"unknown algorithm $algorithm")
        }

        val out = new ByteArrayOutputStream()
        G.v.out = new PrintStream(out)

        val scene = Scene.v()
        val before = System.nanoTime
        scene.loadNecessaryClasses()
        // TODO SET ENTRYPOINTS?
        PackManager.v().runPacks()
        val after = System.nanoTime

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
