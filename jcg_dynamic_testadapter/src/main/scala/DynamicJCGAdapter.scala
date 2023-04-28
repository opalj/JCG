import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.nio.file.Paths
import java.util
import java.util.stream.Collectors

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object DynamicJCGAdapter extends JCGTestAdapter {

    override def possibleAlgorithms(): Array[String] = Array("Dynamic")

    override def frameworkName(): String = "Dynamic"

    val port = 1337

    override def serializeCG(
                       algorithm: String,
                       target: String,
                       mainClass: String,
                       classPath: Array[String],
                       JDKPath: String,
                       analyzeJDK: Boolean,
                       outputFile: String
                   ): Long = {

        val javaPath = Paths.get(JDKPath).getParent.toAbsolutePath + "/bin/java"
        val agentPath = getClass.getClassLoader.getResource("DynamicCG.so").getPath
        val agentArgs = Array(port.toString).mkString(",")
        val cp = target + File.pathSeparator +
            util.Arrays.stream(classPath).collect(Collectors.joining(File.pathSeparator))
        val programArgs = Array.empty[String]

        val reachableMethods = mutable.Set[Method]()
        val edges = mutable.Map[Method, mutable.Map[(Int, Int), mutable.Set[Method]]]()

        val args = List(javaPath, s"-agentpath:$agentPath=$agentArgs", "-cp", cp, mainClass) ++
            programArgs

        val before = System.nanoTime

        new ProcessBuilder(args.asJava).inheritIO().start()

        val socket = new Socket("localhost", port)

        val after = System.nanoTime

        val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))

        System.out.println(reader.readLine())

        socket.close
        reader.close()

        after - before
    }
}