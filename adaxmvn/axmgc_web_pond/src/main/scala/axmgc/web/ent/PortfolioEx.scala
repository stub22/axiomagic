package axmgc.web.ent

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import org.slf4j.LoggerFactory
import spray.json.{DefaultJsonProtocol, JsValue, enrichAny}

private trait PortfolioEx

case class Asset(ticker : String, fullName : String)

case class PriceQuote(asset : Asset, price : Float )



trait PorfolioRtMkr {
	protected lazy val myS4JLog = LoggerFactory.getLogger(this.getClass)

	private val portJsCtx = new DefaultJsonProtocol {
		implicit val jf_asset = jsonFormat2(Asset)
		implicit val jf_priceQuote = jsonFormat2(PriceQuote)
	}

	def mkPortfolioSenderRt(htEntMkr: HtEntMkr) : Route = {
		val asst1 = new Asset("IBM", "Intl Busy Moops")
		val pq1 = new PriceQuote(asst1, 55.70f)
		val asst2 = new Asset("CAT", "Cat Pillar")
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
