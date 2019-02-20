package axmgc.dmo.fin.ontdmp

import org.apache.jena.graph.{Graph => JenaGraph}
import org.apache.jena.ontology.OntModelSpec
import org.apache.jena.rdf.model.{RDFNode, StmtIterator, Statement => JenaStmt, Model => JenaMdl, ModelFactory => JenaMdlFctry, Property => JenaProp, Resource => JenaRsrc}
import org.apache.jena.riot.system.RiotLib
import org.apache.jena.riot.{JsonLDWriteContext, RDFDataMgr, RDFFormat, WriterGraphRIOT}
import org.slf4j.{Logger, LoggerFactory}
import scala.collection.JavaConverters._

trait OntDatMdl

trait ChkFibo extends FiboVocab with StmtXtractFuncs {
	def getS4JLog: Logger

	protected lazy val myLog = getS4JLog

	private def loadFiboGrphMdl(): JenaMdl = {
		val path = path_fiboOnt
		val mdl = RDFDataMgr.loadModel(path)
		mdl
	}

	lazy protected val myFiboOntMdl = loadFiboGrphMdl

	def chkMdlStats: Unit = {
		//
		val mdl = myFiboOntMdl
		val size = mdl.size()
		myLog.info("Mdl size = {}", size)
		dmpPrfxs
		dumpNSs
		betterOIVisitor(mdl)
		visitRdfTypes(mdl)
		// visitOwlImprts
		countSubjs
		// visitProps
	}

	private def dmpPrfxs: Unit = {
		val numPrfxs = myFiboOntMdl.numPrefixes
		myLog.info("Num prefixes: {}", numPrfxs)
		val prfxMap: java.util.Map[String, String] = myFiboOntMdl.getNsPrefixMap
		val sclPrfxMp = prfxMap.asScala
		sclPrfxMp.foreach(entry => myLog.debug("k=" + entry._1 + ", v=" + entry._2))
		// 		sclPrfxMp.foldLeft("", (k,v) => )

	}

	private def dumpNSs: Int = {
		var nsCnt = 0;
		val nsIt = myFiboOntMdl.listNameSpaces()
		while (nsIt.hasNext) {
			val ns = nsIt.nextNs()
			myLog.info("Found NS: {}", ns)
			nsCnt += 1
		}
		myLog.info("Found {} namespaces", nsCnt)
		nsCnt
	}

	private def countSubjs: Int = {
		var subjCnt = 0;
		val subjIt = myFiboOntMdl.listSubjects()
		while (subjIt.hasNext) {
			val subjRes = subjIt.nextResource()
			if ((subjCnt < 1000) && ((subjCnt % 100) < 10)) {
				myLog.info("Found subj-res: {}", subjRes)
			}
			subjCnt += 1
		}
		myLog.info("Found {} subjects", subjCnt)
		subjCnt
	}

	private def betterOIVisitor(jenaMdl : JenaMdl): Unit = {
		val prop_owlImport: JenaProp = jenaMdl.getProperty(baseUriTxt_owl, propLN_owlImports)
		val imap: Map[JenaRsrc, Traversable[JenaRsrc]] = pullRsrcArcsAtProp(prop_owlImport)
		dumpGroupSummary(imap, "'owl:imports'")
		val redundTgts : Seq[JenaRsrc] = imap.values.flatten.toSeq
		val redundantTgtUris: Seq[String] = redundTgts.map(_.getURI)
		myLog.info("Redundant tgt uri count (same as binding count, right?!): {}", redundantTgtUris.size)
		val uniqTgtUris: Seq[String] = redundantTgtUris.distinct
		myLog.info("Uniq tgt uri count: {}", uniqTgtUris.size)
		myLog.info("Uniq tgt uris: {}", uniqTgtUris)
		// For owl:imports, the target URI often has no local name.
		val redundLocalNames : Seq[String] = redundTgts.map(_.getLocalName)
		val uniqLocNams = redundLocalNames.distinct
		myLog.info("Uniq local name count: {}", uniqLocNams.size)
		myLog.info("Uniq local names: {}", uniqLocNams)
	}
	private def visitRdfTypes(jenaMdl : JenaMdl) : Unit = {
		val prop_rdfType: JenaProp = jenaMdl.getProperty(baseUriTxt_rdf, propLN_rdfType)
		val imap: Map[JenaRsrc, Traversable[JenaRsrc]] = pullRsrcArcsAtProp(prop_rdfType)
		dumpGroupSummary(imap, "'rdf:type'")
	}
	private def dumpGroupSummary(bindMap: Map[JenaRsrc, Traversable[JenaRsrc]], groupLabel : String): Unit = {
		var bindCnt = 0
		myLog.info("==========================================================================")
		myLog.info("Dumping stats for group = {}, bind map has width: {}", groupLabel, bindMap.size)
		bindMap.foreach(pair => {
			val subj = pair._1
			val objLst = pair._2
			val objLstLen = objLst.size
			val dmpd = s"Subj ${subj} bound to ${objLstLen} objects."
			bindCnt += objLstLen
			myLog.info(dmpd)
		})
		myLog.info("Finished dumping stats for group={}, binding count total: {}", groupLabel,  bindCnt)
		myLog.info("==========================================================================")
	}
}
trait BadOldChkrGoAway extends ChkFibo {
	import collection.mutable.{ HashMap => MutHashMap, MultiMap => MutMultiMap, Set => MutSet , HashSet => MutHashSet}

	// val mm = new MutHashMap[Int, MutSet[String]] with MutMultiMap[Int, String]
	private def visitOwlImprts : Unit = {
		// Visits all the owl:imports statements in the onto graph, saving the triples into a multivalued-map.
		val imprtMMM = new MutHashMap[JenaRsrc, MutSet[JenaRsrc]] with MutMultiMap[JenaRsrc, JenaRsrc]
		val imprtTgts = new MutHashSet[JenaRsrc]

		val prop_owlImport = myFiboOntMdl.getProperty(baseUriTxt_owl, propLN_owlImports)
		myLog.debug("Owl import property: {}", prop_owlImport)
		val stmtIt = myFiboOntMdl.listStatements(null, prop_owlImport, null)
		var imprtStmtCnt = 0
		while (stmtIt.hasNext) {
			val stmt = stmtIt.nextStatement()
			myLog.trace("Found importer: {}", stmt)
			val subjRes = stmt.getSubject
			val objRes = stmt.getResource
			imprtMMM.addBinding(subjRes, objRes)
			// val chkd = imprtMMM.
			imprtTgts += objRes
			imprtStmtCnt += 1
		}
		// val outList = imprtrs.toList
		// TODO:  Use format strings to make clearer output
		myLog.info("Import multi-map: {}", imprtMMM)
		myLog.info("Import targets set: {}", imprtTgts)
		val imprtTgtTxt = imprtTgts.map(_.getURI).toSeq.sorted
		imprtTgtTxt.foreach({myLog.debug("TgtURI: {}", _)})
		myLog.info("Import stmt count: {}", imprtStmtCnt)
		myLog.info("Unique imported URI  count: {}", imprtTgts.size)
		myLog.info("Count after sort (should be same) {}", imprtTgtTxt.size)

	}

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
