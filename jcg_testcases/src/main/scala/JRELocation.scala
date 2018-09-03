import java.io.File
import java.io.FileInputStream

import play.api.libs.json.Reads
import play.api.libs.json.Json
import play.api.libs.json.Writes
import play.api.libs.json.JsSuccess


case class JRELocation(version: Int, path: String)

object JRELocation {
    implicit val methodReads: Reads[JRELocation] = Json.reads[JRELocation]

    implicit val methodWrites: Writes[JRELocation] = Json.writes[JRELocation]

    def jreDirectory(jreLocationsFile: File, version: Int): String = {
        Json.parse(new FileInputStream(jreLocationsFile)).validate[Array[JRELocation]] match {
            case JsSuccess(location, _) ⇒
                location.find(_.version == version).getOrElse(
                    throw new IllegalArgumentException(s"java version $version not specified in jre locations")
                ).path
            case _ ⇒
                throw new IllegalArgumentException("invalid jre location specification")
        }
    }

    def mapping(jreLocationsFile: File): Map[Int, Array[File]] = {
        Json.parse(new FileInputStream(jreLocationsFile)).validate[Array[JRELocation]] match {
            case JsSuccess(location, _) ⇒
                location.map(
                    jreLocation ⇒ jreLocation.version → getAllJREJars(new File(jreLocation.path))
                ).toMap
            case _ ⇒
                throw new IllegalArgumentException("invalid jre location specification")
        }
    }

    def getAllJREJars(jreDir: File): Array[File] = {
        val jars = jreDir.listFiles(file ⇒ file.getName.endsWith(".jar") | file.getName.endsWith(".jmod"))
        val jarsInSubDirs = jreDir.listFiles(_.isDirectory).flatMap(getAllJREJars)
        jars ++ jarsInSubDirs
    }
}