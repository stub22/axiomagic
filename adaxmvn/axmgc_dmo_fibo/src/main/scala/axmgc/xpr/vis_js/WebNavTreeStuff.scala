package axmgc.xpr.vis_js

import org.slf4j.LoggerFactory
import spray.json.{DefaultJsonProtocol, JsArray, JsBoolean, JsNumber, JsObject, JsString, JsValue, JsonFormat}

import scala.collection.immutable.Seq

private trait WebNavTreeStuff

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

trait JsValueMakers {
	// https://github.com/spray/spray-json/blob/release/1.3.x/src/main/scala/spray/json/JsValue.scala
	// https://www.javadoc.io/doc/io.spray/spray-json_2.12/latest/spray/json/JsObject.html
	def smapToJsObj(fields: Map[String, JsValue]) : JsObject = new JsObject(fields)
	// https://www.javadoc.io/doc/io.spray/spray-json_2.12/latest/spray/json/JsArray.html
	def svectToJsArr(elements: Vector[JsValue]) : JsArray = new JsArray(elements)
	def seqToJsArr(elements: Seq[JsValue]) : JsArray = svectToJsArr(elements.toVector)
	def mkJsString(sv : String) : JsString = new JsString(sv)
	def mkJsNum(bigDec : BigDecimal) = new JsNumber(bigDec)
	def mkJsNum(lv : Long) = new JsNumber(BigDecimal(lv))

}

trait XtraNavPtrFuncs {
	def NPFN_subNavQID = "subqid"
	def NPFN_tblRngQID = "tblqid"

	private val myJSVM = new JsValueMakers{}

	def makeXtraNavPtrs(subNavQID : String, tblRngQID : String) : JsObject = {
		val ptrMap = Map[String, JsValue](
				NPFN_subNavQID -> myJSVM.mkJsString(subNavQID),
				NPFN_tblRngQID -> myJSVM.mkJsString(tblRngQID))
		val ptrObj = myJSVM.smapToJsObj(ptrMap)
		ptrObj
	}
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
trait MakeSampleNavItems {
	private val myMakerFuncs = new NavItemMakerFuncs {}
	private val myJsvMkr = new JsValueMakers{}
	private val myXPM = new XtraNavPtrFuncs {}

	def loadCat(catName : String) : NavItem = {
		val subItems : Seq[NavItem] = catName match {
			case "DUM" => loadDummyFolders()
		}
		val topItem = myMakerFuncs.mkFolderItem("cat_" + catName, subItems)
		topItem
	}
	def mkDummyNavTreeRoots(catNames : Seq[String]) : Seq[NavItem] = {
		val itemA = myMakerFuncs.mkLeafItem("hoowee one item", Some("generic"), None)
		val itemB = myMakerFuncs.mkLeafItem("leafy green", Some("vegetable"), Some("how about Spinach?"))
		val numList = List(JsNumber("2.75"), JsNumber("778.332"))
		val numJSA = myJsvMkr.seqToJsArr(numList)
		val itemC = myMakerFuncs.mkLeafItem("should have xtra JSV", Some("HasXtra"), None).addExtra(numJSA)

		val listAB = List(itemA, itemB, itemC)
		val fieldMap = Map[String, JsValue]("factName" -> JsString("swiggy"), "numList" -> numJSA, "flag" -> JsBoolean(false))
		val grtObj: JsObject = myJsvMkr.smapToJsObj(fieldMap)
		val fldrC = myMakerFuncs.mkFolderItem("foldy-see", listAB).addExtra(grtObj)
		val anthrObj = myJsvMkr.smapToJsObj(Map("wiggleFactor" -> JsNumber(-17.33), "numbLst" -> numJSA, "bonusObj" -> grtObj))
		val dummyCtgFldrs = loadDummyFolders()
		val dumCtsFldr = myMakerFuncs.mkFolderItem("dumcts", dummyCtgFldrs).addExtra(anthrObj)
		val xpD = myXPM.makeXtraNavPtrs("subsOfLzD", "tblOfLzD")
		val lzFldrD = myMakerFuncs.mkLazyFolderItem("should have xtra ptrs", Some("HasXtraPtrs"), Some("Try expanding me!"), Some(xpD))
		val dumLst = List(itemB, fldrC, itemA, dumCtsFldr, lzFldrD)
		dumLst
	}
	val plantCatNames = List("Tree", "Bush", "Grass", "Vine")
	val animalCatNames = List("Mammal", "Fish", "Bird", "Reptile")
	def loadDummyFolders() : Seq[NavItem] = {
		val animalFolderItems: Seq[NavItem]  = animalCatNames.map(ctgNm => {
			val anmGnusItms = loadGenusItems(ctgNm)
			myMakerFuncs.mkFolderItem("anml_" + ctgNm, anmGnusItms)
		})
		val animGrpFldr = myMakerFuncs.mkFolderItem("animals", animalFolderItems)

		val plantFolders : Seq[NavItem] = plantCatNames.map(ctgNm => {
			val plntGnusItms = loadGenusItems(ctgNm)
			myMakerFuncs.mkFolderItem("plnt_" + ctgNm, plntGnusItms)
		})
		val plntGrpFldr = myMakerFuncs.mkFolderItem("plants", plantFolders)
		val dummyFolderList = List(animGrpFldr, plntGrpFldr)
		dummyFolderList
	}

