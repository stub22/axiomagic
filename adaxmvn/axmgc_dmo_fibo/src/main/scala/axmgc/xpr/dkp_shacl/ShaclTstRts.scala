package axmgc.xpr.dkp_shacl

import akka.http.scaladsl.{Http, server => dslServer}
import dslServer.Directives.{complete, entity, get, path, parameterMap, _}
// import dslServer.Directive0

trait ShaclTstRts

trait ShaclOpRts {
	// Validate, Infer, ...
	val pth_valid = "valid"
	val arg_dtPth = "dtPth"
	val arg_shPth = "shPth"

	protected def getTstShaclRun : TstShaclRun

	def mkShaclTstRt  : dslServer.Route = {

		val mainRt = parameterMap { paramMap =>
			path(pth_valid) {

				val vl_dt : String = paramMap.get(arg_dtPth).getOrElse("no_dtPth_set")
				val vl_sh : String = paramMap.get(arg_shPth).getOrElse("no_shPth_set")
				val tstShaclRun = getTstShaclRun
				val rprtRsrc = tstShaclRun.mkValidationReport(vl_dt, vl_sh, true)
				val rprtPrnt : String = tstShaclRun.printReportToString(rprtRsrc.getModel)
				val pmTxt = paramMap.toString()
				val output = "RqParams: " + pmTxt + "\n========================\n" + rprtPrnt
				complete(output)
			} //  ~ path(webFeat.pthTok_dgo)
		}
		mainRt
	}

}
