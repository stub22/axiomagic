package axmgc.web.pond

import akka.NotUsed
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
import axmgc.web.ent.{HtEntMkr, PorfolioRtMkr}
import axmgc.web.json.{MoneyRtMkr, Person, PersonRouteMkr}
import axmgc.web.lnkdt.LDChunkerTest
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
	protected def getLDChunker: LDChunkerTest
	// Remember, the "whens" of route exec are cmplx!
	def makeFeatTstRoute: dslServer.Route = {
		val htEntMkr = getHtEntMkr
		val ldChnkr = getLDChunker
		val pGrldr = new PondGriddler {}
		val personRtMkr = new PersonRouteMkr {}
		val moneyRtMkr = new MoneyRtMkr {}
		val portfRtMkr = new PorfolioRtMkr{}

		val mainRt = parameterMap { paramMap: Map[String, String] =>
			path(pathEW) {
				get {
					val pageTxt = "<h1> HTML string, wrapped in akka-http Entity</h1><br/>" +
							s"<h2>Defined in ${this}.makeFeatTstRoute</h2>"
					val pageEnt = htEntMkr.makeHtmlEntity(pageTxt)
					complete(pageEnt)
				}
			} ~ path(pathPG) { // note tilde connects to next alternate route
				get {
					// val dummyOld = "<h1>Say goooooodbye to akka-http</h1>"
					val pgMsg = pGrldr.toString
					val bonusMsg = "FeatTstRtMkr.makeFeatTstRoute calling: " + pgMsg
					val muchBesterTxt = pGrldr.getSomeXhtml5(bonusMsg)
					val muchBesterEnt = htEntMkr.makeHtmlEntity(muchBesterTxt)
					complete(muchBesterEnt) // HttpEntity(ContentTypes.`text/html(UTF-8)`, muchBesterTxt ))
				}
			} ~ path(pathJsonPreDump) {
				val hdrTxt = s"<h2>${ldChnkr.toString}.getSomeJsonLD yields:</h2><br/>"
				val jsLdTxt = ldChnkr.getSomeJsonLD(true)
				val ldDumpTxt = "<pre>" + jsLdTxt + "</pre>"
				val htEnt = htEntMkr.makeHtmlEntity(hdrTxt + ldDumpTxt)
				complete(htEnt)

			} ~ path(pathJsonLdMime) {
				val jsonDat = ldChnkr.getSomeJsonLD(true)
				val jsonEnt = htEntMkr.makeJsonEntity(jsonDat)
				corsHandler(complete(jsonEnt))
			} ~ path(pathJsonPerson) {
				personRtMkr.mkPersonReceiverRt
			} ~ path(pathJsonMoney) {
				corsHandler(moneyRtMkr.mkMoneySenderRt(htEntMkr))
			} ~ path(pathJsonPortfolio) {
				corsHandler(portfRtMkr.mkPortfolioSenderRt(htEntMkr))
			} ~ path(pathUseSource) {
				val streamingData: Source[ByteString, NotUsed] = Source.repeat("hello \n").take(10).map(ByteString(_))
				// render the response in streaming fashion:
				val chnkdEnt = htEntMkr.makeChunked(streamingData)
				val resp = HttpResponse(entity
						= chnkdEnt)
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

