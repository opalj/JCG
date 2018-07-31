/* BSD 2-Clause License - see OPAL/LICENSE for details. */
import sbt._

import scala.xml.NodeSeq

/**
 * Definiton of the tasks and settings to support exporting OPAL to Maven.
 *
 * @author Michael Eichberg
 * @author Simon Leischnig
 */
object MavenPublishing {

    // method populates sbt.publishTo = SettingKey[Option[Resolver]]
    def publishTo(isSnapshot: Boolean): Option[Resolver] = {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot)
            Some("snapshots" at nexus+"content/repositories/snapshots")
        else
            Some("releases" at nexus+"service/local/staging/deploy/maven2")
    }

    def pomNodeSeq(): NodeSeq = {
        <scm>
            <url>git@bitbucket.org:delors/jcg.git</url>
            <connection>scm:git:git@bitbucket.org:delors/jcg.git</connection>
        </scm>
        <developers>
            <developer>
                <id>reif</id>
                <name>Michael Reif</name>
            </developer>
            <developer>
                <id>florian_kuebler</id>
                <name>Florian Kübler</name>
            </developer>
            <developer>
                <id>eichberg</id>
                <name>Michael Eichberg</name>
                <url>http://www.michael-eichberg.de</url>
            </developer>
        </developers>
    }

}
