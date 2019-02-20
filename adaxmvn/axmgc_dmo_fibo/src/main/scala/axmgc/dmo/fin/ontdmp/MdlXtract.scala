package axmgc.dmo.fin.ontdmp

import org.apache.jena.rdf.model.{RDFNode, StmtIterator}
import org.apache.jena.rdf.model.{RDFNode, StmtIterator, Statement => JenaStmt, Model => JenaMdl, ModelFactory => JenaMdlFctry, Property => JenaProp, Resource => JenaRsrc}
import collection.mutable.{ HashMap => MutHashMap,  HashSet => MutHashSet}

trait  MdlXtract

trait StmtXtractFuncs {
	import scala.collection.JavaConverters._

	def pullRsrcPair_SO (stmt : JenaStmt) : (JenaRsrc, JenaRsrc) = (stmt.getSubject, stmt.getResource)
	def pullGenPair_SO (stmt : JenaStmt) : (JenaRsrc, RDFNode) = (stmt.getSubject, stmt.getObject)

	// 	private def pullRsrcPair_SO (stmt : JenaStmt) : (JenaRsrc, JenaRsrc) = (stmt.getSubject, stmt.getResource)

	// 	val pullFunc = new Function[]

	// Presumes that all o-bjects (in our graph {s,mdlPrp,o}) are Rsrc (not Literals)
	def pullRsrcArcsAtProp (mdlPrp: JenaProp, flipDir : Boolean) : Map[JenaRsrc, Traversable[JenaRsrc]] = {
		val jenaMdl : JenaMdl = mdlPrp.getModel
		val stmtIt = jenaMdl.listStatements(null, mdlPrp, null)
		// val mutMp = new MutHashMap[JenaRsrc, JenaRsrc]
		val pairsJL : java.util.List[(JenaRsrc, JenaRsrc)] = stmtIt.mapWith(stmt => pullRsrcPair_SO(stmt)).toList
		val smp : Iterable[(JenaRsrc, JenaRsrc)] = pairsJL.asScala  // .toMap
		val bnchMap = new MutHashMap[JenaRsrc, List[JenaRsrc]]
		smp.foreach(pair => {
			val sbj : JenaRsrc = if (!flipDir) pair._1 else pair._2
			val obj : JenaRsrc = if (!flipDir) pair._2 else pair._1
			val oldList : List[JenaRsrc] = bnchMap.get(sbj).getOrElse(Nil)
			val revList = obj :: oldList
			bnchMap.put(sbj, revList)
		})
		bnchMap.toMap
	}
	private def stmtIterToScLst (jenaStmtIt : StmtIterator) : List[JenaStmt] = jenaStmtIt.asScala.toList
	private def stmtIterToScIter (jenaStmtIt : StmtIterator) : Iterator[JenaStmt] = jenaStmtIt.asScala

	private def scStmtIterToMultiMap[VT] (scStmtIter : Iterator[JenaStmt],
										  kvXtract : Function1[JenaStmt, (JenaRsrc,VT)]) : Map[JenaRsrc, List[VT]] = {
		val dummyMap = Map[JenaRsrc, List[VT]]()
		dummyMap
	}
	// What could we impl that way?
	private def allBindingsForSubj(subjRes : JenaRsrc) : Map[JenaRsrc, List[RDFNode]] = ??? //
	private def allUsesOfObj(objNode : RDFNode) : Map[JenaRsrc, List[JenaRsrc]] = ??? // Key = prop, val = list of subjs


}