package axmgc.web.pond

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives.{path, _}
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
	lazy val myLogger : Logger = LoggerFactory.getLogger(classOf[AxmgPndr])
	def makeActorSys (actSysNm : String) : ActorSystem = {
		val actrSys: ActorSystem = ActorSystem(actSysNm)
		actrSys
	}

	def launchWebServer (route: dslServer.Route, actSys : ActorSystem, srvIntf : String, portNum: Int) : Unit = {
		implicit val actrSys : ActorSystem = actSys
		implicit val actrMtrlzr = ActorMaterializer()

		val bindingFuture : ConcFut[Http.ServerBinding] =
					Http().bindAndHandle(route, srvIntf, portNum)
		myLogger.info("Server online at http://" + srvIntf + ":" + portNum)
		runUntilNewlineThenExit(actrSys, bindingFuture)
	}

	private def runUntilNewlineThenExit(actSys: ActorSystem, bindFut : ConcFut[Http.ServerBinding]) : Unit = {
		// needed for the future flatMap/onComplete in the end
		implicit val executionContext = actSys.dispatcher
		myLogger.info("Presssss RETURN to stop...")
		StdIn.readLine() // let it run until user presses return
		myLogger.info("Got user return, starting unbind")
		bindFut
				.flatMap(_.unbind()) // trigger unbinding from the port
				.onComplete(_ => actSys.terminate()) // and shutdown when done
		myLogger.info("We finished setting up the terminator, but it may not have completed yet.")
		myLogger.info("END of runUntilNewlineThenExit");
	}

}
class AxmgPndr(actSysName : String, srvHostIntf : String, srvPort : Int) {
	lazy val myLogger : Logger = LoggerFactory.getLogger(classOf[AxmgPndr])
	def doLnchSrvr : Unit = {
		val launcher = new WebServerLauncher {}

		val actrSys = launcher.makeActorSys(actSysName)
		val firstWepBoss : ActorRef = WbSssnStBossFactory.launchWebEventProc(actrSys, "frst01")
		myLogger.info("Launched boss actor: " + firstWepBoss)
		val routeMaker = new RouteMaker() {
			override protected def rmFindHlpActRef(sessID: Long): ActorRef = {
				myLogger.info("Returning boss actor:", firstWepBoss)
				firstWepBoss
			}
		}
		val route = routeMaker.makeComboRoute //makeRouteTree

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
