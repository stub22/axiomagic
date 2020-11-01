package axmgc.web.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{as, complete, entity}
import akka.http.scaladsl.server.Route
import axmgc.web.ent.HtEntMkr
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat, enrichAny}

private trait WebJsonStuff

private trait JsonEntMkr extends SprayJsonSupport

private trait JsonRtMkr

private case class Color(name: String, red: Int, green: Int, blue: Int)
private case class Money(currency: String, amount: BigDecimal)
private case class ManyMoney(flvr : String, lst : List[Money])
// val bal = Money("USD", 100)

private object MyJsonProtocol extends DefaultJsonProtocol {
	implicit val colorFormat = jsonFormat4(Color)

	implicit object MoneyFormat extends JsonFormat[Money] {
		val fmt = """([A-Z]{3}) ([0-9.]+)""".r
		def write(m: Money) = JsString(s"${m.currency} ${m.amount}")
		def read(json: JsValue) = json match {
			case JsString(fmt(c, a)) => Money(c, BigDecimal(a))
			// case _ => deserializationError("String expected")
		}
		def deserializationError(msg: String) : Unit = {
			// throw new DeserializationException(msg)
		}
	}

}

trait MoneyRtMkr {

	def moneyFormatBad(mv : Money) = {
		import MyJsonProtocol._
		val mjs: JsString = MoneyFormat.write(mv)
		mjs
	}
	private val gmf = new DefaultJsonProtocol {
		implicit val mfi = jsonFormat2(Money)
		implicit val mmf = jsonFormat2(ManyMoney)
	}
	def moneyFormatGood(mv : Money): JsValue = {
		import gmf._
		val gmjsv : JsValue = mv.toJson
		gmjsv
	}
	def manyMoney : JsValue = {
		val m1 = new Money("Euros", 99.1)
		val m2 = new Money("Yen", amount = 127.4)
		val mm = new ManyMoney("heyNow", List(m1, m2))
		import gmf._
		val mmjsv = mm.toJson
		mmjsv
	}
	def mkMoneySenderRt(htEntMkr: HtEntMkr) : Route = {
		val mv = new Money("Pounds", 12.2)
		val bad = moneyFormatBad(mv)
		val good = moneyFormatGood(mv)
		val many = manyMoney
		val chosen = many
		val gje = htEntMkr.makeJsonEntity(chosen.toString())
		complete {
			gje
		}
	}
}

// https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/marshalling-directives/completeWith.html
// "It utilizes SprayJsonSupport via the PersonJsonSupport object as the in-scope unmarshaller."

case class Person(name: String, favoriteNumber: Int)
object PersonJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
	implicit val myJF_Person = jsonFormat2(Person)

}

trait PersonRouteMkr {
	def mkPersonReceiverRt: Route = {
		// Need this implicit stuff to do fancy json unmarshalling
		import PersonJsonSupport._
		// complete("nope")
		// https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/marshalling-directives/entity.html
		entity(as[Person]) { prsn => {
			val msg = s"Person: ${prsn.name} - favorite number: ${prsn.favoriteNumber}"
			println("person = " + prsn)
			complete(msg)
		}}
	}
}
/*
The SprayJsonSupport trait provides a FromEntityUnmarshaller[T] and ToEntityMarshaller[T]
for every type T that an implicit spray.json.RootJsonReader and/or spray.json.RootJsonWriter
(respectively) is available for.

Byte, Short, Int, Long, Float, Double, Char, Unit, Boolean
String, Symbol
BigInt, BigDecimal
Option, Either, Tuple1 - Tuple7
List, Array
immutable.{Map, Iterable, Seq, IndexedSeq, LinearSeq, Set, Vector}
collection.{Iterable, Seq, IndexedSeq, LinearSeq, Set}
JsValue
 */