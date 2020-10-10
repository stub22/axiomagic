package axmgc.web.ent

private trait WebScriptRefs

private trait WebScriptPaths {
	val		fldr_wdat = "wdat"
	val		fldr_axmgc_js = fldr_wdat + "/axmgc_js"

	val		fldr_axdp = fldr_axmgc_js + "/_axdp"

	val		scr_axdp = fldr_axdp + "/axdp_v02E.js"


	val		fldr_wrpMjs = fldr_axmgc_js + "/wrp_mjs"
	val 	scr_supdmd 		= fldr_wrpMjs + "/supdmd_E.js"
	val		scr_exprKinds 	= fldr_wrpMjs + "/exprKinds_C.js"
	val		scr_exprWrap 	= fldr_wrpMjs + "/exprWrap_C.js"

	val 	fldr_wrpVeg = fldr_axmgc_js + "/wrp_veg"
	val		scr_vtDatFncs 	= fldr_wrpVeg + "/vtDatFncs_C.js"

	val 	fldr_wrpPltl = fldr_axmgc_js + "/wrp_pltl"
	val		scr_pzDatSurf	= fldr_wrpPltl + "/pzDatSurf_C.js"

	val 	fldr_datGui = fldr_axmgc_js + "/wrp_datgui"
	val		scr_suppDmdPrmGui= fldr_datGui + "/suppDmdPrmGui_E.js"
	val		scr_tplIcoEvts	= fldr_datGui + "/tplIcoEvtHndlr.js"


	def getManyScrPaths : Seq[String] = {
		val paths = List(scr_axdp, scr_supdmd, scr_exprKinds, scr_exprWrap, scr_vtDatFncs, scr_pzDatSurf, scr_suppDmdPrmGui, scr_tplIcoEvts)
		paths
	}


}

/* refs snapped from suppDmd_v0E.html :

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