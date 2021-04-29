package axmgc.dmo.fin.ontdmp

import axmgc.web.cors.CORSHandler
import akka.http.scaladsl.{server => dslServer}
import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}
import axmgc.web.ent.HtEntMkr
import axmgc.xpr.vis_js.{NavJsonEntApi, NavJsonEntImpl, NavJsonTxtApi, RobustNavJsonTxtImpl}

private trait OntNavWebTests

// trait CommonRespApi extends NavItemRespApi {
//	def mkBroadResponse(paramMap : Map[String, String]) : HEStrict
//	def mkNarrowResponse (navQID : String, paramMap : Map[String, String]) : HEStrict
// }
/*
class CommonResponderWrap extends NIRRespImpl {

	protected def getFallbackNavItemResp : NavItemRespApi

	protected def getHtEntMkr : HtEntMkr

	private val QIDVL_UNK = "EMPTY"

	// general response, when no narrowing ID is supplied
	override def mkBroadResponse(paramMap : Map[String, String]) : HEStrict = mkFallbackBroadResponse(paramMap)

	private def mkFallbackBroadResponse(paramMap : Map[String, String]) : HEStrict = {
		getFallbackNavItemResp.mkBroadAnswerEntity(paramMap)
	}

	// narrow response, when a navQID is supplied
	override def mkNarrowResponse (navQID : String, paramMap : Map[String, String]) : HEStrict =
			mkFallbackNarrowResponse (navQID, paramMap)

	private def mkFallbackNarrowResponse(navQID : String, paramMap : Map[String, String]) : HEStrict = {
		if (!navQID.equals(QIDVL_UNK)) {
			getFallbackNavItemResp.mkNarrowAnswerEntity(navQID, paramMap)
		} else {
			mkFallbackBroadResponse(paramMap)
		}
	}
}

 */
/*
class SafeNavItemJsonImpl extends NavJsonTxtApi {
	protected def getFallbackNIJA : NavJsonTxtApi = ???
	override def mkBroadJsonTxt(paramMap: Map[String, String]): String = ???

	override def mkNarrowJsonTxt(navQID: String, paramMap: Map[String, String]): String = ???
}
 */
class DemoOntNavResponder extends RobustNavJsonTxtImpl { // SafeNavItemJsonImpl {
	// Produces nav-item JSON responses for several imported demo-onts (notably Kbpedia and FIBO)
	private lazy val myKbpediaOnt  = new KBPediaOntoWrap {}
	private lazy val myFiboOntChkr = new FiboOntWrap {}

	private val PRMKY_SEL = "sel"
	private val PRMVL_KBPEDIA_RC = "kbpedia"
	private val PRMVL_KBPEDIA_KKO = "kko"
	private val PRMVL_KBPEDIA_TYPOS = "kbtypo"
	private val PRMVL_FIBO = "fibo"
	private val PRMVL_DUM = "dummy"

	override def mkBroadJsonTxt(paramMap : Map[String, String]) : String = {
		val selPrm_opt = paramMap.get(PRMKY_SEL)
		val selPrm = selPrm_opt.getOrElse(PRMVL_DUM)
		selPrm match {
			case PRMVL_FIBO => mkFiboRespBrd(paramMap)
			case PRMVL_KBPEDIA_RC => mkKbprcRespBrd(paramMap)
			case PRMVL_KBPEDIA_KKO => mkKkoRespBrd(paramMap)
			case PRMVL_KBPEDIA_TYPOS => mkKbpTyposRespBrd(paramMap)
			case PRMVL_DUM => super.mkBroadJsonTxt(paramMap)
			case _ => throw new Exception("Unknown selector: " + selPrm)
		}
	}

	private def mkFiboRespBrd(paramMap : Map[String, String]) : String =
		myFiboOntChkr.dumpFiboMdlStatsToLog()

	private def mkKbprcRespBrd(paramMap : Map[String, String]) : String =
		// FIXME:  This stat txt is not actually navdat
		myKbpediaOnt.dumpKbprcStatsToLogAndJsonTxt()

	private def mkKkoRespBrd(paramMap : Map[String, String]) : String =
		// FIXME:  This stat txt is not actually navdat
		myKbpediaOnt.dumpKkoStatsToLogAndJsonTxt()

	private def mkKbpTyposRespBrd(paramMap : Map[String, String]) : String =
		myKbpediaOnt.dumpTypoStatsAsJsonTxt

	override def mkNarrowJsonTxt(navQID: String, paramMap: Map[String, String]): String = ???
}
trait CommonNavRouteBldr {

	protected def getNavJsonEntResp : NavJsonEntApi

	val PN_navQID = "navQID"
	private val myCH = new CORSHandler {}

	import dslServer.Directives.{_} // Establishes  ~   and whatnot
	def mkNavJsonRt(rtPthTxt : String) : dslServer.Route = {
		val njPthRt = path(rtPthTxt) {
			val pmapRt = parameterMap { paramMap =>
				// TODO:  Check pm for IMPORTANT(!?) query prms
				val pm: Map[String, String] = paramMap
				val nqidParam_opt = pm.get(PN_navQID)
				val rspEnt = if(nqidParam_opt.isDefined) {
					val navQID = nqidParam_opt.get
					getNavJsonEntResp.mkNarrowAnswerEntity(navQID, pm)
				} else getNavJsonEntResp.mkBroadAnswerEntity(pm)
				complete(rspEnt)
			}
			pmapRt
		}
		val rtWithCors: dslServer.Route = myCH.corsHandler(njPthRt)
		rtWithCors
	}
}
trait PlainNavRouteBldr extends CommonNavRouteBldr {
	protected def getPlainNavJsonTxtMkr : NavJsonTxtApi
	private val myHTEM = new HtEntMkr {}

	private val myNavJsonEntMkr =  new NavJsonEntImpl {
		override protected def getNavJsonTxtMkr: NavJsonTxtApi = getPlainNavJsonTxtMkr
		override protected def getHtEntMkr: HtEntMkr = myHTEM
	}
	override protected def getNavJsonEntResp: NavJsonEntApi = myNavJsonEntMkr

}
class DemoOntNavRouteBldr extends PlainNavRouteBldr {
	private val myNavJsonTxtMkr = new DemoOntNavResponder

	override protected def getPlainNavJsonTxtMkr: NavJsonTxtApi = myNavJsonTxtMkr
}
/*
	private val myNavBlkResponder = new DemoOntNavResponder {
		private val myDummyTreeResponder = new FakeNavItemRespImpl {}
		override protected def getFallbackNavItemResp = myDummyTreeResponder


		override protected def getHtEntMkr = myHTEM
	}

	override protected def getNavItemResponder : CommonRespApi = myNavBlkResponder

Web data view requires:
	A) Data-record source
	B) Records suitable for JS output via spray-json
	C) Adapter from source records to output records
	D) Akka-Http routes suitable for request+response handling
	E) Suitable approach to result chaining, followup links, related-result caching
	Service-side request+response handling sequence
		s1) decodes request params
		s2) queries A and collects interesting records
		s3) updates caches consistent with E
		s4) uses C to translate results to form B (and maybe more E-updates)
		s5) produces response(-source)
 */