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
		</head>
	}
	def mkTstBdy : XElem = {
		<head>
		</head>
	}

	def getXHPageEnt : HttpEntity.Strict = {
		val headXE = mkTstHd
		val bodyXE = mkTstBdy
		val rootXE = mergeXhtml(headXE, bodyXE)
		val xhEnt = makeXmlEntity (rootXE)
		xhEnt
	}

}
