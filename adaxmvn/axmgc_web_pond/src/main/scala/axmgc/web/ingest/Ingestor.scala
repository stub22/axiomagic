package axmgc.web.ingest

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives.{complete, path, _}
import akka.http.scaladsl.{server => dslServer}
import akka.util.Timeout
import axmgc.web.ent.HtEntMkr
import axmgc.web.pond.OurUrlPaths
import axmgc.web.sssn.{WE_DomClick, WE_Empty, WebEvent}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.Future
import scala.util.{Failure, Success}

// https://stackoverflow.com/questions/37462717/akka-http-how-to-use-an-actor-in-a-request

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


trait IngestRtMkr extends OurUrlPaths  {
	protected def getIngestor : WbEvtIngestor
	def makeIngstRt (lgr : Logger) : dslServer.Route = {
		val ingstr = getIngestor
		val htEntMkr = ingstr.getHtEntMkr
		val wevIngRt =
			path(pathIngstTst) {
				// val lgr = getLogger
				val pretendSessID = -99L
				val actRef = ingstr.weiFindHlpActRef(pretendSessID)
				val emptyEvt = WE_Empty() // parens distinguish apply-instance from hidden case-singleton
				val dclkEvt = WE_DomClick("clkdDomID_tst_99")
				implicit val timeout = Timeout(2.seconds)
				lgr.info("Sending ask, creating future")
				// Ask pattern automatically creates a temp reply actor for us.
				val askFut: Future[Any] = actRef.ask(emptyEvt)
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

