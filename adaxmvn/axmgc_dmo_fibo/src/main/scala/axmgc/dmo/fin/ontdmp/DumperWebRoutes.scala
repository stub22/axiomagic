package axmgc.dmo.fin.ontdmp

import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, _}
import dslServer.Directive0
import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}
import axmgc.web.pond._
import org.slf4j.{Logger, LoggerFactory}

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
	protected lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)
	protected lazy val myTdatChnkr = new TdatChunker {}
	protected lazy val myHtEntMkr = new HtEntMkr {}
	protected lazy val myXEntMkr = new WebXml {}

	protected lazy val myWtplMkr = new WebTupleMaker {
		override protected def getTdatChnkr: TdatChunker = myTdatChnkr
		override protected def getHtEntMkr: HtEntMkr = myHtEntMkr
		override protected def getWebXml: WebXml = myXEntMkr

		override protected def doPageWork(rqPrms: WebRqPrms): Option[IntrnlPonderRslt] = doRealPageWork(rqPrms)
	}

	override def goAndRespondFully(rqParams: Map[String, String]) : HEStrict = {
		myS4JLog.info("goAndRespondFully got paramMap: {}", rqParams)
		val pgTplXmlEnt = myWtplMkr.pgTplXml("dtb_out_domID", rqParams)
		myS4JLog.info("goAndRespondFully built response entity, length: {}", pgTplXmlEnt.contentLength)
		pgTplXmlEnt
	}

	private def doRealPageWork (rqPrms: WebRqPrms): Option[IntrnlPonderRslt] = {
		val myRslt = new IntrnlPonderRslt {
			override def getRqPrms: WebRqPrms = rqPrms
		}
		Some(myRslt)
	}
}