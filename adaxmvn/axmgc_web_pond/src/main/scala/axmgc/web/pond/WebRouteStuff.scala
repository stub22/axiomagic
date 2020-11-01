package axmgc.web.pond

import akka.actor.ActorRef
import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, _}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import axmgc.web.ent.{HtEntMkr, SupplyAndDemand, WebXmlGen}
import axmgc.web.ingest.{IngestRtMkr, WbEvtIngestor}
import axmgc.web.lnkdt.LDChunkerTest
import axmgc.web.rsrc.{WebRsrcFolders, WebRsrcRouteMkr}
import axmgc.web.srvevt.HttpEventSrcRtMkr
import axmgc.web.tuple.{IntrnlPonderRslt, WTRouteMaker, WebRqPrms, WebTupleMaker}
import org.slf4j.{Logger, LoggerFactory}

/*
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

import akka.pattern.ask
import akka.util.Timeout


// import akka.util.duration._

// import spray.json.{DefaultJsonProtocol
import spray.json._
import PersonJsonSupport._
*/
// type Route = RequestContext => Future[RouteResult]

/**
  * @author stub22
  * 2018-9 akka-route DSL test features
  */


trait WebRouteStuff

trait OurUrlPaths extends WebRsrcFolders {
	val pathEW = "ent-wrap"
	val pathPG = "pond-grid"
	val pathJsonPreDump = "json-pre-dump"
	val pathJsonLdMime = "json-ld-mime"
	val pathMore = "moreHere"
	val pathJsonPerson = "jpers"
	val pathJsonMoney = "jmoney"
	val pathJsonPortfolio = "jportf"
	val pathUseSource = "usrc"
	val pathCssT01 = "t01.css"
	val pathIngstTst = "ingst"
	val pgTplTst = "tpltst"
	val pathHttpEvtSrc = "evtSrcT01"

	val pathMenu = "axmenu"

	val lstAllPaths : List[String] = pathEW :: pathPG :: pathJsonPreDump :: pathJsonLdMime ::
			pathMore :: pathJsonPerson :: pathUseSource :: pathCssT01 :: pathIngstTst :: pgTplTst ::
			pathMenu :: Nil

	val xyz123 = "hey"
}


trait RouteWeaver extends  SprayJsonSupport with OurUrlPaths {

	protected lazy val myLDChnkr = new LDChunkerTest {}
	protected lazy val myHtEntMkr = new HtEntMkr {}
	protected lazy val myXEntMkr = new WebXmlGen {}
	protected val mySlf4JLog = LoggerFactory.getLogger(this.getClass)

	protected def rmFindHlpActRef(sessID: Long): ActorRef

	protected lazy val myWtplMkr = new WebTupleMaker {
		override protected def getLDChnkr: LDChunkerTest = myLDChnkr
		override protected def getHtEntMkr: HtEntMkr = myHtEntMkr
		override protected def getWebXml: WebXmlGen = myXEntMkr

		override protected def doPageWork(rqPrms: WebRqPrms): Option[IntrnlPonderRslt] = None
	}
	protected lazy val myWtRtMkr = new WTRouteMaker {
		override protected def getWbTplMkr: WebTupleMaker = myWtplMkr

		override protected def getPathTxt: String = pgTplTst
	}
	lazy val myWbActrXltr = new WbEvtIngestor {
		override def weiFindHlpActRef(sessID: Long): ActorRef = rmFindHlpActRef(sessID)
		override def getHtEntMkr: HtEntMkr = myHtEntMkr
	}
	lazy val myIngstRtMkr = new IngestRtMkr {
		override protected def getIngestor: WbEvtIngestor = myWbActrXltr

		override protected def getPathTxt: String = pathIngstTst
	}
	lazy val myWbRsrcRtMkr = new WebRsrcRouteMkr {}
	lazy val htEvtSrcRtMkr = new HttpEventSrcRtMkr {
		override protected def getPathTxt: String = pathHttpEvtSrc
	}

	lazy val myFTRtMkr = new FeatTstRtMkr {
		override protected def getHtEntMkr: HtEntMkr = myHtEntMkr

		override protected def getLDChunker: LDChunkerTest = myLDChnkr
	}

	private def makeHttpEvtSrcRt : dslServer.Route = {

		val evtSrcRt = htEvtSrcRtMkr.mkEvtSrcRt
		evtSrcRt
	}
	private def makeWbRscRt : dslServer.Route = {
		val wrRt = myWbRsrcRtMkr.makeWbRscRt(mySlf4JLog)
		wrRt
	}
	private def makeWTplRt : dslServer.Route = {
		val wtplRt = myWtRtMkr.makeWbTplRt(mySlf4JLog)
		wtplRt
	}
	private def mkLinkTxt(url: String, label: String) = s"""<a href="${url}">${label}</a>"""
	private def makeMenuRt : dslServer.Route = {
		val menuXhtmlBlock = s"""<div>
									<h2>Axiomagic Test Menu</h2>
									<ol>
			<li>Hey</li>
			<li>Wow</li>
			<li>${mkLinkTxt(pathEW, pathEW)}</li>
			<li>${mkLinkTxt(pathPG, pathPG)}</li>
			<li>${mkLinkTxt(pathJsonPreDump, pathJsonPreDump)}</li>
			<li>${mkLinkTxt(pathJsonLdMime, pathJsonLdMime)}</li>
			<li>${mkLinkTxt(pathMore, pathMore)}</li>
			<li>${mkLinkTxt(pathJsonPerson, pathJsonPerson)}</li>
			<li>${mkLinkTxt(pathJsonMoney, pathJsonMoney)}</li>
			<li>${mkLinkTxt(pathJsonPortfolio, pathJsonPortfolio)}</li>
			<li>${mkLinkTxt(pathUseSource, pathUseSource)}</li>
			<li>${mkLinkTxt(pathCssT01, pathCssT01)}</li>
			<li>${mkLinkTxt(pathIngstTst, pathIngstTst)}</li>
			<li>${mkLinkTxt(pgTplTst, pgTplTst)}</li>
			<li>${mkLinkTxt(pathHttpEvtSrc, pathHttpEvtSrc)}</li>
			<li>${mkLinkTxt("suppdmd", "suppdmd")}</li>
									</ol>
								</div>
							"""
		val htEntMkr = myHtEntMkr
		val pageEnt = htEntMkr.makeHtmlEntity(menuXhtmlBlock)
		path(pathMenu)	{
			complete(pageEnt)
		}
	}

	def mkSuppDmdRt(lgr : Logger) : dslServer.Route = {
		val suppDmdEx = new SupplyAndDemand {}
		val wrt = suppDmdEx.makeSuppDmdRt(lgr)
		lgr.info(s"Made suppDmdRt: ${wrt}")
		wrt.getRoute
	}

	def makeComboRoute : dslServer.Route = {
		val featTstRt = myFTRtMkr.makeFeatTstRoute
		val wbRscRt = makeWbRscRt
		val wtplRt = makeWTplRt
		val ingstRt = myIngstRtMkr.makeIngstRt(mySlf4JLog)
		val httpEvtSrcRt = makeHttpEvtSrcRt
		val suppDmdRt = mkSuppDmdRt(mySlf4JLog)
		val menuRt = makeMenuRt

		val comboRt = wbRscRt ~ wtplRt ~ featTstRt ~ ingstRt ~ httpEvtSrcRt ~ suppDmdRt ~ menuRt
		comboRt
	}




}
