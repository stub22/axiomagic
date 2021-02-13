package axmgc.web.lnkdt

import org.apache.jena.rdf.model.{Model => JenaMdl}
import org.slf4j.{Logger, LoggerFactory}

private trait LDataTestStuff

trait LDChunkerTest {
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)

	def getSomeJsonLD(useBui : Boolean) : String = {
		val sds = new RdfJsonLdAdapter()
		val mdl: JenaMdl = sds.loadThatModel(useBui)
		val mdmp = mdl.toString
		myS4JLog.debug("Loaded jena model: {}", mdmp)
		val jldTxt: String = sds.writeModelToJsonLDString_Pretty(mdl)
		myS4JLog.debug("Formatted: {}", jldTxt)
		jldTxt
	}

}
