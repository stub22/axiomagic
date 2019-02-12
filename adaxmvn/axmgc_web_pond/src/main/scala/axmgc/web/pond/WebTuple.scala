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

trait WebTupleMaker extends HtEntMkr {
	// protected def getXEntMkr : XmlEntMkr
	protected def getTdatChnkr : TdatChunker
	protected def getHtEntMkr : HtEntMkr
	protected def getWebXml : WebXml
	// Option to chain Ctx-Tpl-Ctx-Tpl... means we must be prudent and chop to avoid hogging RAM.

	// Output from page-tuple calc.
	// These 3 optional ents each represent a nestabl, client-fetchable key-value map.
	// Their collection into this tuple indicates that they are meant to be consistent
	// for a client that fetches them in either unified or separate HTTP request operations.
	// The opt_inCtx allows us to memoize the prior-state and input-req.
	case class PgEntTpl(xhEnt_opt : Option[HEStrict], cssEnt_opt : Option[HEStrict],
						jsonEnt_opt : Option[HEStrict], opt_inCtx : Option[PgEvalCtx])

	// Inputs to the page-tuple calculation.
	case class PgEvalCtx(ptxt_id : String, strtLocMsec : Long, opt_prvSssnTpl : Option[PgEntTpl])

	// ptxt_id  contains    client sender block's Html-Dom ID, e.g. div@id.onclick
	def makeEntsForPgAcc(ptxt_id : String) : PgEntTpl = {
		val localMsec: Long = System.currentTimeMillis()
		val pgEvalCtx = PgEvalCtx(ptxt_id, localMsec, None)
		evalPage(pgEvalCtx, false)
	}
	def evalPage(pgEvalCtx : PgEvalCtx, chainBk : Boolean = false) : PgEntTpl = {
		// So far this is a mere simulation of making all required page elements in one swoop.
		val xentMkr = getWebXml // XEntMkr
		val xhPgEnt_opt = Some(xentMkr.getXHPageEnt)
		val htEntMkr = getHtEntMkr
		val cssPgEnt_opt = Some(htEntMkr.makeDummyCssEnt())
		val jsnPgEnt_opt = Some(mkJsonTstEnt)
		val chnBkCtx_opt = if (chainBk) Some (pgEvalCtx) else None
		PgEntTpl(xhPgEnt_opt, cssPgEnt_opt, jsnPgEnt_opt, chnBkCtx_opt)
	}
	def pgTplXml(ptxt_id : String): HEStrict = {
		val pet = makeEntsForPgAcc(ptxt_id)
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
		val tplRt = path(pgTplTst) {
			val pttXml = wbTplMkr.pgTplXml("pg_tpl_tst_id")
			complete(pttXml)
		} // ~
		tplRt
	}
}
