package axmgc.dmo.ksrc.lean_mthlb

import java.io.InputStream

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.{JsonNodeType, ObjectNode}
import org.slf4j.{Logger, LoggerFactory}

private trait JacksonJsonParsingStuff

trait JacksonJsonParsingHelper {
	val myS4JLogger: Logger = LoggerFactory.getLogger(this.getClass)
	private val myRrUtils = new ResourceReadUtils {}
	private val myJJOM = new ObjectMapper

	def doScan(rsrcPth : String): List[ObjectNode] = {
		myS4JLogger.info(".doScan() BEGIN")
		myRrUtils.checkResourceLength(rsrcPth)
		val rstrm: InputStream = getClass.getResourceAsStream(rsrcPth)
		if (rstrm == null) {
			val msg = s"Cannot open rsrc at: ${rsrcPth}"
			myS4JLogger.error(msg)
			throw new Exception(msg)
		}
		val jsonNode: JsonNode = jacksonParse(rstrm)
		val rsltLst : List[ObjectNode] = if (jsonNode.isObject) {
			val objNode: ObjectNode = jsonNode.asInstanceOf[ObjectNode]
			List(objNode)
		} else Nil
		// drainStreamToArr(rstrm)
		rstrm.close()
		// myS4JLogger.info(s"Read ${lineCnt} lines and ${chrCnt} total chars")
		myS4JLogger.info(s".doScan(${rsrcPth}) END")
		rsltLst
	}

	def jacksonParse(inStrm : InputStream) : JsonNode = {
		val jt: JsonNode = myJJOM.readTree(inStrm)
		val nodeType: JsonNodeType = jt.getNodeType
		myS4JLogger.info(s"Read JsonNode of class ${jt.getClass}, with JsonNodeType=${nodeType}")
		jt
	}

}
