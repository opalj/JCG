import java.io.File

import coursier.Cache
import coursier.Dependency
import coursier.Fetch
import coursier.FileError
import coursier.Module
import coursier.Resolution
import coursier.maven.MavenRepository
import play.api.libs.json.Json
import play.api.libs.json.OWrites
import play.api.libs.json.Reads
import play.api.libs.json.Writes
import play.api.libs.json.__
import scalaz.\/

case class ProjectSpecification(
        name: String, target: String, main: Option[String], java: Int, cp: Option[Array[ClassPathEntry]]
) {
    def allClassPathEntryFiles(parent: File): Array[File] = {
        cp.getOrElse(Array.empty).flatMap(_.getLocations.map { location ⇒
            if (location.isAbsolute)
                location
            else
                new File(parent, location.getPath)
        })
    }
}
object ProjectSpecification {
    implicit val configReads: Reads[ProjectSpecification] = Json.reads[ProjectSpecification]
    implicit val configWrites: OWrites[ProjectSpecification] = Json.writes[ProjectSpecification]
}

sealed trait ClassPathEntry {
    def getLocations: Array[File]
}
object ClassPathEntry {
    implicit val classPathEntryReads: Reads[ClassPathEntry] =
        __.read[MavenClassPathEntry].map(x ⇒ x: ClassPathEntry) orElse __.read[LocalClassPathEntry].map(x ⇒ x: ClassPathEntry)

    implicit val classPathEntryWrites: Writes[ClassPathEntry] = Writes[ClassPathEntry] {
        case mvn: MavenClassPathEntry ⇒ MavenClassPathEntry.writer.writes(mvn)
        case lcl: LocalClassPathEntry ⇒ LocalClassPathEntry.writer.writes(lcl)
    }
}
case class MavenClassPathEntry(org: String, id: String, version: String) extends ClassPathEntry {
    override def getLocations: Array[File] = {
        val start = Resolution(Set(Dependency(Module(org, id), version)))
        val repositories = Seq(Cache.ivy2Local, MavenRepository("https://repo1.maven.org/maven2"))
        val fetch = Fetch.from(repositories, Cache.fetch())

        val resolution = start.process.run(fetch).unsafePerformSync
        val r: Seq[\/[FileError, File]] = resolution.artifacts.map(Cache.file(_).run).map(_.unsafePerformSync)
        assert(r.forall(_.isRight))

        r.map(_.toOption.get).toArray
    }
}
object MavenClassPathEntry {
    implicit val reader: Reads[MavenClassPathEntry] = Json.reads[MavenClassPathEntry]
    val writer: Writes[MavenClassPathEntry] = Json.writes[MavenClassPathEntry]
}

/**
 * Represents a local class path entry, i.e. a file such as a jar
 * @param path file to the class path entry: either absolute or relative to the directory of the
 *             specification file.
 */
case class LocalClassPathEntry(path: String) extends ClassPathEntry {
    override def getLocations: Array[File] = Array(new File(path))
}
object LocalClassPathEntry {
    implicit val reader: Reads[LocalClassPathEntry] = Json.reads[LocalClassPathEntry]
    val writer: Writes[LocalClassPathEntry] = Json.writes[LocalClassPathEntry]
}