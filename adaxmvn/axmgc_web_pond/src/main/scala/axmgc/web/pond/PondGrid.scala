package axmgc.web.pond

import axmgc.web.lnkdt.RdfJsonLdAdapter
import org.apache.jena.rdf.model.{Model => JenaModel}

import scala.xml.Elem

/**
  * @author stub22
  */

private trait PondGrid

private trait PondShower {
	private lazy val mySDS = new RdfJsonLdAdapter()

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
		val xmlViewOut = <div><span>[PondID = {pondID}, yep]</span></div>
		xmlViewOut // .toString()
	}

	def getPondNick : String
}
private trait RectUiFuncs {
	// One pond gets one rect and VV
	val OF_JSON = "JSON"
	private def makePondDataDump(pShowers: Traversable[PondShower]) : List[Elem] = {
		// List[String]
		val shownXmlNodes: List[Elem] = pShowers.map(ps => {
			val xmlElem: Elem = ps.getPondViewXhtml(ps.getPondNick, "now")
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

trait PondGriddler {

	def getSomeXhtml5(bonusMsg : String) : String = {
		val banner : String = "<h3>Much Bester Down Here</h3>"
		// val gridMkr = new PondGrid {}
		// val pshwrs = gridMkr.
		val rui = new RectUiFuncs {}
		val (pshwrA, pshwrB, pshwrC) = (new PondShower {
			override def getPondNick: String = "showerA"
		}, new PondShower {
			override def getPondNick: String = "showerB"
		}, new PondShower {
			override def getPondNick: String = bonusMsg
		})
		val pList : List[PondShower] = List(pshwrA, pshwrB, pshwrC)
		val pondShowerDump = rui.makePondDataDump(rui.OF_JSON, pList)

		pondShowerDump
	}
}