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

	private val QIDVL_UNK = "EMPTY"

	private val myDummyTreeResponder = new WebNavItemResponder {}

	def mkBroadResponse(paramMap : Map[String, String]) : HEStrict = {
		val selPrm_opt = paramMap.get(PRMKY_SEL)
		val selPrm = selPrm_opt.getOrElse(PRMVL_DUM)
		selPrm match {
			case PRMVL_FIBO => mkFiboResponse(paramMap)
			case PRMVL_KBPEDIA => mkKbpediaResponse(paramMap)
			case PRMVL_DUM => myDummyTreeResponder.makeAnswerEntity(paramMap)
			case _ => throw new Exception("Unknown selector: " + selPrm)
		}
	}
	def mkNarrowResponse(navQID : String, paramMap : Map[String, String]) : HEStrict = {
		if (!navQID.equals(QIDVL_UNK)) {
			myDummyTreeResponder.mkNarrowAnswerEntity(navQID, paramMap)
		} else {
			mkBroadResponse(paramMap)
		}
	}
	// FIXME:  This HTEM should probably go into a narrower responder ctx
	private val myHTEM = new HtEntMkr {}
	private def mkFiboResponse(paramMap : Map[String, String]) : HEStrict = {
		val fiboStatJsonTxt: String = myFiboOntChkr.dumpFiboMdlStatsToLog()
		val fiboNavdatEnt = myHTEM.makeJsonEntity(fiboStatJsonTxt)
		fiboNavdatEnt
	}
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

	val PN_navQID = "navQID"

	def mkNavJsonRt(rtPthTxt : String) : dslServer.Route = {
		val njPthRt = path(rtPthTxt) {
			val pmapRt = parameterMap { paramMap =>
				// TODO:  Check pm for IMPORTANT(!?) query prms
				val pm: Map[String, String] = paramMap
				val nqidParam_opt = pm.get(PN_navQID)
				val rspEnt = if(nqidParam_opt.isDefined) {
					val navQID = nqidParam_opt.get
					myResponder.mkNarrowResponse(navQID, pm)
				} else myResponder.mkBroadResponse(pm)
				complete(rspEnt)
			}
			pmapRt
		}
		val rtWithCors: dslServer.Route = myCH.corsHandler(njPthRt)
		rtWithCors
	}
}

/*
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