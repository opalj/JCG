import java.io.File

object XCorpusAdapter {

    def main(args: Array[String]): Unit = {
        val projectDir = new File(s"${args(0)}${File.separator}project")
        assert(projectDir.exists())
        val bin = new File(projectDir, "bin.zip")
        assert(bin.exists())
        val libDir = new File(projectDir, "default-lib")
        assert(libDir.exists())
        val libs = libDir.listFiles(f ⇒ f.getName.endsWith(".jar")).map(_.getAbsolutePath)
    }
}
