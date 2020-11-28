package axmgc.xpr.pdm

private trait NumStuff

trait RealNum

trait ApproxRealNum extends RealNum {
	def getApproxFloat : Float
	def getApproxDouble : Double
	def getApproxDecimal(maxDecimalDigits : Int) : BigDecimal
	protected def hasExactNum : Boolean
	protected def getExactNum : Option[ExactRealNum]
}

trait ExactRealNum extends RealNum {
	// These accessors must provide exact answers using computable algos.
	// Will add more layers of strictness to enable future delicate virtuals
	def isZero : Boolean
	def isOne : Boolean
	def isNegOne : Boolean
	def isNegative : Boolean
	def isNonnegative : Boolean
	def isPositive : Boolean

	def isEq(otherNum : ExactRealNum) : Boolean

	def isGrtrThn(otherNum : ExactRealNum) : Boolean

	def isGrtrOrEq(otherNum : ExactRealNum) : Boolean

	// Value is available in memory - not somehow virtualized.
	// Another number may check this during computations or comparisons.
	protected def isReadyInMem : Boolean

	protected def hasApproxNum : Boolean
	def getApproxNum : Option[ApproxRealNum]
}

trait CompExactReal[CER <: CompExactReal[CER]] {

}


trait NonnegRealNum extends ExactRealNum

trait PosRealNum extends NonnegRealNum

trait RationalNum[OPRN <: RationalNum[OPRN]] extends ExactRealNum {
	def plus(otherRN : OPRN) : OPRN
	def mult(otherRN : OPRN) : OPRN
	def negate :  OPRN
	def reciprocal : OPRN
}

trait IntegerNum[IN <: IntegerNum[IN]] extends RationalNum[IN]

trait NonnegRatNum[NnRN <: NonnegRatNum[NnRN]] extends RationalNum[NnRN] with NonnegRealNum

trait PosRatNum extends NonnegRatNum[PosRatNum] with PosRealNum

trait NonnegIntNum extends NonnegRatNum[NonnegIntNum] with IntegerNum[NonnegIntNum]

trait PosIntNum extends NonnegIntNum

trait NumSource {
	def 	getZero	: NonnegIntNum
	def 	getOne : PosIntNum
	def 	getNegOne : IntegerNum[_]
	def 	getPosIntNum (posInt : Int) : PosIntNum
	def 	getPosRatNum (posNumer : PosIntNum, posDenom : PosIntNum) : PosRatNum
}

// Finite measure value - a real number >= 0
trait FiniteMeasNum extends NonnegRealNum {

	// In some cases we might not know if a number is rational.
	def isKnownNNRational : Boolean = false
	def getKnownNNRational : Option[NonnegRatNum[_]] = None

	def hasKnownFiniteDigits : Boolean
	def hasKnownInfiniteDigits : Boolean = false
	def getKnownDecimalDigitCount : Option[Int]
}

trait ProbMeasNum extends FiniteMeasNum   // 0 <= num <= 1

/*
trait RationalMeasNum extends FiniteMeasNum {
	def getNNRatNum : NonnegRatNum[_]
	override def isKnownNNRational : Boolean = true
	override def getKnownNNRational : Option[NonnegRatNum[_]] = Some(getNNRatNum)
}
*/