package axmgc.dmo.fin.ontdmp

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.{Http, server => dslServer}
import akka.stream.ActorMaterializer
import axmgc.web.pond.WebServerLauncher

import scala.concurrent.{Future => ConcFut}
import scala.io.StdIn
import org.slf4j.{Logger, LoggerFactory}
import org.apache.log4j.{BasicConfigurator, Level => Log4JLevel, Logger => Log4JLogger}

object TstOntDmps {
	val myActSysNm = "axmgc_dmo_fin_19"
	lazy val myTontApp = new TstOntApp(myActSysNm)
	lazy val myS4JLogger : Logger = LoggerFactory.getLogger(classOf[TstOntApp])

	def main(args: Array[String]) {
		setupLogging
		myTontApp.launch
		myS4JLogger.warn("END of .main()")
		println("println: .main() says goodbye, but actors are possibly still running.")
	}
	// TODO:  Reconcile Akka logging with SLf4J logging, Log4J, Jena
	private def setupLogging : Unit = {
		BasicConfigurator.configure()
		Log4JLogger.getRootLogger.setLevel(Log4JLevel.TRACE)
		printSomeMsgs
	}
	private def printSomeMsgs : Unit = {
		myS4JLogger.warn("TstOntApp init with one warning msg");
		myS4JLogger.info("Docs on: {}", bcDocTxt)
		myS4JLogger.trace("Wow, even trace detail is so fine and fluffy!")
	}
	val bcDocTxt = """
					 |What BasicConfigurator.configure() does, from:
					 |https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/BasicConfigurator.html
					 |
					 |Add a ConsoleAppender that uses PatternLayout using the PatternLayout.TTCC_CONVERSION_PATTERN
					 |and prints System.out to the root category.
				   """.stripMargin

}
class TstOntApp(myActSysNm : String) extends WebServerLauncher {
	protected lazy val myS4JLogger : Logger = LoggerFactory.getLogger(this.getClass)
	protected lazy val myActorSys = makeActorSys(myActSysNm)

	// TODO:  Return a future supporting clean shutdown
	def launch : Unit = {
		myS4JLogger.info("Launching app with actrSysNm={}", myActSysNm)
		val actSys = myActorSys
		myS4JLogger.debug("Got actorSys handle: {}", actSys)
		val srvIntf = "localhost"
		val srvPort = 8119

		val dmprTpblBrdg: DumperWebFeat = new DumperTupleBridge {}
		val dmprRtMkr = new DmpWbRtMkr {
			override protected def getDumperWebFeat: DumperWebFeat = dmprTpblBrdg
		}
		val dmprRt = dmprRtMkr.makeDmprTstRt
		launchWebServer(dmprRt, actSys, srvIntf, srvPort)
	}
}