package axmgc.xpr.sym_mth

import axmgc.dmo.fin.ontdmp.MdlDmpFncs
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.rdf.model.{Model => JenaMdl}
import org.matheclipse.core.eval.EvalEngine
import org.slf4j.{Logger, LoggerFactory}

trait LoadMath

trait ChkFormulas extends MdlDmpFncs {
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)
	override protected def getS4JLog: Logger = myS4JLog
	private val pth_mthFrmlaTst = "gdat/math_tst/gravgm/grvgmdat_A.ttl"

	lazy val myMdl_MathFrmlas = loadMthFrmlas
	private def loadMthFrmlas : JenaMdl = {
		val pth = pth_mthFrmlaTst
		myS4JLog.info("Starting load of math formulas from rsrc: {}", pth)
		val mdl = RDFDataMgr.loadModel(pth)
		myS4JLog.info("Finished math formula load, model size: {}", mdl.size())
		mdl
	}

	def dumpStatsToLog(): Unit = {
		dumpSomeModelStatsToLog(myMdl_MathFrmlas)
	}
	// TODO:  MathML as XML
	def makeLgrngMathML : String = {
		"lgrng in math-ml"
	}

	import org.matheclipse.core.interfaces.IExpr
	import org.matheclipse.core.parser.ExprParser
	import java.io.Writer

	def getEvalEngine() : EvalEngine

	lazy val myExprParser : ExprParser = new ExprParser(getEvalEngine)

	def connectOrSomething: Unit = {
		// ExprEvaluator does these two steps
		val eeng = getEvalEngine()
		EvalEngine.set(eeng)
		eeng.reset()
	}
	def parseExpr(inputExpression: String): Option[IExpr] = {
		if (inputExpression != null) {
			try {
				// 	public IExpr parse(final String expression) throws SyntaxError {

				val pexpr = myExprParser.parse(inputExpression)
				Some(pexpr)
				// node = fEvalEngine.parseNode(inputExpression);
				// parsedExpression = AST2Expr.CONST.convert(node, fEvalEngine);
			} catch {
				case thr : Throwable => {
					getS4JLog.error("Problem parsing [" + inputExpression + "]", thr)
					None
				}
			}
		} else None
	}
	def evalExpr(expr : IExpr) : IExpr = {
		val eeng = getEvalEngine()
		eeng.evaluate(expr);
	}
	def parseEvalDump(inExprTxt : String) : Option[IExpr] = {
		val parsed_opt : Option[IExpr] = parseExpr(inExprTxt)
		val evaled_opt = parsed_opt.map(evalExpr(_))
		getS4JLog.info("Input={}, Parsed={}, Eval={}", inExprTxt, parsed_opt, evaled_opt)
		evaled_opt
	}
	def testTrigFuncs : Unit = {
		connectOrSomething
		getS4JLog.info("Testing trig func syntax: cosines and sines")
		parseEvalDump("Cos(3.14*0.5)+0.8")
		parseEvalDump("Cos(2.0)")
		parseEvalDump("cos(3.0)")
		parseEvalDump("Cos[4.0]")
		parseEvalDump("cos[5.0]")
		parseEvalDump("sin[6.0]")
		parseEvalDump("Sin[7.0]")
	}
	def testSomeExprs : Unit = {
		connectOrSomething
		getS4JLog.info("Testing exprs")
		val sinAboutHalfPi = "Sin(3.14*0.5)+0.8"
		val exOpt_01 = parseExpr(sinAboutHalfPi)
		getS4JLog.info("Parsed expr:/ {}", exOpt_01)
		val rslt_01 = evalExpr(exOpt_01.get)
		getS4JLog.info("Input={}, Parsed={}, Eval={}", sinAboutHalfPi, exOpt_01, rslt_01)

		val sinTwo = "Sin(2.0)"
		val exOpt_02 = parseExpr(sinTwo)
		getS4JLog.info("Parsed expr: {}", exOpt_02)

		val sinThree = "sin(3.0)"
		val exOpt_03 = parseExpr(sinThree)
		getS4JLog.info("Parsed expr: {}", exOpt_03)

		val sinFour = "Sin[4.0]"
		val exOpt_04 = parseExpr(sinFour)
		getS4JLog.info("Parsed expr: {}", exOpt_04)

		val sinFive = "sin[5.0]"
		val exOpt_05 = parseExpr(sinFive)
		getS4JLog.info("Parsed expr: {}", exOpt_05)
/*
Strict:
axpndr-stdout 7828    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:70) testSomeExprs - Parsed expr:/ Some(Sin*3.14*0.5+0.8)
axpndr-stdout 7828    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:74) testSomeExprs - Parsed expr: Some(Sin*2.0)
axpndr-stdout 7828    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:78) testSomeExprs - Parsed expr: Some(sin*3.0)
axpndr-stdout 7828    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:82) testSomeExprs - Parsed expr: Some(Sin[4.0])/
Relaxed:
axpndr-stdout 8289    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:70) testSomeExprs - Parsed expr:/ Some(plus(sin(times(3.14,0.5)),0.8))
axpndr-stdout 8289    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:74) testSomeExprs - Parsed expr: Some(sin(2.0))
axpndr-stdout 8290    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:78) testSomeExprs - Parsed expr: Some(sin(3.0))
axpndr-stdout 8290    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:82) testSomeExprs - Parsed expr: Some(sin(4.0))
axpndr-stdout 8290    WARN [main] axmgc.xpr.sym_mth.TstSymFncs (TstSymCalculus.scala:82) main - Invoking org.matheclipse.core.eval.Console

Strict:
ndr-stdout 7620    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:71) parseEvalDump - Input=Cos(3.14*0.5)+0.8, Parsed=Some(Cos*3.14*0.5+0.8), Eval=Some(Cos*3.14*0.5+0.8)
axpndr-stdout 7620    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:71) parseEvalDump - Input=Cos(2.0), Parsed=Some(Cos*2.0), Eval=Some(Cos*2.0)
axpndr-stdout 7620    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:71) parseEvalDump - Input=cos(3.0), Parsed=Some(cos*3.0), Eval=Some(cos*3.0)
axpndr-stdout 7620    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:71) parseEvalDump - Input=Cos[4.0], Parsed=Some(Cos[4.0]), Eval=Some(Cos[4.0])
axpndr-stdout 7636    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:71) parseEvalDump - Input=cos[5.0], Parsed=Some(cos[5.0]), Eval=Some(0.28366218546322625)
axpndr-stdout 7636    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:71) parseEvalDump - Input=sin[6.0], Parsed=Some(sin[6.0]), Eval=Some(-0.27941549819892586)
axpndr-stdout 7636    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$3 (LoadMath.scala:71) parseEvalDump - Input=Sin[7.0], Parsed=Some(Sin[7.0]), Eval=Some(Sin[7.0])
axpndr-stdout 7636    WARN [main] axmgc.xpr.sym_mth.TstSymFncs (TstSymCalculus.scala:86) main - END of .main()

Relaxed:

 */
	}
}

