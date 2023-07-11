/**
 *
 *
 * @author Michael Reif
 */
trait Assessment {
    def isSound : Boolean
    def isUnsound: Boolean
    def isTimeout: Boolean
    def combine(other: Assessment) : Assessment
    def toString: String
    def shortNotation : String
}

object Sound extends Assessment {
    override def isSound: Boolean = true
    override def isUnsound: Boolean = false
    override def isTimeout: Boolean = false
    override def combine(other: Assessment): Assessment = other
    override def toString: String = "Sound"
    override def shortNotation: String = "S"
}

object Imprecise extends Assessment {
    override def isSound: Boolean = false
    override def isUnsound: Boolean = false
    override def isTimeout: Boolean = false
    override def combine(other: Assessment): Assessment = {
        other match {
            case Unsound | Error | Timeout => other
            case _ => Imprecise
        }
    }
    override def toString: String = "Imprecise"
    override def shortNotation: String = "I"
}

object Unsound extends Assessment {
    override def isSound: Boolean = false
    override def isUnsound: Boolean = true
    override def isTimeout: Boolean = false
    override def combine(other: Assessment): Assessment = {
      other match {
          case Error | Timeout => other
          case  _ => Unsound
      }
    }
    override def toString: String = "Unsound"
    override def shortNotation: String = "U"
}

object Error extends Assessment {
    override def isSound: Boolean = false
    override def isUnsound: Boolean = false
    override def isTimeout: Boolean = false
    override def combine(other: Assessment): Assessment = {
        other match {
            case Timeout => other
            case _ => Error
        }
    }
    override def toString: String = "Error"
    override def shortNotation: String = "E"
}

object Timeout extends Assessment {
    def isSound : Boolean = false
    def isUnsound: Boolean = false
    def isTimeout: Boolean = true
    def combine(other: Assessment) : Assessment = Timeout
    override def toString: String = "Timeout"
    def shortNotation : String = "T"
}
