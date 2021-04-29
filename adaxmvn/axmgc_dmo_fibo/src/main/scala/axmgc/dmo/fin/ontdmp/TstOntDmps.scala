package axmgc.dmo.fin.ontdmp

import axmgc.web.lnch.FallbackLog4J
import axmgc.web.pond.WebServerLauncher
import org.slf4j.{Logger, LoggerFactory}
import akka.http.scaladsl.{server => dslServer}
import axmgc.dmo.ksrc.lean_mthlb.LnMthlbNavRouteBldr
import axmgc.xpr.vis_js.{MakeSampleSaveRoutes, MakeWebTableRoutes}

object TstOntDmps  {
	val flg_consoleTest = false
	val flg_wbsvcLnch =  true
	val flg_setupFallbackLog4J = false // Set to false if log4j.properties is expected, e.g. from Jena.
	val myFallbackLog4JLevel = org.apache.log4j.Level.INFO

	val myActSysNm = "axmgc_dmo_fin_19"
	val myWbSvcHostName = "localhost"
	val myWbSvcPort = 8973

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
	import dslServer.Directives.{_}

	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)
	protected lazy val myActorSys = makeActorSys(myActSysNm)
	private lazy val myFiboChkr  = new FiboOntWrap {}
	private lazy val myKbpOntWrp = new KBPediaOntoWrap {}

	def chkOntStatsAndPrintToLog : String = {
		chkKbpStats()
	}
	private def chkKbpStats() : String = {
		val typoStatsJsnTxt = myKbpOntWrp.dumpTypoStatsAsJsonTxt
		myS4JLog.info("typoStats json block: {}", typoStatsJsnTxt)
		typoStatsJsnTxt
	}
	private def chkFiboStats() : String = {
		myFiboChkr.dumpFiboMdlStatsToLog()
	}
	def launchWebSvc(svcHostName : String, svcPort : Int, flg_blockUntilEnterKey: Boolean = true) : Unit = {

		// TODO:  Return a future supporting clean shutdown
		myS4JLog.info("Launching app with actrSysNm={}", myActSysNm)
		val actSys = myActorSys
		myS4JLog.debug("Found actorSys handle: {}", actSys)
		val testRt = makeOurTestComboRoute()
		launchWebServer(testRt, actSys, svcHostName, svcPort, flg_blockUntilEnterKey)
	}
	private def makeOurTestComboRoute (): dslServer.Route = {
		val dmprRt = mkDumperRoute()
		val dmOntNvTrRt = mkDemoOntNavTreeRoute("onav")
		val lnMthNvTrRt = mkProofLibNavTreeRoute("lmnav")
		val bonusRt = mkBonusRoute()
		val testComboRt = dmprRt ~ dmOntNvTrRt ~ lnMthNvTrRt ~ bonusRt
		testComboRt
	}
	private def mkDumperRoute(): dslServer.Route = {
		val dmprTpblBrdg: DumperWebFeat = new DumperTupleBridge {
			override protected def findFiboOntWrap: FiboOntWrap = myFiboChkr

			override protected def findKbpediaOntWrap: KBPediaOntoWrap = myKbpOntWrp
		}
		val dmprRtMkr = new DmpWbRtMkr {
			override protected def getDumperWebFeat: DumperWebFeat = dmprTpblBrdg
		}
		val dmprRt = dmprRtMkr.makeDmprTstRt
		dmprRt
	}
	private def mkDemoOntNavTreeRoute(routePathTxt : String) : dslServer.Route = {
		val wnrb = new DemoOntNavRouteBldr{}
		val wnrt = wnrb.mkNavJsonRt(routePathTxt)
		wnrt
	}
	private def mkProofLibNavTreeRoute(routePathTxt : String) : dslServer.Route = {
		val lmnRB = new LnMthlbNavRouteBldr{}
		val lmnRt = lmnRB.mkNavJsonRt(routePathTxt)
		lmnRt
	}

	private def mkBonusRoute() : dslServer.Route = {
		val mssr = new MakeSampleSaveRoutes{}
		val svRt = mssr.mkSavingRt

		val mgtr = new MakeWebTableRoutes{}
		val tbcRt = mgtr.mkSampleColDefsJsonRt("sampcols")
		val tbrRt = mgtr.mkSampleRowsetJsonRt("samprows")

		val bonusRt = tbcRt ~ tbrRt ~ svRt
		bonusRt
	}
}

