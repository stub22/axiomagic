package axmgc.web.pond

import axmgc.web.sssn.WbSssnStBossFactory

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
// import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.{Http, server => dslServer}
import akka.stream.ActorMaterializer

import scala.concurrent.{Future => ConcFut}
import scala.io.StdIn
import org.slf4j.{Logger, LoggerFactory}

/**
 * @author stub22
  * 2018-9 demo launcher code adapted from Akka Http doc examples.
  * See WebRoute for top-level app logic.
 */


trait WebServerLauncher {
	lazy val mySL4JLog : Logger = LoggerFactory.getLogger(this.getClass)
	def makeActorSys (actSysNm : String) : ActorSystem = {
		val actrSys: ActorSystem = ActorSystem(actSysNm)
		actrSys
	}

	def launchWebServer (route: dslServer.Route, actSys : ActorSystem, srvIntf : String,
						 portNum: Int, flg_blockUntilNewlineThenExit : Boolean = true) : Unit = {
		implicit val actrSys : ActorSystem = actSys
		implicit val actrMtrlzr = ActorMaterializer()

		val bindingFuture : ConcFut[Http.ServerBinding] =
					Http().bindAndHandle(route, srvIntf, portNum)
		mySL4JLog.info("Server online at http://" + srvIntf + ":" + portNum)
		if (flg_blockUntilNewlineThenExit) {
			mySL4JLog.info("DEV TEST MODE: launchWebServer() will block until console newline, then will exit!")
			runUntilNewlineThenExit(actrSys, bindingFuture)
		}
	}

	private def runUntilNewlineThenExit(actSys: ActorSystem, bindFut : ConcFut[Http.ServerBinding]) : Unit = {
		// needed for the future flatMap/onComplete in the end
		implicit val executionContext = actSys.dispatcher
		mySL4JLog.info("To stop program, press ENTER twice.  This thread will block until you do.")
		StdIn.readLine() // Block here until user presses return (sometimes twice helps).
		mySL4JLog.info("Got user ENTER, starting unbind")
		bindFut
				.flatMap(_.unbind()) // trigger unbinding from the port
				.onComplete(_ => actSys.terminate()) // and shutdown when done
		mySL4JLog.info("We finished setting up the terminator, but it may not have completed yet.")
		mySL4JLog.info("END of runUntilNewlineThenExit");
	}

}
class AxmgPndr(actSysName : String, srvHostIntf : String, srvPort : Int) {
	lazy val myLogger : Logger = LoggerFactory.getLogger(classOf[AxmgPndr])
	def doLnchSrvr : Unit = {
		val launcher = new WebServerLauncher {}

		val actrSys = launcher.makeActorSys(actSysName)
		val firstWepBoss : ActorRef = WbSssnStBossFactory.launchWebEventProc(actrSys, "frst01")
		myLogger.info("Launched boss actor: " + firstWepBoss)
		val routeWvr = new RouteWeaver() {
			override protected def rmFindHlpActRef(sessID: Long): ActorRef = {
				myLogger.info("Returning boss actor:", firstWepBoss)
				firstWepBoss
			}
		}
		val route: Route = routeWvr.makeComboRoute //makeRouteTree

		myLogger.info("The combo-route is made: " + route)

		launcher.launchWebServer(route, actrSys, srvHostIntf, srvPort)
	}
}
object AxmgcPonderApp {

	def foo(x: Array[String]) = x.foldLeft("")((a, b) => a + b)

	lazy val myLogger : Logger = LoggerFactory.getLogger(classOf[AxmgPndr])
	private def setupLogging : Unit = {
		// Part of our logging config setup is going through Jena, so far.
		import org.apache.jena.atlas.logging.LogCtl
		LogCtl.setLog4j
		// 2019-02-05 added a log4j.properties in our resources root.
	}
	def main(args: Array[String]) {
		println("println: hello, our args foo-fold to: " + foo(args))

		setupLogging
		myLogger.warn("Logger practice warning: Whee!")

		val actSysName = "my-sys"
		val srvIntf = "localhost"
		val srvPort = 8095

		val axPndr = new AxmgPndr(actSysName, srvIntf, srvPort)

		axPndr.doLnchSrvr

		myLogger.warn("END of .main()")
		println("println: goodbye")
	}

}
