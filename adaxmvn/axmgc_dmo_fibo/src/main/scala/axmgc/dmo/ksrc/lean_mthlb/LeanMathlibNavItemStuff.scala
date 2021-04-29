package axmgc.dmo.ksrc.lean_mthlb

import axmgc.xpr.vis_js.{JsValueMakers, NavItem, NavItemMakerFuncs, XtraNavPtrFuncs}
import spray.json.JsObject

import scala.collection.immutable.Seq

private trait LeanMathlibNavItemStuff

class LmlNavItemMaker {
	private val myMakerFuncs = new NavItemMakerFuncs {}
	private val myJsvMkr = new JsValueMakers{}
	private val myXPM = new XtraNavPtrFuncs {}

	def mkBigJsObj : JsObject = ???
	def mkBigTree() : String = {
		val topNavItms: Seq[NavItem] = Nil
		val jsTree = myMakerFuncs.navtreeToJsonValue(topNavItms)
		val jsonTxtPrtty = jsTree.prettyPrint
		jsonTxtPrtty
	}
}
