import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

import play.api.libs.json.Json

/**
 * A small helper to get the size information of computed (serialized) call graphs.
 *
 * @author Florian Kuebler
 * @author Michael Reif
 */
object CallGraphSize {

    /**
     * Prints the number of reachable methods and the number of edges of the given call graphs.
     *
     * @param args
     *      args[0] must be either a path to a serialized call graph or the path to the result
     *      directory. The stucture of the directory case must be the following:
     *      resultDir/target/framework/algorithm/\*.json or a directory with the .json files in it.
     */
    def main(args: Array[String]): Unit = {
        val i = new File(args(0))
        assert(i.exists())

        // take all arguments except the first one as parameter
        val pgkPrefixes = args.takeRight(args.length - 1).toList

        if (i.isDirectory) {
            // for structures like target/framework/algorithm
            for {
                target ← i.listFiles(_.isDirectory).sorted
                framework ← target.listFiles(_.isDirectory)
                algo ← framework.listFiles(_.isDirectory)
                callgraph = s"${framework.getName} ${algo.getName}"
                file ← algo.listFiles{ f ⇒ f.getName.endsWith(".json") || f.getName.endsWith(".zip")  || f.getName.endsWith(".gz") }
            } {
                printStatistic(file, pgkPrefixes, callgraph)
            }

            // for all .json files in the given directory
            for (file ← i.listFiles(_.getName.endsWith(".json"))) {
                printStatistic(file, pgkPrefixes)
            }
        } else {
            // for a given .json file
            assert(i.getName.endsWith(".json"))
            printStatistic(i, pgkPrefixes)
        }
    }

    def printStatistic(cgFile: File, appPackages: List[String], callGraphName : String = ""): Unit = {
        val reachableMethods = EvaluationHelper.readCG(cgFile).reachableMethods

        val appMethods = reachableMethods.count { rm =>
            val declClass = rm.method.declaringClass
            appPackages.exists { pkg =>
                declClass.startsWith(s"L$pkg")
            }
        }

        val edgeCount = reachableMethods.foldLeft(0){ (acc, rm) =>
            acc + rm.callSites.foldLeft(0)((acc,cs) => acc + cs.targets.size)
        }

        val outputName = if(callGraphName.isEmpty) cgFile.getName else callGraphName

        println(s"$outputName - ${reachableMethods.size} reachable methods - $edgeCount call graph edges [application methods: $appMethods]")
    }
}
