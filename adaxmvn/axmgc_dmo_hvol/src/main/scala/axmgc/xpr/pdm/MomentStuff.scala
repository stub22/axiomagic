package axmgc.xpr.pdm

private trait MomentStuff

trait Dist

// Symmetric matrix of N^2 elements => N(N+1)/2 unique elements
// In general they are signed values

// covar(Xi,Xj) = E(Xi-E(Xi)) * E(Xj-E(xj))

// Diagonal entries
// var(Xi) = E(Xi-E(Xi)) ^ 2

// Matrix is posisitive semidefinite
/***
 * Any covariance matrix is symmetric and positive semi-definite and its main diagonal contains variances
 * (i.e., the covariance of each element with itself).
 *
 * In linear algebra, a symmetric N x N real matrix M is said to be positive-definite if the scalar
 * z-t M Z is strictly positive for every non-zero column vector z
 * M z always has a positive inner product with the input, as often observed in physical processes.
 * Put differently, that applying M to z (Mz) keeps the output in the direction of z.
 *
 * The matrix M is positive-definite if and only if the bilinear form <z,w> = z-t M w is positive-definite
 *
 * Looking to see if "Refined" or Dotty-Scala3
 */
trait VectorOfReals
trait SymmMatrixOfReals
trait SymmPosDefMatrixOfReals
trait SymmPosSemidefMatrixOfReals
// Parameterize by alpha + beta, relative to a benchmark asset
trait CovarMatrix extends SymmPosSemidefMatrixOfReals
trait CovarStuff {
	// Want dep-type enforcement of dimensions
	def distToSummaryStats(d : Dist) : (VectorOfReals, SymmPosSemidefMatrixOfReals)
	def mkTruncNormalDist(lowerVect : VectorOfReals, upperVect : VectorOfReals, meanVect : VectorOfReals, covarMtrx : CovarMatrix)
	def mkUniformDist(lowerVect : VectorOfReals, upperVect : VectorOfReals)
}

trait ExpectedValue[RVarT]
trait CertainValue[RVarT] extends ExpectedValue[RVarT]

