package axmgc.web.lnkdt

import java.io.ByteArrayOutputStream

import org.apache.jena.graph.{Graph => JenaGraph}
import org.apache.jena.rdf.model.{Model => JenaModel}
import org.apache.jena.riot.system.{PrefixMap, RiotLib}
import org.apache.jena.riot.{JsonLDWriteContext, RDFDataMgr, RDFFormat, WriterGraphRIOT}
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author stub22
  */

private trait LinkedDataStuff

class RdfJsonLdAdapter {
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)

	val thatModelPath = "gdat/glp_dat_01_owl.ttl"
	val buiTestPath = "gdat/box_ui_cfg_02.ttl"
	def loadThatModel(useBui : Boolean) : JenaModel = {
		val path = if (useBui) buiTestPath else thatModelPath
		val mdl = RDFDataMgr.loadModel(path)
		mdl
	}
	def writeModelToJsonLDString_Pretty (jm : JenaModel) : String = {
		val grph : JenaGraph = jm.getGraph
		val fmt : RDFFormat = RDFFormat.JSONLD_COMPACT_PRETTY
		val jldWCtx : JsonLDWriteContext = new JsonLDWriteContext()
		val outTxt = writeJenGrphToJsonLDStr(grph, fmt, jldWCtx)
		outTxt
	}
	def writeJenGrphToJsonLDStr(jgrph : JenaGraph, fmt : RDFFormat, jldWCtx : JsonLDWriteContext) : String = {
		val outBAOS = new ByteArrayOutputStream
		try {
			val wrtr : WriterGraphRIOT = RDFDataMgr.createGraphWriter(fmt)
			val pm: PrefixMap = RiotLib.prefixMap(jgrph)
			val baseURI : String = null
			wrtr.write(outBAOS, jgrph, pm, baseURI, jldWCtx)
			val outTxt = outBAOS.toString()
			outTxt
		} catch {
			case thr : Throwable => {
				"ERROR=" + thr.toString
			}
		} finally {
			outBAOS.close()
		}
	}

}

