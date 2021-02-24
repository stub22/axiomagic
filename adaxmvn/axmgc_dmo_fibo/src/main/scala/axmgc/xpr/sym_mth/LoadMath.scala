package axmgc.xpr.sym_mth

import axmgc.dmo.fin.ontdmp.{MdlDmpFncs, OntQryMgr}
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.rdf.model.{Model => JenaMdl}
import org.matheclipse.core.eval.{EvalEngine, MathMLUtilities, TeXUtilities}
import org.matheclipse.core.interfaces.IExpr
import org.matheclipse.core.parser.ExprParser
import org.matheclipse.core.expression.{F, S}
import java.io.StringWriter

import org.matheclipse.parser.client.FEConfig
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
	private val ontQryMgr = new OntQryMgr
	def dumpStatsToLog(): Unit = {
		val qryOutJsnTxt =  ontQryMgr.dumpMdlStatsToJsnArrTxt( myMdl_MathFrmlas)
	}
	// TODO:  MathML as XML
	def makeLgrngMathML : String = {
		"lgrng in math-ml"
	}


	def getEvalEngine() : EvalEngine

	lazy val myExprParser : ExprParser = new ExprParser(getEvalEngine)

	def connectOrSomething: Unit = {
		// ExprEvaluator does these two steps
		val eeng = getEvalEngine()
		EvalEngine.set(eeng)
		// TODO:  FIXME - this "reset" method is missing as of 2021-01-14
//		eeng.reset()
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
	def evalExpr(expr : IExpr) : Option[IExpr] = {
		val eeng = getEvalEngine()
		val rsltOrNull : IExpr = eeng.evaluate(expr);
		if (rsltOrNull != null) {
			if (!rsltOrNull.equals(S.Null)) {
				Some(rsltOrNull)
			} else {
				getS4JLog.info("Eval of {} produced result of type {}, which equals F.Null", expr.toString, rsltOrNull.getClass : Any)
				None
			}
		} else {
			getS4JLog.warn("Eval of {} produced java null", expr)
			None
		}
	}
	def parseEvalDump(inExprTxt : String) : Option[IExpr] = {
		val parsed_opt : Option[IExpr] = parseExpr(inExprTxt)
		val evaled_opt = parsed_opt.flatMap(evalExpr(_))
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
		parseEvalDump("Sin[7.0]") // Does not eval if strict and no config
		parseEvalDump("sin[someVar]")
		parseEvalDump("D[sin[someVar],someVar]")
		parseEvalDump("D[Cos[someVar],someVar]")
	}
}

trait ChkChkMth extends ChkFormulas {
	def flag_useRelaxedSyntax : Boolean = false
	def flag_lowerCaseSymbols : Boolean = false

	private lazy val myEvEng : EvalEngine = {
		FEConfig.PARSER_USE_LOWERCASE_SYMBOLS = flag_lowerCaseSymbols;  // This has a big effect on how exprs get interpreted.
		val flag_relaxedSyn = flag_useRelaxedSyntax //  false => not relaxed == capitalization matters == more like Mathematica
		new EvalEngine(flag_relaxedSyn)
	}
	override def getEvalEngine() = myEvEng

	private lazy val myTeXUtil : TeXUtilities = {
		val eeng = getEvalEngine()
		val flag_relaxedSyn = flag_useRelaxedSyntax
		new TeXUtilities(eeng, flag_relaxedSyn);
	}
	private lazy val myMathMLUtil : MathMLUtilities = {
		val eeng = getEvalEngine()
		val flag_relaxedSyn = flag_useRelaxedSyntax
		val flag_mathMTagPrefix = false
		val flag_mathMLHeader = false
		new MathMLUtilities(eeng, flag_mathMTagPrefix, flag_mathMLHeader);
	}
	def exportMathML(expr : IExpr) : String = {
		val stw = new StringWriter
		myMathMLUtil.toMathML(expr, stw)
		stw.toString
	}
	def exportTeX(expr : IExpr) : String = {
		val stw = new StringWriter
		myTeXUtil.toTeX(expr, stw)
		stw.toString
	}

