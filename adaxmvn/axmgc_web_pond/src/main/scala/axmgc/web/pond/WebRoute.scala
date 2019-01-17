package axmgc.web.pond

import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, _}
import dslServer.Directive0
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.scaladsl.Source
import akka.util.ByteString
// import spray.json.{DefaultJsonProtocol
import spray.json._
import PersonJsonSupport._

// type Route = RequestContext => Future[RouteResult]

trait RouteMaker extends  SprayJsonSupport with CORSHandler  {
	val pathA = "patha"
	val pathB = "pathb"
	val pathJsonPreDump = "json-pre-dump"
	val pathJsonLdMime = "json-ld-mime"
	val pathMore = "moreHere"
	val pathJsonPerson = "jpers"
	val pathUseSource = "usrc"

	lazy val myTdatChnkr = new TdatChunker {}
	lazy val myEntMkr = new HtEntMkr {}
	def makeRouteTree: dslServer.Route = {
		path(pathA) {
			get {
				val pageTxt = "<h1>Say hello to akka-http</h1>"
				val pageEnt = myEntMkr.makeHtmlEntity(pageTxt)
				complete(pageEnt)
			}
		} ~ path(pathB) { // note tilde connects to next alternate route
			get {
				val dummyOld = "<h1>Say goooooodbye to akka-http</h1>"
				val muchBesterTxt = myTdatChnkr.getSomeXhtml5()
				val muchBesterEnt = myEntMkr.makeHtmlEntity(muchBesterTxt)
				complete(muchBesterEnt) // HttpEntity(ContentTypes.`text/html(UTF-8)`, muchBesterTxt ))
			}
		} ~ path(pathJsonPreDump) {
			val jsLdTxt = myTdatChnkr.getSomeJsonLD(true)
			val htTxt = "<pre>" + jsLdTxt + "</pre>"
			val htEnt = myEntMkr.makeHtmlEntity(htTxt)
			complete(htEnt)
		} ~ path(pathJsonLdMime) {
			val jsonDat = myTdatChnkr.getSomeJsonLD(true)
			val jsonEnt = myEntMkr.makeJsonEntity(jsonDat)
			corsHandler (complete(jsonEnt))
		} ~ path(pathJsonPerson) {
			complete("nope")
/*
			entity(as[Person]) { prsn => {
				val msg = s"Person: ${prsn.name} - favorite number: ${prsn.favoriteNumber}"
				println("person = " + prsn)
				complete(msg)
			}}
*/
		} ~ path(pathUseSource) {
			val streamingData = Source.repeat("hello \n").take(10).map(ByteString(_))
			// render the response in streaming fashion:
			val chnkdEnt = myEntMkr.makeChunked(streamingData)
			val resp = HttpResponse(entity = chnkdEnt)
			complete(resp)
		}
	}
}

// https://dzone.com/articles/handling-cors-in-akka-http

import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.HttpMethods._

trait CORSHandler{
	private val corsResponseHeaders = List(
		`Access-Control-Allow-Origin`.*,
		`Access-Control-Allow-Credentials`(true),
		`Access-Control-Allow-Headers`("Authorization",
			"Content-Type", "X-Requested-With")
	)
	//this directive adds access control headers to normal responses
	private def addAccessControlHeaders: Directive0 = {
		respondWithHeaders(corsResponseHeaders)
	}
	//this handles preflight OPTIONS requests.
	private def preflightRequestHandler: dslServer.Route = options {
		complete(HttpResponse(StatusCodes.OK).
				withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
	}
	// Wrap the Route with this method to enable adding of CORS headers
	def corsHandler(r: dslServer.Route): dslServer.Route = addAccessControlHeaders {
		preflightRequestHandler ~ r
	}
	// Helper method to add CORS headers to HttpResponse
	// preventing duplication of CORS headers across code
	def addCORSHeaders(response: HttpResponse):HttpResponse =
		response.withHeaders(corsResponseHeaders)
}