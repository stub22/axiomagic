package org.appdapter.axmgc.web.pond

// import org.appdapter.axmgc.web.pond.SomeDataStuff

import org.apache.jena.rdf.model.{Model => JenaModel}

// import scala.xml.*

class PondGrid {

}

trait PondShower {
	private lazy val mySDS = new SomeDataStuff()

	private def loadDummyModel : JenaModel = {
		val mdl = mySDS.loadThatModel()
		val mdmp = mdl.toString
		System.out.println("Loaded: " + mdmp)
		mdl
	}
	private lazy val ourMdl : JenaModel = loadDummyModel
	private lazy val dummyJsonTxt : String = mySDS.writeModelToJsonLDString_Pretty(ourMdl)

	def getPondViewDat(pondID : String, frameTime : String) : String = {
		dummyJsonTxt
	}
}
trait RectUiFuncs {
	// One pond gets one rect and VV
	val OF_JSON = "JSON"
	def makePondDataDump(outFmt : String, pShowers: Traversable[PondShower]) : String = {
		// List[String]
		val shownXmlNodes = pShowers.map(ps => {
			val xmlNode = <div>
			</div>
			xmlNode
		})
		val result = "big ol XML dump : [" + shownXmlNodes.toString + "]"
		result
	}
}
