package axmgc.dmo.fin.ontdmp

import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, _}
import dslServer.Directive0
import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}
import axmgc.web.ent.{HtEntMkr, WebXmlGen}
import axmgc.web.lnkdt.LDChunkerTest
import axmgc.web.pond._
import axmgc.web.tuple.{IntrnlPonderRslt, WebRqPrms, WebTupleMaker}
import org.slf4j.{Logger, LoggerFactory}

trait DumperWebRoutes // Marker for file - only

trait DumperWebFeat {
	val pthTok_dhlo = "dhlo"
	val pthTok_dgo = "dgo"

	// go == "perform now" = do whatever work and return
	def goAndRespondFully(rqParamMap: Map[String, String]) : HEStrict
}

trait DmpWbRtMkr {

	protected lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)

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

		myS4JLog.info(s"Made routes at ${webFeat.pthTok_dhlo} and ${webFeat.pthTok_dgo}")
		dmprMainRt
	}
}

trait DumperTupleBridge extends DumperWebFeat {
	protected lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)
	protected lazy val myTdatChnkr = new LDChunkerTest {}
	protected lazy val myHtEntMkr = new HtEntMkr {}
	protected lazy val myXEntMkr = new WebXmlGen {}

	protected lazy val myWtplMkr = new WebTupleMaker {
		override protected def getLDChnkr: LDChunkerTest = myTdatChnkr
		override protected def getHtEntMkr: HtEntMkr = myHtEntMkr
		override protected def getWebXml: WebXmlGen = myXEntMkr

		override protected def doPageWork(rqPrms: WebRqPrms): Option[IntrnlPonderRslt] = doRealPageWork(rqPrms)
	}

	override def goAndRespondFully(rqParams: Map[String, String]) : HEStrict = {
		myS4JLog.info("goAndRespondFully got paramMap: {}", rqParams)
		val pgTplXmlEnt = myWtplMkr.pgTplXml("dtb_out_domID", rqParams)
		myS4JLog.info("goAndRespondFully built response entity, length: {}", pgTplXmlEnt.contentLength)
		pgTplXmlEnt
	}

	private def doRealPageWork (rqPrms: WebRqPrms): Option[IntrnlPonderRslt] = {
		myS4JLog.info("doRealPageWork got paramMap: {}", rqPrms)
		val opt_nuts = rqPrms.getTextParam("nuts")
		val opt_fibo = rqPrms.getTextParam("fibo")
		val opt_tree = rqPrms.getTextParam("tree")
		val njtxt = if (opt_nuts.isDefined) {
			val nuttyJsonTxt = goNuts(opt_nuts.get)
			nuttyJsonTxt
		} else if (opt_fibo.isDefined) {
			val fiboJsonTxt = dumpSomeFiboStats(opt_fibo.get)
			fiboJsonTxt
		} else if (opt_tree.isDefined) {
			"hmm, tree data fetch does not fit the IntrnlPonderRslt concept if it implies 'full-page' context"
		} else "no 'nuts' or 'fibo' or 'tree' params found"

		val myRslt = new IntrnlPonderRslt {
			override def getRqPrms: WebRqPrms = rqPrms
			override def getOrderedRsltPairs: Seq[(String, String)] = {
				getRqPrms.getParamPairs
			}

			override def specialJsonTxt: String = njtxt
		}
		Some(myRslt)
	}

	private lazy val myKbpediaOnt = new KBPediaOntoWrap {}

	private def goNuts(np : String) : String = {
		myS4JLog.info("goNuts got np: {}", np)
		val kbpMdl = myKbpediaOnt.getKBP_model
		val statJsonTxt: String = myKbpediaOnt.dumpStatsToLogAndJsonTxt()
		statJsonTxt
	}

	protected def getFiboOntChkr : ChkFibo

	private lazy val myFiboOntChkr = getFiboOntChkr

	private def dumpSomeFiboStats(fsp : String) : String = {
		myS4JLog.info("dumpSomeFiboStats got fsp: {}", fsp)
		"[fibo answer goes here]"
	}


}