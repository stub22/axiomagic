package axmgc.dmo.fin.ontdmp

import org.apache.jena.graph.{Graph => JenaGraph}
import org.apache.jena.ontology.OntModelSpec
import org.apache.jena.rdf.model.{RDFNode, StmtIterator, Statement => JenaStmt, Model => JenaMdl, ModelFactory => JenaMdlFctry, Property => JenaProp, Resource => JenaRsrc}
import org.apache.jena.riot.system.RiotLib
import org.apache.jena.riot.{JsonLDWriteContext, RDFDataMgr, RDFFormat, WriterGraphRIOT}
import org.slf4j.{Logger, LoggerFactory}
import scala.collection.JavaConverters._

trait OntDatMdl

trait ChkFibo extends FiboVocab with MdlDmpFncs {

	private def loadFiboGrphMdl(): JenaMdl = {
		val path = path_fiboOnt
		val mdl = RDFDataMgr.loadModel(path)
		mdl
	}
	lazy protected val myFiboOntMdl = loadFiboGrphMdl
	def dumpFiboMdlStatsToLog(): Unit = {
		dumpSomeModelStatsToLog(myFiboOntMdl)
	}

}
trait MdlDmpFncs extends  StmtXtractFuncs  with StdGenVocab {
	protected def getS4JLog: Logger

	protected lazy val myLog: Logger = getS4JLog

	def dumpSomeModelStatsToLog(jenaMdl: JenaMdl): Unit = {
		val size = jenaMdl.size()
		getS4JLog.info("jenaMdl size = {}", size)
		dmpPrfxs(jenaMdl)
		dumpNSs(jenaMdl)
		visitOwlImports(jenaMdl)
		visitRdfTypes(jenaMdl)
		visitSubjRsrcs(jenaMdl)
		dumpTypeHistogram(jenaMdl)
		dumpPropsTallyByName(jenaMdl)
		dumpPropsTallyByCount(jenaMdl)

	}

	private def dmpPrfxs(jenaMdl: JenaMdl): Unit = {
		val numPrfxs = jenaMdl.numPrefixes
		myLog.info("Num prefixes: {}", numPrfxs)
		val prfxMap: java.util.Map[String, String] = jenaMdl.getNsPrefixMap
		val sclPrfxMp = prfxMap.asScala
		var prfxIdx = 0
		sclPrfxMp.foreach(entry => {
			if ((prfxIdx < 20) || (prfxIdx % 20 == 0)) {
				myLog.info("Prefix [{}] = {}", prfxIdx, entry)
			}
			prfxIdx += 1
		})
	}

	private def dumpNSs(jenaMdl: JenaMdl): Int = {
		var nsCnt = 0;
		val nsIt = jenaMdl.listNameSpaces()
		while (nsIt.hasNext) {
			val ns = nsIt.nextNs()
			myLog.info("Found NS [{}]: {}", nsCnt, ns)
			nsCnt += 1
		}
		myLog.info("Found {} 'namespaces', using the idiosyncratic and disreputable method:  model.listNameSpaces() ", nsCnt)
		nsCnt
	}

	private def visitSubjRsrcs(jenaMdl: JenaMdl): Int = {
		var subjCnt = 0;
		val subjIt = jenaMdl.listSubjects()
		while (subjIt.hasNext) {
			val subjRes = subjIt.nextResource()
			if ((subjCnt < 700) && ((subjCnt % 100) < 7)) {
				myLog.info("Found subj-res[{}]: {}", subjCnt, subjRes)
			}
			subjCnt += 1
		}
		myLog.info("Found {} subjects", subjCnt)
		subjCnt
	}

	private def visitOwlImports(jenaMdl: JenaMdl): Unit = {
		val prop_owlImport: JenaProp = jenaMdl.getProperty(baseUriTxt_owl, propLN_owlImports)
		val bindMap: Map[JenaRsrc, Traversable[JenaRsrc]] = pullRsrcArcsAtProp(prop_owlImport, false)
		dumpGroupSummary(bindMap, "'owl:imports'")
		dumpUniqTgts(bindMap)
	}

	private def visitRdfTypes(jenaMdl: JenaMdl): Unit = {
		val prop_rdfType: JenaProp = jenaMdl.getProperty(baseUriTxt_rdf, propLN_rdfType)
		val bindMap: Map[JenaRsrc, Traversable[JenaRsrc]] = pullRsrcArcsAtProp(prop_rdfType, false)
		dumpGroupSummary(bindMap, "'rdf:type'")
		dumpUniqTgts(bindMap)
	}

	private def dumpTypeHistogram(jenaMdl: JenaMdl): Unit = {
		val prop_rdfType: JenaProp = jenaMdl.getProperty(baseUriTxt_rdf, propLN_rdfType)
		val bindMap: Map[JenaRsrc, Traversable[JenaRsrc]] = pullRsrcArcsAtProp(prop_rdfType, true)
		// dumpGroupSummary(bindMap, "typeHisto")
		val srtdKys = bindMap.keys.toSeq.sortBy(_.getURI)
		val sampleSize = 3
		var keyCnt = 0
		srtdKys.foreach(kyRsrc => {
			keyCnt += 1
			val vLst = bindMap.get(kyRsrc).getOrElse(Nil)
			val vLstFront = vLst.take(sampleSize)
			val dmpd = s"key ${keyCnt} [${kyRsrc}] bound to ${vLst.size} values.  First ${sampleSize} are: ${vLstFront}"
			myLog.info(dmpd)
		})
	}

