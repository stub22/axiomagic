package axmgc.xpr.vis_js

import akka.http.scaladsl.{server => dslServer}
import axmgc.web.cors.CORSHandler
import axmgc.web.ent.HtEntMkr
import org.slf4j.LoggerFactory
import spray.json.{DefaultJsonProtocol, JsArray, JsValue, JsonFormat}

private trait WebTableStuff

case class WebColumnDef(field : String, sortable : Boolean, filter : Boolean, checkboxSelection : Option[Boolean])
case class SampleWebRow(rowId : String, title : String, count : Int, amount : Float, flag : Boolean)
trait WebTableDataMaker {
	protected lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)
	private val sampleTableJsonProtoCtx = new DefaultJsonProtocol {
		// We must use lazyFormat wrapper to handle the recursive type
		implicit val jf_sampleRow: JsonFormat[SampleWebRow] = jsonFormat5(SampleWebRow)
		implicit val jf_colDef: JsonFormat[WebColumnDef] = jsonFormat4(WebColumnDef)
	}
	def rowsToJsonValue(rowSeq : Seq[SampleWebRow]) : JsValue = {
		import sampleTableJsonProtoCtx._
		import spray.json.enrichAny
		val rowSeqJson: JsValue = rowSeq.toJson
		rowSeqJson
	}
	def colDefsToJsArray(colDefs : Seq[WebColumnDef]) : JsArray = {
		import sampleTableJsonProtoCtx._
		import spray.json.enrichAny
		val colDefJsArr = colDefs.toJson
		colDefJsArr.asInstanceOf[JsArray]
	}
	def mkSampleColDef(fieldNm : String) : WebColumnDef = {
		val colDef = new WebColumnDef(fieldNm, true, true, None)
		colDef
	}
	def mkSampleColDefs() : Seq[WebColumnDef] = {
		val colNames = List("rowId", "title", "count", "amount", "flag")
		val colDefs = colNames.map(mkSampleColDef(_))
		colDefs
	}
	def mkSampleColDefsJsonTxt : String = {
		val scd = mkSampleColDefs()
		val scdJA = colDefsToJsArray(scd)
		val jsTxt = scdJA.prettyPrint
		jsTxt
	}

	def mkExampleRows(cnt : Int, idPrefix : String) : Seq[SampleWebRow] = {
		val rowIdPairs: Seq[(Int, String)] = (1 to cnt).map(num => (num, idPrefix + num))
		rowIdPairs.map( pair => {
			val (num, id) = pair
			SampleWebRow(id, "title of " + id, num, num * 1.7f, (num % 2 > 0) )
		})
	}
	def exampleRowDataGen(paramMap: Map[String, String]): String = {
		val paramSerText = s"params=[${paramMap.toString()}]"
		myS4JLog.info(s"paramMap=${paramSerText}")

		val rows = mkExampleRows(9, "DUM")
		myS4JLog.info(s"Made asset rows: ${rows}")
		val rsJSV = rowsToJsonValue(rows)
		myS4JLog.info(s"Made rowset-JSV: ${rsJSV}")
		val jsPrtty = rsJSV.prettyPrint
		myS4JLog.info(s"Pretty JSON len: ${jsPrtty.length}")
		jsPrtty
	}
}
trait MakeWebTableRoutes {

	import dslServer.Directives._ // Establishes  ~   and whatnot
	private val myCH = new CORSHandler {}
	private val myHTEM = new HtEntMkr {}
	private val myGCDM = new WebTableDataMaker {}
	val rowsetParamName_fake = "fake"
	val rowsetParamName_gqry = "gqry"
	val gqryFlav_NONE = "NONE"

