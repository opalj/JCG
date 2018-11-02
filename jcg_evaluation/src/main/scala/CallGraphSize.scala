import java.io.File
import java.io.FileInputStream

import play.api.libs.json.Json

/**
 * A small helper to get the size information of computed (serialized) call graphs.
 *
 * @author Florian Kuebler
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
        if (i.isDirectory) {
            // for structures like target/framework/algorithm
            for {
                target ← i.listFiles(_.isDirectory).sorted
                framework ← target.listFiles(_.isDirectory)
                algo ← framework.listFiles(_.isDirectory)
                callgraph = s"$framework $algo"
                file ← algo.listFiles(_.getName.endsWith(".json"))
            } {
                printStatistic(file, callgraph)
            }

            // for all .json files in the given directory
            for (file ← i.listFiles(_.getName.endsWith(".json"))) {
                printStatistic(file)
            }
        } else {
            // for a given .json file
            assert(i.getName.endsWith(".json"))
            printStatistic(i)
        }
    }
    def printStatistic(jsFile: File, callGraphName : String = ""): Unit = {
        val reachableMethods = Json.parse(new FileInputStream(jsFile)).validate[ReachableMethods].get.reachableMethods

        val edgeCount = reachableMethods.flatMap(_.callSites.map(_.targets.size)).sum

        val outputName = if(callGraphName.isEmpty) jsFile.getName else callGraphName

        println(s"$outputName - ${reachableMethods.size} reachable methods - $edgeCount call graph edges")

    }
}
