import java.io.File
import java.io.FileInputStream

import play.api.libs.json.Reads
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.json.JsSuccess

/**
 * Each JRE directory (`path`) is associated with the underlying java version.
 */
case class JRELocation(version: Int, path: String)

/**
 * A JVM locations specification file lists for each on the system available Java version the
 * corresponding [[JRELocation]].
 * Thus, such a file is a Json array of [[JRELocation]] entries.
 *
 * @note The JVM locations specification file should have the name jre.conf.
 *
 * @author Florian Kuebler
 */
object JRELocation {
    implicit val methodReads: Reads[JRELocation] = Json.reads[JRELocation]

    implicit val methodWrites: Writes[JRELocation] = Json.writes[JRELocation]

    /**
     * Retrieves the specified JRE directory for the given java `version`.
     */
    def jreDirectory(jreLocationsFile: File, version: Int): String = {
        Json.parse(new FileInputStream(jreLocationsFile)).validate[Array[JRELocation]] match {
            case JsSuccess(location, _) ⇒
                location.find(_.version == version).getOrElse(
                    throw new IllegalArgumentException(
                        s"java version $version not specified in jre locations"
                    )
                ).path
            case _ ⇒
                throw new IllegalArgumentException("invalid jre location specification")
        }
    }

    /**
     * From the give JRE locations specification file, this method creates a mapping from java
     * version the JRE root directory.
     */
    def mapping(jreLocationsFile: File): Map[Int, String] = {
        Json.parse(new FileInputStream(jreLocationsFile)).validate[Array[JRELocation]] match {
            case JsSuccess(location, _) ⇒
                location.map(jreLocation ⇒ jreLocation.version → jreLocation.path).toMap
            case _ ⇒
                throw new IllegalArgumentException("invalid jre location specification")
        }
    }

    /**
     * Returns all .jar and .jmod files in the given directory and all transitive subdirectories.
     */
    def getAllJREJars(JREPath: String): Array[File] = {
        val jreDir = new File(JREPath)
        val jars = jreDir.listFiles { file ⇒
            file.getName.endsWith(".jar") | file.getName.endsWith(".jmod")
        }
        val jarsInSubDirs = jreDir.listFiles(_.isDirectory).flatMap(f ⇒ getAllJREJars(f.getPath))
        jars ++ jarsInSubDirs
    }
}