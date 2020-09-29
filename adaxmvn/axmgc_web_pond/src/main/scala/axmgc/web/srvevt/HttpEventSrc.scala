package axmgc.web.srvevt

import java.time.LocalTime
import java.time.format.DateTimeFormatter.ISO_LOCAL_TIME

import akka.NotUsed
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.{server => dslServer}
import akka.stream.scaladsl.Source
import axmgc.web.pond.OurUrlPaths

import scala.concurrent.duration._


/*
Started from example code in Akka-Http docs:
https://doc.akka.io/docs/akka-http/current/sse-support.html
 */

trait HttpEventSrc

trait HttpEventSrcRtMkr extends OurUrlPaths  {

// Warning:  As of 2018, EventSource is not supported by Microsoft browsers.
// https://stackoverflow.com/questions/24498141/is-there-a-microsoft-equivalent-for-html5-server-sent-events/
// https://stackoverflow.com/questions/29648747/does-the-edge-browser-support-html5-server-side-events

	def mkEvtSrcRt: dslServer.Route = {
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._

		path(pathHttpEvtSrc) {
			get {
				complete {
					Source
							.tick(2.seconds, 2.seconds, NotUsed)
							.map(_ => LocalTime.now())
							.map(time => ServerSentEvent(ISO_LOCAL_TIME.format(time)))
							.keepAlive(1.second, () => ServerSentEvent.heartbeat)
				}
			}
		}
	}
}
