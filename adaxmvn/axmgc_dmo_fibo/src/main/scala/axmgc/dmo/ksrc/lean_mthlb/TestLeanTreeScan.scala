package axmgc.dmo.ksrc.lean_mthlb

import java.io.{InputStream, InputStreamReader}
import java.net.{URL, URLConnection}
import java.util
import java.util.{Map => JMap}

import axmgc.web.lnch.FallbackLog4J

import scala.collection.{immutable, mutable, Map => SMap}
/*
Using Jackson-Databind which we already had on classpath thanks to oracle-nosql-client
Note security vulnerabilities related to reflection in some versions of jackson-databind
 */
import com.fasterxml.jackson.databind.node.{JsonNodeType, ObjectNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer
import scala.io.Source

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

		leanExpScanner.doScanExpWeb()

		leanExpScanner.doScanExpStrct()

	}
}
trait ResourceReadUtils {
	val myS4JLogger: Logger = LoggerFactory.getLogger(this.getClass)
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
}
/*
	// As of 2021-May a recent snapshot of the mathlib docs may usually be found at:
	// https://github.com/leanprover-community/mathlib_docs/tree/master/docs
	// in the form of an HTML tree, which also includes a single json dump file
	// "export_db.json.gz", specifying 7 fields
	//  "filename", "kind", "is_meta", "line", "src_link", "docs_link", "decl_header_html"
	// This file copied into build tree as lml_exweb_20210521_sz196MB.json
	//  The github snap does NOT (as of 2021-05) contain the more detailed export.json file
	//  we really want, which is produced by:
	//	https://github.com/leanprover-community/doc-gen/blob/master/src/export_json.lean
	//	using some subset of the mathlib library (or a sufficiently similar collection).
	//  Following that step, print_docs.py does a lot of stuff, including producing the less
	//  structured export_db.json, which is all we had access to until:
	//  2021-05-29:  Received a snapshot of export.json from Bryan G. Chen on Zulip Chat.
	// https://leanprover.zulipchat.com/#narrow/stream/113489-new-members/topic/Studying.20mathlib.20as.20a.20knowledge.20artifact.3B.20.20seeking.20.22expor.2E.2E.2E/near/240727843
	// copied into axio build tree as:  lml_exstruct_20210529_sz91MB.json
*/
class LeanExportTreeScanner() {
	val myS4JLogger: Logger = LoggerFactory.getLogger(this.getClass)
	private val myRrUtils = new ResourceReadUtils {}
	// Resource path needs leading '/' in this case!
	private val pth_lmlExpWebJson = "/gdat/lean_mathlib/lml_exweb_20210521_sz196MB.json"
	private val pth_lmlExpStrctJson = "/gdat/lean_mathlib/lml_exstruct_20210529_sz91MB.json"
	def doScanExpWeb() : Unit = {
		doScan(pth_lmlExpWebJson)
	}
	def doScanExpStrct() : Unit = {
		doScan(pth_lmlExpStrctJson)
	}

	def doScan(rsrcPth : String): Unit = {
		myS4JLogger.info(".doScan() BEGIN")
		myRrUtils.checkResourceLength(rsrcPth)
		val rstrm: InputStream = getClass.getResourceAsStream(rsrcPth)
		if (rstrm == null) {
			val msg = s"Cannot open rsrc at: ${rsrcPth}"
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
		myS4JLogger.info(s".doScan(${rsrcPth}) END")
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
		val fldzIt: util.Iterator[JMap.Entry[String, JsonNode]] = jsonObj.fields()
		val lbuf = new ListBuffer[(String, JsonNode)]
		while (fldzIt.hasNext) {
			val fldEntry: JMap.Entry[String, JsonNode] = fldzIt.next()
			lbuf.append((fldEntry.getKey, fldEntry.getValue))
		}
		val l = lbuf.toList
		myS4JLogger.info(s"Got list of length: ${l.length}")
		val namesOnly: Seq[String] = l.map(_._1)
		myS4JLogger.info(s"First 1000 names ${namesOnly.take(1000)}")
		myS4JLogger.info(s"Last 1000 names ${namesOnly.takeRight(1000)}")
		val m: SMap[String, JsonNode] = lbuf.toMap
		val firstPairs: Seq[(String, JsonNode)] = l.take(3)
		logBar()
		myS4JLogger.info(s"First 3 pairs: ${firstPairs}")
		firstPairs.foreach(p => {
			myS4JLogger.info(s"name=${p._1}, node=[${prettyPrint(p._2)}]")
		})
		logBar()
		val firstNodes: Seq[JsonNode] = l.take(3).map(_._2)
		myS4JLogger.info(s"First 3 nodes: ${firstNodes}")
		firstNodes.foreach(n => {
			myS4JLogger.info(s"Pretty Node: ${prettyPrint(n)}")
		})
		logBar()
		val jja = new JacksonJsonAnlyz {}
		val allNodes: immutable.Seq[JsonNode] = l.map(_._2)
		val fnHistoMap = jja.mkFieldHistoMap(allNodes)
		myS4JLogger.info(s"Field Histo Map: ${fnHistoMap}")
		val kindMap = jja.mkUniqFieldValHistoMap(allNodes, "kind")
		myS4JLogger.info(s"Kind Histo Map: ${kindMap}")
	}
	private def logBar() : Unit = {
		myS4JLogger.info("================================================================")
	}
	def prettyPrint(jn : JsonNode) : String = {
		val pptxt = jwpp.writeValueAsString(jn)
		pptxt
	}
	// private def countKinds()
	def toScalaMap(jsonObj : ObjectNode) : Map[String,JsonNode] = {
		??? // 	val mutaMap = new scala.mutable.
	}
}

trait JacksonJsonAnlyz {
	import scala.collection.JavaConverters._
	// Count the number of times each field occurs
	def mkFieldHistoMap(jns : Traversable[JsonNode]) : Map[String, Int] = {
		val mutMap = new mutable.HashMap[String, Int]
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
	def mkUniqFieldValHistoMap(jns : Traversable[JsonNode], fieldNm : String) : Map[String, Int] = {
		val mutMap = new mutable.HashMap[String, Int]
		jns.foreach(jn => {
			val fld: JsonNode = jn.get(fieldNm)
			val fvtxt = fld.asText("NOT_A_VALUE_FIELD")
			val oldCnt : Int = mutMap.getOrElse(fvtxt, 0)
			val upCnt = oldCnt + 1
			mutMap.put(fvtxt, upCnt)
		})
		mutMap.toMap
	}

}