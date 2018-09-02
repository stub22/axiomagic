package org.appdapter.axmgc.web.pond

import java.io.ByteArrayOutputStream

import org.apache.jena.rdf.model.{Model => JenaModel}
import org.apache.jena.riot.{JsonLDWriteContext, RDFDataMgr, RDFFormat, WriterGraphRIOT}
import org.apache.jena.riot.system.PrefixMap
import org.apache.jena.riot.system.RiotLib
import org.apache.jena.sparql.core.DatasetGraph
import java.io.IOException

import org.apache.jena.sparql.util.{Context => SparqlCtx}
import org.apache.jena.graph.{Graph => JenaGraph}

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
		val outTxt = writeModelToJsonLDString(grph, fmt, jldWCtx)
		outTxt
	}
	def writeModelToJsonLDString (jgrph : JenaGraph, fmt : RDFFormat, jldWCtx : JsonLDWriteContext) : String = {
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
