package lbex.akhttp.urts

/*
Code in this package copied from this example and hacked slightly to fit under Axmgc:
https://github.com/akka/akka-http-quickstart-scala.g8/
Package "lbex.akhttp.urts" is public domain sample code, distributed under:
http://creativecommons.org/publicdomain/zero/1.0/.
 */

//#quick-start-server
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration.Duration
import scala.util.{ Failure, Success }

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

//#main-class
object QuickstartServer extends App with UserRoutes {

  // set up ActorSystem and other dependencies here
  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher
  //#server-bootstrapping

  val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistryActor")

  //#main-class
  // from the UserRoutes trait
  lazy val routes: Route = userRoutes
  //#main-class

  //#http-server
  val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, "localhost", 8153)

  serverBinding.onComplete {
    case Success(bound) =>
		val bndHostStrng = bound.localAddress.getHostString
		val bndPortNum = bound.localAddress.getPort
		println("host=" + bndHostStrng + ", port=" + bndPortNum)
    case Failure(e) =>
		Console.err.println(s"Server could not start!")
		e.printStackTrace()
		system.terminate()
  }

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
