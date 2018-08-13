import org.opalj.log.LogContext
import org.opalj.log.LogMessage
import org.opalj.log.OPALLogger

class DevNullLogger extends OPALLogger {
    override def log(message: LogMessage)(implicit ctx: LogContext): Unit = {}
}