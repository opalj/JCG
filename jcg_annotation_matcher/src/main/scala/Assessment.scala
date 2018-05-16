/**
 *
 *
 * @author Michael Reif
 */
trait Assessment {
    def isSound : Boolean
    def isUnsound: Boolean
    def combine(other: Assessment) : Assessment
    override def toString: String
    def shortNotation : String
}

object Sound extends Assessment {
    override def isSound: Boolean = true
    override def isUnsound: Boolean = false
    override def combine(other: Assessment): Assessment = other
    override def toString: String = "Sound"
    override def shortNotation: String = "S"
}

object Imprecise extends Assessment {
    override def isSound: Boolean = false
    override def isUnsound: Boolean = false
    override def combine(other: Assessment): Assessment = {
        other match {
            case Unsound => other
            case _ => Imprecise
        }
    }
    override def toString: String = "Imprecise"
    override def shortNotation: String = "I"
}

object Unsound extends Assessment {
    override def isSound: Boolean = false
    override def isUnsound: Boolean = true
    override def combine(other: Assessment): Assessment = Unsound
    override def toString: String = "Unsound"
    override def shortNotation: String = "U"
}
