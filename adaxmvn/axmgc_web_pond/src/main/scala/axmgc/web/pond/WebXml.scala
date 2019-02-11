package axmgc.web.pond
import akka.http.scaladsl.model.HttpEntity

import scala.xml.{Elem => XElem, Node => XNode, NodeSeq => XNodeSeq, Null => XNull, Attribute => XAttr, UnprefixedAttribute => XUAttr}

class WebXml extends XmlEntMkr {
	val svgHlpr = new WebSvg {}
	def mergeXhtml (xeHead : XElem, xeBody : XElem) : XElem = {
		<html>
			{xeHead}
			{xeBody}
		</html>
	}
	def mkTstHd : XElem = {
		<head>
			<title>WebXml made this title</title>
		</head>
	}
	def mkTstBdy : XElem = {
		<body>

			<div>
				<span>WebXml made this here body, and made it real good.</span>
			</div>
			<div>
				{mkLnkA ("/patha", "link to /patha", None, Some("aaclz othr"))}
				<span>SPC</span>
				{mkLnkA ("/pathb", "link to /pathb", Some("lpb_idv"), Some("bbclz"))}
			</div>
			<div>
				{svgHlpr.mkDivWithSvgIcon("access_alarms")}
				{svgHlpr.mkDivWithSvgIcon("mood-happy-outline")}
				{svgHlpr.mkDivWithSvgIcon("zoomout")}
			</div>
		</body>
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

	def getXHPageEnt : HttpEntity.Strict = {
		val headXE = mkTstHd
		val bodyXE = mkTstBdy
		val rootXE = mergeXhtml(headXE, bodyXE)
		// Default output entity mime-type is xml, which browser don't wanna render.
		// So we set mime-type to html, which seems OK so far.
		val ctyp = htmlCntType
		val xhEnt = makeXmlEntity (rootXE, ctyp)
		xhEnt
	}

}
