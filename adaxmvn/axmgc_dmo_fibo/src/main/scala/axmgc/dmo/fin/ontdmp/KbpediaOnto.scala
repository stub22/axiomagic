package axmgc.dmo.fin.ontdmp
import org.apache.jena.rdf.model.{Model => JenaMdl}
import org.apache.jena.riot.RDFDataMgr
import org.slf4j.{Logger, LoggerFactory}

trait KbpediaOnto

trait KBPediaOntoLoader {
	private val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)

	private val pth_kbp_v20 = "gdat/kbpedia/kbpedia_ref_cncpts_v20_36MB.n3"

	private val pth_kbp_v25 = "gdat/kbpedia/kbp_ref_concepts_v250_sz39MB.n3"
	private val pth_kko_v25 = "gdat/kbpedia/kko_v250_sz366KB.n3"

	private val fldr_kbtyp_v25 = "gdat/kbpedia/typologies_v250/"

	private val myMdl_KBPRC = loadJenaModelFromRsrc(pth_kbp_v25)
	private val myMdl_KKO = loadJenaModelFromRsrc(pth_kko_v25)

	private def loadJenaModelFromRsrc(rsrcPth : String) : JenaMdl = {
		val pth = pth_kbp_v25
		myS4JLog.info("Starting load of jena model from rsrcPth: {}", rsrcPth)
		val mdl = RDFDataMgr.loadModel(rsrcPth)
		myS4JLog.info("Finished load, model size: {}", mdl.size())
		mdl
	}

	def getKBPRC_model = myMdl_KBPRC

	def getKKO_model = myMdl_KKO

	private val all_typo_fnms = List("ActionTypes-typology.n3",
		"AdjunctualAttributes-typology.n3",
		"Agents-typology.n3",
		"Animals-typology.n3",
		"AreaRegion-typology.n3",
		"Artifacts-typology.n3",
		"Associatives-typology.n3",
		"AtomsElements-typology.n3",
		"AttributeTypes-typology.n3",
		"AudioInfo-typology.n3",
		"AVInfo-typology.n3",
		"BiologicalProcesses-typology.n3",
		"Chemistry-typology.n3",
		"Concepts-typology.n3",
		"ConceptualSystems-typology.n3",
		"Constituents-typology.n3",
		"ContextualAttributes-typology.n3",
		"CopulativeRelations-typology.n3",
		"Denotatives-typology.n3",
		"DirectRelations-typology.n3",
		"Diseases-typology.n3",
		"Drugs-typology.n3",
		"EconomicSystems-typology.n3",
		"EmergentKnowledge-typology.n3",
		"Eukaryotes-typology.n3",
		"EventTypes-typology.n3",
		"Facilities-typology.n3",
		"FoodDrink-typology.n3",
		"Forms-typology.n3",
		"Generals-typology.n3",
		"Geopolitical-typology.n3",
		"Indexes-typology.n3",
		"Information-typology.n3",
		"InquiryMethods-typology.n3",
		"IntrinsicAttributes-typology.n3",
		"KnowledgeDomains-typology.n3",
		"LearningProcesses-typology.n3",
		"LivingThings-typology.n3",
		"LocationPlace-typology.n3",
		"Manifestations-typology.n3",
		"MediativeRelations-typology.n3",
		"Methodeutic-typology.n3",
		"NaturalMatter-typology.n3",
		"NaturalPhenomena-typology.n3",
		"NaturalSubstances-typology.n3",
		"OrganicChemistry-typology.n3",
		"OrganicMatter-typology.n3",
		"Organizations-typology.n3",
		"Persons-typology.n3",
		"Places-typology.n3",
		"Plants-typology.n3",
		"Predications-typology.n3",
		"PrimarySectorProduct-typology.n3",
		"Products-typology.n3",
		"Prokaryotes-typology.n3",
		"ProtistsFungus-typology.n3",
		"RelationTypes-typology.n3",
		"RepresentationTypes-typology.n3",
		"SecondarySectorProduct-typology.n3",
		"Shapes-typology.n3",
		"SituationTypes-typology.n3",
		"SocialSystems-typology.n3",
		"Society-typology.n3",
		"SpaceTypes-typology.n3",
		"StructuredInfo-typology.n3",
		"Symbolic-typology.n3",
		"Systems-typology.n3",
		"TertiarySectorService-typology.n3",
		"Times-typology.n3",
		"TimeTypes-typology.n3",
		"TopicsCategories-typology.n3",
		"VisualInfo-typology.n3",
		"WrittenInfo-typology.n3",
	)

	private def trialLoadAllTypos() : Unit = {

	}

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
}