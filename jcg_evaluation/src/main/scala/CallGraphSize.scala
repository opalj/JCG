import java.io.File
import java.io.FileInputStream

import play.api.libs.json.Json

object CallGraphSize {
    def main(args: Array[String]): Unit = {
        val i = new File(args(0))
        assert(i.exists())
        if (i.isDirectory) {
            for (file ← i.listFiles(_.getName.endsWith(".json"))) {
                printNumReachableMethods(file)
            }
        } else {
            assert(i.getName.endsWith(".json"))
            printNumReachableMethods(i)
        }
    }
    def printNumReachableMethods(jsFile: File): Unit = {
        val size = Json.parse(new FileInputStream(jsFile)).validate[ReachableMethods].get.reachableMethods.size
        println(s"${jsFile.getName} - $size")
    }
}
