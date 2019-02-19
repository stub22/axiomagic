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

trait  WebTuple

trait WebRqPrms {
	protected def fetchParamMap : Map[String, String]
	lazy private val myParamMap = fetchParamMap
	private def getPValTxtOpt (keyName : String) :  Option[String] = myParamMap.get(keyName)

	def getPathParam (keyName : String) : Option[String] = {
		getPValTxtOpt(keyName)
	}
	def getPVal_UriTxt (keyName : String) : String = ???
	def getPVal_QNameTxt (keyName : String) : String = ???
	def getPVal_ScalaInt (keyName : String) : Int = ???
}
trait IntrnlPonderRslt {
	// protected def getRqPrms : WebRqPrms
	def dumpAsTxt : String = "$$$ dumped IPR $$$"
}
trait WebTupleMaker extends HtEntMkr {

	protected def getTdatChnkr : TdatChunker
	protected def getHtEntMkr : HtEntMkr
	protected def getWebXml : WebXml

	protected def doPageWork(rqPrms : WebRqPrms) : Option[IntrnlPonderRslt]

	// Option to chain Ctx-Tpl-Ctx-Tpl... means we must be prudent and chop to avoid hogging RAM.

	// Output from page-tuple calc.
	// These 3 optional ents each represent a separate, nestable, client-fetchable key-value map.
	// (XML, CSS, JSON) each match that conceptual description.
	// The multiformat aggregation into this tuple indicates the parts are meant to be consistent
	// for a client that fetches them in either unified or separate HTTP request operations.
	// The opt_inCtx allows us to memoize the prior-state and input-req history, if desired.
	// (Not yet proven to be practically important, but may be an important tie-in point soon).
	case class PgEntTpl(xhEnt_opt : Option[HEStrict], cssEnt_opt : Option[HEStrict],
						jsonEnt_opt : Option[HEStrict], opt_inCtx : Option[PgEvalCtx])

	// Inputs to the page-tuple calculation.
	case class PgEvalCtx(ptxt_id : String, strtLocMsec : Long, wrqPrms :WebRqPrms, opt_prvSssnTpl : Option[PgEntTpl])

	// ptxt_id  contains    client sender block's Html-Dom ID, e.g. div@id.onclick
	def makeEntsForPgAcc(ptxt_id : String, wrqPrms : WebRqPrms) : PgEntTpl = {
		val localMsec: Long = System.currentTimeMillis()
		val pgEvalCtx = PgEvalCtx(ptxt_id, localMsec, wrqPrms, None)
		evalFullPageNow(pgEvalCtx, false)
	}
	def evalFullPageNow(pgEvalCtx : PgEvalCtx, chainBk : Boolean = false) : PgEntTpl = {
		val wrqPrms : WebRqPrms = pgEvalCtx.wrqPrms
		val intrnlRslt = new IntrnlPonderRslt {}
		// So far this is a mere simulation of making all required page elements in one swoop.
		val xentMkr = getWebXml // XEntMkr
		val outEnt = xentMkr.getXHPageEnt(Some(intrnlRslt))
		val xhPgEnt_opt = Some(outEnt)
		val htEntMkr = getHtEntMkr
		val cssPgEnt_opt = Some(htEntMkr.makeDummyCssEnt())
		val jsnPgEnt_opt = Some(mkJsonTstEnt)
		val chnBkCtx_opt = if (chainBk) Some (pgEvalCtx) else None
		PgEntTpl(xhPgEnt_opt, cssPgEnt_opt, jsnPgEnt_opt, chnBkCtx_opt)
	}
	def pgTplXml(ptxt_id : String, rqParamMap: Map[String, String]): HEStrict = {
		val wrqParams = new WebRqPrms {
			override protected def fetchParamMap: Map[String, String] = rqParamMap
		}
		val pet = makeEntsForPgAcc(ptxt_id, wrqParams)
		pet.xhEnt_opt.get
	}
	def mkJsonTstEnt: HEStrict = {
		val tdatChnkr = getTdatChnkr
		val jsonDat = tdatChnkr.getSomeJsonLD(true)
		val htEntMkr = getHtEntMkr
		val jsonEnt = htEntMkr.makeJsonEntity(jsonDat)
		jsonEnt
	}

}
trait WTRouteMaker extends OurUrlPaths {
	protected def getWbTplMkr : WebTupleMaker
	def makeWbTplRt (lgr : Logger) : dslServer.Route = {
		val wbTplMkr = getWbTplMkr

		val tplRt = parameterMap { rqParams : Map[String, String] => path(pgTplTst) {
			val pttXml = wbTplMkr.pgTplXml("pg_tpl_tst_id", rqParams)
			complete(pttXml)
		}} // ~
		tplRt
	}
}
