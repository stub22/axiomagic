package axmgc.web.pond

import java.io.ByteArrayOutputStream

import org.apache.jena.graph.{Graph => JenaGraph}
import org.apache.jena.rdf.model.{Model => JenaModel}
import org.apache.jena.riot.system.RiotLib
import org.apache.jena.riot.{JsonLDWriteContext, RDFDataMgr, RDFFormat, WriterGraphRIOT}

/**
  * @author stub22
  */

class SomeDataStuff {
	val thatModelPath = "gdat/glp_dat_01_owl.ttl"
	def loadThatModel() : JenaModel = {
		val mdl = RDFDataMgr.loadModel(thatModelPath)
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
trait TdatChunker {
	def getSomeJsonLD() : String = {
		val sds = new SomeDataStuff()
		val mdl = sds.loadThatModel()
		val mdmp = mdl.toString
		System.out.println("Loaded: " + mdmp)
		val jldTxt = sds.writeModelToJsonLDString_Pretty(mdl)
		System.out.println("Formatted: " + jldTxt)
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