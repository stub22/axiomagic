package axmgc.dmo.ksrc.lean_mthlb

import org.slf4j.{Logger, LoggerFactory}
import axmgc.web.lnch.FallbackLog4J

import java.util.{Map => JMap, Iterator => JIterator}

import scala.collection.immutable.{Seq, Map => SMap}
import scala.collection.mutable.{ListBuffer => SMListBuf}

/*
Using Jackson-Databind, which we already had on classpath thanks to oracle-nosql-client.
Note security vulnerabilities related to reflection in some versions of jackson-databind.
 */
import com.fasterxml.jackson.databind.node.{ObjectNode, ArrayNode}
import com.fasterxml.jackson.databind.{JsonNode}

object TestLeanTreeScan {

	val flg_setupFallbackLog4J = false // Set to false if log4j.properties is expected, e.g. from Jena.
	val myFallbackLog4JLevel = org.apache.log4j.Level.INFO
	lazy val myFLog4J = new FallbackLog4J {}

	// Resource path needs leading '/' in this case!
	private val rsrcPth_lmlExpWebJson = "/gdat/lean_mathlib/lml_exweb_20210521_sz196MB.json"
	private val rsrcPth_lmlExpStrctJson = "/gdat/lean_mathlib/lml_exstruct_20210529_sz91MB.json"

	// lazy val myS4JLogger: Logger = LoggerFactory.getLogger(classOf[LeanExportTreeScanner])