trait ChkChkMth extends ChkFormulas {
	private lazy val myEvEng : EvalEngine = {
		import org.matheclipse.core.basic.Config;
		Config.PARSER_USE_LOWERCASE_SYMBOLS = false;
		val flag_relaxedSyn = flag_useRelaxedSyntax //  false => not relaxed == capitalization matters == more like Mathematica
		new EvalEngine(flag_relaxedSyn)
	}
	override def getEvalEngine() = myEvEng

	def flag_useRelaxedSyntax : Boolean = true
}
/*
Symja has export pathways to MathML and to TeX.
// 	synchronized public void toMathML(final IExpr objectExpression, final Writer out) {

			MathMLUtilities mathUtil = new MathMLUtilities(engine, false, false);

			StringWriter stw = new StringWriter();
			mathUtil.toMathML("Sum[i, {i,1,n}]", stw);

public class TeXExample {
	public static void main(String[] args) {
		try {
			// false -> distinguish between upper- and lowercase identifiers:
			Config.PARSER_USE_LOWERCASE_SYMBOLS = false;
			// false -> switch to Mathematica syntax mode:
			EvalEngine engine = new EvalEngine(false);
			//
			TeXUtilities texUtil = new TeXUtilities(engine, false);

			StringWriter stw = new StringWriter();
			texUtil.toTeX("Sum[i, {i,1,n}]", stw);
			// print: \sum_{i = 1}^{n}i
			System.out.println(stw.toString());

			stw = new StringWriter();
			texUtil.toTeX("MatrixForm[{{a,b},{c,d}}]", stw);
			// print:
			// \begin{pmatrix} a & b \\
			// c & d \\
			// \end{pmatrix}
			System.out.println(stw.toString());

		} catch (SyntaxError e) {
			// catch Symja parser errors here
			System.out.println(e.getMessage());
		} catch (MathException me) {
			// catch Symja math errors here
			System.out.println(me.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

 */