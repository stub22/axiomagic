package axmgc.dmo.fin.ontdmp

import org.apache.jena.graph.{Graph => JenaGraph}
import org.apache.jena.ontology.OntModelSpec
import org.apache.jena.rdf.model.{Model => JenaModel, ModelFactory => JenaModelFactory, Resource => JenaResource}
import org.apache.jena.riot.system.RiotLib
import org.apache.jena.riot.{JsonLDWriteContext, RDFDataMgr, RDFFormat, WriterGraphRIOT}
import org.slf4j.{Logger, LoggerFactory}

trait OntDatMdl

trait ChkFibo {
	def getS4JLog : Logger
	private lazy val myLog = getS4JLog
	val path_fiboOnt = "gdat/fibo_ont/fibo_2018Q4_all_4MB.ttl"
	private def loadFiboGrphMdl() : JenaModel = {
		val path = path_fiboOnt
		val mdl = RDFDataMgr.loadModel(path)
		mdl
	}
	lazy val myMdl = loadFiboGrphMdl
	def chkMdlStats : Unit = {

		val mdl = myMdl
		val size = mdl.size()
		myLog.info("Mdl size = {}", size)
		dmpPrfxs
		dumpNSs
		visitImports
		countSubjs
		// visitProps
	}
	import scala.collection.JavaConverters._
	private def dmpPrfxs : Unit = {
		val numPrfxs = myMdl.numPrefixes
		myLog.info("Num prefixes: {}", numPrfxs)
		val prfxMap : java.util.Map[String, String] = myMdl.getNsPrefixMap
		val sclPrfxMp = prfxMap.asScala
		sclPrfxMp.foreach(entry => myLog.debug("k=" + entry._1 + ", v=" + entry._2))
// 		sclPrfxMp.foldLeft("", (k,v) => )

	}
/*
NsIterator listNameSpaces()
(You probably don't want this method; more likely you want the PrefixMapping methods that Model supports.)
List the namespaces used by predicates and types in the model.
This method is really intended for use by the RDF/XML writer, which needs to know these namespaces to generate
correct and vaguely pretty XML.
The namespaces returned are those of
(a) every URI used as a property in the model and
(b) those of every URI that appears as the object of an rdf:type statement.
 */
	private def dumpNSs : Int = {
		var nsCnt = 0;
		val nsIt = myMdl.listNameSpaces()
		while (nsIt.hasNext) {
			val ns = nsIt.nextNs()
			myLog.info("Found NS: {}", ns)
			nsCnt += 1
		}
		myLog.info("Found {} namespaces", nsCnt)
		nsCnt
	}
	private def countSubjs : Int = {
		var subjCnt = 0;
		val subjIt = myMdl.listSubjects()
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
	// first import all necessary types from package `collection.mutable`
	import collection.mutable.{ HashMap => MutHashMap, MultiMap => MutMultiMap, Set => MutSet , TreeSet => MutTreeSet, HashSet => MutHashSet}

	// to create a `MultiMap` the easiest way is to mixin it into a normal
	// `Map` instance
	val mm = new MutHashMap[Int, MutSet[String]] with MutMultiMap[Int, String]
	private def visitImports : Unit = {
		// owl:imports
		// val imprtrs = new scala.collection.mutable.ListBuffer[JenaResource]
		val imprtMMM = new MutHashMap[JenaResource, MutSet[JenaResource]] with MutMultiMap[JenaResource, JenaResource]
		val imprtTgts = new MutHashSet[JenaResource]
		val owlUriTxt = "http://www.w3.org/2002/07/owl#"
		val prop_owlImport = myMdl.getProperty(owlUriTxt, "imports")
		myLog.info("Owl import property: {}", prop_owlImport)
		val stmtIt = myMdl.listStatements(null, prop_owlImport, null)
		var imprtStmtCnt = 0
		while (stmtIt.hasNext) {
			val stmt = stmtIt.nextStatement()
			myLog.trace("Found importer: {}", stmt)
			val subjRes = stmt.getSubject
			val objRes = stmt.getResource
			imprtMMM.addBinding(subjRes, objRes)
			imprtTgts += objRes
			imprtStmtCnt += 1
		}
		// val outList = imprtrs.toList
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
		// val omSpec2 : OntModelSpec =  OntModelSpec.OWL_MEM_LITE
		val omSpec3 : OntModelSpec = OntModelSpec.RDFS_MEM
		val omSpec4 = OntModelSpec.OWL_DL_MEM_RULE_INF

		val ontMdl1 = JenaModelFactory.createOntologyModel(omSpec1, myMdl)
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