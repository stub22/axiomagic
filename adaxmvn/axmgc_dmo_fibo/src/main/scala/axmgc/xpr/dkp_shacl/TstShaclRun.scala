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
	val dmmyBaseUriTxt = "urn:dummy"
	def mkVldRprtTxt : String = {
		val testRsrcPth = "/gdat/shacl_tst/core/property/class-001.test.ttl"
		val reportRsrc : JenaResource = loadValidateReport(testRsrcPth)
		val rprtMdl = reportRsrc.getModel
		val prntdRprt = printReportToString(rprtMdl)
		prntdRprt
	}
	def printReportToString (rprtMdl : JenaModel) : String = {
		val prntdRprt = ModelPrinter.get.print(rprtMdl)
		prntdRprt
	}
	private def readLclRsrcMdlTrtl (rsrcPath : String) : JenaModel = {
		val inFileResStrm = openLocalRsrc(rsrcPath)
		val mdl = readTrtlToMemMdl(inFileResStrm, dmmyBaseUriTxt)
		mdl
	}
	def loadValidateReport(unifiedMdlPth: String) : JenaResource = {
		val vldtTstMdl_dual : JenaModel = readLclRsrcMdlTrtl(unifiedMdlPth)
		val instDtMdl = vldtTstMdl_dual
		val shpsDtMdl = vldtTstMdl_dual
		val flg_shpVldt = false // since we know inst + shapes are same, this extra pass would be wasteful.
		val reportRsrc = mkValidationReport(instDtMdl, shpsDtMdl, flg_shpVldt)
		reportRsrc
	}
	def mkValidationReport(instDtMdl : JenaModel, shpsMdl : JenaModel, flg_instHasShps : Boolean): JenaResource = {
		val reportRsrc : JenaResource = ValidationUtil.validateModel(instDtMdl, shpsMdl, flg_instHasShps)
		//		val rprtMdl = reportRsrc.getModel
		reportRsrc
	}
	def mkValidationReport(pthInst : String, pthShp : String, flg_instHasShps : Boolean): JenaResource = {
		val resStrm_inst : InputStream = openLocalRsrc(pthInst)
		val mdl_inst = readTrtlToMemMdl(resStrm_inst, dmmyBaseUriTxt)

		val resStrm_shp : InputStream = openLocalRsrc(pthShp)
		val mdl_shp = readTrtlToMemMdl(resStrm_shp, dmmyBaseUriTxt)

		mkValidationReport(mdl_inst, mdl_shp, flg_instHasShps)
	}


		/**
	  * Validates a given data Model against all shapes from a given shapes Model.
	  * If the shapesModel does not include the system graph triples then these will be added.
	  * Entailment regimes are applied prior to validation.
	  * @param dataModel  the data Model
	  * @param shapesModel  the shapes Model
	  * @param validateShapes  true to also validate any shapes in the data Model (false is faster)
	  * @return an instance of sh:ValidationReport in a results Model
	public static Resource validateModel(Model dataModel, Model shapesModel, boolean validateShapes) {
		return validateModel(dataModel, shapesModel, new ValidationEngineConfiguration().setValidateShapes(validateShapes));
	}
	  */

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
/*
https://henrietteharmse.com/2018/03/14/rule-execution-with-shacl/

https://github.com/henrietteharmse/henrietteharmse/blob/master/blog/tutorial/jena/source/shacl/src/main/java/org/shacl/tutorial/ShaclClassification.java


import org.topbraid.shacl.rules.RuleUtil

import org.topbraid.jenax.util.JenaUtil
import org.topbraid.shacl.rules.RuleUtil
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths
   def main(args: Array[String]): Unit =  { try  { val path: Path = Paths.get(".").toAbsolutePath.normalize
val data: String = "file:" + path.toFile.getAbsolutePath + "/src/main/resources/bakery.ttl"
val shape: String = "file:" + path.toFile.getAbsolutePath + "/src/main/resources/bakeryRules.ttl"
val reasoner: Nothing = ReasonerRegistry.getRDFSReasoner
val dataModel: Nothing = JenaUtil.createDefaultModel
dataModel.read(data)
val infModel: Nothing = ModelFactory.createInfModel(reasoner, dataModel)
val validity: Nothing = infModel.validate
if (!(validity.isValid))  { logger.trace("Conflicts")
val i: Nothing = validity.getReports
while ( { i.hasNext})  { logger.trace(" - " + i.next)
}
}
else  { val shapeModel: Nothing = JenaUtil.createDefaultModel
shapeModel.read(shape)
var inferenceModel: Nothing = JenaUtil.createDefaultModel
inferenceModel = RuleUtil.executeRules(infModel, shapeModel, inferenceModel, null)
val inferences: String = path.toFile.getAbsolutePath + "/src/main/resources/inferences.ttl"
val inferencesFile: Nothing = new Nothing(inferences)
inferencesFile.createNewFile
val reportOutputStream: OutputStream = new FileOutputStream(inferencesFile)
RDFDataMgr.write(reportOutputStream, inferenceModel, RDFFormat.TTL)
}
} catch {
case t: Throwable =>
logger.error(WTF_MARKER, t.getMessage, t)
}
}
 */