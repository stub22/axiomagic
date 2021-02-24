package axmgc.web.ent

import akka.http.scaladsl.model.HttpEntity
import axmgc.web.rsrc.{WebRsrcFolders, WebSvg}
import axmgc.web.tuple.IntrnlPonderRslt

import scala.xml.{Elem => XElem, Null => XNull, UnprefixedAttribute => XUAttr, Node => XNode, Text => XText, NodeSeq => XNodeSeq}

private trait WebXml

trait XmlAttribFuncs {

}
trait WebLinkXmlFuncs {
	private def maybeAppendAttr(elem : XElem, attrName : String, attrVal_opt : Option[String]) : XElem = {
		// Can we use "MetaData" .next stuff to control attribute ordering?
		// https://www.scala-lang.org/api/2.12.4/scala-xml/scala/xml/MetaData.html
		if (attrVal_opt.isDefined) {
			// Watch out for scala.xml.Null
			val attr = new XUAttr(attrName, attrVal_opt.get, XNull)
			elem % attr
		} else elem
	}
	// Attributes seem to come out in opposite order from how they are added here.
	// Remember css-class values are separated by spaces
	def mkLnkA(hrefURL : String, labelTxt : String, id_opt : Option[String],
			   clz_opt : Option[String]) : XElem = {
		val baseEl = <a href={hrefURL}>{labelTxt}</a>
		val elWthId = maybeAppendAttr(baseEl, "id", id_opt)
		val elWthClz = maybeAppendAttr(elWthId, "class", clz_opt)
		elWthClz
	}
}
trait WebStringFuncs {
	def nTabs(n : Int) : String = {
		"\t" * n
	}
}
trait WebHeadFuncs {
	val webStringHlpr = new WebStringFuncs {}
	def mkScriptElem(scriptPath : String) : XElem = {
		<script src={scriptPath}></script>
	}
	def mkScriptXNS(scrPths : Seq[String]) : XNodeSeq = {
		val includeFormatNodes = true
		val includeTabs = true
		val numTabs = 3
		val crNode = if (includeTabs) new XText("\n" + webStringHlpr.nTabs(numTabs)) else new XText("\n")
		val scrNodes: Seq[XNode] = scrPths.flatMap(sp => {
			val scr = <script src={sp}></script>
			if (includeFormatNodes) List(scr,crNode) else List(scr)
		})
		val ndsq = XNodeSeq.fromSeq(scrNodes)
		ndsq
	}

}

trait XmlPageHelp extends XmlEntMkr {
	val headHlpr = new WebHeadFuncs {}

	def mergeXhtml (xeHead : XElem, xeBody : XElem) : XElem = {
		<html>
			{xeHead}
			{xeBody}
		</html>
	}
	def makePageEnt(pageXE : XElem) : HttpEntity.Strict = {
		val ctyp = htmlCntType
		val xhEnt = makeXmlEntity (pageXE, ctyp)
		xhEnt
	}

	def mkTstScrXNS : XNodeSeq = {
		val scrPaths = new WebScriptPaths {}
		val pathSeq = scrPaths.getManyScrPaths
		val scrXNS = headHlpr.mkScriptXNS(pathSeq)
		scrXNS
	}

}

class WebXmlGen extends XmlEntMkr with WebRsrcFolders  {
	val svgHlpr = new WebSvg {}
	val lnkHlpr = new WebLinkXmlFuncs {}
	val xPageHlp = new XmlPageHelp {}

	def mkIconStylXNS : XNodeSeq = {
		???
	}

