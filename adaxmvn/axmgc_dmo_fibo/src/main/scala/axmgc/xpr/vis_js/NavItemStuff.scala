package axmgc.xpr.vis_js

import org.slf4j.LoggerFactory
import spray.json.{DefaultJsonProtocol, JsObject, JsValue, JsonFormat}

import scala.collection.immutable.Seq

private trait NavItemStuff

// Spray-json calls this a "recursive" type.
// Spray-json does not seem to offer a way to preserve the order of fields output by JsObject, as of 2015:
// https://github.com/spray/spray-json/issues/119
// This means that `children` field may appear anywhere in the output, which makes reading a big tree visually
// (in the JSON-text form) rather unpleasant.
// Are circe or other JSON libs more flexible in this regard?

// xtra is application stuff, unrelated to fancytree.  (Will be copied into fancytree node.data)
// xtra.subqid is opaque-ID to be sent by client during a lazy-fetch of this nav(tree)-item's children.
// xtra.grdqid is opaque-ID to be sent by client when requesting table-items that match this navItem.
case class NavItem(title: String, folder : Option[Boolean], expanded : Option[Boolean],
				   `lazy`: Option[Boolean], `type`: Option[String], tooltip : Option[String],
				   children : Option[Seq[NavItem]], xtra : Option[JsValue]) {
	def addExtra(jsv : JsValue) : NavItem = NavItem(title, folder, expanded, `lazy`, `type`, tooltip, children, Option(jsv))
}

trait NavItemMakerFuncs {
	protected lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)
	private val navJsonProtoCtx = new DefaultJsonProtocol {
		// We must use lazyFormat wrapper to handle the recursive type
		implicit val jf_navItem: JsonFormat[NavItem] = lazyFormat(jsonFormat8(NavItem))
	}
	def navtreeToJsonValue(rootItems : Seq[NavItem]) : JsValue = {
		import navJsonProtoCtx._
		import spray.json.enrichAny
		val navJson: JsValue = rootItems.toJson
		navJson
	}
	def mkLeafItem(title : String, typ_opt : Option[String], ttip_opt : Option[String]) : NavItem = {
		NavItem(title, Some(false), None, None, typ_opt, ttip_opt, None, None)
	}
	def mkFolderItem(title : String, subItems : Seq[NavItem]) = {
		NavItem(title, Some(true), Some(false), Some(false), None, None, Some(subItems), None)
	}
	def mkLazyFolderItem(title : String, opt_type : Option[String], opt_ttip: Option[String], opt_xtra : Option[JsObject]) = {
		NavItem(title, Some(true), Some(false), Some(true), opt_type, opt_ttip, None, opt_xtra)
	}
}
