package axmgc.dmo.ksrc.lean_mthlb

import axmgc.dmo.fin.ontdmp.{PlainNavRouteBldr}
import axmgc.xpr.vis_js.{NavJsonTxtApi, RobustNavJsonTxtImpl}

private trait LeanMathlibFolderStuff

class LeanMathlibTreeDataSrc extends RobustNavJsonTxtImpl {
	// Produces nav-item JSON responses for Lean Mathlib packages + proof-files

	private val PRMKY_SEL = "sel"
	private val PRMVL_LEAN_TREE = "leantree"
	private val PRMVL_DUM = "dummy"

	override def mkBroadJsonTxt(paramMap: Map[String, String]): String = {
		val selPrm_opt = paramMap.get(PRMKY_SEL)
		val selPrm = selPrm_opt.getOrElse(PRMVL_DUM)
		selPrm match {
			case PRMVL_LEAN_TREE => "LEAN BROAD ANSWER" //  mkLeanMathlibNavRespBrd(paramMap)
			case PRMVL_DUM => "FALLBACK BROAD ANSWR" // myFbackNIR.mkBroadAnswerEntity(paramMap)
			case _ => throw new Exception("Unknown selector: " + selPrm)
		}

	}
	override def mkNarrowJsonTxt(navQID: String, paramMap: Map[String, String]): String = ???
}

class LnMthlbNavRouteBldr extends PlainNavRouteBldr {
	override protected def getPlainNavJsonTxtMkr: NavJsonTxtApi = new LeanMathlibTreeDataSrc
}
