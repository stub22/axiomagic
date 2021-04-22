package axmgc.dmo.fin.ontdmp


import org.apache.jena.rdf.model.{RDFNode, StmtIterator, Model => JenaMdl, ModelFactory => JenaMdlFctry, Property => JenaProp, Resource => JenaRsrc, Statement => JenaStmt}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

import scala.collection.mutable.ListBuffer

private trait MdlDumpStuff

trait MdlDmpFncs extends  StmtXtractFuncs  with StdGenVocab {
	protected def getS4JLog: Logger

	private val myLog: Logger = getS4JLog

	def dmpPrfxs(jenaMdl: JenaMdl): MdlSummaryStat = {
		val numPrfxs = jenaMdl.numPrefixes
		myLog.info("Num prefixes: {}", numPrfxs)
		val prfxMap: java.util.Map[String, String] = jenaMdl.getNsPrefixMap
		val sclPrfxMp = prfxMap.asScala
		var prfxIdx = 0
		sclPrfxMp.foreach(entry => {
			if ((prfxIdx < 20) || (prfxIdx % 20 == 0)) {
				myLog.info("Prefix [{}] = {}", prfxIdx, entry)
			}
			prfxIdx += 1
		})
		MdlSummaryStat("prefixCnt", numPrfxs)
	}

	def dumpNSs(jenaMdl: JenaMdl): MdlSummaryStat = {
		var nsCnt = 0;
		val nsIt = jenaMdl.listNameSpaces()
		while (nsIt.hasNext) {
			val ns = nsIt.nextNs()
			myLog.info("Found NS [{}]: {}", nsCnt, ns)
			nsCnt += 1
		}
		myLog.info("Found {} 'namespaces', using the idiosyncratic and disreputable method:  model.listNameSpaces() ", nsCnt)
		MdlSummaryStat("namespaceCnt", nsCnt)
	}

	def visitSubjRsrcs(jenaMdl: JenaMdl): MdlSummaryStat = {
		var subjCnt = 0;
		val subjIt = jenaMdl.listSubjects()
		while (subjIt.hasNext) {
			val subjRes = subjIt.nextResource()
			if ((subjCnt < 700) && ((subjCnt % 100) < 7)) {
				myLog.info("Found subj-res[{}]: {}", subjCnt, subjRes)
			}
			subjCnt += 1
		}
		myLog.info("Found {} subjects", subjCnt)
		MdlSummaryStat("subjects", subjCnt)
	}

	def visitOwlImports(jenaMdl: JenaMdl): MdlStat = {
		val prop_owlImport: JenaProp = jenaMdl.getProperty(baseUriTxt_owl, propLN_owlImports)
		val bindMap: Map[JenaRsrc, Traversable[JenaRsrc]] = pullRsrcArcsAtProp(prop_owlImport, false)
		dumpGroupSummary(bindMap, "'owl:imports'")
		val uniqTgtCnt = dumpUniqTgts(bindMap)
		MdlSummaryStat("owlImportsCount", uniqTgtCnt)
	}

	def visitRdfTypes(jenaMdl: JenaMdl): MdlStat = {
		val prop_rdfType: JenaProp = jenaMdl.getProperty(baseUriTxt_rdf, propLN_rdfType)
		val bindMap: Map[JenaRsrc, Traversable[JenaRsrc]] = pullRsrcArcsAtProp(prop_rdfType, false)
		dumpGroupSummary(bindMap, "'rdf:type'")
		val uniqTgtCnt = dumpUniqTgts(bindMap)
		MdlSummaryStat("rdfTypsCount", uniqTgtCnt)
	}

	def dumpTypeHistogram(jenaMdl: JenaMdl): MdlStat = {
		val prop_rdfType: JenaProp = jenaMdl.getProperty(baseUriTxt_rdf, propLN_rdfType)
		val bindMap: Map[JenaRsrc, Traversable[JenaRsrc]] = pullRsrcArcsAtProp(prop_rdfType, true)
		// dumpGroupSummary(bindMap, "typeHisto")
		val srtdKys = bindMap.keys.toSeq.sortBy(_.getURI)
		val sampleSize = 3
		var keyCnt = 0
		srtdKys.foreach(kyRsrc => {
			keyCnt += 1
			val vLst = bindMap.get(kyRsrc).getOrElse(Nil)
			val vLstFront = vLst.take(sampleSize)
			val dmpd = s"key ${keyCnt} [${kyRsrc}] bound to ${vLst.size} values.  First ${sampleSize} are: ${vLstFront}"
			myLog.info(dmpd)
		})
		MdlSummaryStat("histKeyCnt", keyCnt)
	}

	// private def doDump(currCnt, initSeg, mod)
	private def dumpGroupSummary(bindMap: Map[JenaRsrc, Traversable[JenaRsrc]], groupLabel: String): Int = {
		val initSegLen = 10
		val skipWidth = 25
		val sampleSize = 3
		var bindCnt = 0
		var keyCnt = 0
		myLog.info("==========================================================================")
		myLog.info("Dumping stats for group = {}, bind map has width: {}", groupLabel, bindMap.size)
		bindMap.foreach(pair => {
			val keyRsrc = pair._1
			val vLst = pair._2
			val vLstLen = vLst.size
			keyCnt += 1
			bindCnt += vLstLen
			if (keyCnt <= initSegLen || (keyCnt % skipWidth == 0)) {
				val objLstBegin = vLst.take(sampleSize)
				val dmpd = s"key ${keyCnt} [${keyRsrc}] bound to ${vLstLen} values.  First ${sampleSize} are: ${objLstBegin}"
				myLog.info(dmpd)
			}
		})
		myLog.info("Finished dumping stats for group={}, binding count total: {}", groupLabel, bindCnt)
		myLog.info("==========================================================================")
		bindCnt
	}

