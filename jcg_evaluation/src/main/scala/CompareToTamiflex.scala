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
        }

        var tf : Method => Boolean = _ => true
        if(typeFilter.nonEmpty){
            tf = m => m.declaringClass == typeFilter
        }

        val reachableMethods = parseCallGraph(callGraphFile).keySet.filter(tf)
        val tamiflexResults = parseTamiflexResults(tamiflexOutput).filter(tf)

        if(printMethods){
            reachableMethods.mkString("ReachableMethods: \n\n","\n", "\n\n")
        }

        var i = 0
        tamiflexResults.foreach { method =>
            if(!reachableMethods.contains(method)){
                println(s"Unreachable: ${method.toString}")
            } else {
                i = i + 1;
            }
        }

        println(s"\n\n$i of ${tamiflexResults.size} all methods are reachable")

    }

    private def parseCallGraph(callGraphFile: String) = {
        Json.parse(new FileInputStream(callGraphFile)).validate[ReachableMethods].get.toMap
    }

    def extractMethod(targetInfo: String, reflectionType: String) : Method = {
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
            val declCls = parts(0).trim
            val methodInfo = parts(1).trim.replaceAll("[(]|[)]", " ").trim
            val methodParts = methodInfo.split(" ")
            val returnType = convertTypeToJVMNotation(methodParts(0))
            val methodName = methodParts(1)
            val methodArgs = methodParts.takeRight(methodParts.length - 2).map(convertTypeToJVMNotation(_)).toList
            Method(methodName, declCls, returnType, methodArgs)
        }
    }

    private def convertTypeToJVMNotation(typeString: String) : String = {
        typeString match {
            case "void" => "V"
            case "int" => "I"
            case "boolean" => "Z"
            case "long" => "J"
            case "double" => "D"
            case "float" => "F"
            case t => {
                val prefix = if(t.endsWith("]")) "[L" else "L"
                s"$prefix${typeString.replace('.', '/')};"
            }
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
        Method("<init>", s"L${declType.replace('.', '/')};", "V", List.empty[String])
    }

    def createStaticInitializer(declType: String) : Method = {
        Method("<clinit>", s"L${declType.replace('.', '/')};", "V", List.empty[String])
    }
}
//case class Method(name: String, declaringClass: String, returnType: String, parameterTypes: List[String]) {
//
//    override def toString: String = {
//        s"$declaringClass { $returnType $name(${parameterTypes.mkString(", ")})}"
//    }
//}

/*
Class.newInstance;org.apache.xalan.templates.ElemApplyTemplates;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemAttribute;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemCallTemplate;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemChoose;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemCopyOf;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemElement;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemForEach;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemIf;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemLiteralResult;org.apache.xalan.processor.ProcessorLRE.startElement;254;;
Class.newInstance;org.apache.xalan.templates.ElemMessage;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemOtherwise;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemParam;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemSort;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemTemplate;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemText;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemValueOf;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemVariable;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemWhen;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
Class.newInstance;org.apache.xalan.templates.ElemWithParam;org.apache.xalan.processor.ProcessorTemplateElem.startElement;63;;
 */