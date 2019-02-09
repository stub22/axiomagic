package axmgc.web.pond
import akka.http.scaladsl.model.HttpEntity

import scala.xml.{Elem => XElem, Node => XNode, NodeSeq => XNodeSeq}

class WebXml extends XmlEntMkr {
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
				{mkLnkA ("/patha", "link to /patha")}
			</div>
		</body>
	}
	def mkLnkA(hrefURL : String, labelTxt : String) : XElem = {
		<a href={hrefURL}>{labelTxt}</a>
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
