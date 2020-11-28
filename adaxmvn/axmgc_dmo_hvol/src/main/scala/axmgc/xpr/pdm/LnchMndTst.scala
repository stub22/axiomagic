package axmgc.xpr.pdm

object LnchMndTst {
	def main(args: Array[String]): Unit = {
		println("OK then!")
		firstBigDecStuff
	}
	def firstBigDecStuff : Unit = {
		val bd_1 = BigDecimal.apply(1)
		val bd_4 = BigDecimal.apply(4)
		val bd_6 = BigDecimal.apply(6)

		val bd_oneSixth = bd_1 / bd_6

		val bd_oneFourth = bd_1 / bd_4

		println(s"1/4 = ${bd_oneFourth}")

		println(s"1/6 = ${bd_oneSixth}")
	}
}
trait MndStf {
	/*
In scala 2 we cannot (easily, generally) make the dimension part of the type.
In Idris and dependent typed languages, we have that power.

	Dirac distribution = deterministic event, with probability 1

	Uniform distribution = equi-probable over entire support

Event N-box  NBox

Sum N-Space = disjoint union of N-Box,

Product N-Space = Cartesian product of M-Space * K-Space (where M + K = N)
             = 1-Box * 1-Box ... N times


Interval[
Box isA Space


Orthotope(
Interval(Lower,Upper)
EventSpace(N-Dims,


In geometry, an orthotope[1] (also called a hyperrectangle or a box)
is the generalization of a rectangle for higher dimensions, formally
defined as the Cartesian product of intervals.

A three-dimensional orthotope is also called a right rectangular prism,
rectangular cuboid, or rectangular parallelepiped.
	 */
}


