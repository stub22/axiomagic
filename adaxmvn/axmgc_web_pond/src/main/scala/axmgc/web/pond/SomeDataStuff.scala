package axmgc.web.pond

import java.io.ByteArrayOutputStream

import org.apache.jena.graph.{Graph => JenaGraph}
import org.apache.jena.rdf.model.{Model => JenaModel}
import org.apache.jena.riot.system.RiotLib
import org.apache.jena.riot.{JsonLDWriteContext, RDFDataMgr, RDFFormat, WriterGraphRIOT}
import org.slf4j.{Logger, LoggerFactory}

/**
  * @author stub22
  */

class SomeDataStuff {
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
			val pm = RiotLib.prefixMap(jgrph)
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
trait RsrcNms {
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)

	import scala.io.Source

	private val pth_icNms = "gdat/rsrc_nms/icon_name_x3290.txt"

	private def readTxtLines(pthToRsrc: String): Seq[String] = {
		val src = Source.fromResource(pthToRsrc)
		val lineSeq = src.getLines().toSeq
		myS4JLog.info("Read {} lines", lineSeq.size)
		myS4JLog.info("First 5 lines:  {} ", lineSeq.take(5).toList)
		myS4JLog.info("Last 5 lines:  {} ", lineSeq.takeRight(5).toList)
		lineSeq
	}
	def readIcnNms(): Seq[String] = {
		readTxtLines(pth_icNms)
	}
}
trait TdatChunker {
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)

	def getSomeJsonLD(useBui : Boolean) : String = {
		val sds = new SomeDataStuff()
		val mdl = sds.loadThatModel(useBui)
		val mdmp = mdl.toString
		myS4JLog.debug("Loaded: {}", mdmp)
		val jldTxt = sds.writeModelToJsonLDString_Pretty(mdl)
		myS4JLog.debug("Formatted: {}", jldTxt)
		jldTxt
	}

	def getSomeXhtml5() : String = {
		val banner : String = "<h3>Much Bester Down Here</h3>"
		val gridMkr = new PondGrid {}
		// val pshwrs = gridMkr.
		val rui = new RectUiFuncs {}
		val (pshwrA, pshwrB) = (new PondShower {},new PondShower {})
		val pList : List[PondShower] = List(pshwrA, pshwrB)
		val pondShowerDump = rui.makePondDataDump(rui.OF_JSON, pList)

		pondShowerDump
	}

}