	// private def doDump(currCnt, initSeg, mod)
	private def dumpGroupSummary(bindMap: Map[JenaRsrc, Traversable[JenaRsrc]], groupLabel: String): Unit = {
		val initSegLen = 10
		val skipWidth = 25
		val sampleSize = 3
		var bindCnt = 0
		var keyCnt = 0
		myLog.info("==========================================================================")
		myLog.info("Dumping stats for group = {}, bind map has width: {}", groupLabel, bindMap.size)
		bindMap.foreach(pair => {
			val keyRsrc = pair._1
			val vLst = pair._2
			val vLstLen = vLst.size
			keyCnt += 1
			bindCnt += vLstLen
			if (keyCnt <= initSegLen || (keyCnt % skipWidth == 0)) {
				val objLstBegin = vLst.take(sampleSize)
				val dmpd = s"key ${keyCnt} [${keyRsrc}] bound to ${vLstLen} values.  First ${sampleSize} are: ${objLstBegin}"
				myLog.info(dmpd)
			}
		})
		myLog.info("Finished dumping stats for group={}, binding count total: {}", groupLabel, bindCnt)
		myLog.info("==========================================================================")
	}

	private def dumpUniqTgts(bindMap: Map[JenaRsrc, Traversable[JenaRsrc]]): Unit = {
		val redundTgts: Seq[JenaRsrc] = bindMap.values.flatten.toSeq
		val redundantTgtUris: Seq[String] = redundTgts.map(_.getURI)
		myLog.info("Redundant tgt uri count (same as binding count, right?!): {}", redundantTgtUris.size)
		val uniqTgtUris: Seq[String] = redundantTgtUris.distinct.sorted
		myLog.info("Uniq tgt uri count: {}", uniqTgtUris.size)
		myLog.info("Uniq tgt uris: {}", uniqTgtUris)
		// For owl:imports, the target URI often has no local name.
		val redundLocalNames: Seq[String] = redundTgts.map(_.getLocalName)
		val uniqLocNams = redundLocalNames.distinct.sorted
		myLog.info("Uniq local name count: {}", uniqLocNams.size)
		myLog.info("Uniq local names: {}", uniqLocNams)
	}

	def dumpPropsTallyByName(mdl: JenaMdl): Unit = {
		val sckr = new MdlPrpSucker {}
		val tallyMap: Map[JenaProp, Int] = sckr.tallyProps(mdl)
		val propsByURI = tallyMap.keys.toSeq.sortBy(_.getURI)
		myLog.info("Uniq prop count: {}", propsByURI.size)
		dumpPropsTally(propsByURI, tallyMap)
	}

	def dumpPropsTallyByCount(mdl: JenaMdl): Unit = {
		val sckr = new MdlPrpSucker {}
		val tallyMap: Map[JenaProp, Int] = sckr.tallyProps(mdl)
		val pairsByCount: Seq[(JenaProp, Int)] = tallyMap.toSeq.sortBy(_._2)
		myLog.info("Pairs count: {}", pairsByCount.size)
		val propsByCount = pairsByCount.map(_._1)
		dumpPropsTally(propsByCount, tallyMap)
	}

	def dumpPropsTally(propSeq: Seq[JenaProp], tallyMap: Map[JenaProp, Int]): Unit = {
		var propIdx = 0
		propSeq.foreach(prp => {
			propIdx += 1
			val tally = tallyMap.getOrElse(prp, -9999999)
			val dumped = s"Prop[${propIdx}] uri=${prp.getURI} tally=${tally}"
			myLog.info(dumped)
		})
	}
}

trait BadOldChkrGoAway extends ChkFibo {

	private def visitProps : Long = {
		val omSpec1 : OntModelSpec =  OntModelSpec.OWL_MEM_RDFS_INF
		val omSpec3 : OntModelSpec = OntModelSpec.RDFS_MEM
		val omSpec4 = OntModelSpec.OWL_DL_MEM_RULE_INF

		val ontMdl1 = JenaMdlFctry.createOntologyModel(omSpec1, myFiboOntMdl)
		val ontSz_1 = ontMdl1.size()
		myLog.info("ontMdl1 size = {}", ontSz_1)
		ontSz_1
	}
	private def visitOnts : Int = {
		val ontDefUriTxt = "owl:imports"
		// owl: <http://www.w3.org/2002/07/owl#>
		0
	}

}

/*
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.omg.org/spec/LCC/Countries/CountryRepresentation/
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.omg.org/spec/LCC/Countries/ISO3166-1-CountryCodes/
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.omg.org/spec/LCC/Countries/ISO3166-2-SubdivisionCodes/
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.omg.org/spec/LCC/Countries/Regions/ISO3166-2-SubdivisionCodes-CA/
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.omg.org/spec/LCC/Countries/Regions/ISO3166-2-SubdivisionCodes-GB/
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.omg.org/spec/LCC/Countries/Regions/ISO3166-2-SubdivisionCodes-US/
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.omg.org/spec/LCC/Languages/ISO639-1-LanguageCodes/
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.omg.org/spec/LCC/Languages/ISO639-2-LanguageCodes/
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.omg.org/spec/LCC/Languages/LanguageRepresentation/
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.omg.org/techprocess/ab/SpecificationMetadata/
6797 [main] DEBUG axmgc.dmo.fin.ontdmp.TstOntApp  - TgtURI: http://www.w3.org/2004/02/skos/core
 */
/* --- Copied Javadoc from:
// https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/rdf/model/Model.html#listNameSpaces--
NsIterator listNameSpaces()
(You probably don't want this method; more likely you want the PrefixMapping methods that Model supports.)
List the namespaces used by predicates and types in the model.
This method is really intended for use by the RDF/XML writer, which needs to know these namespaces to generate
correct and vaguely pretty XML.
The namespaces returned are those of
(a) every URI used as a property in the model and
(b) those of every URI that appears as the object of an rdf:type statement.
 */
