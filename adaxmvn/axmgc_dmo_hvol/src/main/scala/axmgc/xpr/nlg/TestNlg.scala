package axmgc.xpr.nlg

import org.nlogo.app.{App => NlgFullApp}
import org.nlogo.lite.{AppletPanel, InterfaceComponent => NlgIntfc}
import org.nlogo.headless.{HeadlessWorkspace => NlgHdlsWrk}
import org.nlogo.workspace.{Controllable => NlgCntrlbl}

/*
 *  FIXME: NetLogo is distributed under GPL license, so this experiment should be treated as an optional
 *  demo-plugin for the axiomagic.dmo.hvol module.
 *  https://ccl.northwestern.edu/netlogo/docs/copyright.html
 */
object TestNlg {
	def main(args: Array[String]) {
		val workingDir = System.getProperty("user.dir");
		println("user.dir=", workingDir)
		val boss = new NlgBoss {}
		// boss.runNetlogoFullGui(args) // ClassNotFoundException: org.jhotdraw.framework.DrawingEditor
		boss.runNetlogoMinGui() // Works and returns immediately.  But no exit-signals set up yet.
		// boss.runNetlogoHeadless()
		println("End of main at: " + java.util.Calendar.getInstance().getTime)
	}
}
trait NlgBoss {
	val nlogoFldr = "nlogo_models_gpl/" // "models/"
	val fireTstMdlPth = nlogoFldr + "/Sample Models/Earth Science/Fire.nlogo"

	val rprtrBurned = "burned-trees"
	val nope : org.nlogo.core.Model = null

	// Starting with example code from here.
	// https://github.com/NetLogo/NetLogo/wiki/Controlling-API
	def runNetlogoHeadless(): Unit = {
		val workspace = NlgHdlsWrk.newInstance
		workspace.open(fireTstMdlPth)
		earthFireCmds(workspace)
		val cntBrntTrees : Object = workspace.report(rprtrBurned)
		println("[report burned-trees] returns: ", cntBrntTrees, " java-class=" + cntBrntTrees.getClass)

		workspace.dispose()
	}
	def runNetlogoFullGui(args : Array[String]) {
		// Fails due to lib conflict
		NlgFullApp.main(args)
		val ourApp = NlgFullApp.app
		awtInvokeAndWait {
			ourApp.open(fireTstMdlPth)
		}
		earthFireCmds(ourApp)
		println(ourApp.report(rprtrBurned))
	}

	def runNetlogoMinGui() {
		val frame = new javax.swing.JFrame
		val comp = new  NlgIntfc(frame) // Extends AppletPanel which implements .command(), but not NlgCntrlbl
		val mdlPth = fireTstMdlPth
		
		awtInvokeAndWait {
			frame.setSize(1000, 700)
			frame.add(comp)
			frame.setVisible(true)
			comp.open(fireTstMdlPth)
		}
		println("Returned from awtInvokeAndWait")
		// earthFireCmds(comp)   	// Problem:  NlgIntfc does not implement NlgCntrlbl
		val cmdTgt : AppletPanel = comp  // AppletPanel is not NlgCntrlbl either, but it has .command()
		appPanel_earthFireCmds(cmdTgt)
		println(comp.report(rprtrBurned))
	}
	// Problem:  NlgIntfc does not implement NlgCntrlbl
	def earthFireCmds(cmdTgt: NlgCntrlbl): Unit = {
		cmdTgt.command("set density 62")
		cmdTgt.command("random-seed 0")
		cmdTgt.command("setup")
		cmdTgt.command("repeat 50 [ go ]")
	}
	def appPanel_earthFireCmds(cmdTgt : AppletPanel) : Unit = {
		cmdTgt.command("set density 62")
		cmdTgt.command("random-seed 0")
		cmdTgt.command("setup")
		cmdTgt.command("repeat 50 [ go ]")
	}
	def awtInvokeAndWait(block: => Unit) {
		val rnbl = new Runnable() { def run() { block } }
		java.awt.EventQueue.invokeAndWait(rnbl)
	}
}
/*
Exception in thread "main" java.lang.NoClassDefFoundError: org/jhotdraw/framework/DrawingEditor


	at org.picocontainer.DefaultPicoContainer.addComponent(DefaultPicoContainer.java:518)
	at org.nlogo.util.Pico.add(Pico.scala:14)
	at org.nlogo.app.App$.mainWithAppHandler(App.scala:119)
	at org.nlogo.app.App$.main(App.scala:68)
	at axmgc.xpr.nlg.NlgBoss.runNetlogoFullGui(TestNlg.scala:27)
	at axmgc.xpr.nlg.NlgBoss.runNetlogoFullGui$(TestNlg.scala:26)
	at axmgc.xpr.nlg.TestNlg$$anon$1.runNetlogoFullGui(TestNlg.scala:11)
	at axmgc.xpr.nlg.TestNlg$.main(TestNlg.scala:12)
	at axmgc.xpr.nlg.TestNlg.main(TestNlg.scala)
 */