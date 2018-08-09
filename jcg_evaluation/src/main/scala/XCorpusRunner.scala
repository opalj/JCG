object XCorpusRunner {
    def main(args: Array[String]): Unit = {
        org.opalj.hermes.HermesCLI.main(Array("-config", "xcorpus.json", "-statistics", "hermes.csv", "-writeLocations" , "hermesResults"))
    }
}
