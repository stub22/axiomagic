package axmgc.web.rsrc

import akka.http.scaladsl.server.Directives.{complete, get, path, _}
import akka.http.scaladsl.server.PathMatchers
import akka.http.scaladsl.{server => dslServer}
import org.slf4j.Logger  // Que hace?

trait WebRsrc // file-marker trait matching filename

trait WebRsrcFolders {
	val FLD_SEP = '/'
	val ptok_wdat = "wdat"
	val ptok_styl = "stylz"
	val ptok_ivct = "img_vctr"
	val ptok_scrjs = "axmgc_js"
	val fldrPth_wdat = ptok_wdat
	val fldrPth_styl = fldrPth_wdat + FLD_SEP + ptok_styl
	val fldrPth_ivct = fldrPth_wdat + FLD_SEP + ptok_ivct
	val fldrPth_scrjs = fldrPth_wdat + FLD_SEP + ptok_scrjs

}

trait WebRsrcRouteMkr extends WebRsrcFolders {
	def makeWbRscRt (lgr : Logger) : dslServer.Route = {
		lgr.info(s"fldrPth_styl = $fldrPth_styl")
		lgr.info(s"fldrPth_ivct = $fldrPth_ivct")
		lgr.info(s"fldrPth_scrjs = $fldrPth_scrjs")
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

		// 2021-12-26 Added postfixOps (which is "not recommended") as workaround during upgrade to Scala 2.13
		// https://www.scala-lang.org/api/2.13.x/scala/language$.html#postfixOps:languageFeature.postfixOps
		// See note below line 67 (was line 60):      pathPrefix (ptok_scrjs / )

		import scala.language.postfixOps

		val wbRscRt =
		get {
			parameterMap { paramMap =>
				lgr.info("paramMap: {}", paramMap)
				pathPrefix(ptok_wdat) {
					lgr.info("matched prefix: {}", ptok_wdat)
					path (ptok_styl / Segment) { fnm =>
						lgr.info("matched GET style-file: {}", fnm)
						val resNm = fldrPth_styl + FLD_SEP + fnm
						lgr.info("returning style resource at: {}", resNm)
						// What about mime-type header?
						getFromResource(resNm)
					} ~ path (ptok_ivct / Segment) { fnm =>
						lgr.info("matched GET img_vctr-file: {}", fnm)
						val resNm = fldrPth_ivct + FLD_SEP + fnm
						lgr.info("returning vct-image resource at: {}", resNm)
						// What about mime-type header?
						getFromResource(resNm)
					} ~ pathPrefix (ptok_scrjs / ) {
/*
On upgrade to Scala 2.13 we encountered this error relating to the / operator:
[ERROR] E:/_emnt/axio_git_clnz/agc_02/adaxmvn/axmgc_web_pond/src/main/scala/axmgc/web/rsrc/WebRsrc.scala:60: postfix operator / needs to be enabled
by making the implicit value scala.language.postfixOps visible.
 */
						lgr.info ("Found GET js-script prefix: {}", ptok_scrjs)
						extractUnmatchedPath { jsPth =>
							lgr.info("Extract js-script path: {}", jsPth)
							val resNm = fldrPth_scrjs + FLD_SEP + jsPth
							lgr.info("returning javascript resource at: {}", resNm)
							// What about mime-type header?
							getFromResource(resNm)
						}
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
	private val tstIcNmLns =
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
	private val someIconNms : Seq[String] = tstIcNmLns.toString.split("\n").toSeq
			.map(_.trim)
			.filter(_ != "")


	private lazy val myRsrcNms = new RsrcNms {}
	// lazy val myRealIconNms =
	private def loadIconNms() : Seq[String] = {
		val nmLines : Seq[String] = myRsrcNms.readIcnNms
		nmLines
	}
	private lazy val myIconNms_sorted : Seq[String] = loadIconNms()
	def getShuffledIcoNms : Seq[String] = scala.util.Random.shuffle(myIconNms_sorted)

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
