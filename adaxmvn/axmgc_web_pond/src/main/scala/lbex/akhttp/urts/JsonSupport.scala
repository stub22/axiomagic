package lbex.akhttp.urts

/*
Code in this package copied from this example and hacked slightly to fit under Axmgc:
https://github.com/akka/akka-http-quickstart-scala.g8/
Package "lbex.akhttp.urts" is public domain sample code, distributed under:
http://creativecommons.org/publicdomain/zero/1.0/.
 */

//#json-support
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val userJsonFormat = jsonFormat3(User)
  implicit val usersJsonFormat = jsonFormat1(Users)

  implicit val actionPerformedJsonFormat = jsonFormat1(UserRegistryActor.ActionPerformed)
}
//#json-support
