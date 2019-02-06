package axmgc.web.pond

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import scala.collection.mutable

// Akka-Http does not include a built-in client HTTP session hookup.
// One answer is:
// https://github.com/softwaremill/akka-http-session
// 2019-Feb: In this Axmgc prototype we are using fake session keys to ensure
// that app+data are multi-user internally.
// Actor for session state mgmt.

class FakeWebSssn (sessID : Long, usrHndl : String) {
	val myCreTimeMsec : Long = System.currentTimeMillis()
}
class FakeSssMgr {
	val tstSssn_A = new FakeWebSssn(2971, "Annabelle")
	val tstSssn_B = new FakeWebSssn(3853, "Bernadette")
	val tstSssn_C = new FakeWebSssn(4129, "Charlotte")
}
trait QuadStoreHndl
trait FakeUserData {
	def getQSH : QuadStoreHndl
}
class FakeUserDataMgr {
	val myUsrDatMap = new mutable.HashMap[Long, FakeUserData]
	def makeFUD : FakeUserData = {
		val made = new FakeUserData() {
			lazy val myQSH = new QuadStoreHndl(){}
			override def getQSH: QuadStoreHndl = myQSH
		}
		made
	}
	def getUserDat (sessID : Long) : FakeUserData = {
		val fud = myUsrDatMap.getOrElseUpdate(sessID, makeFUD)
		fud
	}
}
trait TWebEvent {

}
sealed abstract class WebEvent extends TWebEvent {
	// val myEvtInstMsec : Long = System.currentTimeMillis()
}
case class WE_Empty() extends WebEvent
case class WE_DomClick(domIdTxt : String) extends WebEvent

trait TWebAnswer {

}
sealed abstract class WebAnswer extends TWebAnswer {
	val myEvtInstMsec : Long = System.currentTimeMillis()
}
// case class WA_Empty() extends WebAnswer
case class WA_Summary(summaryTxt : String) extends WebAnswer


class AppSsnnBoss {
	private lazy val mySssMgr : FakeSssMgr = new FakeSssMgr()
	private lazy val myUDM : FakeUserDataMgr = new FakeUserDataMgr()
}

class AppSsnnStMgr_ActImpl  extends Actor with ActorLogging {
	override def receive = {
		// We can't case-match on a superclass unless it has an unapply method
		// in the companion Object.
		case empt:  WE_Empty => {
			log.warning("Trying empt")
			rcvEmpty(sender, empt)
		}
		case dclk : WE_DomClick =>  {
			log.warning("Trying dclk: " + dclk)
			rcvDomClick(sender, dclk)
		}
			/*
		case otherMsg => {
			log.warning("Received admin/other msg: {}", otherMsg)
		}
			*/
	}
	def rcvEmpty(sndr : ActorRef, empt : WE_Empty)  = {
		log.info("Processing empty inMsg: ", empt)
	}
	def rcvDomClick(sndr : ActorRef, dclk : WE_DomClick) : Unit = {
		log.info("Processing domClick: {}", dclk)
		val summTxt = "Dom Click rcvd" + dclk + " , thx.  Here's answer: ____ "
		val answrSumm = WA_Summary(summTxt)
		sndr.tell(answrSumm, self)
// 		"domClick-sentAnswrAlrdy"
	}
}

object WbSssnStBossFactory {
	def launchWebEventProc (actrSys : ActorSystem, nmSfx : String): ActorRef = {
		val actrName = "wepBoss-" + nmSfx
		val wepActrRef = actrSys.actorOf(Props[AppSsnnStMgr_ActImpl], actrName)
		wepActrRef
	}
}

/*
There are generally two ways of getting a reply from an Actor:
the first is by a sent message (actor ! msg), which
only works if the original sender was an Actor) and the second is through a Future.

 */
/*
Quotes from:

https://groups.google.com/forum/#!topic/akka-user/-VF0ZeIt054

It's possible to create an actor from the mapAsync function and return the Future of an ask request to that new actor.
What am I missing?
--

I'm not up to speed on Flows, so I don't know what the proposed
solution would look like in a route definition.
In the meantime (this is just for a new proof of concept app)
I embraced the Ask in the route definition and create one-offs in response.
I didn't use forwarding as Patrik mentioned. Instead
the one-off has a "requester" property and after doing a bunch of tells and receives,
completes the ask future by sending a response to the requester. I hope this is OK -
it seems to work as a bridge between AskWorld and TellDon'tAskWorld.
 */