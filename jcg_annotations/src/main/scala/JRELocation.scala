import java.io.File
import java.io.FileInputStream

import play.api.libs.json.JsSuccess
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Writes

case class JRELocation(version: Int, path: String)

object JRELocation {
    implicit val methodReads: Reads[JRELocation] = Json.reads[JRELocation]

    implicit val methodWrites: Writes[JRELocation] = Json.writes[JRELocation]

    def mapping(jreLocationsFile: File): Map[Int, String] = {
        Json.parse(new FileInputStream(jreLocationsFile)).validate[Array[JRELocation]] match {
            case JsSuccess(location, _) ⇒
                location.map(jreLocation ⇒ jreLocation.version → jreLocation.path).toMap
            case _ ⇒
                throw new IllegalArgumentException("invalid jre location specification")
        }
    }
}