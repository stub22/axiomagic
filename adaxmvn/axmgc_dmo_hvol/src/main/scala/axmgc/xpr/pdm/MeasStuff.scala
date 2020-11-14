package axmgc.xpr.pdm

private trait MeasStuff

// Measurable component of a measurable space = subset of the underlying set
// Commonly this is an element of the Borel space
trait MeasComp {
	// Example:  A 3-cube has 6 at dimRedCnt 1
	// An n-cube has 2 * N
	def countBoundaryComps(dimRedCnt : Int)
	def findBoundaryComps(dimRedCnt : Int) : MeasComp
}

// We would like the dimension to be part of the type.
// In Scala-2 we don't have dependent types
trait ConvexEuclideanRegion extends MeasComp {
	def getDimension : PosIntNum
}

trait VectorPoint extends ConvexEuclideanRegion {
	def getCoordNum (idx: Int) : ExactRealNum
}

trait Interval extends ConvexEuclideanRegion {
	def getLowerNum : ExactRealNum
	def getUpperNum : ExactRealNum
	def getLength : ExactRealNum
}

trait Box extends ConvexEuclideanRegion {
	def getOrigin : VectorPoint
	def getLength(idx : Int) : NonnegRealNum
	def getInterval(idx : Int) : Interval

}

trait UnitBox extends Box



// Measure space over some measurable space, where measure-values are finite
trait FinvalMeasSpc[-MC <: MeasComp, +FMN <: FiniteMeasNum] extends MeasComp {
	// mcomp is usually a point, a euclidean region, or a sum of such.
	def measure(mcomp : MC) : FMN
	def fullMeasure : FMN
	def emptyMeasure : FMN

}
trait ProbSpc[-MC <: MeasComp, +PMN <: ProbMeasNum] extends FinvalMeasSpc[MC, PMN] {

}
// This Rec records an immutable measure value num
// A set of these recs for many cmps gives a verbose form of a distribution (may viz as histogram nums over N-dim cmps)
class ProbMeasureRec[-MC <: MeasComp, +PMN <: ProbMeasNum, -PS <: ProbSpc[MC, PMN]](spc : PS, cmp : MC, num : PMN) {
}
// A bucketed distribution is equivalent to a histogram, which is a plottable structure that may include record-detail
// plottable as shape, color.
trait ObsFunc {
	// Observe by finding the co-vector space at which some real-valued F is an extremum, which is generally either
	// A) On a boundary of the space.
	// B) At a critical point where the gradient of the functional F derivative is zero (gradient is diag of jacobian).
	// def observeFunctional(F)
}

/***
A distribution is a probability measure on some underlying set, which we usually take to be a compact convex set of
Rn vectors of dimension N.  This set defines the measurable space.
The dist is a real valued function over the space, visualizable as an N-dimensional histogram.
A distribution may be described by equations and finite approximations, which may be determined from data and/or edited by users.
We use a distribution as an interactive vehicle for plotting and editing.
 */