	private def dumpUniqTgts(bindMap: Map[JenaRsrc, Traversable[JenaRsrc]]): Int = {
		val redundTgts: Seq[JenaRsrc] = bindMap.values.flatten.toSeq
		val redundantTgtUris: Seq[String] = redundTgts.map(_.getURI)
		myLog.info("Redundant tgt uri count (same as binding count, right?!): {}", redundantTgtUris.size)
		val uniqTgtUris: Seq[String] = redundantTgtUris.distinct.sorted
		myLog.info("Uniq tgt uri count: {}", uniqTgtUris.size)
		myLog.info("Uniq tgt uris: {}", uniqTgtUris)
		// For owl:imports, the target URI often has no local name.
		val redundLocalNames: Seq[String] = redundTgts.map(_.getLocalName)
		val uniqLocNams = redundLocalNames.distinct.sorted
		myLog.info("Uniq local name count: {}", uniqLocNams.size)
		myLog.info("Uniq local names: {}", uniqLocNams)
		uniqLocNams.size
	}

	def dumpPropsTallyByName(mdl: JenaMdl): Int  = {
		val sckr = new MdlPrpSucker {}
		val tallyMap: Map[JenaProp, Int] = sckr.tallyProps(mdl)
		val propsByURI: Seq[JenaProp] = tallyMap.keys.toSeq.sortBy(_.getURI)
		val uniqPropCnt = propsByURI.size
		myLog.info("Uniq prop count: {}", uniqPropCnt)
		dumpPropsTally(propsByURI, tallyMap)
		uniqPropCnt
	}

	def dumpPropsTallyByCount(mdl: JenaMdl): Int = {
		val sckr = new MdlPrpSucker {}
		val tallyMap: Map[JenaProp, Int] = sckr.tallyProps(mdl)
		val pairsByCount: Seq[(JenaProp, Int)] = tallyMap.toSeq.sortBy(_._2)
		myLog.info("Pairs count: {}", pairsByCount.size)
		val propsByCount: Seq[JenaProp] = pairsByCount.map(_._1)
		dumpPropsTally(propsByCount, tallyMap)
		propsByCount.size
	}

	def dumpPropsTally(propSeq: Seq[JenaProp], tallyMap: Map[JenaProp, Int]): Unit = {
		var propIdx = 0
		propSeq.foreach(prp => {
			propIdx += 1
			val tally = tallyMap.getOrElse(prp, -9999999)
			val dumped = s"Prop[${propIdx}] uri=${prp.getURI} tally=${tally}"
			myLog.info(dumped)
		})

	}
}

class OntQryMgr {
	private val myS4JLog = LoggerFactory.getLogger(this.getClass)

	private val mdlDumpFncs = new MdlDmpFncs {
		override protected def getS4JLog: Logger = myS4JLog
	}

	private val statToJson = new MdlSttJsonMaker {}

	def mkTrivStt(stNm : String, stCnt : Int) : MdlStat = {
		myS4JLog.info(s"mkTrivStt nm=${stNm} cnt=${stCnt}")
		new MdlSummaryStat(stNm, stCnt)
	}
	private def hedgeAndMakeBoth (jenaMdl: JenaMdl): (Seq[MdlStat], Seq[MdlSummaryStat]) = {
		val rstts = new ListBuffer[MdlStat]
		val rsltStats = new ListBuffer[MdlSummaryStat]
		val size = jenaMdl.size()
		val mdlSzStt = mkTrivStt("jenaMdlSize", size.toInt)
		val prfxStt = mdlDumpFncs.dmpPrfxs(jenaMdl)
		val nsStt = mdlDumpFncs.dumpNSs(jenaMdl)
		rstts.append(mdlSzStt, prfxStt, nsStt)
		rsltStats.append(prfxStt, nsStt)
		val owlImpsRprt: MdlStat = mdlDumpFncs.visitOwlImports(jenaMdl)
		rstts.append(owlImpsRprt)
		val rdfTypsRprt: MdlStat = mdlDumpFncs.visitRdfTypes(jenaMdl)
		rstts.append(rdfTypsRprt)
		val subjRprt: MdlSummaryStat = mdlDumpFncs.visitSubjRsrcs(jenaMdl)
		rsltStats.append(subjRprt)
		rstts.append(subjRprt)
		val typHistoRprt: MdlStat = mdlDumpFncs.dumpTypeHistogram(jenaMdl)
		val prpTyllyByNmRprt: Int = mdlDumpFncs.dumpPropsTallyByName(jenaMdl)
		val prpTllyByCntRprt: Int = mdlDumpFncs.dumpPropsTallyByCount(jenaMdl)
		rstts.append(typHistoRprt)
		(rstts.toList, rsltStats.toList)
	}
	private def dumpOntoSummaryStatsToLog(jenaMdl: JenaMdl): Seq[MdlSummaryStat] = {
		val pair = hedgeAndMakeBoth(jenaMdl)
		pair._2
	}
	private def collectGenMdlStats(jenaMdl: JenaMdl): Seq[MdlStat] = {
		val pair = hedgeAndMakeBoth(jenaMdl)
		pair._1
	}

	def dumpMdlStatsToJsnArrTxt(jenaMdl: JenaMdl) : String = {
		val statJsnArrTxt = if (false) {
			val statSeq: Seq[MdlSummaryStat] = dumpOntoSummaryStatsToLog(jenaMdl)
			statToJson.summStatsToJsArrTxt(statSeq, true)
		} else {
			val mstts = collectGenMdlStats(jenaMdl)
			statToJson.mdlStatsToJsArrTxt(mstts, true)
		}
		statJsnArrTxt
	}
}
