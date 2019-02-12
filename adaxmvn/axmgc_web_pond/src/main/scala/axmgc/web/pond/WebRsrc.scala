package axmgc.web.pond

import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, _}
import org.slf4j.Logger
// Que hace lo?
import dslServer.Directive0

trait WebRsrc // file-marker trait matching filename

trait WebResBind {
	val FLD_SEP = '/'
	val ptok_wdat = "wdat"
	val ptok_styl = "stylz"
	val ptok_ivct = "img_vctr"
	val fldrPth_wdat = ptok_wdat
	val fldrPth_styl = fldrPth_wdat + FLD_SEP + ptok_styl
	val fldrPth_ivct = fldrPth_wdat + FLD_SEP + ptok_ivct

	val urlPth_styIcn = FLD_SEP + fldrPth_styl +  FLD_SEP + "icmbg_sty.css"
	val urlPth_styDem = FLD_SEP + fldrPth_styl +  FLD_SEP + "icmbg_dem.css"
	val urlPth_styGrd = FLD_SEP + fldrPth_styl +  FLD_SEP + "ictst_grid.css"

}

trait WebRsrcRouteMkr extends WebResBind {
	def makeWbRscRt (lgr : Logger) : dslServer.Route = {
		lgr.info("fldrPath_styl = " + fldrPth_styl)
		lgr.info(s"fpi = $fldrPth_ivct ")
		/*
		The path matching DSL describes what paths to accept after URL decoding.
		This is why the path-separating slashes have special status and cannot
		simply be specified as part of a string!
		The string “foo/bar” would match the raw URI path “foo%2Fbar”

rawPathPrefix(x): it matches x and leaves a suffix (if any) unmatched.
pathPrefix(x): is equivalent to rawPathPrefix(Slash ~ x). It matches a leading slash followed by x and then leaves a suffix unmatched.
path(x): is equivalent to rawPathPrefix(Slash ~ x ~ PathEnd). It matches a leading slash followed by x and then the end.
pathEnd: is equivalent to just rawPathPrefix(PathEnd). It is matched only when there is nothing left to match from the path. This directive should not be used at the root as the minimal path is the single slash.
pathSingleSlash: is equivalent to rawPathPrefix(Slash ~ PathEnd). It matches when the remaining path is just a single slash.
pathEndOrSingleSlash: is equivalent to rawPathPrefix(PathEnd) or rawPathPrefix(Slash ~ PathEnd). It matches either when there is no remaining path or is just a single slash.
		 */
		// path requires whole remaining path being matched
		val wbRscRt =
		get {
			parameterMap { paramMap =>
				lgr.info("paramMap: {}", paramMap)
				pathPrefix(ptok_wdat) {
					lgr.info("matched prefix: {}", ptok_wdat)
					path (ptok_styl / Segment) { fnm =>
						lgr.info("matched GET style-file: {}", fnm)
						val resNm = fldrPth_styl + FLD_SEP + fnm
						lgr.info("returning resource at: {}", resNm)
						// What about mime-type header?
						getFromResource(resNm)
					} ~ path (ptok_ivct / Segment) { fnm =>
						lgr.info("matched GET img_vctr-file: {}", fnm)
						val resNm = fldrPth_ivct + FLD_SEP + fnm
						lgr.info("returning resource at: {}", resNm)
						// What about mime-type header?
						getFromResource(resNm)
					} ~	pass {
						lgr.info("Extracting unexpected path following wdat")
						extractUnmatchedPath { remPath =>
							lgr.info("remaining path: {}", remPath)
							complete(s"Did not understand request for webdata at: $remPath with params: $paramMap")

						}
					}
				}
			}
		}
		wbRscRt
	}
}

trait IconNmSrc {
	val tstIcNmLns =
		"""
local_bar
screen_lock_portrait
folder-add
battery-75
zoom-out
wb_cloudy
target8
image_aspect_ratio
crop_free
document-stroke
filter_center_focus
chart-line
windows8
radio4
signal_wifi_off
heart5
		"""

/*
stripMargin(marginChar: Char): String
For every line in this string:
Strip a leading prefix consisting of blanks or control characters followed by
marginChar from the line.  WHERE marginChar defaults to `|`

 */
	val someIconNms : Seq[String] = tstIcNmLns.toString.split("\n").toSeq
			.map(_.trim)
			.filter(_ != "")

}
	/*
val route =
  path("a") {
    complete(HttpResponse(entity = "foo"))
  } ~
    path("b") {
      complete(StatusCodes.OK)
    } ~
    path("c") {
      complete(StatusCodes.Created -> "bar")
    } ~
    path("d") {
      complete(201 -> "bar")
    } ~
    path("e") {
      complete(StatusCodes.Created, List(`Content-Type`(`text/plain(UTF-8)`)), "bar")
    } ~
    path("f") {
      complete(201, List(`Content-Type`(`text/plain(UTF-8)`)), "bar")
    } ~
    path("g") {
      complete(Future { StatusCodes.Created -> "bar" })
    } ~
    (path("h") & complete("baz")) // `&` also works with `complete` as the 2nd argument

	 */
/*
type Route = RequestContext => Future[RouteResult]

		val pageTxt = "<h1>Say hello to akka-http</h1>"
		val pageEnt = myEntMkr.makeHtmlEntity(pageTxt)
		complete(pageEnt)
*/
/*
val order = path("order" / IntNumber) & parameters('oem, 'expired ?)
val route =
  order { (orderId, oem, expired) =>
...
  }
 */
