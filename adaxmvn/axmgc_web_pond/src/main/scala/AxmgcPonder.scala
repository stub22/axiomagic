package org.appdapter.axmgc.web.pond

import org.apache.jena.atlas.logging.LogCtl
import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, server => dslServer}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{path, _}
import akka.stream.ActorMaterializer
import scala.concurrent.{Future => ConcFut}
import scala.io.StdIn
/**
 * @author stub22
 */

trait RouteMaker {
	val pathA = "patha"
	val pathB = "pathb"
	val pathJsonPreDump = "json-pre-dump"
	val pathJsonLdMime = "json-ld-mime"
	val pathMore = "moreHere"

	val htmlCntType = ContentTypes.`text/html(UTF-8)`
	val jsonCntType = ContentTypes.`application/json`

	def makeRouteTree: dslServer.Route = {
		path(pathA) {
			get {
				val pageTxt = "<h1>Say hello to akka-http</h1>"
				val pageEnt = makeHtmlEntity(pageTxt)
				complete(pageEnt)
			}
		} ~ // note tilde connects to next alternate route
				path(pathB) {
					get {
						val dummyOld = "<h1>Say goodbye to akka-http</h1>"
						val muchBesterTxt = getSomeXhtml5()
						val muchBesterEnt = makeHtmlEntity(muchBesterTxt)
						complete(muchBesterEnt) // HttpEntity(ContentTypes.`text/html(UTF-8)`, muchBesterTxt ))
					}
				} ~ path(pathJsonPreDump) {
			val jsLdTxt = getSomeJsonLD()
			val htTxt = "<pre>" + jsLdTxt + "</pre>"
			val htEnt = makeHtmlEntity(htTxt)
			complete(htEnt)
		} ~ path(pathJsonLdMime) {
			val jsonDat = getSomeJsonLD()
			// Note scala backticks, used to identify variables containing special chars
			complete(HttpEntity(jsonCntType, jsonDat))
		}
	}
	def makeHtmlEntity (htmlTxt : String) : HttpEntity.Strict = {
		HttpEntity(htmlCntType, htmlTxt)
	}
	def makeJsonEntity (jsonTxt : String) : HttpEntity.Strict = {
		HttpEntity(jsonCntType, jsonTxt)
	}

	def getSomeJsonLD() : String = {
		val sds = new SomeDataStuff()
		val mdl = sds.loadThatModel()
		val mdmp = mdl.toString
		System.out.println("Loaded: " + mdmp)
		val jldTxt = sds.writeModelToJsonLDString_Pretty(mdl)
		System.out.println("Formatted: " + jldTxt)
		jldTxt
	}

	def getSomeXhtml5() : String = {
		val banner : String = "<h3>Much Bester Down Here</h3>"
		val gridMkr = new PondGrid {}
		// val pshwrs = gridMkr.
		val rui = new RectUiFuncs {}
		val (pshwrA, pshwrB) = (new PondShower {},new PondShower {})
		val pList : List[PondShower] = List(pshwrA, pshwrB)
		val pondShowerDump = rui.makePondDataDump(rui.OF_JSON, pList)

		pondShowerDump
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
		val srvPort = 8080
		launchWebServer(route, actSysName, srvIntf, srvPort)
	}

	def launchWebServer (route: dslServer.Route, actSysNm : String, srvIntf : String, portNum: Int) : Unit = {
		implicit val actrSys : ActorSystem = ActorSystem("my-system")
		implicit val actrMtrlzr = ActorMaterializer()

		val bindingFuture : ConcFut[Http.ServerBinding]
				= Http().bindAndHandle(route, srvIntf, portNum)
		println("Server online at http://" + srvIntf + "/" + portNum)
		runUntilNewlineThenExit(actrSys, bindingFuture)
	}

	private def runUntilNewlineThenExit(actSys: ActorSystem, bindFut : ConcFut[Http.ServerBinding]) : Unit = {
		// needed for the future flatMap/onComplete in the end
		implicit val executionContext = actSys.dispatcher
		println("Press RETURN to stop...")
		StdIn.readLine() // let it run until user presses return
		bindFut
				.flatMap(_.unbind()) // trigger unbinding from the port
				.onComplete(_ => actSys.terminate()) // and shutdown when done

	}

}
