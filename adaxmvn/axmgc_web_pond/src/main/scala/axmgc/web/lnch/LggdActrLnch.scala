package axmgc.web.lnch

import axmgc.web.pond.WebServerLauncher
import org.slf4j.{Logger, LoggerFactory}

trait LggdActrLnch {

}

class WbSrvcLnchr (myActSysNm : String, myHostNm : String, myPortNum : Int)
		extends WebServerLauncher {
	protected lazy val myS4JLogger: Logger = LoggerFactory.getLogger(this.getClass)
	protected lazy val myActorSys = makeActorSys(myActSysNm)

	// TODO:  Return a future supporting clean shutdown
	def launch: Unit = {
	}
}
