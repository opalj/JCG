import java.io.File
import java.io.FileInputStream

import play.api.libs.json.Json
/**
 *
 * @author Michael Reif
 */
object CompareJVMProfiledMethods {

    def main(args: Array[String]): Unit = {
        var callGraphFile = ""
        var profile = ""

        args.sliding(2, 2).toList.collect {
            case Array("--callgraph", cg) ⇒
                assert(callGraphFile.isEmpty, "--callgraph is specified multiple times")
                callGraphFile = cg
            case Array("--profile", cg) ⇒
                assert(profile.isEmpty, "--tamiflex is specified multiple times")
                profile = cg
        }

        val reachableMethods = parseCallGraph(callGraphFile).keySet
        val profiledMethods = parseProfileMethods(profile)
        val numProfiledMethods = profiledMethods.size

        println(s"profiled methods: $numProfiledMethods")
        println(s"reachable methods: ${reachableMethods.size}")

        val unreachable = profiledMethods.filter { m =>
            reachableMethods.find(_.nameBasedEquals(m)).isEmpty
        }

        println(s"unreachable: ${unreachable.size}")
        println(
            unreachable.mkString("unreachable methods: \n\n","\t\n", "")
        )

        println(s"\n\n ${numProfiledMethods - unreachable.size} of $numProfiledMethods all methods are reachable")
    }

    private def parseCallGraph(callGraphFile: String) = {
        EvaluationHelper.readCG(new File(callGraphFile)).toMap
    }

    private def parseProfileMethods(profile: String): List[Method] = {
        var data : List[Method] = List.empty
        val bufferedSource = scala.io.Source.fromFile(profile)
        for (line <- bufferedSource.getLines) {
            val cols = line.split("\t").map(_.trim)
            val className= cols(0)
            val methodName = cols(1)
            val method = Method(methodName, className, "", List.empty)
            data = data :+ method
        }
        bufferedSource.close
        data
    }

}