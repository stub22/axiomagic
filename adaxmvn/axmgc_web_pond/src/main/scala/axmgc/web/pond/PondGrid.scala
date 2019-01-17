package axmgc.web.pond

import org.apache.jena.rdf.model.{Model => JenaModel}

import scala.xml.Elem

/**
  * @author stub22
  */

class PondGrid {

}

trait PondShower {
	private lazy val mySDS = new SomeDataStuff()

	private def loadDummyModel : JenaModel = {
		val mdl = mySDS.loadThatModel(false)
		val mdmp = mdl.toString
		System.out.println("Loaded: " + mdmp)
		mdl
	}
	private lazy val ourMdl : JenaModel = loadDummyModel
	private lazy val dummyJsonTxt : String = mySDS.writeModelToJsonLDString_Pretty(ourMdl)

	def getPondViewDat(pondID : String, frameTime : String) : String = {
		dummyJsonTxt
	}
	def getPondViewXhtml(pondID : String, frameTime : String) : Elem = {
		val xmlViewOut = <div><span>[PondID = { pondID}, yep]</span></div>
		xmlViewOut // .toString()
	}

}
trait RectUiFuncs {
	// One pond gets one rect and VV
	val OF_JSON = "JSON"
	def makePondDataDump(pShowers: Traversable[PondShower]) : List[Elem] = {
		// List[String]
		val shownXmlNodes: List[Elem] = pShowers.map(ps => {
			val xmlElem: Elem = ps.getPondViewXhtml("pid", "now")
			xmlElem
		}).toList
		shownXmlNodes
	}
	def makePondDataDump(outFmt : String, pShowers: Traversable[PondShower]) : String = {
		val elems = makePondDataDump(pShowers)
		val result = "big ol XML dump : [" + elems.toString + "]"
		result
	}
}
