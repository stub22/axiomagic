package axmgc.dmo.ksrc.lean_mthlb

import java.io.{InputStream, InputStreamReader}
import java.net.{URL, URLConnection}
import java.util
import java.util.Map

import axmgc.web.lnch.FallbackLog4J
import com.fasterxml.jackson.databind.node.{JsonNodeType, ObjectNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.slf4j.{Logger, LoggerFactory}
import spray.json.ParserInput
import spray.json.ParserInput.DefaultParserInput

import scala.collection.mutable.ListBuffer
import scala.io.Source

/*
Using Jackson which we already had on classpath thanks to oracle-nosql
 */
object TestLeanTreeScan {

	val flg_setupFallbackLog4J = false // Set to false if log4j.properties is expected, e.g. from Jena.
	val myFallbackLog4JLevel = org.apache.log4j.Level.INFO
	lazy val myFLog4J = new FallbackLog4J {}

	lazy val myS4JLogger: Logger = LoggerFactory.getLogger(classOf[LeanExportTreeScanner])

	def main(args: Array[String]) {
		if (flg_setupFallbackLog4J) {
			myFLog4J.setupFallbackLogging(myFallbackLog4JLevel)
		}

		val leanExpScanner = new LeanExportTreeScanner()

		leanExpScanner.doScan()

	}
}
class LeanExportTreeScanner() {
	val myS4JLogger: Logger = LoggerFactory.getLogger(this.getClass)
	// Resource path needs leading '/' in this case!
	private val pth_lmlExpJson = "/gdat/lean_mathlib/lml_export_db_20210521_sz196MB.json"
	def doScan(): Unit = {
		myS4JLogger.info(".doScan() BEGIN")
		checkResourceLength(pth_lmlExpJson)
		val rstrm: InputStream = getClass.getResourceAsStream(pth_lmlExpJson)
		if (rstrm == null) {
			val msg = s"Cannot open rsrc at: ${pth_lmlExpJson}"
			myS4JLogger.error(msg)
			throw new Exception(msg)
		}
		val jsonNode = jacksonParse(rstrm)
		if (jsonNode.isObject) {
			val objNode = jsonNode.asInstanceOf[ObjectNode]
			anlyzJON(objNode)
		}
		// drainStreamToArr(rstrm)
		rstrm.close()
		// myS4JLogger.info(s"Read ${lineCnt} lines and ${chrCnt} total chars")
		myS4JLogger.info(".doScan() END")
	}
	// 191 MB (200,695,650 bytes)
	// Naive read to array in 69s:  Read stream into char-array of len: 200695650
	def drainStreamToArr(rstrm : InputStream): Unit = {
		val isrc =	Source.fromInputStream(rstrm)
		val iarr: Array[Char] = isrc.toArray
		myS4JLogger.info(s"Read stream into char-array of len: ${iarr.length}")

	}
	def checkResourceLength(rpath : String ) : Long = {
		val rsrc: URL = getClass().getResource(rpath)
		myS4JLogger.info(s"Resource URL: ${rsrc}")
		val rconn: URLConnection = rsrc.openConnection()
		val rlen: Int = rconn.getContentLength()
		myS4JLogger.info(s"Resource length: ${rlen}")
		rlen
	}
	private final val EOI = '\uFFFF'

	def javaxParse() : Unit = {
		// javaee javax.json.Json
	}
	lazy private val jckOM = new ObjectMapper
	lazy private val jwpp = jckOM.writerWithDefaultPrettyPrinter()
	def jacksonParse(inStrm : InputStream) : JsonNode = {
		val jt: JsonNode = jckOM.readTree(inStrm)
		val nodeType: JsonNodeType = jt.getNodeType
		myS4JLogger.info(s"Read JsonNode of class ${jt.getClass}, with JsonNodeType=${nodeType}")
		jt
	}
	def anlyzJON(jsonObj : ObjectNode) : Unit = {
		import scala.collection.JavaConverters._
		val fldNmzIt = jsonObj.fieldNames()
		val fnmzLst = fldNmzIt.asScala.toList
		myS4JLogger.info(s"Got ${fnmzLst.length} names, first-10=${fnmzLst.take(10)}, last-10=${fnmzLst.takeRight(10)}")
		val fldzIt: util.Iterator[Map.Entry[String, JsonNode]] = jsonObj.fields()
		val lbuf = new ListBuffer[(String, JsonNode)]
		while (fldzIt.hasNext) {
			val fldEntry: Map.Entry[String, JsonNode] = fldzIt.next()
			lbuf.append((fldEntry.getKey, fldEntry.getValue))
		}
		val l = lbuf.toList
		myS4JLogger.info(s"Got list of length: ${l.length}")
		val namesOnly: Seq[String] = l.map(_._1)
		myS4JLogger.info(s"First 1000 names ${namesOnly.take(1000)}")
		myS4JLogger.info(s"Last 1000 names ${namesOnly.takeRight(1000)}")
		val m: Predef.Map[String, JsonNode] = lbuf.toMap
		val firstNodes: Seq[JsonNode] = l.take(3).map(_._2)
		myS4JLogger.info(s"First 3 nodes: ${firstNodes}")
		firstNodes.foreach(n => {
			myS4JLogger.info(s"Pretty Node: ${prettyPrint(n)}")
		})
	}
	def prettyPrint(jn : JsonNode) : String = {
		val pptxt = jwpp.writeValueAsString(jn)
		pptxt
	}
	def toScalaMap(jsonObj : ObjectNode) : Map[String,JsonNode] = {
		??? // 	val mutaMap = new scala.mutable.
	}
	def sprayJsonDirectParse(rstrm : InputStream, knownLen : Int) : Unit = {
		val rdr = new InputStreamReader(rstrm)
		val ourParserInput = new DefaultParserInput {
			override def nextChar(): Char = {
				val chrInt = rdr.read()
				if (chrInt < 0) EOI else chrInt.toChar
			}

			override def nextUtf8Char(): Char = nextChar

			override def length: Int = knownLen

			override def sliceString(start: Int, end: Int): String = ???

			override def sliceCharArray(start: Int, end: Int): Array[Char] = ???
		}
		// while (rdr.)
	}
	def drainStreamAsLines(rstrm : InputStream): Unit = {
		/*
				val lines: Iterator[String] =
				var lineCnt : Long = 0
				var chrCnt : Long = 0
				lines.foreach(line => {
					lineCnt += 1
					chrCnt += line.length
				})
		 */

	}
}