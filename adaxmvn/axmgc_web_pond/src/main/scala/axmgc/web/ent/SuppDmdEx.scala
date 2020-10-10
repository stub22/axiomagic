package axmgc.web.ent

private trait SuppDmdEx

trait SupplyAndDemand {

	private val xmlBody = <body>
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
}
