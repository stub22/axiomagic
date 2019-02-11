package axmgc.web.pond

import scala.xml.{Elem => XElem, Node => XNode, NodeSeq => XNodeSeq}


// An answer can be empty, or a part can be missing, but a part (or part-key)
// cannot be empty.

trait WebAskAPI

trait TWAPartKey
sealed abstract class WAPartKey extends TWAPartKey {

}
case class WAP_Xhtml(domId_opt : Option[String]) extends WAPartKey
case class WAP_Css(cssFileNm_opt : Option[String]) extends WAPartKey
case class WAP_Trtl(graphUriTxt : String) extends WAPartKey
case class WAP_Json(jsonFileNm : String) extends WAPartKey

sealed abstract class WAContentMsg

trait TWebAnsPart {
	type PartValTyp
	def getPartKey : WAPartKey
	def getPartVal : PartValTyp
}
trait TWebAnswer {
	def getKnownPartKeys : Traversable[WAPartKey]
	def getPart_opt (key : WAPartKey) : Option[TWebAnsPart]
}
sealed abstract class WebAnswer extends TWebAnswer {
	// Consider which timestamps should be marked as @transient
	val myEvtInstMsec : Long = System.currentTimeMillis()
}
case class WA_Empty() extends WebAnswer {
	override def getKnownPartKeys: Traversable[WAPartKey] = Nil
	override def getPart_opt(key: WAPartKey) = None
}
abstract case class WA_Summary(summaryTxt : String) extends WebAnswer
abstract case class WA_Full () extends WebAnswer

trait RegularPV {
}
case class RPV_StringPart(myTxt : String) extends RegularPV
case class RPV_XmlElemPart(myElem : XElem) extends RegularPV
case class RPV_XmlSeqPart(myElem : XNodeSeq) extends RegularPV

trait TRegConsumer {
	// xhel should already contain dom-id attr, if any.
	def consumeWiredXhtmlElem (domId_opt : Option[String], xhel : XElem) : Unit
	def consumeCssFile (flNm : Option[String], cssTxt : String) : Unit
	def consumeJsnFile (flNm : String, jsnTxt : String) : Unit
	def consumeTrtlFile (flNm : String, trtlTxt : String) : Unit
}
trait TRWAnswrApply {
	def apply(cnsmr : TRegConsumer, prtKy :  TWAPartKey, regPV : RegularPV) : Unit = {
		prtKy match  {
			case wapKyXht : WAP_Xhtml => {
				regPV match {
					case xelPrt : RPV_XmlElemPart => {
						val xpv = xelPrt.myElem
						cnsmr.consumeWiredXhtmlElem(wapKyXht.domId_opt, xelPrt.myElem)
					}
				}
			}
			case wapKyCss: WAP_Css => {
				regPV match {
					case strngPrt: RPV_StringPart => {
						cnsmr.consumeCssFile(wapKyCss.cssFileNm_opt, strngPrt.myTxt)
					}
				}
			}
			case wapJsn: WAP_Json => {

			}
			case wapTrtl: WAP_Trtl => {

			}

		}
	}
	private def applyXhtmlElem () = {

	}
}