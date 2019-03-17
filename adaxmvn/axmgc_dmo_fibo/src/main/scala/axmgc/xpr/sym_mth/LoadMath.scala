package axmgc.xpr.sym_mth

import axmgc.dmo.fin.ontdmp.MdlDmpFncs
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.rdf.model.{Model => JenaMdl}
import org.slf4j.{Logger, LoggerFactory}

trait LoadMath

trait ChkFormulas extends MdlDmpFncs {
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)
	override protected def getS4JLog: Logger = myS4JLog
	private val pth_mthFrmlaTst = "gdat/math_tst/gravgm/grvgmdat_A.ttl"

	lazy val myMdl_MathFrmlas = loadMthFrmlas
	private def loadMthFrmlas : JenaMdl = {
		val pth = pth_mthFrmlaTst
		myS4JLog.info("Starting load of math formulas from rsrc: {}", pth)
		val mdl = RDFDataMgr.loadModel(pth)
		myS4JLog.info("Finished math formula load, model size: {}", mdl.size())
		mdl
	}

	def dumpStatsToLog(): Unit = {
		dumpSomeModelStatsToLog(myMdl_MathFrmlas)
	}
}
