package axmgc.web.ontui.ontfld
import widoco.gui.GuiController

object TstOdocGen  {
	def main(args: Array[String]): Unit = {
		println("TstOdocGen.START")
		val workingDir = System.getProperty("user.dir");
		println("user.dir=" + workingDir)
		println("GuiController.main.START")
		GuiController.main(args)
		println("GuiController.main.END")
	}

}

/*
    public static void main(String[] args) {
        if (args.length > 0) {
            new GuiController(args);
        } else {
            new GuiController();
        }

    }
 */
