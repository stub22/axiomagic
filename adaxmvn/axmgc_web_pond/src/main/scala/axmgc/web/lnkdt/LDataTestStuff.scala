package axmgc.web.lnkdt

import org.slf4j.{Logger, LoggerFactory}

private trait LDataTestStuff

trait LDChunkerTest {
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)

	def getSomeJsonLD(useBui : Boolean) : String = {
		val sds = new RdfJsonLdAdapter()
		val mdl = sds.loadThatModel(useBui)
		val mdmp = mdl.toString
		myS4JLog.debug("Loaded: {}", mdmp)
		val jldTxt = sds.writeModelToJsonLDString_Pretty(mdl)
		myS4JLog.debug("Formatted: {}", jldTxt)
		jldTxt
	}

}
