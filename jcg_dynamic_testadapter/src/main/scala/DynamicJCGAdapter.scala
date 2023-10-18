import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.Writer
import java.io.InputStreamReader
import java.net.ServerSocket
import java.nio.file.Paths
import java.util
import java.util.stream.Collectors

import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.Using

import org.opalj.br.MethodDescriptor

object DynamicJCGAdapter extends JCGTestAdapter {
    
    override def possibleAlgorithms(): Array[String] = Array("Dynamic")

    override def frameworkName(): String = "Dynamic"
    
    //TODO
    val port = 1337

    override def serializeCG(
        algorithm: String,
        target: String,
        mainClass: String,
        classPath: Array[String],
        JDKPath: String,
        analyzeJDK: Boolean,
        outputFile: String,
        jvmArgs: Array[String],
        programArgs: Array[String]
    ): Long = {

        val javaPath = Paths.get(JDKPath).getParent.toAbsolutePath + "/bin/java"
        //TODO
        //val agentPath = getClass.getClassLoader.getResource("DynamicCG.so").getPath
        val agentPath = "/home/JCG/JCG/jcg_dynamic_testadapter/src/main/resources/DynamicCG.so"
        val agentArgs = Array(port.toString).mkString(",")
        val cp = target + File.pathSeparator +
            util.Arrays.stream(classPath).collect(Collectors.joining(File.pathSeparator))

        val reachableMethods = mutable.Set[Method]()
        val edges = mutable.Map[Method, mutable.Map[(Int, Int), mutable.Set[Method]]]()

        var args = List(javaPath)
        args ++= jvmArgs
        args :+= s"-agentpath:$agentPath=$agentArgs"
        args ++= List("-cp", cp)
        args :+= mainClass
        args ++= programArgs

        val before = System.nanoTime
        
        val result = Using.Manager { use =>
            val serverSocket = use(new ServerSocket(port))

            new ProcessBuilder(args.asJava).inheritIO().start()

            val clientSocket = use(serverSocket.accept)
            serverSocket.close()

            val in = use(new BufferedReader(new InputStreamReader(clientSocket.getInputStream)))

            val endMessage = "End of Callgraph"
            var caller = in.readLine()

            while(caller != endMessage){
            	
            	val parsedCaller = if(caller == "TopLevel"){
			        None     	
            	} else{
            		Some(parseMethod(caller))
            	}

            	val pc = in.readLine().toInt
            	val lineNumber = in.readLine().toInt
            	val callee = in.readLine()
            	val pcLn = (pc, lineNumber)
            	val parsedCallee = parseMethod(callee)
            	
           	    reachableMethods += parsedCallee

                if (parsedCaller.isDefined) {
                    if (!edges.contains(parsedCaller.get)) {
                        edges += ((parsedCaller.get, mutable.Map[(Int, Int), mutable.Set[Method]]()))
                    }

                    val targets = edges(parsedCaller.get)

                    if (!targets.contains(pcLn)) {
                        targets += ((pcLn, mutable.Set[Method]()))
                    }

                    targets(pcLn) += parsedCallee
                }

                caller = in.readLine()	
            }

            System.nanoTime()
        }
        
        if(result.isFailure){
        	throw result.failed.get
        }
        
        val out = new BufferedWriter(new FileWriter(outputFile))
        var edgeCount = 0
        println(reachableMethods.size)
        out.write(s"""{"reachableMethods":[""")
        var firstRM = true
        for {
            rm ← reachableMethods
        } {
            if (firstRM) {
                firstRM = false
            } else {
                out.write(",")
            }
            out.write("{\"method\":")
            writeMethodObject(rm, out)
            out.write(",\"callSites\":[")
            if (edges.contains(rm)) {
                val callSites = edges(rm)
                edgeCount += callSites.values.foldLeft(0)((v, s) ⇒ v + s.size)
                writeCallSites(callSites, out)
            }
            out.write("]}")
        }
        println(edgeCount)
        out.write("]}")
        out.flush()
        out.close()
        
        val after = result.getOrElse(before)

        after - before
    }
    
    def parseMethod(param: String): Method = {
    	val calleeData = param.split(':')
           val calleeNameDesc = calleeData(1).split("\\(")
          val calleeDesc = MethodDescriptor("("+calleeNameDesc(1))
          Method(calleeNameDesc(0), calleeData(0), calleeDesc.returnType.toJVMTypeName, calleeDesc.parameterTypes.map(_.toJVMTypeName).toList)
    }
    
   
    private def writeCallSites(
        callSites: mutable.Map[(Int, Int), mutable.Set[Method]],
        out:       Writer
    ): Unit = {
        var firstCS = true
        for (callSite ← callSites) {
            if (firstCS) {
                firstCS = false
            } else {
                out.write(",")
            }
            writeCallSite(callSite._1, callSite._2, out)
        }
    }

    private def writeCallSite(
        pcLn:      (Int, Int),
        targets: mutable.Set[Method],
        out:     Writer
    ): Unit = {
        out.write("{\"declaredTarget\":")
        writeMethodObject(Method("", "", "", List.empty), out)
        out.write(",\"line\":")
        out.write(pcLn._2.toString)
        out.write(",\"pc\":")
        out.write(pcLn._1.toString)
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
        method: Method,
        out:    Writer
    ): Unit = {
        out.write("{\"name\":\"")
        out.write(method.name)
        out.write("\",\"declaringClass\":\"")
        out.write(method.declaringClass)
        out.write("\",\"returnType\":\"")
        out.write(method.returnType)
        out.write("\",\"parameterTypes\":[")
        if (method.parameterTypes.length > 0)
            out.write(method.parameterTypes.mkString("\"", "\",\"", "\""))
        out.write("]}")
    }
}

