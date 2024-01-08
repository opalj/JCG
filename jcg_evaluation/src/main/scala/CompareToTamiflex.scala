import java.io.File
import java.io.FileInputStream

import play.api.libs.json.Json
/**
 *
 * @author Michael Reif
 */
object CompareToTamiflex {

    def main(args: Array[String]): Unit = {
        var callGraphFile = ""
        var tamiflexOutput = ""
        var typeFilter = ""
        var printMethods = false
        var debug = false

        args.sliding(2, 2).toList.collect {
            case Array("--callgraph", cg) ⇒
                assert(callGraphFile.isEmpty, "--callgraph is specified multiple times")
                callGraphFile = cg
            case Array("--tamiflex", cg) ⇒
                assert(tamiflexOutput.isEmpty, "--tamiflex is specified multiple times")
                tamiflexOutput = cg
            case Array("--typefilter", tf) ⇒
                typeFilter = tf
            case Array("--printmethods", pm) ⇒
                printMethods = pm == "t"
            case Array("--debug", d) ⇒
                debug = d == "t"
        }

        var tf : Method => Boolean = m => true
        if(typeFilter.nonEmpty){
            tf = m => {
                    val result = m.declaringClass == typeFilter
                    if(debug){
                        println(s"\t${m.declaringClass} ?==? $typeFilter [$result]")
                    }
                    result
                }
        }

        if(debug) {
            println("1. processing call graph")
        }

        val reachableMethods = parseCallGraph(callGraphFile).keySet.filter(tf)

        if(debug) {
            println("2. processing tamiflex results")
        }

        val tamiflexResults = parseTamiflexResults(tamiflexOutput).filter(tf)

        if(printMethods){
            println(reachableMethods.mkString("\tReachableMethods: \n\n","\n\t", "\n\n"))
        }

        if(debug) {
            println("3. Doing the comparision")
        }

        var i = 0
        tamiflexResults.foreach { method =>
            if(!reachableMethods.contains(method)){
                println(s"\tUnreachable: ${method.toString}")
            } else {
                println(s"\t\t\tReachable: ${method.toString}")
                i = i + 1;
            }
        }

        println(s"\n\n$i of ${tamiflexResults.size} all methods are reachable")
    }

    private def parseCallGraph(callGraphFile: String) = {
        EvaluationHelper.readCG(new File(callGraphFile)).toMap
    }

    def extractMethod(targetInfo: String, reflectionType: String) : Method = {
        import MethodFactory.convertTypeToJVMNotation

        val isDeclTypeOnly = targetInfo.charAt(0) != '<'
        if(isDeclTypeOnly){
            val isForName = reflectionType.equals("Class.forName")
            if(isForName)
                MethodFactory.createStaticInitializer(targetInfo)
            else
                MethodFactory.createDefaultInitializer(targetInfo)
        } else {
            // we have declType and method information
            //<org.apache.xpath.functions.FuncQname: void <init>()>
            val information = targetInfo.substring(1, targetInfo.length-1)
            val parts = information.split(":")
            val declCls = convertTypeToJVMNotation(parts(0).trim)
            val methodInfo = parts(1).trim.replaceAll("[(]|[)]", " ").trim
            val methodParts = methodInfo.split(" ")
            val returnType = convertTypeToJVMNotation(methodParts(0))
            val methodName = methodParts(1)

            @inline def extractArguments() : List[String] = {
                if(methodParts.size == 2) {
                    List.empty
                } else {
                    methodParts(2).split(",").map(convertTypeToJVMNotation(_)).toList
                }
            }
            val methodArgs = extractArguments()
            Method(methodName, declCls, returnType, methodArgs)
        }
    }

    private def parseTamiflexResults(tamiflexResult: String): List[Method] = {
        var tamiflexData : List[Method] = List.empty
        val bufferedSource = scala.io.Source.fromFile(tamiflexResult)
        for (line <- bufferedSource.getLines) {
            val cols = line.split(";").map(_.trim)
            val usedAPI = cols(0)
            val targetInfo = cols(1)
            val method = extractMethod(targetInfo, usedAPI)
            tamiflexData = tamiflexData :+ method
        }
        bufferedSource.close
        tamiflexData
    }

}

object MethodFactory {
//case class Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String])

    def createDefaultInitializer(declType: String) : Method = {
        Method("<init>", convertTypeToJVMNotation(declType), "V", List.empty[String])
    }

    def createStaticInitializer(declType: String) : Method = {
        Method("<clinit>", convertTypeToJVMNotation(declType), "V", List.empty[String])
    }

    def convertTypeToJVMNotation(typeString: String) : String = {
        typeString match {
            case "void" => "V"
            case "int" => "I"
            case "boolean" => "Z"
            case "long" => "J"
            case "double" => "D"
            case "float" => "F"
            case t if t.endsWith(("]")) => {
                val baseType = t.substring(0, t.length-2)
                s"[${convertTypeToJVMNotation(baseType)}"
            }
            case _ => s"L${typeString.replace('.', '/')};"

        }
    }
}
//case class Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String]) {
//
//    override def toString: String = {
//        s"$declaringClass { $returnType $name(${parameterTypes.mkString(", ")})}"
//    }
//}
