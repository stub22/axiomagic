package axmgc.web.pond

import akka.actor.ActorRef
import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, _}
import dslServer.Directive0
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import axmgc.web.cors.CORSHandler
import axmgc.web.ent.HtEntMkr
import org.slf4j.{Logger, LoggerFactory}

/*
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.pattern.ask
import akka.util.Timeout
*/
trait WebFeatTest

trait FeatTstRtMkr extends CORSHandler  with OurUrlPaths {
	protected def getHtEntMkr : HtEntMkr
	protected def getTdatChnkr: TdatChunker
	// Remember, the "whens" of route exec are cmplx!
	def makeFeatTstRoute: dslServer.Route = {
		val htEntMkr = getHtEntMkr
		val tdatChnkr = getTdatChnkr
		val mainRt = parameterMap { paramMap: Map[String, String] =>
			path(pathA) {
				get {
					val pageTxt = "<h1> HTML string, wrapped in akka-http Entity</h1>"
					val pageEnt = htEntMkr.makeHtmlEntity(pageTxt)
					complete(pageEnt)
				}
			} ~ path(pathB) { // note tilde connects to next alternate route
				get {
					val dummyOld = "<h1>Say goooooodbye to akka-http</h1>"
					val muchBesterTxt = tdatChnkr.getSomeXhtml5()
					val muchBesterEnt = htEntMkr.makeHtmlEntity(muchBesterTxt)
					complete(muchBesterEnt) // HttpEntity(ContentTypes.`text/html(UTF-8)`, muchBesterTxt ))
				}
			} ~ path(pathJsonPreDump) {
				val jsLdTxt = tdatChnkr.getSomeJsonLD(true)
				val htTxt = "<pre>" + jsLdTxt + "</pre>"
				val htEnt = htEntMkr.makeHtmlEntity(htTxt)
				complete(htEnt)


			} ~ path(pathJsonLdMime) {
				val jsonDat = tdatChnkr.getSomeJsonLD(true)
				val jsonEnt = htEntMkr.makeJsonEntity(jsonDat)
				corsHandler(complete(jsonEnt))
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
				val chnkdEnt = htEntMkr.makeChunked(streamingData)
				val resp = HttpResponse(entity = chnkdEnt)
				println("This usrc response gets constructed NOW!", resp)
				complete(resp)
			} ~ path(pathCssT01) {
				println("Running the route of the css request, params=", paramMap)
				complete {
					println("Completing css request, params=", paramMap)
					val cssEnt = htEntMkr.makeDummyCssEnt()
					cssEnt
				}
			} // ~
		}
		mainRt
	}
}

