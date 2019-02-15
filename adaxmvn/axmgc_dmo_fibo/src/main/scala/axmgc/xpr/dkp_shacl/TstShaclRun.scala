package axmgc.xpr.dkp_shacl

import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.Resource
import org.apache.jena.util.FileUtils
import org.topbraid.jenax.util.JenaUtil
import org.topbraid.shacl.util.ModelPrinter
import org.topbraid.shacl.validation.ValidationUtil

import org.slf4j.{Logger, LoggerFactory}

trait TstShaclRun {
	protected def getSL4JLog : Logger
	lazy val myLog = getSL4JLog

	def mkVldRprtTxt : String = {
		// Load the main data model
		val dataModel = JenaUtil.createMemoryModel
		val txtUri_dmmy = "urn:dummy"
		val inFilePth = "/gdat/shacl_tst/core/property/class-001.test.ttl"
		myLog.info("inFilePath: {}", inFilePth)
		val inFileResStrm = classOf[ValidationUtil].getResourceAsStream(inFilePth)

		dataModel.read(inFileResStrm, txtUri_dmmy, FileUtils.langTurtle)

		val instDtMdl = dataModel
		val shapsDtMdl = dataModel
		val flg_shpVldt = true

		val report = ValidationUtil.validateModel(instDtMdl, shapsDtMdl, flg_shpVldt)
		val rprtMdl = report.getModel
		val prntdRprt = ModelPrinter.get.print(rprtMdl)
		prntdRprt
	}
}
