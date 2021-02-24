package axmgc.dmo.fin.ontdmp

import org.slf4j.{Logger, LoggerFactory}
import spray.json.{DefaultJsonProtocol, JsArray, JsValue, JsonFormat}

import scala.collection.mutable.ListBuffer

private trait OntJsonStuff

case class MdlSummaryStat(statName : String, itemCount : Int)

trait MdlJsonProtos {
	protected val mySlf4JLog = LoggerFactory.getLogger(this.getClass)

	private val mdlSummStatJsonProtoCtx = new DefaultJsonProtocol {
		// jsonFormatN(Type) defines marshalling for a case-class-Type with N fields
		// When we want recursive types, must add a lazyFormat wrapper
		implicit val jf_mdlSummStat: JsonFormat[MdlSummaryStat] = jsonFormat2(MdlSummaryStat)
	}

	def summStatsToJsArr(summStats : Seq[MdlSummaryStat]) : JsArray = {
		import mdlSummStatJsonProtoCtx._
		import spray.json.enrichAny
		val summSeqJson: JsValue = summStats.toJson
		summSeqJson.asInstanceOf[JsArray]
	}
	// TODO:  Add flag for sorted print
	def summStatsToJsArrTxt(summStats : Seq[MdlSummaryStat], flag_prettyPrnt : Boolean) : String = {
		val jsArr: JsArray = summStatsToJsArr(summStats)
		val jsArrTxt = if (flag_prettyPrnt) jsArr.prettyPrint else jsArr.compactPrint
		jsArrTxt
	}
}

