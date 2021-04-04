package axmgc.xpr.vis_js

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.{server => dslServer}
import axmgc.web.cors.CORSHandler
import org.slf4j.{Logger, LoggerFactory}
import spray.json.{JsObject, JsValue}

private trait WebSaveStuff

trait MakeSampleSaveRoutes {
	private val myCH = new CORSHandler {}
	// private val myHTEM = new HtEntMkr {}
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)
	val pth_saveInfo = "svinfo"
	def mkSavingRt : dslServer.Route = {
		// TODO: Try with custom JSON protocols
		// TODO: Try with combined parameters + entities
		import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

		// Decode JSON sent in post request
		val svRt = path(pth_saveInfo) {
			concat (get {
					parameterMap { paramMap =>
						myS4JLog.info(s"savinfo GET parms=${paramMap}")
						complete(s"savinfo GET says OK for parms=${paramMap}")
					}
				},
				post {
					// https://doc.akka.io/docs/akka-http/current/routing-dsl/directives/marshalling-directives/entity.html
					entity(as[JsValue]) { weakJsVal =>
						myS4JLog.info(s"savinfo POST: weakJsVal=${weakJsVal}")
						val decoded : Any = weakJsVal match {
							case JsObject(objFlds) => {
								myS4JLog.info(s"JsObject fields=${objFlds}")
								objFlds
							}
						}
						complete(s"savinfo POST says OK, decoded as ${decoded}")
					}
				},
				put {
					myS4JLog.info("savinfo PUT")
					complete("savinfo PUT says OK")
				})
		}
		myCH.corsHandler(svRt)
	}

}