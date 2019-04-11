package axmgc.xpr.nlg
import java.awt.EventQueue

import org.nlogo.app.App
import org.nlogo.headless.HeadlessWorkspace


object TestNlg {
	def main(args: Array[String]) {
		val workingDir = System.getProperty("user.dir");
		println("user.dir=", workingDir)
		val boss = new NlgBoss {}
		// boss.runNetlogoFullGui(args)
		boss.runNetlogoHeadless()
	}
}
trait NlgBoss {
	val nope : org.nlogo.core.Model = null

	// Starting with example code from here.
	// https://github.com/NetLogo/NetLogo/wiki/Controlling-API
	def runNetlogoHeadless(): Unit = {
		val workspace = HeadlessWorkspace.newInstance
		workspace.open("models/Sample Models/Earth Science/Fire.nlogo")
		workspace.command("set density 62")
		workspace.command("random-seed 0")
		workspace.command("setup")
		workspace.command("repeat 50 [ go ]")
		println(workspace.report("burned-trees"))
		workspace.dispose()
	}
	def runNetlogoFullGui(args : Array[String]) {
		App.main(args)
		wait {
			App.app.open("models/Sample Models/Earth Science/Fire.nlogo")
		}
		App.app.command("set density 62")
		App.app.command("random-seed 0")
		App.app.command("setup")
		App.app.command("repeat 50 [ go ]")
		println(App.app.report("burned-trees"))
	}
	def wait(block: => Unit) {
		EventQueue.invokeAndWait(
			new Runnable() { def run() { block } } )
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