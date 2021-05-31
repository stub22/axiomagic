package axmgc.dmo.ksrc.lean_mthlb

import org.slf4j.{Logger, LoggerFactory}
import axmgc.web.lnch.FallbackLog4J

import java.io.{InputStream}
import java.net.{URL, URLConnection}
import scala.io.Source

import java.util
import java.util.{Map => JMap}

import scala.collection.{Map => SMap}
import scala.collection.mutable.{ListBuffer => SMListBuf, HashMap => SMHashMap}

/*
Using Jackson-Databind, which we already had on classpath thanks to oracle-nosql-client.
Note security vulnerabilities related to reflection in some versions of jackson-databind.
 */
import com.fasterxml.jackson.databind.node.{JsonNodeType, ObjectNode, ArrayNode}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}

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
	// "export_db.json.gz", which defines 7 fields for each documented library element:
	//  "filename", "kind", "is_meta", "line", "src_link", "docs_link", "decl_header_html"
	// A snapshot of this file is manually copied into local build tree as lml_exweb_20210521_sz196MB.json
	// That snapshot contains 84,502 library elements.
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
		val webObjNodes  = doScan(pth_lmlExpWebJson)
		myS4JLogger.info(s"Scan of EXP-WEB=${pth_lmlExpWebJson} found ${webObjNodes.length} JSON objNodes")
		val webTopNode = webObjNodes.head
		anlyzFieldNames(webTopNode, "web-export-top")
		anlyzFields(webTopNode, true, true)
	}