	def loadGenusItems(gnusNm : String) : Seq[NavItem] = {
		mkDummyLeafItems(10, s"gnus_${gnusNm}_" )
	}

	def mkDummyLeafItems(cnt : Int, nmPrefix : String) : Seq[NavItem] = {
		val nms: Seq[String] = (1 to cnt).map(nmPrefix + _)
		nms.map(nm => myMakerFuncs.mkLeafItem(nm, Some("dummy"), Some("ttip for " + nm)))
	}
}


trait NavItemMakerTests {
	private lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)
	private val mySampleItemMkr = new MakeSampleNavItems {}
	private val myMakerFuncs = new NavItemMakerFuncs {}

	def testNavTreeDataGen(paramMap: Map[String, String]): String = {

		val cat01_opt: Option[String] = paramMap.get("cat01")
		val cat02_opt = paramMap.get("cat02")
		val catNames = List(cat01_opt, cat02_opt).flatten
		val paramSerText = s"params=[${paramMap.toString()}]"
		val catNameTxt = s"catNames=[${catNames.toString()}]"
		myS4JLog.info(s"${paramSerText} parsed to ${catNameTxt}")
		val debugRoot: NavItem = mkDebugItemTree(List(paramSerText, catNameTxt))
		val itms: Seq[NavItem] = debugRoot +: mySampleItemMkr.mkDummyNavTreeRoots(catNames)
		myS4JLog.info(s"Made item-tree: ${itms}")
		val itmJSV = myMakerFuncs.navtreeToJsonValue(itms)
		myS4JLog.info(s"Made item-JSV: ${itmJSV}")
		val jsPrtty = itmJSV.prettyPrint
		myS4JLog.info(s"Pretty JSON len: ${jsPrtty.length}")
		jsPrtty
	}
	def testFocusedTreeDataGen(navQID : String, paramMap: Map[String, String]): String = {
		val paramSerText = s"params=[${paramMap.toString()}]"
		val catNameTxt = s"navQID=[${navQID.toString()}]"
		myS4JLog.info(s"${paramSerText} parsed to ${catNameTxt}")
		val debugRoot: NavItem = mkDebugItemTree(List(paramSerText, catNameTxt))
		val itms: Seq[NavItem] = List(debugRoot)
		myS4JLog.info(s"Made item-tree: ${itms}")
		val itmJSV = myMakerFuncs.navtreeToJsonValue(itms)
		myS4JLog.info(s"Made item-JSV: ${itmJSV}")
		val jsPrtty = itmJSV.prettyPrint
		myS4JLog.info(s"Pretty JSON len: ${jsPrtty.length}")
		jsPrtty
	}

