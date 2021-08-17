package axmgc.xpr.vis_js

private trait NavJsonMkrStuff

import akka.http.scaladsl.model.HttpEntity.{Strict => HEStrict}
import axmgc.web.ent.HtEntMkr
trait NavJsonEntApi {
	def mkBroadAnswerEntity(paramMap : Map[String, String]) : HEStrict
	def mkNarrowAnswerEntity(navQID : String, paramMap : Map[String, String]) : HEStrict
}
trait NavJsonTxtApi {
	def mkBroadJsonTxt(paramMap : Map[String, String]) : String
	def mkNarrowJsonTxt(navQID : String, paramMap : Map[String, String]) : String

}
trait NavJsonEntImpl extends NavJsonEntApi {
	protected def getNavJsonTxtMkr : NavJsonTxtApi
	protected def getHtEntMkr : HtEntMkr
	override def mkBroadAnswerEntity(paramMap : Map[String, String]) : HEStrict = {
		val broadJsonTxt = getNavJsonTxtMkr.mkBroadJsonTxt(paramMap)
		getHtEntMkr.makeJsonEntity(broadJsonTxt)
	}
	override def mkNarrowAnswerEntity(navQID : String, paramMap : Map[String, String]) : HEStrict = {
		val nrrowJsonTxt = getNavJsonTxtMkr.mkNarrowJsonTxt(navQID, paramMap)
		getHtEntMkr.makeJsonEntity(nrrowJsonTxt)
	}
}
class FakeNavJsonTxtImpl extends NavJsonTxtApi {
	private val myNimTsts = new NavItemMakerTests{}

	override def mkBroadJsonTxt(paramMap: Map[String, String]): String = {
		myNimTsts.testNavTreeDataGen(paramMap)
	}
	override def mkNarrowJsonTxt(navQID: String, paramMap: Map[String, String]): String = {
		myNimTsts.testFocusedTreeDataGen(navQID, paramMap)
	}

}

class RobustNavJsonTxtImpl extends NavJsonTxtApi {
	private val myFallbackImpl = new FakeNavJsonTxtImpl
	override def mkBroadJsonTxt(paramMap: Map[String, String]): String = {

		myFallbackImpl.mkBroadJsonTxt(paramMap)
	}

	override def mkNarrowJsonTxt(navQID: String, paramMap: Map[String, String]): String = {
		myFallbackImpl.mkNarrowJsonTxt(navQID, paramMap)
	}
}