/*
doScanExpStrct - Scan of EXP-STRUCT=/gdat/lean_mathlib/lml_exstruct_20210529_sz91MB.json found 1 JSON objNodes
anlyzFieldNames - Got 5 names, first-10=List(decls, instances, mod_docs, notes, tactic_docs), last-10=List(decls, instances, mod_docs, notes, tactic_docs)
grabFieldPairs - Got list of length: 5
doScanExpStrct - Sruct-type descs: List((decls,ARRAY,class com.fasterxml.jackson.databind.node.ArrayNode), (instances,OBJECT,class com.fasterxml.jackson.databind.node.ObjectNode), (mod_docs,OBJECT,class com.fasterxml.jackson.databind.node.ObjectNode), (notes,ARRAY,class com.fasterxml.jackson.databind.node.ArrayNode), (tactic_docs,ARRAY,class com.fasterxml.jackson.databind.node.ArrayNode))
doScanExpStrct - declsArrayNode size: 79515
anlyzFieldNames - Got 644 names, first-10=List(add_action, add_cancel_comm_monoid, add_cancel_monoid, add_comm_group, add_comm_group.is_Z_bilin, add_comm_monoid, add_comm_semigroup, add_group, add_group.fg, add_left_cancel_monoid), last-10=List(uniform_add_group, uniform_space, unique, unique_factorization_monoid, vadd_comm_class, wf_dvd_monoid, witt_vector.is_poly, witt_vector.is_poly₂, wseq.productive, zsqrtd.nonsquare)

 */
	private val myJJA = new JacksonJsonAnlyz {}
	val LML_FLD_DECLS = "decls" // array
	val LML_FLD_INSTANCES = "instances" // obj
	val LML_FLD_MOD_DOCS = "mod_docs" // obj
	val LML_FLD_NOTES = "notes" // array
	val LML_FLD_TACTIC_DOCS = "tactic_docs" // array
	def doScanExpStrct() : Unit = {
		val strctObjNodes = doScan(pth_lmlExpStrctJson)
		myS4JLogger.info(s"Scan of EXP-STRUCT=${pth_lmlExpStrctJson} found ${strctObjNodes.length} JSON objNodes")
		val strctTopNode = strctObjNodes.head
		anlyzFieldNames(strctTopNode, "struct-top")
		val strctPairsList: Seq[(String, JsonNode)] = grabFieldPairs(strctTopNode)
		val typDescs = strctPairsList.map(nmNd => (nmNd._1, nmNd._2.getNodeType, nmNd._2.getClass))
		myS4JLogger.info(s"Sruct-type descs: ${typDescs}")
		val declsAN: ArrayNode = strctTopNode.get(LML_FLD_DECLS).asInstanceOf[ArrayNode]
		anlyzArr(declsAN, "decls")
		val instncsON : ObjectNode = strctTopNode.get(LML_FLD_INSTANCES).asInstanceOf[ObjectNode]
		anlyzFieldNames(instncsON, "instances")
		val modDocsON = strctTopNode.get(LML_FLD_MOD_DOCS).asInstanceOf[ObjectNode]
		anlyzFieldNames(modDocsON, "mod-docs")
		val notesAN = strctTopNode.get(LML_FLD_NOTES).asInstanceOf[ArrayNode]
		anlyzArr(notesAN, "notes")
		val tacticsAN = strctTopNode.get(LML_FLD_TACTIC_DOCS).asInstanceOf[ArrayNode]
		anlyzArr(tacticsAN, "tactic-docs")
	}

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
	lazy private val jckOM = new ObjectMapper
	lazy private val jwpp = jckOM.writerWithDefaultPrettyPrinter()
	def jacksonParse(inStrm : InputStream) : JsonNode = {
		val jt: JsonNode = jckOM.readTree(inStrm)
		val nodeType: JsonNodeType = jt.getNodeType
		myS4JLogger.info(s"Read JsonNode of class ${jt.getClass}, with JsonNodeType=${nodeType}")
		jt
	}

	def anlyzFieldNames(jsonObj : ObjectNode, objDesc : String) : Unit = {
		import scala.collection.JavaConverters._
		val fldNmzIt = jsonObj.fieldNames()
		val fnmzLst = fldNmzIt.asScala.toList
		myS4JLogger.info(s"obj_${objDesc} has ${fnmzLst.length} field names, first-10=${fnmzLst.take(10)}, last-10=${fnmzLst.takeRight(10)}")
	}
	def grabFieldPairs(jsonObj : ObjectNode) : List[(String, JsonNode)] = {
		// FIXME:  Do in fewer lines as a conversion
		val fldzIt: util.Iterator[JMap.Entry[String, JsonNode]] = jsonObj.fields()
		val lbuf = new SMListBuf[(String, JsonNode)]
		while (fldzIt.hasNext) {
			val fldEntry: JMap.Entry[String, JsonNode] = fldzIt.next()
			lbuf.append((fldEntry.getKey, fldEntry.getValue))
		}
		val l = lbuf.toList
		myS4JLogger.info(s"Got list of length: ${l.length}")
		l
	}
	def anlyzFields(jsonObj : ObjectNode, flg_dmpPairs : Boolean, flg_dmpNodes : Boolean) : Unit = {
		import scala.collection.JavaConverters._
		val fpList = grabFieldPairs(jsonObj)
		val namesOnly: Seq[String] = fpList.map(_._1)
		myS4JLogger.info(s"First 1000 names ${namesOnly.take(1000)}")
		myS4JLogger.info(s"Last 1000 names ${namesOnly.takeRight(1000)}")
		logBar()
		if (flg_dmpPairs) {
			val m: SMap[String, JsonNode] = fpList.toMap
			val firstPairs: Seq[(String, JsonNode)] = fpList.take(3)

			myS4JLogger.info(s"First 3 pairs: ${firstPairs}")
			firstPairs.foreach(p => {
				myS4JLogger.info(s"name=${p._1}, node=[${prettyPrint(p._2)}]")
			})
			logBar()
		}
		if (flg_dmpNodes) {
			val firstNodes: Seq[JsonNode] = fpList.take(3).map(_._2)
			myS4JLogger.info(s"First 3 nodes: ${firstNodes}")
			firstNodes.foreach(n => {
				myS4JLogger.info(s"Pretty Node: ${prettyPrint(n)}")
			})
			logBar()
		}
		val allNodes: Seq[JsonNode] = fpList.map(_._2)
		val fnHistoMap = myJJA.mkFieldHistoMap(allNodes)
		myS4JLogger.info(s"Field Histo Map: ${fnHistoMap}")
		val kindMap = myJJA.mkUniqFieldValHistoMap(allNodes, "kind")
		myS4JLogger.info(s"Kind Histo Map: ${kindMap}")
	}
	def anlyzArr(arrNode : ArrayNode, arrDesc : String) : Unit = {
		val elemTypSig = myJJA.chkElemTypz(arrNode)
		myS4JLogger.info(s"Array[${arrDesc}] has ${arrNode.size()} elements, with type-sig: ${elemTypSig}")
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
	def mkUniqFieldValHistoMap(jns : Traversable[JsonNode], fieldNm : String) : Map[String, Int] = {
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

}