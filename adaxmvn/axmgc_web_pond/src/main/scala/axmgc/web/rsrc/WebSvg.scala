package axmgc.web.rsrc

import scala.xml.{Elem => XElem}

trait WebSvg extends WebRsrcFolders {
	val pthStylIco = fldrPth_styl + FLD_SEP + "icmbg_sty.css"
	val pthStylIcoDem = fldrPth_styl + FLD_SEP + "icmbg_dem.css"
	def getCssResPths : List[String] = List(pthStylIco, pthStylIcoDem)
	val pthIco3290 = fldrPth_ivct + FLD_SEP + "icon_syms_2018_3290.svg"
	val icoNmPrfx = "icon-"
	val svgBaseClzNms = "icon "
	val divOutClzNms = "glyph fs1"
	val divInClzNms = "clearfix pbs"
	private def mkIcoNmFl (tail: String) = icoNmPrfx + tail
	private def mkIcoHref(tail: String) : String = pthIco3290 + "#" + mkIcoNmFl(tail)
	private def mkIcoClzs(tail: String) : String =  svgBaseClzNms + mkIcoNmFl(tail)

	private lazy val myIcnmSrc = new IconNmSrc {}

	def mkDblDivWithSvgIcon(icoTstTl : String) : XElem = {
		val ddsElem =
			<div class="glyph fs1">
				<div class="clearfix pbs">
					<svg class={mkIcoClzs(icoTstTl)}>
						<use xlink:href={mkIcoHref(icoTstTl)}>
						</use>
					</svg>
					<span class="name"> {mkIcoNmFl(icoTstTl)}</span>
				</div>
			</div>
		ddsElem
	}
	def mkManySvgDivs(lst_icoTl : Seq[String]) : Seq[XElem] = {
		val f = lst_icoTl.map(icoTl => mkDblDivWithSvgIcon(icoTl))
		f
	}
	def wrapInDiv(ndLst : Seq[XElem], prntID : String, prntClzs : String) : XElem = {
		<div id={prntID} class={prntClzs}>{ndLst}</div>
	}
	def testNmLst : Seq[String] = {
		// myIcnmSrc.someIconNms
		myIcnmSrc.getShuffledIcoNms.take(18)
	}
	def mkDDSTstBlk : XElem = {
		val icNmSeq = testNmLst
		val icNmCnt = icNmSeq.length

		val pclzs = "grdCnt" // Defined in ictst_grid.css or other.
		val svgDblDivs = mkManySvgDivs(icNmSeq)
		val wrpDv = wrapInDiv(svgDblDivs, "bunchaSVGDvs_01", pclzs)
		wrpDv
	}

	// def lstToNdSq :
}
