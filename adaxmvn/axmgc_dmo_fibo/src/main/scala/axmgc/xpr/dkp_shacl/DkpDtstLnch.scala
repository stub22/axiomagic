package axmgc.xpr.dkp_shacl

import axmgc.dmo.fin.ontdmp.TstOntDmps.printSomeMsgs
import axmgc.web.pond.WebServerLauncher
import org.slf4j.{Logger, LoggerFactory}
import org.apache.log4j.{BasicConfigurator, Level => Log4JLevel, Logger => Log4JLogger}


object DkpDtstLnch {
	def main(args: Array[String]) {
		// setupLogging - not needed if we invoke Jena right away and accept its config.
		launchDataTstBatch
		launchDataTstWeb
		println("println: .main() says goodbye") // , but actors are possibly still running.")
	}
	private def setupLogging : Unit = {
		// Needed only if no other logging setup is provided.
		BasicConfigurator.configure()
		Log4JLogger.getRootLogger.setLevel(Log4JLevel.INFO)
	}
	private lazy val myDDApp = new DkpDtstApp()

	def launchDataTstBatch(): Unit = {
		myDDApp.runTstBatch
	}
	def launchDataTstWeb(): Unit = {
		val srvIntf = "localhost"
		val srvPort = 8117
		val actSysNm = "dkpdtst"
		myDDApp.launchTstWbsrvc(actSysNm, srvIntf, srvPort)
	}
}
import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, parameterMap, _}
class DkpDtstApp {
	val myAppLggr = LoggerFactory.getLogger(this.getClass)
	lazy val myShaclTst = new TstShaclRun {
		override protected def getSL4JLog: Logger = myAppLggr
	}

	def runTstBatch: Unit = {
		val rprt = myShaclTst.mkVldRprtTxt
		myAppLggr.info("Validation Report\n=====================\n : {}", rprt)
		myAppLggr.info("=====================\nChecking ont dat\n===================")

		myShaclTst.checkShaclOntDat
	}
	private lazy val myWsvcLnchr = new WebServerLauncher {}

	def launchTstWbsrvc(actSysNm : String, hostNm : String, portNum : Int) : Unit = {
		val wbSvcRt = mkWbSvcRt
		val actSys = myWsvcLnchr.makeActorSys(actSysNm)
		myWsvcLnchr.launchWebServer(wbSvcRt, actSys, hostNm, portNum)
	}
// launchWebServer(dmprRt, actSys, srvIntf, srvPort)

	private lazy val myShaclRts = new ShaclOpRts {
		override protected def getTstShaclRun: TstShaclRun = myShaclTst
	}

	private def mkWbSvcRt : dslServer.Route  = {
		myShaclRts.mkShaclTstRt
	}

}