	def main(args: Array[String]) {
		if (flg_setupFallbackLog4J) {
			myFLog4J.setupFallbackLogging(myFallbackLog4JLevel)
		}

		val leanExpScanner = new LeanExportTreeScanner(rsrcPth_lmlExpWebJson, rsrcPth_lmlExpStrctJson)

		leanExpScanner.doScanExpWeb()

		leanExpScanner.doScanExpStrct()

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

doScanExpStrct - Scan of EXP-STRUCT=/gdat/lean_mathlib/lml_exstruct_20210529_sz91MB.json found 1 JSON objNodes
anlyzFieldNames - Got 5 names, first-10=List(decls, instances, mod_docs, notes, tactic_docs), last-10=List(decls, instances, mod_docs, notes, tactic_docs)
grabFieldPairs - Got list of length: 5
doScanExpStrct - Sruct-type descs: List((decls,ARRAY,class com.fasterxml.jackson.databind.node.ArrayNode), (instances,OBJECT,class com.fasterxml.jackson.databind.node.ObjectNode), (mod_docs,OBJECT,class com.fasterxml.jackson.databind.node.ObjectNode), (notes,ARRAY,class com.fasterxml.jackson.databind.node.ArrayNode), (tactic_docs,ARRAY,class com.fasterxml.jackson.databind.node.ArrayNode))
doScanExpStrct - declsArrayNode size: 79515
anlyzFieldNames - Got 644 names, first-10=List(add_action, add_cancel_comm_monoid, add_cancel_monoid, add_comm_group, add_comm_group.is_Z_bilin, add_comm_monoid, add_comm_semigroup, add_group, add_group.fg, add_left_cancel_monoid), last-10=List(uniform_add_group, uniform_space, unique, unique_factorization_monoid, vadd_comm_class, wf_dvd_monoid, witt_vector.is_poly, witt_vector.is_poly₂, wseq.productive, zsqrtd.nonsquare)
 */

class LeanExportTreeScanner(rsrcPth_lmlExpWebJson : String, rsrcPth_lmlExpStrctJson : String) {
	val myS4JLogger: Logger = LoggerFactory.getLogger(this.getClass)
	private val myParsingHelper = new JacksonJsonParsingHelper {}
	private val myJJA = new JacksonJsonAnlyz {}

	def doScanExpWeb() : Unit = {
		val webObjNodes  = myParsingHelper.doScan(rsrcPth_lmlExpWebJson)
		myS4JLogger.info(s"Scan of EXP-WEB=${rsrcPth_lmlExpWebJson} found ${webObjNodes.length} JSON objNodes")
		val webTopNode = webObjNodes.head
		anlyzFieldNames(webTopNode, "web-export-top")
		anlyzFields(webTopNode, true, true)
	}

	val LML_FLD_DECLS = "decls" // array
	val LML_FLD_INSTANCES = "instances" // obj
	val LML_FLD_MOD_DOCS = "mod_docs" // obj
	val LML_FLD_NOTES = "notes" // array
	val LML_FLD_TACTIC_DOCS = "tactic_docs" // array

	val LML_DCLFLD_STRUCT_FLDS = "structure_fields"
	val LML_DCLFLD_NAME = "name"
	val LML_DCLFLD_LINE = "line"
	val LML_DCLFLD_EQNS = "equations"
	val LML_DCLFLD_CNSTRCTRS = "constructors"
	val LML_DCLFLD_ATTRS = "attributes"
	val LML_DCLFLD_FNAME = "filename"
	val LML_DCLFLD_ARGS = "args"
	val LML_DCLFLD_DOC_STRNG = "doc_string"
	val LML_DCLFLD_KIND = "kind"
	val LML_DCLFLD_TYPE = "type"
	val LML_DCLFLD_FLG_META = "is_meta"

	def doScanExpStrct() : Unit = {
		val strctObjNodes = myParsingHelper.doScan(rsrcPth_lmlExpStrctJson)
		myS4JLogger.info(s"Scan of EXP-STRUCT=${rsrcPth_lmlExpStrctJson} found ${strctObjNodes.length} JSON objNodes")
		val strctTopNode = strctObjNodes.head
		anlyzFieldNames(strctTopNode, "struct-top")
		val strctPairsList: Seq[(String, JsonNode)] = grabFieldPairs(strctTopNode)
		val typDescs = strctPairsList.map(nmNd => (nmNd._1, nmNd._2.getNodeType, nmNd._2.getClass))
		myS4JLogger.info(s"Struct-type descs: ${typDescs}")
		anlyzDecls(strctTopNode)
		anlyzInstncs(strctTopNode)
		val modDocsON = strctTopNode.get(LML_FLD_MOD_DOCS).asInstanceOf[ObjectNode]
		anlyzFieldNames(modDocsON, LML_FLD_MOD_DOCS)
		val notesAN = strctTopNode.get(LML_FLD_NOTES).asInstanceOf[ArrayNode]
		anlyzArr(notesAN, LML_FLD_NOTES)
		val tacticsAN = strctTopNode.get(LML_FLD_TACTIC_DOCS).asInstanceOf[ArrayNode]
		anlyzArr(tacticsAN, LML_FLD_TACTIC_DOCS)
	}
	private def anlyzDecls(strctTopNode : ObjectNode) : Unit = {
		import scala.collection.JavaConverters._
		val declsAN: ArrayNode = strctTopNode.get(LML_FLD_DECLS).asInstanceOf[ArrayNode]
		anlyzArr(declsAN, LML_FLD_DECLS)
		val declNodes: Iterator[JsonNode] = declsAN.iterator().asScala
		val declsFieldHistoMap = myJJA.mkFieldHistoMap(declNodes)
		myS4JLogger.info(s"Decls field histogram: ${declsFieldHistoMap}")
		// Decls field histogram: Map(structure_fields -> 79515, name -> 79515, line -> 79515, equations -> 79515, constructors -> 79515, attributes -> 79515, filename -> 79515, args -> 79515, doc_string -> 79515, kind -> 79515, type -> 79515, is_meta -> 79515)
		val firstDecls: Array[JsonNode] = declsAN.iterator().asScala.take(3).toArray
		(0 to 2).foreach(idx => {
			myS4JLogger.warn(s"decl[${idx}] = ${myJJA.prettyPrint(firstDecls(idx))}")
		})
	}
	private def anlyzInstncs(strctTopNode : ObjectNode) : Unit = {
		import scala.collection.JavaConverters._
		val instncsON : ObjectNode = strctTopNode.get(LML_FLD_INSTANCES).asInstanceOf[ObjectNode]
		anlyzFieldNames(instncsON, LML_FLD_INSTANCES)
		val fpList: Seq[(String, JsonNode)] = grabFieldPairs(instncsON)
		// val firstPairs: Array[(String, JsonNode)] = instncsON.iterator().asScala.take(3).toArray
		(0 to 2).foreach(idx => {
			val instPair = fpList(idx)
			val (instNm, instRec) = instPair
			val instRecDump = instRec.toString
			val recDumpLen = instRecDump.length
			val recDumpHead = instRecDump.take(256)
			myS4JLogger.info(s"inst[${idx}]: name=${instNm} rec-dump-len=${recDumpLen} rec-dump-head=${recDumpHead}")
		})
	}
	def anlyzFieldNames(jsonObj : ObjectNode, objDesc : String) : Unit = {
		import scala.collection.JavaConverters._
		val fldNmzIt = jsonObj.fieldNames()
		val fnmzLst = fldNmzIt.asScala.toList
		myS4JLogger.info(s"obj_${objDesc} has ${fnmzLst.length} field names, first-10=${fnmzLst.take(10)}, last-10=${fnmzLst.takeRight(10)}")
	}
	def grabFieldPairs(jsonObj : ObjectNode) : List[(String, JsonNode)] = {
		// FIXME:  Redo in fewer lines+mutOps, using conversions
		val fldzIt: JIterator[JMap.Entry[String, JsonNode]] = jsonObj.fields()
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
		val fpList: Seq[(String, JsonNode)] = grabFieldPairs(jsonObj)
		val namesOnly: Seq[String] = fpList.map(_._1)
		myS4JLogger.info(s"First 1000 names ${namesOnly.take(1000)}")
		myS4JLogger.info(s"Last 1000 names ${namesOnly.takeRight(1000)}")
		logBar()
		if (flg_dmpPairs) {
			val m: SMap[String, JsonNode] = fpList.toMap
			val firstPairs: Seq[(String, JsonNode)] = fpList.take(3)

			myS4JLogger.info(s"First 3 pairs: ${firstPairs}")
			firstPairs.foreach(p => {
				myS4JLogger.info(s"name=${p._1}, node=[${myJJA.prettyPrint(p._2)}]")
			})
			logBar()
		}
		if (flg_dmpNodes) {
			val firstNodes: Seq[JsonNode] = fpList.take(3).map(_._2)
			myS4JLogger.info(s"First 3 nodes: ${firstNodes}")
			firstNodes.foreach(n => {
				myS4JLogger.info(s"Pretty Node: ${myJJA.prettyPrint(n)}")
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

}

/*
interface DeclInfo {
  name: string;
  args: efmt[];
  type: efmt;
  doc_string: string;
  filename: string;
  line: int;
  attributes: string[];
  equations: efmt[];
  kind: string;
  structure_fields: [string, efmt][];
  constructors: [string, efmt][];
}
```
Where efmt is defined as follows ('c' is a concatenation, 'n' is nesting):
```typescript
type efmt = ['c', efmt, efmt] | ['n', efmt] | string;
```
 */

/*
arrgh

 */