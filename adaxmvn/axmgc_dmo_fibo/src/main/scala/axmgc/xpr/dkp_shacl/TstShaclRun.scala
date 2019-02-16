package axmgc.xpr.dkp_shacl

import java.io.InputStream

import org.apache.jena.rdf.model.{Model => JenaModel, ModelFactory => JenaModelFactory, Resource => JenaResource}
import org.apache.jena.util.FileUtils
import org.topbraid.jenax.util.JenaUtil
import org.topbraid.shacl.util.ModelPrinter
import org.topbraid.shacl.validation.ValidationUtil
import org.slf4j.{Logger, LoggerFactory}

// Release v1.1.0 of Topbraid depends on 3.7.0 of Jena, which is behind our
// current Axiomagic 3.8.0.
// But this commit from Jul 2018 upgrades the shacl source.
// https://github.com/TopQuadrant/shacl/commit/ef50b4bde32ed0e9019f2a6f5189c988bd12a8f0

trait TstShaclRun {
	protected def getSL4JLog : Logger
	lazy val myLog = getSL4JLog

	def mkVldRprtTxt : String = {
		// Load the main data model
		val dmmyBaseUriTxt = "urn:dummy"
		val testRsrcPth = "/gdat/shacl_tst/core/property/class-001.test.ttl"

		val inFileResStrm = openLocalRsrc(testRsrcPth)
		val vldtTstMdl_dual = readTrtlToMemMdl(inFileResStrm, dmmyBaseUriTxt)

		val instDtMdl = vldtTstMdl_dual
		val shapsDtMdl = vldtTstMdl_dual
		val flg_shpVldt = true

		val report = ValidationUtil.validateModel(instDtMdl, shapsDtMdl, flg_shpVldt)
		val rprtMdl = report.getModel
		val prntdRprt = ModelPrinter.get.print(rprtMdl)
		prntdRprt
	}
	/*
https://jena.apache.org/documentation/javadoc/jena/org/apache/jena/rdf/model/Model.html#read-java.io.InputStream-java.lang.String-java.lang.String-
base - the base uri to be used when converting relative URI's to absolute URI's. (Resolving relative URIs and fragment IDs is done by prepending the base URI to the relative URI/fragment.) If there are no relative URIs in the source, this argument may safely be null. If the base is the empty string, then relative URIs will be retained in the model. This is typically unwise and will usually generate errors when writing the model back out.
	 */
	private def readTrtlToMemMdl(inStrm : InputStream, baseUriTxt : String) : JenaModel = {
		val dataModel = JenaUtil.createMemoryModel  // TopBraid utility method, probly about same as JenaFactory
		dataModel.read(inStrm, baseUriTxt, FileUtils.langTurtle)
		dataModel
	}
	private def openLocalRsrc(resPth : String) : InputStream = {
		myLog.info("opening local rsrc: {}", resPth)

		this.getClass.getResourceAsStream(resPth)
	}
	private def openShaclLibRes (resPth : String) : InputStream = {
		classOf[ValidationUtil].getResourceAsStream(resPth)
	}
	def checkShaclOntDat: Unit = {
		val resPath_shacl = "/rdf/shacl.ttl"
		val resPath_dash = "/rdf/dash.ttl"
		val resPath_sysTrip = "/rdf/system-triples.ttl"
		val resPath_tosh = "/rdf/tosh.ttl"

		val resStrm_vocabShacl = openShaclLibRes(resPath_shacl)
		val dmmyBaseUriTxt = "urn:dummy"
		val mdl_vocabShacl = readTrtlToMemMdl(resStrm_vocabShacl, dmmyBaseUriTxt)
		myLog.info("Statement count for shacl vocab: {}", mdl_vocabShacl.size)
		myLog.info("Dumping shacl vocab: {}", mdl_vocabShacl)

		val resStrm_glossOwl = openShaclLibRes(resPath_sysTrip)
		val mdl_glossOwl = readTrtlToMemMdl(resStrm_glossOwl, dmmyBaseUriTxt)
		myLog.info("Statement count for owl glossary: {}", mdl_glossOwl.size)
		myLog.info("Dumping owl glossary: {}", mdl_glossOwl)

		val resStrm_vocabDash = openShaclLibRes(resPath_dash)
		val mdl_vocabDash = readTrtlToMemMdl(resStrm_vocabDash, dmmyBaseUriTxt)
		myLog.info("Statement count for dash vocab: {}", mdl_vocabDash.size)
		myLog.info("Dumping dash vocab: {}", mdl_vocabDash)

	}
}
