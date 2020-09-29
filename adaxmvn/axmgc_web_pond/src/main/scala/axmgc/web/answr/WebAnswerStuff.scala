package axmgc.web.answr

// An answer can be empty, or a part can be missing, but a part (or part-key)
// cannot be empty.

private trait WebAnswerStuff

protected trait TWAPartKey

protected sealed abstract class WAPartKey extends TWAPartKey {}
case class WAP_Xhtml(domId_opt : Option[String]) extends WAPartKey
case class WAP_Css(cssFileNm_opt : Option[String]) extends WAPartKey
case class WAP_Trtl(graphUriTxt : String) extends WAPartKey
case class WAP_Json(jsonFileNm : String) extends WAPartKey

// sealed abstract class WAContentMsg

trait TWebAnsPart {
	type PartValTyp
	def getPartKey : WAPartKey
	def getPartVal : PartValTyp
}
trait TWebAnswer {
	def getKnownPartKeys : Traversable[WAPartKey]
	def getPart_opt (key : WAPartKey) : Option[TWebAnsPart]
}
sealed abstract class WebAnswer extends TWebAnswer {
	// Consider which timestamps should be marked as @transient
	val myEvtInstMsec : Long = System.currentTimeMillis()
}
case class WA_Empty() extends WebAnswer {
	override def getKnownPartKeys: Traversable[WAPartKey] = Nil
	override def getPart_opt(key: WAPartKey) = None
}
abstract case class WA_Summary(summaryTxt : String) extends WebAnswer
abstract case class WA_Full () extends WebAnswer

