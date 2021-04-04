package axmgc.dmo.fin.ontdmp

import axmgc.web.cors.CORSHandler
import akka.http.scaladsl.{server => dslServer}
import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}
import axmgc.web.ent.HtEntMkr
import axmgc.xpr.vis_js.WebNavItemResponder

private trait OntNavWebTests

trait OntNavResponder {

	private lazy val myKbpediaOnt  = new KBPediaOntoWrap {}
	private lazy val myFiboOntChkr = new ChkFibo {}

	private val PRMKY_SEL = "sel"
	private val PRMVL_KBPEDIA = "kbpedia"
	private val PRMVL_FIBO = "fibo"
	private val PRMVL_DUM = "dummy"

	private val myDummyTreeResponder = new WebNavItemResponder {}

	def mkGoodResponse(paramMap : Map[String, String]) : HEStrict = {
		val selPrm_opt = paramMap.get(PRMKY_SEL)
		val selPrm = selPrm_opt.getOrElse(PRMVL_DUM)
		selPrm match {
			case PRMVL_FIBO => mkFiboResponse(paramMap)
			case PRMVL_KBPEDIA => mkKbpediaResponse(paramMap)
			case PRMVL_DUM => myDummyTreeResponder.makeAnswerEntity(paramMap)
			case _ => throw new Exception("Unknown selector: " + selPrm)
		}
	}
	// FIXME:  This HTEM should probably go into a narrower responder ctx
	private val myHTEM = new HtEntMkr {}
	private def mkFiboResponse(paramMap : Map[String, String]) : HEStrict = ???
	private def mkKbpediaResponse(paramMap : Map[String, String]) : HEStrict = {
		// FIXME:  This stat txt is not actually navdat
		val statJsonTxt: String = myKbpediaOnt.dumpStatsToLogAndJsonTxt()
		val navdatEnt = myHTEM.makeJsonEntity(statJsonTxt)
		navdatEnt
	}
}

trait OntNavRouteBldr {
	import dslServer.Directives.{_} // Establishes  ~   and whatnot

	private val myResponder = new OntNavResponder {}
	private val myCH = new CORSHandler {}

	def mkNavJsonRt(rtPthTxt : String) : dslServer.Route = {
		val njPthRt = path(rtPthTxt) {
			val pmapRt = parameterMap { paramMap =>
				// TODO:  Check pm for query prms
				val pm: Map[String, String] = paramMap
				val rspEnt = myResponder.mkGoodResponse(pm)
				complete(rspEnt)
			}
			pmapRt
		}
		val rtWithCors: dslServer.Route = myCH.corsHandler(njPthRt)
		rtWithCors
	}

}