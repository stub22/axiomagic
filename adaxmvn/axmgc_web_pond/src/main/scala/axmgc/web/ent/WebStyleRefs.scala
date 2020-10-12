package axmgc.web.ent

import axmgc.web.rsrc.WebRsrcFolders

private trait WebStyleRefs

trait WebStylePaths extends WebRsrcFolders {
	val urlPth_styIcn = FLD_SEP + fldrPth_styl +  FLD_SEP + "icmbg_sty.css"
	val urlPth_styDem = FLD_SEP + fldrPth_styl +  FLD_SEP + "icmbg_dem.css"
	val urlPth_styGrd = FLD_SEP + fldrPth_styl +  FLD_SEP + "ictst_grid.css"

	val urlPth_stySuppDmd = FLD_SEP + fldrPth_styl +  FLD_SEP + "suppDmd_sty.css"
}
