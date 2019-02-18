package axmgc.dmo.fin.ontdmp

/*
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.{Http, server => dslServer}
import akka.stream.ActorMaterializer
*/
import axmgc.web.lnch.FallbackLog4J
import axmgc.web.pond.WebServerLauncher

// import scala.concurrent.{Future => ConcFut}
// import scala.io.StdIn
import org.slf4j.{Logger, LoggerFactory}
// import org.apache.log4j.{BasicConfigurator, Level => Log4JLevel, Logger => Log4JLogger}

object TstOntDmps extends  {
	val myActSysNm = "axmgc_dmo_fin_19"
	val flg_consoleTest = true
	val flg_wbsvcLnch = true

	lazy val myFLog4J = new FallbackLog4J {}
	lazy val myTontApp = new TstOntApp(myActSysNm)
	lazy val myS4JLogger : Logger = LoggerFactory.getLogger(classOf[TstOntApp])

	def main(args: Array[String]) {
		myFLog4J.setupLogging
		if (flg_consoleTest) {
			myTontApp.chkOntStatsAndPrintToLog // Run our console-output test code.
			myS4JLogger.info(".main() is half done, console-output tests are complete.  ")
		}
		if (flg_wbsvcLnch) {
			myS4JLogger.info(".main() begins second half, launching web service ")
			myTontApp.launchWebSvc // Launch axiomagic web micro-service using akka http to answer requests.
		}
		myS4JLogger.warn("END of .main()")
		println("println: .main() says goodbye") // , but actors are possibly still running.")
	}

}
class TstOntApp(myActSysNm : String) extends WebServerLauncher {
	protected lazy val myS4JLogger : Logger = LoggerFactory.getLogger(this.getClass)
	protected lazy val myActorSys = makeActorSys(myActSysNm)

	// TODO:  Return a future supporting clean shutdown
	def launchWebSvc : Unit = {
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
	def chkOntStatsAndPrintToLog : Unit = {
		val cf  = new ChkFibo {
			override def getS4JLog: Logger = myS4JLogger
		}
		cf.chkMdlStats
	}
}