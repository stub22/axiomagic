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
import org.slf4j.{Logger, LoggerFactory}


import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}


import akka.pattern.ask
import akka.util.Timeout

// https://stackoverflow.com/questions/37462717/akka-http-how-to-use-an-actor-in-a-request

// import akka.util.duration._

// import spray.json.{DefaultJsonProtocol
import spray.json._
import PersonJsonSupport._
// type Route = RequestContext => Future[RouteResult]

/**
  * @author stub22
  * 2018-9 akka-route DSL test features
  */


trait WebRoute

trait HelpAble {
	lazy val myLogger : Logger = LoggerFactory.getLogger(this.getClass)
	protected def getLogger : Logger = myLogger
	protected def findHelpActRef(sessID : Long) : ActorRef

	def sendWebEvt (tgtRef: ActorRef, sndrRef : ActorRef, wbEvt : WebEvent): Unit = {
		tgtRef.tell(wbEvt, sndrRef)
	}
	def sendEmptyWebEvt(sessID: Long, sndrRef : ActorRef) : Unit = {
		val tgtRef = findHelpActRef(sessID)
		val emptyEvt = WE_Empty
		// val future = tgtRef ? emptyEvt

		tgtRef.tell(emptyEvt, sndrRef)
	}
}
trait RouteMaker extends  SprayJsonSupport with CORSHandler with HelpAble {
	val pathA = "patha"
	val pathB = "pathb"
	val pathJsonPreDump = "json-pre-dump"
	val pathJsonLdMime = "json-ld-mime"
	val pathMore = "moreHere"
	val pathJsonPerson = "jpers"
	val pathUseSource = "usrc"
	val pathCssT01 = "t01.css"
	val pathSssnTst = "sssntst"

	lazy val myTdatChnkr = new TdatChunker {}
	lazy val myEntMkr = new HtEntMkr {}
	lazy val myXEntMkr = new WebXml {}


	def mkJsonTstEnt: HEStrict = {
		val jsonDat = myTdatChnkr.getSomeJsonLD(true)
		val jsonEnt = myEntMkr.makeJsonEntity(jsonDat)
		jsonEnt
	}
	// Option to chain Ctx-Tpl-Ctx-Tpl... means we must be prudent and chop to avoid hogging RAM.

	// Output from page-tuple calc.  These 3 ents all represent nestable key-value maps.
	case class PgEntTpl(xhEnt : HEStrict, cssEnt : HEStrict, jsonEnt : HEStrict, opt_inCtx : Option[PgEvalCtx])

	// Inputs to the page-tuple calculation.
	case class PgEvalCtx(ptxt_id : String, strtLocMsec : Long, opt_prvSssnTpl : Option[PgEntTpl])

	// ptxt_id  contains    client sender block's Html-Dom ID, e.g. div@id.onclick
	def makeEntsForPgAcc(ptxt_id : String) : PgEntTpl = {
		val localMsec: Long = System.currentTimeMillis()
		val pgEvalCtx = PgEvalCtx(ptxt_id, localMsec, None)
		evalPage(pgEvalCtx, false)
	}
	def evalPage(pgEvalCtx : PgEvalCtx, chainBk : Boolean = false) : PgEntTpl = {
		val xhPgEnt = myXEntMkr.getXHPageEnt
		val cssPgEnt = myEntMkr.makeDummyCssEnt()
		val jsnPgEnt = mkJsonTstEnt
		PgEntTpl(xhPgEnt, cssPgEnt, jsnPgEnt, if (chainBk) Some (pgEvalCtx) else None)
	}
	// Remember, the "whens" of route exec are cmplx!
	def makeRouteTree: dslServer.Route = {
		parameterMap { paramMap =>
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
				println("This usrc response gets constructed NOW!", resp)
				complete(resp)
			}  ~ path(pathCssT01) {
				println ("Running the route of the css request, params=", paramMap)
				complete {
					println("Completing css request, params=", paramMap)
					val cssEnt = myEntMkr.makeDummyCssEnt()
					cssEnt
				}
			} ~ path(pathSssnTst) {
				val lgr = getLogger
				val pretendSessID = -99L
				val actRef = findHelpActRef(pretendSessID)
				val emptyEvt = WE_Empty() // parens distinguish apply-instance from hidden case-singleton
				val dclkEvt = WE_DomClick("clkdDomID_tst_99")
				implicit val timeout = Timeout(2.seconds)
				lgr.info("Sending ask, creating future")
				// Ask pattern automatically creates a temp reply actor for us.
				val askFut = actRef.ask(emptyEvt)
				// Here "onComplete" is an akka-http directive (not the same-named method of the future),
				// which creates a route.
				onComplete(askFut) {
					case Success(r) => {
						val rsltTxt = "<h2>ans=[" + r.toString + "]</h2>"
						val rsltEnt = myEntMkr.makeHtmlEntity(rsltTxt)
						complete(rsltEnt)
					}
					case Failure(e) => {
						val failTxt = "<h2>err=[" + e.toString + "]</h2>"
						val failEnt = myEntMkr.makeHtmlEntity(failTxt)
						complete(failEnt)
					}
				}
				/*
				val rsltFut = askFut.map {

				}
				askFut.onComplete {
					case WA_Summary(sumTxt) => {
						complete {
							lgr.info("Received summary text back: " + sumTxt)
							val rsltTxt = "<h2>ansSum=" + sumTxt + "</h2>"
							val trEnt = myEntMkr.makeHtmlEntity(rsltTxt)
						}
					}
					case other => {

					}
				}
				// val usrFut: Future[Users] = (userRegistryActor ? qryMsg).mapTo[Users]
				// sendEmptyWebEvt(pretendSessID, self)
				*/
			}
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