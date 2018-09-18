import java.io.File

import coursier.Module
import coursier.Dependency
import coursier.Resolution
import coursier.Cache
import coursier.Fetch
import coursier.FileError
import coursier.maven.MavenRepository
import play.api.libs.json.JsPath
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.OWrites
import play.api.libs.json.Writes
import scalaz.\/

/**
 * Specifies a target project.
 *
 * @author Florian Kuebler
 */
case class ProjectSpecification(
        name:               String,
        java:               Int,
        main:               Option[String],
        private val target: String,
        private val cp:     Option[Array[ClassPathEntry]]
) {
    /**
     * For the specified [[ClassPathEntry]]s it computes all concrete class path entries.
     *
     * @param parent
     *               In case any locations are specified as relative path, `parent` is used as
     *               root directory.
     */
    def allClassPathEntryFiles(parent: File): Array[File] = {
        cp.getOrElse(Array.empty).flatMap(_.getLocations.map { location ⇒
            if (location.isAbsolute)
                location
            else
                new File(parent, location.getPath)
        })
    }

    /**
      * For the specified [[ClassPathEntry]]s it computes all concrete class path entries.
      * The entries are given as canonical path.
      *
      * @param parent
      *               In case any locations are specified as relative path, `parent` is used as
      *               root directory.
      */
    def allClassPathEntryPaths(parent: File): Array[String] = {
        allClassPathEntryFiles(parent).map(_.getCanonicalPath)
    }

    /**
     * Retrieves the file of the specified target.
     *
     * @param parent In case the location of the target is specified as relative path, `parent`
     *               is used as root directory to get the correct file.
     * @return
     */
    def target(parent: File): File = {
        val tgtFile = new File(target)
        if (tgtFile.isAbsolute)
            tgtFile
        else
            new File(parent, target)
    }
}

object ProjectSpecification {
    implicit val reader: Reads[ProjectSpecification] = Json.reads[ProjectSpecification]
    implicit val writer: OWrites[ProjectSpecification] = Json.writes[ProjectSpecification]
}

/**
 * A single class path entry can contain multiple *real* class path entry files (locations).
 * As an example a [[MavenClassPathEntry]] may contain consist out of the specified project and its
 * dependencies.
 *
 * @note currently only [[LocalClassPathEntry]] and [[MavenClassPathEntry]] as supported.
 *       Adding other kinds of entries must result in changes of the reads/writes of the companion
 *       object.
 */
sealed trait ClassPathEntry {
    def getLocations: Array[File]
}

object ClassPathEntry {
    implicit val reader: Reads[ClassPathEntry] =
        JsPath.read[MavenClassPathEntry].map(x ⇒ x: ClassPathEntry) orElse JsPath.read[LocalClassPathEntry].map(x ⇒ x: ClassPathEntry)

    implicit val writer: Writes[ClassPathEntry] = Writes[ClassPathEntry] {
        case mvn: MavenClassPathEntry ⇒ MavenClassPathEntry.writer.writes(mvn)
        case lcl: LocalClassPathEntry ⇒ LocalClassPathEntry.writer.writes(lcl)
    }
}

/**
 * Specifies an artifact (including its dependencies) in the maven central repository.
 */
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
    implicit val writer: Writes[MavenClassPathEntry] = Json.writes[MavenClassPathEntry]
}

/**
 * Represents a local class path entry, i.e. a file such as a jar
 *
 * @param path file to the class path entry: either absolute or relative to the directory of the
 *             specification file.
 */
case class LocalClassPathEntry(path: String) extends ClassPathEntry {
    override def getLocations: Array[File] = Array(new File(path))
}

object LocalClassPathEntry {
    implicit val reader: Reads[LocalClassPathEntry] = Json.reads[LocalClassPathEntry]
    implicit val writer: Writes[LocalClassPathEntry] = Json.writes[LocalClassPathEntry]
}