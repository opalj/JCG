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
     *      resultDir/target/framework/algorithm/\*.json
     */
    def main(args: Array[String]): Unit = {
        val i = new File(args(0))
        assert(i.exists())
        if (i.isDirectory) {
            for {
                target ← i.listFiles(_.isDirectory).sorted
                framework ← target.listFiles(_.isDirectory)
                algo ← framework.listFiles(_.isDirectory)
                file ← algo.listFiles(_.getName.endsWith(".json"))
            } {
                val size = Json.parse(new FileInputStream(file)).validate[ReachableMethods].get.reachableMethods.size
                println(s"${target.getName} ${framework.getName} ${algo.getName} ${file.getName} - $size")
            }

            for (file ← i.listFiles(_.getName.endsWith(".json"))) {
                printStatistic(file)
            }
        } else {
            assert(i.getName.endsWith(".json"))
            printStatistic(i)
        }
    }
    def printStatistic(jsFile: File): Unit = {
        val reachableMethods = Json.parse(new FileInputStream(jsFile)).validate[ReachableMethods].get.reachableMethods

        val edgeCount = reachableMethods.flatMap(_.callSites.map(_.targets.size)).sum
        println(s"${jsFile.getName} - ${reachableMethods.size} reachable methods - $edgeCount call graph edges")

    }
}
