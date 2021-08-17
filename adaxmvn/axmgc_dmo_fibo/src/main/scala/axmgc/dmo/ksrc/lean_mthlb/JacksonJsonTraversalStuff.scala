package axmgc.dmo.ksrc.lean_mthlb

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.{ArrayNode, JsonNodeType}

import scala.collection.mutable.{HashMap => SMHashMap}

private trait JacksonJsonTraversalStuff

trait JacksonJsonAnlyz {
	import scala.collection.JavaConverters._
	// Count the number of times each field occurs
	def mkFieldHistoMap(jns : TraversableOnce[JsonNode]) : Map[String, Int] = {
		val mutMap = new SMHashMap[String, Int]
		jns.foreach(jn => {
			val fldNmzIt = jn.fieldNames()
			val fnmzLst = fldNmzIt.asScala.toList
			fnmzLst.foreach(fn => {
				val oldCnt : Int = mutMap.getOrElse(fn, 0)
				val upCnt = oldCnt + 1
				mutMap.put(fn, upCnt)
			})
		})
		mutMap.toMap
	}
	def mkUniqFieldValHistoMap(jns : TraversableOnce[JsonNode], fieldNm : String) : Map[String, Int] = {
		val mutMap = new SMHashMap[String, Int]
		jns.foreach(jn => {
			val fld: JsonNode = jn.get(fieldNm)
			val fvtxt = fld.asText("NOT_A_VALUE_FIELD")
			val oldCnt : Int = mutMap.getOrElse(fvtxt, 0)
			val upCnt = oldCnt + 1
			mutMap.put(fvtxt, upCnt)
		})
		mutMap.toMap
	}
	def chkElemTypz(arrNode : ArrayNode): Either[Map[JsonNodeType, Int], JsonNodeType] = {
		val histMap = new SMHashMap[JsonNodeType,Int]()
		val elemIt = arrNode.iterator()
		while (elemIt.hasNext) {
			val elem = elemIt.next()
			val elemTyp: JsonNodeType = elem.getNodeType
			val prevCnt = histMap.getOrElse(elemTyp, 0)
			histMap.put(elemTyp, prevCnt + 1)
		}
		val m = histMap.toMap
		if (m.size == 1) {
			Right(m.head._1)
		} else Left(m)
	}
	lazy private val jckOM = new ObjectMapper
	lazy private val jwpp = jckOM.writerWithDefaultPrettyPrinter()

	def prettyPrint(jn : JsonNode) : String = {
		val pptxt = jwpp.writeValueAsString(jn)
		pptxt
	}

}
