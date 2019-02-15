package axmgc.xpr.dkp_shacl

import axmgc.dmo.fin.ontdmp.TstOntDmps.printSomeMsgs
import axmgc.web.pond.WebServerLauncher
import org.slf4j.{Logger, LoggerFactory}
import org.apache.log4j.{BasicConfigurator, Level => Log4JLevel, Logger => Log4JLogger}


object DkpDtstLnch {
	def main(args: Array[String]) {
		// setupLogging - not needed if we invoke Jena right away and accept its config.
		launchDataTstBatch
		println("println: .main() says goodbye") // , but actors are possibly still running.")
	}
	private def setupLogging : Unit = {
		// Needed only if no other logging setup is provided.
		BasicConfigurator.configure()
		Log4JLogger.getRootLogger.setLevel(Log4JLevel.INFO)
	}
	def launchDataTstBatch(): Unit = {
		val ddApp = new DkpDtstApp()
		ddApp.runTstBatch
	}
	def launchDataTstWeb(): Unit = {
		val srvIntf = "localhost"
		val srvPort = 8117
	}
}

class DkpDtstApp {
	val myAppLggr = LoggerFactory.getLogger(this.getClass)
	lazy val myShaclTst = new TstShaclRun {
		override protected def getSL4JLog: Logger = myAppLggr
	}
	def runTstBatch : Unit = {
		val rprt = myShaclTst.mkVldRprtTxt
		myAppLggr.info("Validation Report\n=====================\n : {}", rprt)
	}
}
