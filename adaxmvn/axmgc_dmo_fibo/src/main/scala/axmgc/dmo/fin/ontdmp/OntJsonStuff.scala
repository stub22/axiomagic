package axmgc.dmo.fin.ontdmp

import org.slf4j.{Logger, LoggerFactory}
import spray.json.{DefaultJsonProtocol, JsArray, JsObject, JsValue, JsonFormat, RootJsonFormat}

import scala.collection.mutable.ListBuffer

private trait OntJsonStuff

sealed trait MdlStat // Experimental union type for follwoing json-serial-write cases (cannot read without clznm)
case class MdlSummaryStat(statName : String, itemCount : Int) extends MdlStat
case class MdlHistoBinStat(binName : String, binCount : Int, binSamples: List[String])
case class MdlHistoStat(totalBinCntEst : Int, excludedBinCntEst : Int, binStats : List[MdlHistoBinStat]) extends MdlStat
case class AggStat(aggName : String, subStats : List[MdlStat]) extends MdlStat

trait MdlStatJsonProto extends DefaultJsonProtocol {
	// jsonFormatN(Type) defines marshalling for a case-class-Type with N fields
	// When we want recursive types, must add a lazyFormat wrapper
	implicit val jf_mdlSummStat: JsonFormat[MdlSummaryStat] = jsonFormat2(MdlSummaryStat)
	implicit val jf_mdlHistoStat: JsonFormat[MdlHistoStat] = jsonFormat3(MdlHistoStat)
	implicit val jf_mdlHBinStat : JsonFormat[MdlHistoBinStat] = jsonFormat3(MdlHistoBinStat)

	// Manually defined switcher for the various subtypes of MSmmStt
	implicit val jf_mSmmStt = new RootJsonFormat[MdlStat] {
		override def write(msstt: MdlStat) : JsValue = {
			msstt match {
				case ss : MdlSummaryStat => jf_mdlSummStat.write(ss)
				case hs : MdlHistoStat => jf_mdlHistoStat.write(hs)
				case as : AggStat => jf_aggStat.write(as)
			}
		}
		override def read(json: JsValue): MdlStat = ???
	}

	// AggStat may contain any other mdlStats.  Can we avoid the extra lazyFormat() wrapper in this case?
	implicit val jf_aggStat : JsonFormat[AggStat] = jsonFormat2(AggStat)

}
trait MdlSttJsonMaker {
	protected val mySlf4JLog = LoggerFactory.getLogger(this.getClass)

	private val mdlSummStatJsonProtoCtx = new MdlStatJsonProto{}

	private def summStatsToJsArr(summStats : Seq[MdlSummaryStat]) : JsArray = {
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

trait MssttJsonAdapter {
	private val mdlSummStatJsonProtoCtx = new MdlStatJsonProto{}

	def statToJson(oneStat : MdlStat) : JsObject = {
		import mdlSummStatJsonProtoCtx._
		import spray.json.enrichAny
		val jv = oneStat match {
			case histoStat : MdlHistoStat => histoStat.toJson
			case summStat : MdlSummaryStat => summStat.toJson
		}
		jv.asJsObject
	}
}