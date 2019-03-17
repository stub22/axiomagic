package axmgc.dmo.fin.ontdmp
import org.apache.jena.rdf.model.{Model => JenaMdl}
import org.apache.jena.riot.RDFDataMgr
import org.slf4j.{Logger, LoggerFactory}

trait KbpediaOnto



trait KBPediaOntoWrap extends MdlDmpFncs {
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)
	override protected def getS4JLog: Logger = myS4JLog

	private val pth_kbpRefCnc = "gdat/kbpedia/kbpedia_ref_cncpts_v20_36MB.n3"

	lazy val myMdl_KBPRC = loadKBRefCnc
	private def loadKBRefCnc : JenaMdl = {
		val pth = pth_kbpRefCnc
		myS4JLog.info("Starting load of ref concepts from rsrc: {}", pth)
		val mdl = RDFDataMgr.loadModel(pth)
		myS4JLog.info("Finished load, model size: {}", mdl.size())
		mdl
	}

	def dumpStatsToLog(): Unit = {
		dumpSomeModelStatsToLog(myMdl_KBPRC)
	}

}