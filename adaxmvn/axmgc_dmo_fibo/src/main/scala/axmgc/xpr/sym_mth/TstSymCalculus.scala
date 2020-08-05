package axmgc.xpr.sym_mth


import axmgc.web.lnch.FallbackLog4J
import org.matheclipse.parser.client.FEConfig
import org.slf4j.{Logger, LoggerFactory}

trait TstSymFncs {
	def mkSymjLst = 0

}
object TstSymCalculus {
	lazy val myFLog4J = new FallbackLog4J {}
	val myFallbackLog4JLevel = org.apache.log4j.Level.INFO
	val flg_setupFallbackLog4J = false // Set to false if log4j.properties is expected, e.g. from Jena.
	val flg_tstRdfEmbed = false
	val flg_runConsole = true
	val flg_preferMmaMode = true
	lazy val myS4JLogger : Logger = LoggerFactory.getLogger(classOf[TstSymFncs])

	def main(args: Array[String]) {
		if (flg_setupFallbackLog4J) {
			myFLog4J.setupFallbackLogging(myFallbackLog4JLevel)
		}
		myS4JLogger.warn("Making ChkChkMth")
		val mthChkr = new ChkChkMth {}
		myS4JLogger.warn("Testing ChkFormulas, relaxed_flag={}, lowerCaseSyms_flag={}", mthChkr.flag_useRelaxedSyntax, mthChkr.flag_lowerCaseSymbols)
		mthChkr.testTrigFuncs
		mthChkr.testExports
		if (flg_tstRdfEmbed) {
			mthChkr.dumpStatsToLog
		}
		if (flg_runConsole) {
			if (flg_preferMmaMode) {
				runMmaConsole(args)
			} else {
				FEConfig.PARSER_USE_LOWERCASE_SYMBOLS = true;
				runRelaxedConsole(args)
			}
		}
		myS4JLogger.warn("END of .main()")
		println("println: .main() says goodbye") // , but actors are possibly still running

	}
	def runRelaxedConsole(args: Array[String]) : Unit = {
		myS4JLogger.warn("Invoking org.matheclipse.core.eval.Console")
		org.matheclipse.core.eval.Console.main(args)
	}
	def runMmaConsole(args: Array[String]) : Unit = {
		myS4JLogger.warn("Invoking org.matheclipse.core.eval.MMAConsole")
		org.matheclipse.core.eval.MMAConsole.main(args)
	}
}
