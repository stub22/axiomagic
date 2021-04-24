package axmgc.dmo.fin.ontdmp
import java.io.StringReader

import org.apache.jena.rdf.model.{ModelFactory, Model => JenaMdl}
import org.apache.jena.riot.{Lang, RDFDataMgr}
import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source

trait KbpediaOnto

trait KBPediaOntoLoader {
	private val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)

	private val pth_kbp_v20 = "gdat/kbpedia/kbpedia_ref_cncpts_v20_36MB.n3"

	private val pth_kbp_v25 = "gdat/kbpedia/kbp_ref_concepts_v250_sz39MB.n3"
	private val pth_kko_v25 = "gdat/kbpedia/kko_v250_sz366KB.n3"

	private val pth_kbpPrefixes_v25 = "gdat/kbpedia/kbprc_prefixes.n3"
	private val fldr_kbpTyp_v25 = "gdat/kbpedia/typologies_v250/"

	private val myMdl_KBPRC = loadJenaModelFromRsrc(pth_kbp_v25)
	private val myMdl_KKO = loadJenaModelFromRsrc(pth_kko_v25)

	private def loadJenaModelFromRsrc(rsrcPth : String) : JenaMdl = {
		myS4JLog.info("Starting load of jena model from rsrcPth: {}", rsrcPth)
		val mdl = RDFDataMgr.loadModel(rsrcPth)
		myS4JLog.info("Finished load, model size: {}", mdl.size())
		mdl
	}
	private def readIntoJenaModelFromRsrc(tgtMdl : JenaMdl, rsrcPth : String) : Unit = {
		// This does not seem able to use prefixes already defined in the tgtMdl.
		// (Parser lives upstream, and wants to be in charge of its own prefixes).
		myS4JLog.info("Starting read of jena model from rsrcPth: {} into existing model of size: {}", rsrcPth, tgtMdl.size())
		RDFDataMgr.read(tgtMdl, rsrcPth)
		myS4JLog.info("Finished load, final model size is: {}", tgtMdl.size())
	}

	def getKBPRC_model = myMdl_KBPRC

	def getKKO_model = myMdl_KKO

	def readTextResourceLines(pthToRsrc: String): Seq[String] = {
		myS4JLog.info("Opening text resource at path: {}", pthToRsrc)
		val src = Source.fromResource(pthToRsrc, getClass.getClassLoader)
		val textLines: Iterator[String] = src.getLines()
		val lineSeq = textLines.toList
		src.close()
		myS4JLog.info("Read {} lines from {} ", lineSeq.size, pthToRsrc )
		lineSeq
	}
	def loadAllTyposUsingPrefixesAsText(): Unit = {
		val prefixTextLines = readTextResourceLines(pth_kbpPrefixes_v25)
		all_kbpedia_typo_fnms.foreach(fnm => {
			val kbpTypoMdl = loadTypoModelWithPrefixLines(fnm, prefixTextLines)
		})
	}
	def loadTypoModelWithPrefixLines(fNameTail : String, prefixLines : Seq[String]) : JenaMdl = {
		val mdlRsrcPath = fldr_kbpTyp_v25 + fNameTail
		val mdlLines = readTextResourceLines(mdlRsrcPath)
		val comboLines = prefixLines ++ mdlLines
		myS4JLog.info("loadTypoModelWithPrefixLines for {} got line counts:  prefix={}, model={}, combo={}",
					mdlRsrcPath, prefixLines.size : Integer, mdlLines.size : Integer, comboLines.size: Integer)
		val comboText = comboLines.mkString("\n")
		val txtRdr = new StringReader(comboText)
		val jmdl = ModelFactory.createDefaultModel()
		RDFDataMgr.read(jmdl, txtRdr, mdlRsrcPath, Lang.N3)
		myS4JLog.info("Finished load from {}, final model size is: {}", mdlRsrcPath, jmdl.size())
		jmdl
	}
	private def brokenTrialLoadAllTypos() : Unit = {
		val prfxModel = loadJenaModelFromRsrc(pth_kbpPrefixes_v25)
		val prfxMap = prfxModel.getNsPrefixMap
		myS4JLog.info("Loaded prefixes model: {}", prfxModel)
		all_kbpedia_typo_fnms.foreach(fnm => {
			val jmdl = brokenWorkaroundForLoadTypologyModel(fnm, prfxModel)
		})
	}
	private def brokenWorkaroundForLoadTypologyModel(fNameTail : String, prefixModel : JenaMdl) : JenaMdl = {
		/*
		Failed workaround for missing prefixes in the typology source files.
		Parser does not use prefixes that exist in the target model.
		Seems that to make it work we would need to create a special ParserProfile,
		which is not commonly done.
https://www.javadoc.io/static/org.apache.jena/jena-arq/3.8.0/org/apache/jena/riot/system/ParserProfile.html
		Note that makeParserProfile() here always starts with an empty prefix map:
			 prefixMap = PrefixMapFactory.create();
		https://github.com/apache/jena/blob/main/jena-arq/src/main/java/org/apache/jena/riot/RDFParser.java
		 */
		val prfxMap = prefixModel.getNsPrefixMap
		myS4JLog.info("Applying prefix mapping: {}", prfxMap)
		val jmdl = ModelFactory.createDefaultModel()
		jmdl.setNsPrefixes(prfxMap)
		val rsrcPth = fldr_kbpTyp_v25 + fNameTail
		readIntoJenaModelFromRsrc(jmdl, rsrcPth)
		// val jmdl = loadJenaModelFromRsrc(rsrcPth)
		jmdl
	}

	private val all_kbpedia_typo_fnms = List("ActionTypes-typology.n3",	"AdjunctualAttributes-typology.n3", "Agents-typology.n3",
		"Animals-typology.n3", "AreaRegion-typology.n3", "Artifacts-typology.n3", "Associatives-typology.n3", "AtomsElements-typology.n3",
		"AttributeTypes-typology.n3", "AudioInfo-typology.n3", "AVInfo-typology.n3", "BiologicalProcesses-typology.n3",
		"Chemistry-typology.n3", "Concepts-typology.n3", "ConceptualSystems-typology.n3", "Constituents-typology.n3",
		"ContextualAttributes-typology.n3",	"CopulativeRelations-typology.n3", "Denotatives-typology.n3", "DirectRelations-typology.n3",
		"Diseases-typology.n3", "Drugs-typology.n3", "EconomicSystems-typology.n3", "EmergentKnowledge-typology.n3", "Eukaryotes-typology.n3",
		"EventTypes-typology.n3", "Facilities-typology.n3",	"FoodDrink-typology.n3", "Forms-typology.n3", "Generals-typology.n3",
		"Geopolitical-typology.n3", "Indexes-typology.n3", "Information-typology.n3", "InquiryMethods-typology.n3",
		"IntrinsicAttributes-typology.n3", "KnowledgeDomains-typology.n3", "LearningProcesses-typology.n3", "LivingThings-typology.n3",
		"LocationPlace-typology.n3", "Manifestations-typology.n3", "MediativeRelations-typology.n3", "Methodeutic-typology.n3",
		"NaturalMatter-typology.n3", "NaturalPhenomena-typology.n3", "NaturalSubstances-typology.n3", "OrganicChemistry-typology.n3",
		"OrganicMatter-typology.n3", "Organizations-typology.n3", "Persons-typology.n3", "Places-typology.n3", "Plants-typology.n3",
		"Predications-typology.n3", "PrimarySectorProduct-typology.n3", "Products-typology.n3", "Prokaryotes-typology.n3",
		"ProtistsFungus-typology.n3", "RelationTypes-typology.n3", "RepresentationTypes-typology.n3", "SecondarySectorProduct-typology.n3",
		"Shapes-typology.n3", "SituationTypes-typology.n3", "SocialSystems-typology.n3", "Society-typology.n3", "SpaceTypes-typology.n3",
		"StructuredInfo-typology.n3", "Symbolic-typology.n3", "Systems-typology.n3", "TertiarySectorService-typology.n3",
		"Times-typology.n3", "TimeTypes-typology.n3", "TopicsCategories-typology.n3", "VisualInfo-typology.n3",	"WrittenInfo-typology.n3")

}

trait KBPediaOntoWrap extends MdlDmpFncs {
	private val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)
	override protected def getS4JLog: Logger = myS4JLog

	private val myOntLoader = new KBPediaOntoLoader {}
	private val ontQryMgr = new OntQryMgr

	def dumpKbprcStatsToLogAndJsonTxt(): String = {
		val kbprcMdl = myOntLoader.getKBPRC_model
		val kbprcStatJsnTxt = ontQryMgr.dumpMdlStatsToJsnArrTxt(kbprcMdl)
		kbprcStatJsnTxt
	}
	def dumpKkoStatsToLogAndJsonTxt(): String = {
		val kkoMdl = myOntLoader.getKKO_model
		val kkoStatJsnTxt = ontQryMgr.dumpMdlStatsToJsnArrTxt(kkoMdl)
		kkoStatJsnTxt
	}
	def dumpTypoStats : String = {
		myOntLoader.loadAllTyposUsingPrefixesAsText() // brokenTrialLoadAllTypos()
		"done"
	}
}