	def mkTstHd : XElem = {
		val scrXNS = xPageHlp.mkTstScrXNS
		val stylPths = new WebStylePaths {}
		<head>
			<title>WebXml Generated header contains this title</title>

			<meta name="viewport" content="width=device-width, initial-scale=1"></meta>
			<meta charset="utf-8"></meta>

			<link rel="stylesheet" href={stylPths.urlPth_styIcn}></link>
			<link rel="stylesheet" href={stylPths.urlPth_styDem}></link>
			<link rel="stylesheet" href={stylPths.urlPth_styGrd}></link>

			{scrXNS}
		</head>
	}
	def mkDmmyBdy : XElem = {
		val tmpJS = new TmpJscrptHolder {}
		val bdyElem = <body id="wx_tst_bdy_id" onload="attchHndlrsAtId('wx_tst_bdy_id')">
			<div>
				<span>WebXml made this here body, and made it real good.</span>
			</div>
			<div>
				{lnkHlpr.mkLnkA ("/patha", "link to /patha", None, Some("aaclz othr"))}
				<span>SPC</span>
				{lnkHlpr.mkLnkA ("/pathb", "link to /pathb", Some("lpb_idv"), Some("bbclz"))}
			</div>
			<div>
				{svgHlpr.mkDblDivWithSvgIcon("access_alarms")}
				{svgHlpr.mkDblDivWithSvgIcon("mood-happy-outline")}
				{svgHlpr.mkDblDivWithSvgIcon("zoomout")}
			</div>
			<div>
				<p id="tt_out">ticker output goes here</p>
				<button onclick="startTicker()">Start Ticker</button>
				<button onclick="stopTicker()">Stop Ticker</button>
			</div>
			<div>
				<span>Test Icon Names: </span><br/>
				<span>{svgHlpr.testNmLst}</span>
			</div>
			<div>
				{svgHlpr.mkDDSTstBlk}
			</div>
		</body>
		bdyElem
	}

	/*		This works for embedding a script as text within a CData
			<script>
			// comment hiding begin-cdat {tmpJS.myEvntTstScr_cdata}
			</script>

			This works within <body>

			<script src="/wdat/axmgc_js/wrp_datgui/tplIcoEvtHndlr.js"></script>
	  */
	def mkRealBdy(ipr : IntrnlPonderRslt) : XElem = {
		val iprPairs = ipr.getOrderedRsltPairs
		val iprEls = iprPairs.map(mkPairElem(_))
		val bdyElem = <body id="wx_real_bdy_id" onload="attchHndlrsAtId('wx_tst_bdy_id')">
			<div>Did somebody ask for a REAL body?</div>
			<div>IntrnlPonderRslt Dump:<br/>{ipr.dumpAsTxt}</div>
			<div>{iprEls}</div>
			<div>special: <pre>{ipr.specialJsonTxt}</pre></div>
		</body>
		bdyElem
	}
	def mkPairElem (pair : (String, String)) : XElem = {
		<div>
			<span>key='{pair._1}'</span><span> , </span><span>val='{pair._2}'</span>
		</div>
	}


	def getXHPageEnt(opt_Rslt : Option[IntrnlPonderRslt]) : HttpEntity.Strict = {
		val headXE = mkTstHd
		val bodyXE : XElem = opt_Rslt.map(mkRealBdy).getOrElse(mkDmmyBdy)
		val pageXE = xPageHlp.mergeXhtml(headXE, bodyXE)
		// Default output entity mime-type is xml, which browser don't wanna render.
		// So we set mime-type to html, which seems OK so far.

		val xhEnt = xPageHlp.makePageEnt(pageXE) //  makeXmlEntity (rootXE, ctyp)
		xhEnt
	}
}

trait TmpJscrptHolder {
	lazy val myEvntTstScr_cdata = new scala.xml.PCData(myEvntTstScr_raw)
	private val myEvntTstScr_raw =
		"""//another comment after bgn-cdat then LINE-BREAK:|
		  |function makeRandomColor(){
		  |// https://stackoverflow.com/questions/1484506/random-color-generator
		  |    var c = '';
		  |    while (c.length < 6) {
		  |        c += (Math.random()).toString(16).substr(-6).substr(-1)
		  |    }
		  |    return '#'+c;
		  |}
		  |//comment hiding end-cdat""".stripMargin

}
