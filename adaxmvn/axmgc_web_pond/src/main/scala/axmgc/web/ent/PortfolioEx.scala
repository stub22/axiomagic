package axmgc.web.ent

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory
import spray.json.{DefaultJsonProtocol, JsValue, enrichAny}

private trait PortfolioEx

// These example case classes are convertible to JSON using protocol defined below
// trait ListableThing
case class InfoBlock(infoTxt : String)
case class Asset(ticker : String, fullName : String, infBlocks : List[InfoBlock])
case class PriceQuote(asset : Asset, price : Float )

trait PorfolioRtMkr {
	protected lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)

	// One good way to make JsValues is using this type of spray-JSON protocol.
	// The gnarly implicit values are built using jsonFormatN methods,
	// which are named according to number of fields to be serialized.
	private val portJsCtx = new DefaultJsonProtocol {
		implicit val jf_infBlk
		= jsonFormat1(InfoBlock)
		implicit val jf_asset = jsonFormat3(Asset)
		implicit val jf_priceQuote = jsonFormat2(PriceQuote)
	}

	def mkPortfolioSenderRt(htEntMkr: HtEntMkr) : Route = {

		val asst1 = new Asset("IBM", "Intl  Busy Moops", Nil)
		val pq1 = new PriceQuote(asst1, 55.70f)
		val goodInfo = """This is an InfoBlock obj within List within Aset within PriceQuote within List"""
		val someBlks = List(InfoBlock("ONE BLK"), InfoBlock(goodInfo), InfoBlock("ANOTHER BLK"))
		val asst2 = new Asset("CAT", "Cat Pillar", someBlks)
		val pq2 = new PriceQuote(asst2, 33.19f)
		val pql = List[PriceQuote](pq1, pq2)
		import portJsCtx._
		import spray.json.enrichAny
		val portJV: JsValue = pql.toJson
		myS4JLog.info(s"Made portfolio JsValue: ${portJV}")
		val portEnt = htEntMkr.makeJsonEntity(portJV.toString())
		complete(portEnt)
	}
}
