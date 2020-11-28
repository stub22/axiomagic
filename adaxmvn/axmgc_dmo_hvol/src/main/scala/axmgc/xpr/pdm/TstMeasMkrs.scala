package axmgc.xpr.pdm

private trait TstMeasMkrs

trait MakeRats {
	def mk
}

class DecimWrap(bd : BigDecimal) extends ExactRealNum {
	private def isInt = bd.isValidInt
	private def asInt : Option[Int] = if(isInt) Some(bd.toIntExact) else None
	override def isZero: Boolean = asInt.map(_ == 0).getOrElse(false)
	override def isOne: Boolean = ???

	override def isNegOne: Boolean = ???

	override def isNegative: Boolean = ???

	override def isNonnegative: Boolean = ???

	override def isPositive: Boolean = ???

	override def isEq(otherNum: ExactRealNum): Boolean = ???

	override def isGrtrThn(otherNum: ExactRealNum): Boolean = ???

	override def isGrtrOrEq(otherNum: ExactRealNum): Boolean = ???

	override protected def isReadyInMem: Boolean = ???

	override protected def hasApproxNum: Boolean = ???

	override def getApproxNum: Option[ApproxRealNum] = ???
}

class EzPosProbNum(bd : BigDecimal) extends DecimWrap(bd) with ProbMeasNum {
	private def huh = {
	//	val x = bd.
	}
	override def hasKnownFiniteDigits: Boolean = ???

	override def getKnownDecimalDigitCount: Option[Int] = ???

	override def isZero: Boolean = ???

	override def isOne: Boolean = ???

	override def isNegOne: Boolean = ???

	override def isNegative: Boolean = ???

	override def isNonnegative: Boolean = ???

	override def isPositive: Boolean = ???

	override def isEq(otherNum: ExactRealNum): Boolean = ???

	override def isGrtrThn(otherNum: ExactRealNum): Boolean = ???

	override def isGrtrOrEq(otherNum: ExactRealNum): Boolean = ???

	override protected def isReadyInMem: Boolean = ???

	override protected def hasApproxNum: Boolean = ???

	override def getApproxNum: Option[ApproxRealNum] = ???
} // with

// class EzRatMeasNum() extends ProbMeasNum with RationalMeasNum {

trait EzMeasMkr {
	def mkTstProbMeas : Unit = {
		// val spc = new ProbSpc[] {}
	}
}
