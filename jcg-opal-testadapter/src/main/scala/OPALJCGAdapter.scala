import java.io.File
import java.io.PrintWriter

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import org.opalj.br.Method
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.cg.CallGraphKey
import org.opalj.br.instructions.MethodInvocationInstruction
import org.opalj.log.OPALLogger
import org.opalj.log.StandardLogContext
import play.api.libs.json.JsArray
import play.api.libs.json.Json

object OPALJCGAdapter {
    def main(args: Array[String]): Unit = {
        val tgtPath = args(1)
        val cp = args(2)

        val baseConfig: Config = ConfigFactory.load()
        val configKey = "org.opalj.br.analyses.cg.CallGraphKey.factory"

        val config = args(0) match {
            case "CHA" ⇒
                val chaString = "org.opalj.br.analyses.cg.CHACallGraphFactory"
                baseConfig.withValue(configKey, ConfigValueFactory.fromAnyRef(chaString))
            case _ ⇒ ???
        }

        val logContext = new StandardLogContext
        OPALLogger.register(logContext)

        val p = Project(Array(new File(tgtPath)), Array(new File(cp)), logContext, config)
        val cg = p.get(CallGraphKey)

        val callSites = for {
            cf ← p.allProjectClassFiles
            (m, code) ← cf.methodsWithBody
            (pc, tgts) ← cg.calls(m)
        } yield Json.obj(
            "line" → code.lineNumber(pc),
            "method" → createMethodObject(m),
            "declaredTarget" → createMethodObject(code.instructions(pc).asMethodInvocationInstruction),
            "targets" → tgts.map(_.classFile.thisType.fqn)
        )
        val json = Json.obj("callSites" → new JsArray(callSites))

        println(Json.prettyPrint(json))

        val pw = new PrintWriter(new File(args(3)))
        pw.write(json.toString())
        pw.close

    }

    private def createMethodObject(method: Method) = {
        Json.obj(
            "name" → method.name,
            "declaringClass" → method.classFile.thisType.toJVMTypeName,
            "returnType" → method.returnType.toJVMTypeName,
            "parameterTypes" → method.parameterTypes.map(_.toJVMTypeName)

        )
    }

    private def createMethodObject(instr: MethodInvocationInstruction) = {
        Json.obj(
            "name" → instr.name,
            "declaringClass" → instr.declaringClass.toJVMTypeName,
            "returnType" → instr.methodDescriptor.returnType.toJVMTypeName,
            "parameterTypes" → instr.methodDescriptor.parameterTypes.map(_.toJVMTypeName)
        )
    }
}
