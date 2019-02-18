package axmgc.dmo.fin.ontdmp

import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, _}
import dslServer.Directive0
import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}
import axmgc.web.pond.{HtEntMkr, TdatChunker, WebTupleMaker, WebXml}

trait DumperWebRoutes // Marker for file - only

trait DumperWebFeat {
	val pthTok_dhlo = "dhlo"
	val pthTok_dgo = "dgo"

	// go == "perform now" = do whatever work and return
	def goAndRespondFully(rqParamMap: Map[String, String]) : HEStrict
}

trait DmpWbRtMkr {

	protected def getDumperWebFeat : DumperWebFeat

	def makeDmprTstRt  : dslServer.Route = {
		val webFeat = getDumperWebFeat
		val hloPth = webFeat.pthTok_dhlo
		val dmprMainRt = parameterMap { rqParamMap: Map[String, String] =>
			path(hloPth) {
				// val pttXml = wbTplMkr.pgTplXml("pg_tpl_tst_id")
				complete("Raw hlo from feature object: " + webFeat)
			} ~ path(webFeat.pthTok_dgo) {
				val rspEnt = webFeat.goAndRespondFully(rqParamMap)
				complete(rspEnt) // "dgo went and responded...")
			} // ~
		}
		dmprMainRt
	}
}

trait DumperTupleBridge extends DumperWebFeat {
	protected lazy val myTdatChnkr = new TdatChunker {}
	protected lazy val myHtEntMkr = new HtEntMkr {}
	protected lazy val myXEntMkr = new WebXml {}

	protected lazy val myWtplMkr = new WebTupleMaker {
		override protected def getTdatChnkr: TdatChunker = myTdatChnkr
		override protected def getHtEntMkr: HtEntMkr = myHtEntMkr
		override protected def getWebXml: WebXml = myXEntMkr
	}

	override def goAndRespondFully(rqParams: Map[String, String]) : HEStrict = {
		val pgTplXmlEnt = myWtplMkr.pgTplXml("dtb_out_domID", rqParams)
		pgTplXmlEnt
	}
}