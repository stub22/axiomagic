package axmgc.dmo.xpr.io.prqt

import org.slf4j.{Logger, LoggerFactory}
import axmgc.web.lnch.FallbackLog4J
import java.util.{Iterator => JIterator, Map => JMap}

import axmgc.xpr.io.prqt.CopiedFromExGeneric

import scala.collection.immutable.{Seq, Map => SMap}
import scala.collection.mutable.{ListBuffer => SMListBuf}

object TestPrqtIoEasily {

	val flg_setupFallbackLog4J = false // Set to false if log4j.properties is expected, e.g. from Jena.
	val myFallbackLog4JLevel = org.apache.log4j.Level.INFO
	lazy val myFLog4J = new FallbackLog4J {}

	private val rsrcPth_lmlExpWebJson = "/gdat/lean_mathlib/lml_exweb_20210521_sz196MB.json"
	private val rsrcPth_lmlExpStrctJson = "/gdat/lean_mathlib/lml_exstruct_20210529_sz91MB.json"

	def main(args: Array[String]) {
		if (flg_setupFallbackLog4J) {
			myFLog4J.setupFallbackLogging(myFallbackLog4JLevel)
		}

		val prqtSrcScanner = new PrqtSourceScanner(rsrcPth_lmlExpWebJson, rsrcPth_lmlExpStrctJson)

		prqtSrcScanner.doScan()


	}
}

class PrqtSourceScanner(rsrcPth_lmlExpWebJson : String, rsrcPth_lmlExpStrctJson : String) {
	val myS4JLogger: Logger = LoggerFactory.getLogger(this.getClass)

	def doScan() : Unit = {
		logBar()
		myS4JLogger.info(s"Pretending to scan prqt file=${rsrcPth_lmlExpWebJson}, how bow da?")
		doGenericWriteThenRead
		logBar()
	}
	def doGenericWriteThenRead : Unit = {
		val cfeg = new CopiedFromExGeneric {}
		cfeg.writePrqtFile
		cfeg.readPrqtFile
	}
	private def logBar() : Unit = {
		myS4JLogger.info("================================================================")
	}

}