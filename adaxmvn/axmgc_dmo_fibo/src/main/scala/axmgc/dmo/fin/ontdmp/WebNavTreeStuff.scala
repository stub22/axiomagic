package axmgc.dmo.fin.ontdmp

import org.slf4j.LoggerFactory
import spray.json.{DefaultJsonProtocol, JsValue, JsonFormat}
import scala.collection.immutable.Seq

private trait WebNavTreeStuff

// Spray-json calls this a "recursive" type.
// Spray-json does not seem to offer a way to preserve the order of fields output by JsObject, as of 2015:
// https://github.com/spray/spray-json/issues/119
// This means that `children` field may appear anywhere in the output, which makes reading a big tree visually
// (in the JSON-text form) rather unpleasant.
// Are circe or other JSON libs more flexible in this regard?

case class NavItem(title: String, folder : Option[Boolean], expanded : Option[Boolean],
				   `lazy`: Option[Boolean], `type`: Option[String], tooltip : Option[String],
				   children : Option[Seq[NavItem]])

trait NavItemMakerFuncs {
	protected lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)
	private val navJsonProtoCtx = new DefaultJsonProtocol {
		// We must use lazyFormat wrapper to handle the recursive type
		implicit val jf_navItem: JsonFormat[NavItem] = lazyFormat(jsonFormat7(NavItem))
	}
	def navtreeToJsonValue(rootItems : Seq[NavItem]) : JsValue = {
		import navJsonProtoCtx._
		import spray.json.enrichAny
		val navJson: JsValue = rootItems.toJson
		navJson
	}
	def mkLeafItem(title : String, typ_opt : Option[String], ttip_opt : Option[String]) : NavItem = {
		NavItem(title, Some(false), None, None, typ_opt, ttip_opt, None)
	}
	def mkFolderItem(title : String, subItems : Seq[NavItem]) = {
		NavItem(title, Some(true), Some(false), Some(false), None, None, Some(subItems))
	}
	def loadCat(catName : String) : NavItem = {
		val subItems : Seq[NavItem] = catName match {
			case "DUM" => loadDummyFolders()
		}
		val topItem = mkFolderItem("cat_" + catName, subItems)
		topItem
	}
	def mkDummyNavTreeRoots(catNames : Seq[String]) : Seq[NavItem] = {
		val itemA = mkLeafItem(title = "hoowee one item", Some("generic"), None)
		val itemB = mkLeafItem("leafy green", Some("vegetable"), Some("how about Spinach?"))
		val listAB = List(itemA, itemB)
		val fldrC = mkFolderItem("foldy-see", listAB)

		val dummyCtgFldrs = loadDummyFolders()
		val dumCtsFldr = mkFolderItem("dumcts", dummyCtgFldrs)
		val dumLst = List(itemB, fldrC, itemA, dumCtsFldr)
		dumLst
	}
	val plantCatNames = List("Tree", "Bush", "Grass", "Vine")
	val animalCatNames = List("Mammal", "Fish", "Bird", "Reptile")
	def loadDummyFolders() : Seq[NavItem] = {
		val animalFolderItems: Seq[NavItem]  = animalCatNames.map(ctgNm => {
			val anmGnusItms = loadGenusItems(ctgNm)
			mkFolderItem("anml_" + ctgNm, anmGnusItms)
		})
		val animGrpFldr = mkFolderItem("animals", animalFolderItems)

		val plantFolders : Seq[NavItem] = plantCatNames.map(ctgNm => {
			val plntGnusItms = loadGenusItems(ctgNm)
			mkFolderItem("plnt_" + ctgNm, plntGnusItms)
		})
		val plntGrpFldr = mkFolderItem("plants", plantFolders)
		val dummyFolderList = List(animGrpFldr, plntGrpFldr)
		dummyFolderList
	}
	def loadGenusItems(gnusNm : String) : Seq[NavItem] = {
		mkDummyLeafItems(10, s"gnus_${gnusNm}_" )
	}

	def mkDummyLeafItems(cnt : Int, nmPrefix : String) : Seq[NavItem] = {
		val nms: Seq[String] = (1 to cnt).map(nmPrefix + _)
		nms.map(nm => mkLeafItem(nm, Some("dummy"), Some("ttip for " + nm)))
	}
}


trait NavItemMakerTests {
	private lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)
	private lazy val myNvItmMkr = new NavItemMakerFuncs {}

	def testNavTreeDataGen(paramMap: Map[String, String]): String = {

		val cat01_opt: Option[String] = paramMap.get("cat01")
		val cat02_opt = paramMap.get("cat02")
		val catNames = List(cat01_opt, cat02_opt).flatten
		val paramSerText = s"params=[${paramMap.toString()}]"
		val catNameTxt = s"catNames=[${catNames.toString()}]"
		myS4JLog.info(s"${paramSerText} parsed to ${catNameTxt}")
		val debugRoot: NavItem = mkDebugItemTree(List(paramSerText, catNameTxt))
		val itms: Seq[NavItem] = debugRoot +: myNvItmMkr.mkDummyNavTreeRoots(catNames)
		myS4JLog.info(s"Made item-tree: ${itms}")
		val itmJSV = myNvItmMkr.navtreeToJsonValue(itms)
		myS4JLog.info(s"Made item-JSV: ${itmJSV}")
		val jsPrtty = itmJSV.prettyPrint
		myS4JLog.info(s"Pretty JSON len: ${jsPrtty.length}")
		jsPrtty
	}

	def mkDebugItemTree(dbgMsgs: Seq[String]): NavItem = {
		val dbgItms = dbgMsgs.map(itmMsg => myNvItmMkr.mkLeafItem(itmMsg, Some("debug"), Some("ttip for " + itmMsg)))
		val dbgRoot = myNvItmMkr.mkFolderItem("debug-items", dbgItms)
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

import axmgc.web.ent.HtEntMkr
import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}

trait WebNavItemResponder {
	private val myNimTsts = new NavItemMakerTests{}
	private val myHTEM = new HtEntMkr {}

	def makeAnswerEntity(paramMap : Map[String, String]) : HEStrict = {
		val jsTxt = myNimTsts.testNavTreeDataGen(paramMap)
		val navdatEnt = myHTEM.makeJsonEntity(jsTxt)
		navdatEnt
	}

}
