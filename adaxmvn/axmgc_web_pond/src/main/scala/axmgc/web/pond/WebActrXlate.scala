package axmgc.web.pond

import akka.actor.ActorRef
import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, _}
import dslServer.Directive0
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}
import akka.stream.scaladsl.Source
import akka.util.{ByteString, Timeout}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success}

trait WebActrXlate

trait WbEvtIngestor {
	lazy val myLogSlf4J : Logger = LoggerFactory.getLogger(this.getClass)
	protected def getLogger : Logger = myLogSlf4J
	def weiFindHlpActRef(sessID : Long) : ActorRef
	def getHtEntMkr : HtEntMkr

	def ingestWebEvt(tgtRef: ActorRef, sndrRef : ActorRef, wbEvt : WebEvent): Unit = {
		tgtRef.tell(wbEvt, sndrRef)
	}
	def ingestEmptyWebEvt(sessID: Long, sndrRef : ActorRef) : Unit = {
		val tgtRef = weiFindHlpActRef(sessID)
		val emptyEvt = WE_Empty
		// val future = tgtRef ? emptyEvt

		tgtRef.tell(emptyEvt, sndrRef)
	}

}

import akka.pattern.ask
import scala.concurrent.duration._


trait WbEvtRtMkr extends OurUrlPaths  {
	protected def getIngestor : WbEvtIngestor
	def makeWbTplRt (lgr : Logger) : dslServer.Route = {
		val ingstr = getIngestor
		val htEntMkr = ingstr.getHtEntMkr
		val wevIngRt =
			path(pathSssnTst + "_xp") {
				// val lgr = getLogger
				val pretendSessID = -99L
				val actRef = ingstr.weiFindHlpActRef(pretendSessID)
				val emptyEvt = WE_Empty() // parens distinguish apply-instance from hidden case-singleton
				val dclkEvt = WE_DomClick("clkdDomID_tst_99")
				implicit val timeout = Timeout(2.seconds)
				lgr.info("Sending ask, creating future")
				// Ask pattern automatically creates a temp reply actor for us.
				val askFut = actRef.ask(emptyEvt)
				// Here "onComplete" is an akka-http directive (not the same-named method of the future),
				// which creates a route.
				// https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/future-directives/onComplete.html
				onComplete(askFut) {
					case Success(r) => {
						val rsltTxt = "<h2>ans=[" + r.toString + "]</h2>"
						val rsltEnt = htEntMkr.makeHtmlEntity(rsltTxt)
						complete(rsltEnt)
					}
					case Failure(e) => {
						val failTxt = "<h2>err=[" + e.toString + "]</h2>"
						val failEnt = htEntMkr.makeHtmlEntity(failTxt)
						complete(failEnt)
					}
				}
			} // ~
		wevIngRt
	}
}

