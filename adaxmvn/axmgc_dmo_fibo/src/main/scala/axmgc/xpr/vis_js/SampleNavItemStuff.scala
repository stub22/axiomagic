package axmgc.xpr.vis_js

import org.slf4j.LoggerFactory
import spray.json.{JsBoolean, JsNumber, JsObject, JsString, JsValue}

import scala.collection.immutable.Seq

private trait SampleNavItemStuff

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

