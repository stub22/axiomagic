package axmgc.web.lnch

import axmgc.web.pond.WebServerLauncher
// import org.apache.log4j.BasicConfigurator
import org.slf4j.{Logger, LoggerFactory}

trait LggdActrLnch

class WbSrvcLnchr (myActSysNm : String, myHostNm : String, myPortNum : Int)
		extends WebServerLauncher {
	protected lazy val myS4JLogger: Logger = LoggerFactory.getLogger(this.getClass)
	protected lazy val myActorSys = makeActorSys(myActSysNm)

	// TODO:  Return a future supporting clean shutdown
	def launch: Unit = {
	}
}

import org.apache.log4j.{BasicConfigurator, Level => Log4JLevel, Logger => Log4JLogger}

trait FallbackLog4J {
	// TODO:  Reconcile Akka logging with SLf4J logging, Log4J, Jena
	protected lazy val myS4JLogger : Logger = LoggerFactory.getLogger(this.getClass)

	def setupFallbackLogging(log4JLevel: Log4JLevel) : Unit = {
		BasicConfigurator.configure()
		Log4JLogger.getRootLogger.setLevel(log4JLevel)
		printSomeMsgs
	}

	private def printSomeMsgs : Unit = {
		myS4JLogger.warn("TstOntApp init with one warning msg");
		myS4JLogger.warn("Vapid message at INFO level");
		myS4JLogger.debug("Debug message providing help-docs on logging: {}", bcDocTxt)
		myS4JLogger.trace("Wow, even this trace detail is so fine and fluffy!")
	}
	val bcDocTxt = """
					 |What BasicConfigurator.configure() does, from:
					 |https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/BasicConfigurator.html
					 |
					 |Add a ConsoleAppender that uses PatternLayout using the PatternLayout.TTCC_CONVERSION_PATTERN
					 |and prints System.out to the root category.
				   """.stripMargin

}