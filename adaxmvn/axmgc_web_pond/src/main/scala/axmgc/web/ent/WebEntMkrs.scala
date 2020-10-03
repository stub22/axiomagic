package axmgc.web.ent

/**
  * @author stub22
  */

trait WebEntMkrs {}


import akka.http.scaladsl.model._
import akka.stream.scaladsl.Source
import akka.util.ByteString

// constructive, not prescriptive
trait HtEntMkr {
	// Note scala backticks, used to identify variables containing special chars
	val htmlCntType = ContentTypes.`text/html(UTF-8)`
	val jsonCntType = ContentTypes.`application/json`
	val plainCntType = ContentTypes.`text/plain(UTF-8)`
	val xmlCntType = ContentTypes.`text/xml(UTF-8)`
	// val cssCntType = ContentTypes.`te
// https://stackoverflow.com/questions/38061599/how-to-serve-a-text-css-file-from-akka-backend
	private val cssMType = MediaTypes.`text/css`
	private val u8Chrset = HttpCharsets.`UTF-8`
	private val cssU8CType = ContentType(cssMType, u8Chrset)
	// text/css

	def makeHtmlEntity(htmlTxt: String): HttpEntity.Strict = {
		HttpEntity(htmlCntType, htmlTxt)
	}

	def makeJsonEntity(jsonTxt: String): HttpEntity.Strict = {
		HttpEntity(jsonCntType, jsonTxt)
	}

	def makeChunked (strmDat : Source[ByteString, Any]) : HttpEntity.Chunked = {
		val chnkStrmEnt = HttpEntity.Chunked.fromData(plainCntType, strmDat)
		chnkStrmEnt
	}

	private val dummyCssTxt =
		"""
	body {
		background-color: green;
	}
		"""
	def makeDummyCssEnt() : HttpEntity.Strict = {
		HttpEntity(cssU8CType, dummyCssTxt)
	}
}

import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport

import scala.xml.NodeSeq

trait XmlEntMkr extends HtEntMkr with ScalaXmlSupport {
	type AlNodeSeq = NodeSeq

	def makeXmlEntity(nodeSeq: NodeSeq, ctyp: ContentType.WithCharset = xmlCntType): HttpEntity.Strict = {
		HttpEntity(ctyp, nodeSeq.toString()) // the "toString" is not what we want!
	}

}
/*
This directive is meant to be used at level of route.
def entity[T](um: FromRequestUnmarshaller[T]): Directive1[T]
Description
Unmarshalls the request entity to the given type and passes it to its inner Route.
An unmarshaller returns an Either with Right(value) if successful or Left(exception) for a failure.
The entity method will either pass the value to the inner route
or map the exception to a Rejection.

The entity directive works in conjunction with as and akka.http.scaladsl.unmarshalling to
convert some serialized “wire format” value into a higher-level object structure.
This directive simplifies extraction and error handling to the specified type from the request.
 */
/*
By default, any values within the { ... } markers will first be converted to a String (using its toString method) and then wrapped in a Text before embedding in the XML. However, if the expression within the braces is already of type NodeSeq, the interpolation will simply embed that value without any conversion. For example:

val ns1 = <foo/>
val ns2 = <bar>{ ns1 }</bar>       // => <bar><foo/></bar>
You can even embed something of type Seq[Node] and the interpolation will “do the right thing”, flattening the sequence into an XML fragment which takes the place of the interpolated segment:

val xs = List(<foo/>, <bar/>)
val ns = <baz>{ xs }</baz>          // => <baz><foo/><bar/></baz>
*/