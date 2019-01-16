package axmgc.web.pond

import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, _}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.Source
import akka.util.ByteString
// import spray.json.{DefaultJsonProtocol
import spray.json._
import PersonJsonSupport._

trait RouteMaker extends  SprayJsonSupport {
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
			val jsLdTxt = myTdatChnkr.getSomeJsonLD()
			val htTxt = "<pre>" + jsLdTxt + "</pre>"
			val htEnt = myEntMkr.makeHtmlEntity(htTxt)
			complete(htEnt)
		} ~ path(pathJsonLdMime) {
			val jsonDat = myTdatChnkr.getSomeJsonLD()
			val jsonEnt = myEntMkr.makeJsonEntity(jsonDat)
			complete(jsonEnt)
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
