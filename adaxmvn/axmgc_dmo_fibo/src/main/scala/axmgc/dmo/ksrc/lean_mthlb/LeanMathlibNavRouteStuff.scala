package axmgc.dmo.ksrc.lean_mthlb

import axmgc.dmo.fin.ontdmp.{PlainNavRouteBldr}
import axmgc.xpr.vis_js.{NavJsonTxtApi, RobustNavJsonTxtImpl}

private trait LeanMathlibNavRouteStuff


class LnMthLbNavJsonTxtImpl extends RobustNavJsonTxtImpl {
	// Produces nav-item JSON responses for Lean Mathlib packages + proof-files

	private val PRMKY_SEL = "sel"
	private val PRMVL_LEAN_TREE = "leantree"
	private val PRMVL_DUM = "dummy"

	private val myLmlNim = new LmlNavItemMaker

	override def mkBroadJsonTxt(paramMap: Map[String, String]): String = {
		val selPrm_opt = paramMap.get(PRMKY_SEL)
		val selPrm = selPrm_opt.getOrElse(PRMVL_DUM)
		selPrm match {
			case PRMVL_LEAN_TREE => myLmlNim.mkBigTree() // "LEAN BROAD ANSWER"
			case PRMVL_DUM => super.mkBroadJsonTxt(paramMap) // "FALLBACK BROAD ANSWR"
			case _ => throw new Exception("Unknown selector: " + selPrm)
		}
	}
	override def mkNarrowJsonTxt(navQID: String, paramMap: Map[String, String]): String = ???
}

class LnMthlbNavRouteBldr extends PlainNavRouteBldr {
	override protected def getPlainNavJsonTxtMkr: NavJsonTxtApi = new LnMthLbNavJsonTxtImpl
}
