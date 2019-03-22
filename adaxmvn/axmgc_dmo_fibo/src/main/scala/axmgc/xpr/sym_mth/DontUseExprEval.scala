package axmgc.xpr.sym_mth

import org.matheclipse.core.eval.ExprEvaluator
import org.matheclipse.core.expression.F._

trait DontUseExprEval {
	def doStuff : Unit = {

		// ExprEvaluator uses relaxedSyntax == true, unless we plug in our own EvalEngine

		val util = new ExprEvaluator(false, 100);

		// solve 2 equations: Solve({x^2+11==y, x+y==-9}, {x,y})
		// val result = util.evaluate(F.Solve(F.List(F.Equal(F.Sqr(x) + ZZ(11L), y), Equal(x + y, CN9)), List(x,y)));
		// println(result);


		// solve D(sin(x)*cos(x),x)
		val rs2  = util.evaluate(D(Sin(x) * Cos(x), x));
		println(rs2);


		// Integrate(sin(x)^5,x)
		val rs3 = util.evaluate(Integrate(Power(Sin(x), 5), x));
		println(rs3);


		// NIntegrte(Cos)x,{x, 0, Pi})
		// val rs4  = util.evaluate(NIntegrate(Cos(x), F.List(x, C0, Pi)));
		// println(rs4);
	}
	def doCalculus { // } main(args: Array[String]) {
		//    Config.PARSER_USE_LOWERCASE_SYMBOLS = true;
		try {
			val util = new ExprEvaluator(false, 100);
			var scalaForm = util.toScalaForm("D(sin(x)*cos(x),x)");
			println(scalaForm);
			var result = util.evaluate(D(Sin(x) * Cos(x), x));
			println(result);

			scalaForm = util.toScalaForm("Integrate(sin(x)^5,x)");
			println(scalaForm);
			result = util.evaluate(Integrate(Power(Sin(x), 5), x));
			println(result);

			// result = util.evaluate(NIntegrate(Cos(x), List(x, C0, Pi)));
			// println(result);
		} catch {
			case e: Exception => println("exception caught: " + e);
		}
	}
}
