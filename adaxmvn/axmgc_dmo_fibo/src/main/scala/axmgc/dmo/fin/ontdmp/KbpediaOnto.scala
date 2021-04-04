package axmgc.dmo.fin.ontdmp
import org.apache.jena.rdf.model.{Model => JenaMdl}
import org.apache.jena.riot.RDFDataMgr
import org.slf4j.{Logger, LoggerFactory}

trait KbpediaOnto

trait KBPediaOntoWrap extends MdlDmpFncs {
	private val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)
	override protected def getS4JLog: Logger = myS4JLog

	private val pth_kbp_v20 = "gdat/kbpedia/kbpedia_ref_cncpts_v20_36MB.n3"
	private val pth_kbp_v25 = "gdat/kbpedia/kbp_ref_concepts_v250_sz39MB.n3"

	private val myMdl_KBPRC = loadKBRefCnc
	private val ontQryMgr = new OntQryMgr

	private def loadKBRefCnc : JenaMdl = {
		val pth = pth_kbp_v25
		myS4JLog.info("Starting load of ref concepts from rsrc: {}", pth)
		val mdl = RDFDataMgr.loadModel(pth)
		myS4JLog.info("Finished load, model size: {}", mdl.size())
		mdl
	}

	def getKBP_model = myMdl_KBPRC

	def dumpStatsToLogAndJsonTxt(): String = {
		val resultJsnTxt = ontQryMgr.dumpMdlStatsToJsnArrTxt(myMdl_KBPRC)
		resultJsnTxt
	}

}