import java.io.File

object CheckForExceptions {

    def main(args: Array[String]): Unit = {
        for (arg ← args) {
            val f = new File(arg)
            assert(f.exists() && f.isDirectory)
            Evaluation.main(Array(
                "--input", arg,
                "--output", "evaluation",
                "--adapter", "WALA",
                "--adapter", "Soot",
                "--algorithm-prefix", "RTA",
                "--exclude-jdk",
                "--debug"
            ))
        }
    }

}
