package axmgc.web.pond

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, DeserializationException, JsString, JsValue, JsonFormat}

private trait WebJsonStuff

private trait JsonEntMkr extends SprayJsonSupport

private trait JsonRtMkr

private case class Color(name: String, red: Int, green: Int, blue: Int)
private case class Money(currency: String, amount: BigDecimal)

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

// It utilizes SprayJsonSupport via the PersonJsonSupport object as the in-scope unmarshaller.


private case class Person(name: String, favoriteNumber: Int)
private object PersonJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
	implicit private val myJF_Person : JsonFormat[Person] = jsonFormat2(Person)
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