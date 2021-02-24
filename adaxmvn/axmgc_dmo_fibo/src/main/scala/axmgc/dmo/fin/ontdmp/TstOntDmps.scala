package axmgc.dmo.fin.ontdmp

import axmgc.web.lnch.FallbackLog4J
import axmgc.web.pond.WebServerLauncher
import org.apache.jena.riot.RDFDataMgr
import org.slf4j.{Logger, LoggerFactory}

object TstOntDmps  {
	val flg_consoleTest = true
	val flg_wbsvcLnch = true
	val flg_setupFallbackLog4J = false // Set to false if log4j.properties is expected, e.g. from Jena.
	val myFallbackLog4JLevel = org.apache.log4j.Level.INFO

	val myActSysNm = "axmgc_dmo_fin_19"
	val myWbSvcHostName = "localhost"
	val myWbSvcPort = 8119

	lazy val myFLog4J = new FallbackLog4J {}
	lazy val myTontApp = new TstOntApp(myActSysNm)
	lazy val myS4JLogger : Logger = LoggerFactory.getLogger(classOf[TstOntApp])

	def main(args: Array[String]) {
		if (flg_setupFallbackLog4J) {
			myFLog4J.setupFallbackLogging(myFallbackLog4JLevel)
		}
		if (flg_consoleTest) {
			myTontApp.chkOntStatsAndPrintToLog // Run our console-output test code.
			myS4JLogger.info(".main() is half done, console-output tests are complete.  ")
		}
		if (flg_wbsvcLnch) {
			// Launch axiomagic web micro-service using akka http to answer requests.
			myS4JLogger.info(".main() begins second half, launching web service ")
			myTontApp.launchWebSvc(myWbSvcHostName, myWbSvcPort)
		}
		myS4JLogger.warn("END of .main()")
		println("println: .main() says goodbye") // , but actors are possibly still running
	}
}
class TstOntApp(myActSysNm : String) extends WebServerLauncher {
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)
	protected lazy val myActorSys = makeActorSys(myActSysNm)
	private lazy val myOntChkr  = new ChkFibo {}
	def chkOntStatsAndPrintToLog : Unit = {
		myOntChkr.dumpFiboMdlStatsToLog()
	}
	def launchWebSvc(svcHostName : String, svcPort : Int, flg_blockUntilEnterKey: Boolean = true) : Unit = {

		// TODO:  Return a future supporting clean shutdown
		myS4JLog.info("Launching app with actrSysNm={}", myActSysNm)
		val actSys = myActorSys
		myS4JLog.debug("Found actorSys handle: {}", actSys)

		val dmprTpblBrdg: DumperWebFeat = new DumperTupleBridge {}
		val dmprRtMkr = new DmpWbRtMkr {
			override protected def getDumperWebFeat: DumperWebFeat = dmprTpblBrdg
		}
		val dmprRt = dmprRtMkr.makeDmprTstRt
		launchWebServer(dmprRt, actSys, svcHostName, svcPort, flg_blockUntilEnterKey)
	}
}
