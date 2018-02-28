import java.io.File
import java.io.PrintWriter

import org.opalj.br.Method
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.cg.CallGraphKey
import org.opalj.br.instructions.MethodInvocationInstruction
import play.api.libs.json.JsArray
import play.api.libs.json.Json
object OPALJCGAdapter {
    def main(args: Array[String]): Unit = {
        val tgtPath = args(1)
        val p = Project(new File(tgtPath))
        val cg = p.get(CallGraphKey)

        val callSites = for {
            m ← p.allMethodsWithBody
            body = m.body.get
            (pc, tgts) ← cg.calls(m)
        } yield Json.obj(
            "line" → body.lineNumber(pc),
            "method" → createMethodObject(m),
            "declaredTarget" → createMethodObject(body.instructions(pc).asMethodInvocationInstruction),
            "targets" → tgts.map(_.classFile.thisType.fqn)
        )
        val json = Json.obj("callSites" → new JsArray(callSites))

        val pw = new PrintWriter(new File(args(2)))
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