	def mkSampleRowsetJsonRt(routePth : String) : dslServer.Route = {
		val arjPthRt = path(routePth) {
			val pmapRt = parameterMap { paramMap =>
				val pm: Map[String, String] = paramMap
				val useFakeRwst : Boolean = pm.get(rowsetParamName_fake).isDefined
				val gqry_flav : String = pm.get(rowsetParamName_gqry).getOrElse(gqryFlav_NONE)
				val jsTxt = if (useFakeRwst)
					myGCDM.exampleRowDataGen(pm)
				else "[]" // fetchGqryResultTxt(gqry_flav, pm)
				// TODO:  Check pm for formatting prefs
				val assetRowsetJsonEnt = myHTEM.makeJsonEntity(jsTxt)
				complete(assetRowsetJsonEnt)
			}
			pmapRt
		}
		myCH.corsHandler(arjPthRt)
	}
	def mkSampleColDefsJsonRt(routePth : String) : dslServer.Route = {
		val acdRt = path(routePth) {
			val pmapRt = parameterMap { paramMap =>
				val pm: Map[String, String] = paramMap
				val jsTxt = myGCDM.mkSampleColDefsJsonTxt
				val colDefJsonEnt = myHTEM.makeJsonEntity(jsTxt)
				complete(colDefJsonEnt)
			}
			pmapRt
		}
		myCH.corsHandler(acdRt)
	}
}
/*
		val stuffyJsArr: JsArray = rrg.grabStuffyRowsAsJsArray(strHndl, sta) // .getIdIntColNm, sta.getStuffMapColNm)
		val rsltCnt = stuffyJsArr.elements.size
		val prettyJATxt = stuffyJsArr.prettyPrint
	def grabStuffyRowsAsJsArray (strHndl: TableStoreHandle, sta : StuffTableAdapter) : JsArray = {
		val tblHndl = sta.getTblHndl(strHndl)
		val idColNm = sta.getIdIntColNm
		val stfMpColNm = sta.getStuffMapColNm
		grabStuffyRowsAsJsArray(tblHndl, idColNm, stfMpColNm)
	}
		private def grabStuffyRowsAsJsArray (srcTblHndl : TableHandle, idColNm : String, stuffMapColNm : String) : JsArray = {
		val rowObjs: Seq[JsObject] = grabAllStuffyRowsAsFlatJsonObjs(srcTblHndl, idColNm, stuffMapColNm)
		val jsArr = new JsArray(rowObjs.toVector)
		jsArr
	}
	// Query a table that has one "stuff-map" column
	// Output one flat JSON record per DB row, in form of spray-json JsObject
	// Use the stuff-map keys as the JsObj field keys
	// For now all the stuff-field values will be Strings, but the primary key is treated as a number
	private def grabAllStuffyRowsAsFlatJsonObjs(srcTblHndl : TableHandle, idColNm : String, stuffMapColNm : String) : Seq[JsObject] = {
		val qia = new QueryIterAdapter {
			override def getTableHandle: TableHandle = srcTblHndl
		}
		val qfnc : Function1[Row, JsObject] = r => {
			// assume that idColNm does not appear as a stuffKey
			// assume that idCol is integer-numeric (Int, Long, or BigInt)
			// TODO:  Implement numeric columns, money, dates - and test client-side sorting
			val rw: Row = r
			val idFld: FieldValue = rw.get(idColNm)
			val idJsNum = myBSJC.toJsNumber(idFld)
			val mpFld: FieldValue = rw.get(stuffMapColNm)
			val mfm: MapValue = mpFld.asMap()
			val flg_prettyJson = true
			val stuffAsJsonTxt = mfm.toJsonString(flg_prettyJson)
			myS4JLog.debug((s"Borkl json at id=${idJsNum}: ${stuffAsJsonTxt}"))
			val stuffJMap: util.Map[String, FieldValue] = mfm.getFields
			val stuffSMap: Map[String, FieldValue] = stuffJMap.asScala.toMap
			val stuffJSVs: Map[String, JsValue] = stuffSMap.mapValues(fv => myBSJC.scalarToJsValue(fv))
			val comboJSV_map: Map[String, JsValue] = stuffJSVs + (idColNm -> idJsNum)
			val comboJsObj = new JsObject(comboJSV_map)
			comboJsObj
		}
		val rsltSeq: Seq[JsObject] = qia.applyToAllRows_andYieldResults(qfnc)
		rsltSeq
	}
	def scalarToJsValue(fv : FieldValue) : JsValue = {
		if (fv.isNumeric) {
			toJsNumber(fv)
		} else if (fv.isBoolean) {
			toJsBoolean(fv)
		} else toJsString(fv)
	}

	private def fetchGqryResultTxt(gqryFlav : String, paramMap : Map[String, String]) : String = {
		// https://stackoverflow.com/questions/7078022/why-does-pattern-matching-in-scala-not-work-with-variables
		// What you're looking for is a stable identifier. In Scala, these must either start with an uppercase
		// letter, or be surrounded by backticks.
		gqryFlav match {
			case `gqryFlav_XX` => {
				myBWT.xxRowData(paramMap)
			}

	private def fetchGqryColDefsTxt(gqryFlav : String, paramMap : Map[String, String]) : String = {
		gqryFlav match {
			case `gqryFlav_XX` => {
				myBWT.xxColDefs(paramMap)
			}
 */