	def parseEvalExport(inExprTxt : String) {
		val parsed_opt : Option[IExpr] = parseExpr(inExprTxt)
		if (parsed_opt.isDefined) {
			val evaled_opt: Option[IExpr] = parsed_opt.flatMap(evalExpr(_))
			if (evaled_opt.isDefined) {
				val evalOutExpr = evaled_opt.get
				if (evalOutExpr == null) {
					getS4JLog.error("Huh?  EvalOutExpr is null for parsed_opt: {}", parsed_opt)
				} else {
					getS4JLog.info("EvalOutExpr.class = {}", evalOutExpr.getClass())
				}
				val evalMathML = exportMathML(evalOutExpr)
				val evalTeX = exportTeX(evalOutExpr)
				getS4JLog.info("Input={}, Parsed={}, Eval={}, Eval.MathML={}, Eval.TeX={}", inExprTxt, parsed_opt.get, evalOutExpr, evalMathML, evalTeX)
			} else {
				val prsdExpr = parsed_opt.get
				val prsdMathML = exportMathML(prsdExpr)
				val prsdTeX = exportTeX(prsdExpr)
				getS4JLog.info("Input={}, Parsed={}, NO EVAL, Parsed.MathML={}, Parsed.TeX={}", inExprTxt, prsdExpr, prsdMathML, prsdTeX)
			}
		} else {
			getS4JLog.warn("Parsing failed for input: {}", inExprTxt)
		}
	}
	def testExports : Unit = {
		parseEvalExport("Cos[1/4 * Pi]")
		parseEvalExport("ArcTan[1.0]*4.0")
		parseEvalExport("fov[v1_] := v1 ^ 3 - 1.08")
		parseEvalExport("ftv[v1_, v2_] := v1 * v2 + 0.2")
		parseEvalExport("fmv[v1_] := fov[v1] + ftv[v1, v1 / 2.0]")
		parseEvalExport("fmv[10.0]")
	}
	/*
	Plotly 3D surface plot is part of their   "gl3d" bundle,
	which relies on   gl-vis/gl-surface3d   which relies on  stack.gl

	Appears that SVG is used in the 2D layout to set the viewport rectangle,
	and then gl3d code is rendering onto that rectangle, bound via css-class
	"gl-container"

	 */
	def testSvg : Unit = {

	}
}
/* Leaner, relaxed:
axpndr-stdout 7633    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Cos(3.14*0.5)+0.8, Parsed=Some(Cos(0.5*3.14)+0.8), Eval=Some(0.8007963267107333)
axpndr-stdout 7633    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Cos(2.0), Parsed=Some(Cos(2.0)), Eval=Some(-0.4161468365471424)
axpndr-stdout 7633    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=cos(3.0), Parsed=Some(Cos(3.0)), Eval=Some(-0.9899924966004454)
axpndr-stdout 7633    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Cos[4.0], Parsed=Some(Cos(4.0)), Eval=Some(-0.6536436208636119)
axpndr-stdout 7633    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=cos[5.0], Parsed=Some(Cos(5.0)), Eval=Some(0.28366218546322625)
axpndr-stdout 7633    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=sin[6.0], Parsed=Some(Sin(6.0)), Eval=Some(-0.27941549819892586)
axpndr-stdout 7633    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Sin[7.0], Parsed=Some(Sin(7.0)), Eval=Some(0.6569865987187891)
axpndr-stdout 7633    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=sin[someVar], Parsed=Some(Sin(somevar)), Eval=Some(Sin(somevar))
axpndr-stdout 7648    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=D[sin[someVar],someVar], Parsed=Some(D(Sin(somevar),somevar)), Eval=Some(Cos(somevar))
Leaner, strict, PARSER_USE_LOWERCASE_SYMBOLS defaults to true:
axpndr-stdout 7563    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Cos(3.14*0.5)+0.8, Parsed=Some(Cos*3.14*0.5+0.8), Eval=Some(0.8+1.57*Cos)
axpndr-stdout 7563    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Cos(2.0), Parsed=Some(2.0*Cos), Eval=Some(2.0*Cos)
axpndr-stdout 7563    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=cos(3.0), Parsed=Some(3.0*Cos), Eval=Some(3.0*Cos)
axpndr-stdout 7563    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Cos[4.0], Parsed=Some(Cos[4.0]), Eval=Some(Cos[4.0])
axpndr-stdout 7563    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=cos[5.0], Parsed=Some(Cos[5.0]), Eval=Some(0.28366218546322625)
axpndr-stdout 7563    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=sin[6.0], Parsed=Some(Sin[6.0]), Eval=Some(-0.27941549819892586)
axpndr-stdout 7579    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Sin[7.0], Parsed=Some(Sin[7.0]), Eval=Some(Sin[7.0])
axpndr-stdout 7579    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=sin[someVar], Parsed=Some(Sin[someVar]), Eval=Some(Sin[someVar])
axpndr-stdout 7579    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=D[sin[someVar],someVar], Parsed=Some(D[Sin[someVar],someVar]), Eval=Some(Cos[someVar])
axpndr-stdout 7594    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=D[Cos[someVar],someVar], Parsed=Some(D[Cos[someVar],someVar]), Eval=Some(Cos'[someVar])
Leaner, strict, PARSER_USE_LOWERCASE_SYMBOLS set to false
axpndr-stdout 2893    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Cos(3.14*0.5)+0.8, Parsed=Some(Cos*3.14*0.5+0.8), Eval=Some(0.8+1.57*Cos)
axpndr-stdout 2893    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Cos(2.0), Parsed=Some(2.0*Cos), Eval=Some(2.0*Cos)
axpndr-stdout 2893    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=cos(3.0), Parsed=Some(3.0*cos), Eval=Some(3.0*cos)
axpndr-stdout 2893    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Cos[4.0], Parsed=Some(Cos[4.0]), Eval=Some(-0.6536436208636119)
axpndr-stdout 2893    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=cos[5.0], Parsed=Some(cos[5.0]), Eval=Some(cos[5.0])
axpndr-stdout 2893    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=sin[6.0], Parsed=Some(sin[6.0]), Eval=Some(sin[6.0])
axpndr-stdout 2893    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=Sin[7.0], Parsed=Some(Sin[7.0]), Eval=Some(0.6569865987187891)
axpndr-stdout 2893    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=sin[someVar], Parsed=Some(sin[someVar]), Eval=Some(sin[someVar])
axpndr-stdout 2909    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=D[sin[someVar],someVar], Parsed=Some(D[sin[someVar],someVar]), Eval=Some(sin'[someVar])
axpndr-stdout 2924    INFO [main] axmgc.xpr.sym_mth.TstSymCalculus$$anon$2 (LoadMath.scala:71) parseEvalDump - Input=D[Cos[someVar],someVar], Parsed=Some(D[Cos[someVar],someVar]), Eval=Some(-Sin[someVar])

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

From Mathematica docs:
"Integrals of rational functions are straightforward to evaluate, and always come out in terms of rational functions,
logarithms and inverse trigonometric functions."
 */