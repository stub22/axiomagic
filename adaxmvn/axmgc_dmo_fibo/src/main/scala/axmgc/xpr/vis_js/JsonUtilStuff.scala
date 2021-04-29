package axmgc.xpr.vis_js

import spray.json.{JsArray, JsNumber, JsObject, JsString, JsValue}

import scala.collection.immutable.Seq

private trait JsonUtilStuff

trait JsValueMakers {
	// https://github.com/spray/spray-json/blob/release/1.3.x/src/main/scala/spray/json/JsValue.scala
	// https://www.javadoc.io/doc/io.spray/spray-json_2.12/latest/spray/json/JsObject.html
	def smapToJsObj(fields: Map[String, JsValue]) : JsObject = new JsObject(fields)
	// https://www.javadoc.io/doc/io.spray/spray-json_2.12/latest/spray/json/JsArray.html
	def svectToJsArr(elements: Vector[JsValue]) : JsArray = new JsArray(elements)
	def seqToJsArr(elements: Seq[JsValue]) : JsArray = svectToJsArr(elements.toVector)
	def mkJsString(sv : String) : JsString = new JsString(sv)
	def mkJsNum(bigDec : BigDecimal) = new JsNumber(bigDec)
	def mkJsNum(lv : Long) = new JsNumber(BigDecimal(lv))
}