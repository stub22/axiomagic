package axmgc.web.answr

import scala.xml.{Elem => XElem, Node => XNode, NodeSeq => XNodeSeq}

private trait WebAnswerConsumers

protected sealed trait RegularPV {}
private case class RPV_StringPart(myTxt : String) extends RegularPV
private case class RPV_XmlElemPart(myElem : XElem) extends RegularPV
private case class RPV_XmlSeqPart(myElem : XNodeSeq) extends RegularPV

// 2020-09-25 Note: none of these traits are fully implemented yet.
// Temporarily marking these traits as private scope, to keep track of what is unfinished.
private trait TRegConsumer {
	// xhel should already contain dom-id attr, if any.
	def consumeWiredXhtmlElem (domId_opt : Option[String], xhel : XElem) : Unit
	def consumeCssFile (flNm : Option[String], cssTxt : String) : Unit
	def consumeJsnFile (flNm : String, jsnTxt : String) : Unit
	def consumeTrtlFile (flNm : String, trtlTxt : String) : Unit
}
private trait TRWAnswrApply {
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
