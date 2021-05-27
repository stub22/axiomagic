package axmgc.dmo.ksrc.lean_mthlb

import java.io.File
import java.net.URL

import axmgc.xpr.vis_js.{JsValueMakers, NavItem, NavItemMakerFuncs, XtraNavPtrFuncs}
import org.slf4j.LoggerFactory
import spray.json.JsObject

import scala.collection.immutable.Seq

private trait LeanMathlibNavItemStuff

class LmlNavItemMaker {
	private val myS4JLog = LoggerFactory.getLogger(this.getClass)

	private val myMakerFuncs = new NavItemMakerFuncs {}
	private val myJsvMkr = new JsValueMakers{}
	private val myXPM = new XtraNavPtrFuncs {}

	def mkBigJsObj : JsObject = ???
	def mkBigTree() : String = {
		chkFolderNaive(getClass, "/gdat/lean_mathlib/src")
		val pondClz = classOf[axmgc.web.json.Person]
		chkFolderNaive(pondClz, "/wdat")
		val topNavItms: Seq[NavItem] = Nil
		val jsTree = myMakerFuncs.navtreeToJsonValue(topNavItms)
		val jsonTxtPrtty = jsTree.prettyPrint
		jsonTxtPrtty
	}

	def chkFolderNaive(mrkClz : Class[_], fpath : String) : Unit = {
		val fldrUrl: URL = mrkClz.getResource(fpath)
		checkFolderUrl(fldrUrl)
	}
	def checkFolderUrl (fldrUrl : URL) : Unit = {
		val dirf = new File(fldrUrl.toURI)
		if (dirf.isDirectory) {
			val dlst: Array[File] = dirf.listFiles()
			dlst.foreach(f => {
				val fobj: File = f
				myS4JLog.info("Found file={}", f)
			})
		}
	}

	def beDumbOnPurpose : Unit = ???
}
