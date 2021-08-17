package axmgc.xpr.vis_js

import org.slf4j.LoggerFactory
import spray.json.{DefaultJsonProtocol, JsArray, JsBoolean, JsNumber, JsObject, JsString, JsValue, JsonFormat}

import scala.collection.immutable.Seq

private trait WebNavTreeStuff

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

