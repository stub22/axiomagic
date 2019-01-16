package org.appdapter.axmgc.web.pond

import org.apache.jena.atlas.logging.LogCtl
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{path, _}
import akka.stream.ActorMaterializer

import scala.io.StdIn
/**
 * @author ${user.name}
 */
object AxmgcPonderApp {
	val pathA = "patha"
	val pathB = "pathb"
	val pathJsonPreDump = "json-pre-dump"
	val pathJsonLdMime = "json-ld-mime"
	val pathMore = "moreHere"

	def foo(x : Array[String]) = x.foldLeft("")((a,b) => a + b)

	def main(args : Array[String]) {
		println( "Hello World!" )
		println("concat arguments = " + foo(args))

		LogCtl.setLog4j

		import org.slf4j.LoggerFactory
		val logger = LoggerFactory.getLogger(classOf[App])
		logger.warn("logger warning whee")
		launchWebServer
	}
	val htmlCntType = ContentTypes.`text/html(UTF-8)`
	def launchWebServer : Unit = {
		implicit val system = ActorSystem("my-system")
		implicit val materializer = ActorMaterializer()
		// needed for the future flatMap/onComplete in the end
		implicit val executionContext = system.dispatcher

		val route =
	path(pathA) {
		get {
			val pageTxt = "<h1>Say hello to akka-http</h1>"
			complete(HttpEntity(htmlCntType, pageTxt))
		}
	} ~ // note tilde connects to next case
	path(pathB) {
		get {
		val dummyOld = "<h1>Say goodbye to akka-http</h1>"
		val muchBester = getSomeXhtml5()
		complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, muchBester ))
		}
	} ~ path(pathJsonPreDump) {
		val jsLdTxt = getSomeJsonLD()
		val htTxt = "<pre>" + jsLdTxt + "</pre>"
		val htEnt = makeHtmlEntity(htTxt)
		complete(htEnt)
	}  ~ path(pathJsonLdMime) {
	val x = getSomeJsonLD()
		// Note scala backticks, used to identify variables containing special chars
		complete(HttpEntity(ContentTypes.`application/json`, x ))
	}

	val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

	println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
	StdIn.readLine() // let it run until user presses return
	bindingFuture
		.flatMap(_.unbind()) // trigger unbinding from the port
		.onComplete(_ => system.terminate()) // and shutdown when done
	}
	def makeHtmlEntity (htmlTxt : String) : HttpEntity.Strict = {
		HttpEntity(ContentTypes.`text/html(UTF-8)`, htmlTxt)
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