	def mkDebugItemTree(dbgMsgs: Seq[String]): NavItem = {
		val dbgItms = dbgMsgs.map(itmMsg => myMakerFuncs.mkLeafItem(itmMsg, Some("debug"), Some("ttip for " + itmMsg)))
		val dbgRoot = myMakerFuncs.mkFolderItem("debug-items", dbgItms)
		dbgRoot
	}
}

object RunNavItemMakerTests {
	private lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)

	def main(args: Array[String]): Unit = {
		goRunTests
	}
	def goRunTests : Unit = {
		// val loggingLauncher = new LoggingLauncher {}
		// loggingLauncher.setup
		val bwt = new NavItemMakerTests {}
		val emptyParamMap = Map[String,String]()
		val ntJsonTxt = bwt.testNavTreeDataGen(emptyParamMap)
		myS4JLog.info(s"Navtree JSON txt: ${ntJsonTxt}")
	}
}

import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}
import axmgc.web.ent.HtEntMkr
trait NavJsonEntApi {
	def mkBroadAnswerEntity(paramMap : Map[String, String]) : HEStrict
	def mkNarrowAnswerEntity(navQID : String, paramMap : Map[String, String]) : HEStrict
}
trait NavJsonTxtApi {
	def mkBroadJsonTxt(paramMap : Map[String, String]) : String
	def mkNarrowJsonTxt(navQID : String, paramMap : Map[String, String]) : String

}
trait NavJsonEntImpl extends NavJsonEntApi {
	protected def getNavJsonTxtMkr : NavJsonTxtApi
	protected def getHtEntMkr : HtEntMkr
	override def mkBroadAnswerEntity(paramMap : Map[String, String]) : HEStrict = {
		val broadJsonTxt = getNavJsonTxtMkr.mkBroadJsonTxt(paramMap)
		getHtEntMkr.makeJsonEntity(broadJsonTxt)
	}
	override def mkNarrowAnswerEntity(navQID : String, paramMap : Map[String, String]) : HEStrict = {
		val nrrowJsonTxt = getNavJsonTxtMkr.mkNarrowJsonTxt(navQID, paramMap)
		getHtEntMkr.makeJsonEntity(nrrowJsonTxt)
	}
}
class FakeNavJsonTxtImpl extends NavJsonTxtApi {
	private val myNimTsts = new NavItemMakerTests{}

	override def mkBroadJsonTxt(paramMap: Map[String, String]): String = {
		myNimTsts.testNavTreeDataGen(paramMap)
	}
	override def mkNarrowJsonTxt(navQID: String, paramMap: Map[String, String]): String = {
		myNimTsts.testFocusedTreeDataGen(navQID, paramMap)
	}

}

class RobustNavJsonTxtImpl extends NavJsonTxtApi {
	private val myFallbackImpl = new FakeNavJsonTxtImpl
	override def mkBroadJsonTxt(paramMap: Map[String, String]): String = {

		myFallbackImpl.mkBroadJsonTxt(paramMap)
	}

	override def mkNarrowJsonTxt(navQID: String, paramMap: Map[String, String]): String = {
		myFallbackImpl.mkNarrowJsonTxt(navQID, paramMap)
	}
}
/*
private class FakeNavItemRespImpl() extends NavJsonEntImpl {
	private val myNimTsts = new NavItemMakerTests{}

	private val myHTEM = new HtEntMkr {}

	override protected def getHtEntMkr: HtEntMkr = myHTEM

	override protected def getNavJsonTxtMkr: NavJsonTxtApi = new NavJsonTxtApi {
	}
}
*/

/* Before refactor was:
override def mkBroadAnswerEntity(paramMap : Map[String, String]) : HEStrict = {
	val jsTxt = myNimTsts.testNavTreeDataGen(paramMap)
	val navdatEnt = myHTEM.makeJsonEntity(jsTxt)
	navdatEnt
}
override def mkNarrowAnswerEntity(navQID : String, paramMap : Map[String, String]) : HEStrict = {
	val jsTxt = myNimTsts.testFocusedTreeDataGen(navQID, paramMap)
	val navdatEnt = myHTEM.makeJsonEntity(jsTxt)
	navdatEnt
}
 */
