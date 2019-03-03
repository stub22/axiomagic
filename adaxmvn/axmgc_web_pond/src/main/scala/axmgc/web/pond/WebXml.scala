package axmgc.web.pond
import akka.http.scaladsl.model.HttpEntity

import scala.xml.{Attribute => XAttr, Elem => XElem, Node => XNode, NodeSeq => XNodeSeq, Null => XNull, UnprefixedAttribute => XUAttr}

class WebXml extends XmlEntMkr with WebResBind  {
	val svgHlpr = new WebSvg {}
	def mergeXhtml (xeHead : XElem, xeBody : XElem) : XElem = {
		<html>
			{xeHead}
			{xeBody}
		</html>
	}
	def mkTstHd : XElem = {
		<head>
			<title>WebXml Generated header contains this title</title>

			<meta name="viewport" content="width=device-width, initial-scale=1"></meta>
			<meta charset="utf-8"></meta>

			<link rel="stylesheet" href={urlPth_styIcn}></link>
			<link rel="stylesheet" href={urlPth_styDem}></link>
			<link rel="stylesheet" href={urlPth_styGrd}></link>
		</head>
	}
	def mkDmmyBdy : XElem = {
		val tmpJS = new TmpJscrptHolder {}
		val bdyElem = <body id="wx_tst_bdy_id" onload="attchHndlrsAtId('wx_tst_bdy_id')">
			<div>
				<span>WebXml made this here body, and made it real good.</span>
			</div>
			<div>
				{mkLnkA ("/patha", "link to /patha", None, Some("aaclz othr"))}
				<span>SPC</span>
				{mkLnkA ("/pathb", "link to /pathb", Some("lpb_idv"), Some("bbclz"))}
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
			<script>
			// comment hiding begin-cdat {tmpJS.myEvntTstScr_cdata}
			</script>
		</body>
		bdyElem
	}
	def mkRealBdy(ipr : IntrnlPonderRslt) : XElem = {
		val iprPairs = ipr.getOrderedRsltPairs
		val iprEls = iprPairs.map(mkPairElem(_))
		val bdyElem = <body id="wx_real_bdy_id" onload="attchHndlrsAtId('wx_tst_bdy_id')">
			<div>Did somebody ask for a REAL body?</div>
			<div>IntrnlPonderRslt Dump:<br/>{ipr.dumpAsTxt}</div>
			<div>{iprEls}</div>
		</body>
		bdyElem
	}
	def mkPairElem (pair : (String, String)) : XElem = {
		<div>
			<span>key='{pair._1}'</span><span> , </span><span>val='{pair._2}'</span>
		</div>
	}

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

	def getXHPageEnt(opt_Rslt : Option[IntrnlPonderRslt]) : HttpEntity.Strict = {
		val headXE = mkTstHd
		val bodyXE : XElem = opt_Rslt.map(mkRealBdy).getOrElse(mkDmmyBdy)
		val rootXE = mergeXhtml(headXE, bodyXE)
		// Default output entity mime-type is xml, which browser don't wanna render.
		// So we set mime-type to html, which seems OK so far.
		val ctyp = htmlCntType
		val xhEnt = makeXmlEntity (rootXE, ctyp)
		xhEnt
	}
}

trait TmpJscrptHolder {
	lazy val myEvntTstScr_cdata = new scala.xml.PCData(myEvntTstScr_raw)
	val myEvntTstScr_raw =
		"""//another comment after bgn-cdat then LINE-BREAK:
		  |function routeEvt(evt) {
		  |    // alert('Ancestor got click, evtTgt=' + event.target)
		  |    // Seems that target for keypress is always the body...
		  |    var evtTyp = evt.type
		  |    var evtTgt = evt.target
		  |    var etID = evtTgt.id
		  |    var currTgt = evt.currentTarget
		  |    var ctID = currTgt.id
		  |    var dbgYes = (! evtTyp.includes("mouse"))
		  |    if (dbgYes) {
		  |    		var dbgTxt = "routeEvt{type=" + evtTyp + ", target=" + evtTgt + ", etID=" + etID + ", currTgt=" + currTgt + ", ctID=" + ctID + "}"
		  |    		console.log(dbgTxt)
		  |    }
		  |    var prevC = evtTgt.style.color
		  |    var nextC =  makeRandomColor()
		  |    if (dbgYes) {
		  |    		var clrDbg = "changing color from " + prevC + " to " + nextC
		  |    		console.log(clrDbg)
		  |    }
		  |    evtTgt.style.color = nextC
		  |	   evtTgt.style.fill = nextC
		  |}
		  |
		  |function makeRandomColor(){
		  |// https://stackoverflow.com/questions/1484506/random-color-generator
		  |    var c = '';
		  |    while (c.length < 6) {
		  |        c += (Math.random()).toString(16).substr(-6).substr(-1)
		  |    }
		  |    return '#'+c;
		  |}
		  |
		  |
		  |function attchHndlrs(domElmt) {
		  |    var ourEvtNms = ['click', 'mouseover', 'mouseout', 'mousemove', 'keypress']
		  |    // https://javascript.info/bubbling-and-capturing
		  |    // Optional 3rd arg is boolean, where true => capture-handler, but dflt=false => bubble handler
		  |    // is preferred.
		  |    console.log("handler names are: ", ourEvtNms)
		  |    console.log("attaching handlers to element: ", domElmt)
		  |    ourEvtNms.forEach(function(nm) {
		  |        domElmt.addEventListener(nm, routeEvt)
		  |    })
		  |}
		  |function attchHndlrsAtId(domID) {
		  |		console.log("looking up dom el for event handlers at: ", domID)
		  |    var domEl = document.getElementById(domID)
		  |    attchHndlrs(domEl)
		  |}
		  |
		  |var myTicker = null
		  |var myIntervalMsec = 1000
		  |function startTicker () {
		  |    if (myTicker == null) {
		  |        myTicker = window.setInterval(myTickFunc, myIntervalMsec);
		  |    } else {
		  |        console.log("Ticker already running, ignoring START rq")
		  |    }
		  |}
		  |function stopTicker () {
		  |    if (myTicker != null) {
		  |        window.clearInterval(myTicker)
		  |        myTicker = null;
		  |    } else {
		  |        console.log("Ticker isn't running, ignoring STOP rq")
		  |    }
		  |}
		  |function myTickFunc() {
		  |  var d = new Date();
		  |  document.getElementById("tt_out").innerHTML = d.toLocaleTimeString();
		  |}
		  |//comment hiding end-cdat""".stripMargin

}
