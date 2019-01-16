package axmgc.web.pond

/**
  * @author stub22
  */

trait WebOut {}

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}

// constructive, not prescriptive
trait HtEntMkr {
	// Note scala backticks, used to identify variables containing special chars
	val htmlCntType = ContentTypes.`text/html(UTF-8)`
	val jsonCntType = ContentTypes.`application/json`

	def makeHtmlEntity(htmlTxt: String): HttpEntity.Strict = {
		HttpEntity(htmlCntType, htmlTxt)
	}

	def makeJsonEntity(jsonTxt: String): HttpEntity.Strict = {
		HttpEntity(jsonCntType, jsonTxt)
	}
}

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport
import scala.xml.NodeSeq

trait XmlEntMkr extends ScalaXmlSupport {
	type AlNodeSeq = NodeSeq

}

