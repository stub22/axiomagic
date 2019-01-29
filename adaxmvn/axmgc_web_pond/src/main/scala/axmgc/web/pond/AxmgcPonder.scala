package axmgc.web.pond

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.{Http, server => dslServer}
import akka.stream.ActorMaterializer
import org.apache.jena.atlas.logging.LogCtl

import scala.concurrent.{Future => ConcFut}
import scala.io.StdIn

/**
 * @author stub22
  * 2018-9 demo launcher code adapted from Akka Http doc examples.
  * See WebRoute for top-level app logic.
 */


trait WebServerLauncher {
	def launchWebServer (route: dslServer.Route, actSysNm : String, srvIntf : String, portNum: Int) : Unit = {
		implicit val actrSys : ActorSystem = ActorSystem(actSysNm)
		implicit val actrMtrlzr = ActorMaterializer()

		val bindingFuture : ConcFut[Http.ServerBinding] =
					Http().bindAndHandle(route, srvIntf, portNum)
		println("Server online at http://" + srvIntf + ":" + portNum)
		runUntilNewlineThenExit(actrSys, bindingFuture)
	}

	private def runUntilNewlineThenExit(actSys: ActorSystem, bindFut : ConcFut[Http.ServerBinding]) : Unit = {
		// needed for the future flatMap/onComplete in the end
		implicit val executionContext = actSys.dispatcher
		println("Presssss RETURN to stop...")
		StdIn.readLine() // let it run until user presses return
		println("Got user return, starting unbind")
		bindFut
				.flatMap(_.unbind()) // trigger unbinding from the port
				.onComplete(_ => actSys.terminate()) // and shutdown when done
		println("We finished setting up the terminator, but it may not have completed yet.")
		println("END of runUntilNewlineThenExit");
	}

}
object AxmgcPonderApp {

	def foo(x: Array[String]) = x.foldLeft("")((a, b) => a + b)

	def main(args: Array[String]) {
		println("Hello World!")
		println("concat arguments = " + foo(args))

		LogCtl.setLog4j

		import org.slf4j.LoggerFactory
		val logger = LoggerFactory.getLogger(classOf[App])
		logger.warn("logger warning whee")
		val routeMaker = new RouteMaker {}
		val route = routeMaker.makeRouteTree

		val actSysName = "my-sys"
		val srvIntf = "localhost"
		val srvPort = 8095
		val launcher = new WebServerLauncher {}
		launcher.launchWebServer(route, actSysName, srvIntf, srvPort)
		println("END of .main()");
	}

}
