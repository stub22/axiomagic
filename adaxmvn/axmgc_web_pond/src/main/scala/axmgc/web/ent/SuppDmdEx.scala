package axmgc.web.ent

import akka.http.scaladsl.server.Directives.{complete, parameterMap, path}
import akka.http.scaladsl.{server => dslServer}
import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}
import axmgc.web.tuple.WebRqPrms
import org.slf4j.Logger

import scala.xml.{Elem => XElem, Node => XNode, NodeSeq => XNodeSeq, Null => XNull, Text => XText, UnprefixedAttribute => XUAttr}

private trait SuppDmdEx


trait SupplyAndDemand {

	val stylePaths = "suppDmd_sty.css"
	val scriptPaths = "sdPlotGui.js"

	private val stylPths = new WebStylePaths {}
	val xph = new XmlPageHelp{}

	private val mjScriptTxt =
		""" //another comment after bgn-cdat then LINE-BREAK:
			MathJax.Hub.Config({
				jax: ["input/TeX","output/HTML-CSS"],
				displayAlign: "left"
			});
	    //comment hiding end-cdat  """.stripMargin

	lazy val mjScript_cdata = new scala.xml.PCData(mjScriptTxt)

	private val scrXNS = xph.mkTstScrXNS

	val xmlHead = <head>
		<title>AxMgc generated HTML : SuppDmd Example</title>

		<meta name="viewport" content="width=device-width, initial-scale=1"></meta>
		<meta charset="utf-8"></meta>

		<link rel="stylesheet" href={stylPths.urlPth_stySuppDmd}></link>

		{scrXNS}

		<script type="text/x-mathjax-config">// comment hiding begin-cdat {mjScript_cdata}</script>

		<!-- Dat-GUI from CDN (not from NPM) -->
		<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/dat-gui/0.7.6/dat.gui.js"></script>
		<!-- MathJax from CDN (not from NPM):  note the async attribute  -->
		<script type="text/javascript" async="async"
				src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.5/MathJax.js?config=TeX-MML-AM_CHTML"></script>

		<script type="text/javascript" defer="defer" src="wdat/axmgc_js/wrp_datgui/sdPlotGui.js"></script>
	</head>

	val xmlBody = <body>
		<h3>m: Supply, Demand, Price, Cost, Profit, Income Tax, Carbon Fee</h3>
		<div class="flx-horiz">
			<div id="suppDmd_mdl_desc">
				<table>
					<caption>Decision Variables</caption>
					<tr><th>Description</th><th>Symbol</th><th>Range Bounds</th></tr>
					<tr><th>Absolute Qty</th><td>X_qtyAbs</td><td>nope</td></tr>
					<tr><th>Quality</th><td>X_quality</td><td>nope</td></tr>
					<tr><th>Unit Price</th><td>X_unitPrice</td><td>nope</td></tr>

				</table>

				<table id="mp_tab">
					<caption>Model Params affecting Cost and Demand</caption>
					<tr><th>Description</th><th>Symbol</th><th>Value From GUI</th></tr>
					<tr><th>Quality-Cost Power</th><td>P_qualCostPwr</td><td>nope</td></tr>
					<tr><th>Quantity Cost Power</th><td>P_qtyCostPwr</td><td>nope</td></tr>
					<tr><th>Fixed Cost</th><td>P_fixedCost</td><td>nope</td></tr>
					<tr><th>Quality Demand Powery</th><td>P_qualDemandPwr</td><td>nope</td></tr>
					<tr><th>Price Demand Power</th><td>P_priceDemandPwr</td><td>nope</td></tr>
					<tr><th>Max Quantity</th><td>C_qtyMax</td><td>nope</td></tr>
				</table>

				<table>
					<caption>Effects To Calculate</caption>
					<tr><th>Description</th><th>Symbol</th><th>Defining Expression</th></tr>
					<tr><th>Normalized Qty</th><td>V_qtyNrm</td><td id="mth_qtyNrm"></td></tr>
					<tr><th>Variable Cost</th><td>V_vrblCost</td><td id="mth_vrblCost"></td></tr>
					<tr><th>Total Cost</th><td>V_totalCost</td><td id="mth_totalCost"></td></tr>
					<tr><th>Demand</th><td>V_demand</td><td id="mth_demand"></td></tr>
					<tr><th>Max Profit</th><td>V_maxProfit</td><td id="mth_maxProfit"></td></tr>
				</table>
			</div>
			<div id="prpr">
				<div id="param_scrn"></div>
				<div>After Dat-GUI is hidden by insert</div>
			</div>
			<div id="div_tues_flow" class="inset_div">
				<div>before the Loopy #TuesdayMoney snapshot</div>
				<img id="img_tues_flow" class="inset_img"  src="_img/tues_carb_12_run_B_crop.png"
					 width="989" height="609"/>
				<div>after Loopy #TuesdayMoney</div>
			</div>
		</div>
		<hr/>
		<div class="flx-horiz">
			<div>
				<div>before the vega scatterplots</div>
				<div id="vvd3">Inside #vvd3</div>
				<div>after the scatterplots</div>
			</div>
			<div>
				<div>before the peep mdl</div>
				<div id="div_peep_nlg">
					<img src="_img/peep_mdl_C01b_interface.png"></img>
				</div>
				<div>between peeps and plotly-3D</div>
				<div id="pl3dx3">Inside #pl3dx3</div>
				<div>after plotly-3D</div>
			</div>
		</div>
	</body>

	def mkResponse(rqParams : Map[String, String]) : HEStrict = {
		val headXE = xmlHead
		val bodyXE = xmlBody
		val pageXE = xph.mergeXhtml(headXE, bodyXE)
		val pageEnt = xph.makePageEnt(pageXE)
		pageEnt
	}
	def makeSuppDmdRt (lgr : Logger) : dslServer.Route = {
		val pathTxt = "suppdmd"

		val sdRoute = parameterMap { rqParams : Map[String, String] => path(pathTxt) {
			val rspEnt = mkResponse(rqParams)
			complete(rspEnt)
		}}
		sdRoute
	}
}

/*
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width">
  <title>Calculate: Supply, Demand, Price</title>
    <script type="text/javascript"  src="../_lib/_axdp/axdp_v02E.js"></script>
    <script type="text/javascript" src="../_lib/ax_js_2E/wrp_mjs/supdmd_E.js"></script>
    <script type="text/javascript" src="../_lib/ax_js_2E/wrp_mjs/exprKinds_C.js"></script>
    <script type="text/javascript" src="../_lib/ax_js_2E/wrp_mjs/exprWrap_C.js"></script>
    <script type="text/javascript" src="../_lib/ax_js_2E/wrp_veg/vtDatFncs_C.js"></script>
    <script type="text/javascript" src="../_lib/ax_js_2E/wrp_pltl/pzDatSurf_C.js"></script>
    <script type="text/javascript" src="../_lib/ax_js_2E/wrp_datgui/suppDmdPrmGui_E.js"></script>
    <script type="text/x-mathjax-config">
MathJax.Hub.Config({
    jax: ["input/TeX","output/HTML-CSS"],
    displayAlign: "left"
});
    </script>
    <!-- Dat-GUI from CDN (not from NPM) -->
    <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/dat-gui/0.7.6/dat.gui.js"></script>
    <!-- MathJax from CDN (not from NPM):  note the async attribute  -->
    <script type="text/javascript" async
            src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.5/MathJax.js?config=TeX-MML-AM_CHTML"></script>
 */