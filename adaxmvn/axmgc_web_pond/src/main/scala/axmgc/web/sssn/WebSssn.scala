package axmgc.web.sssn

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import axmgc.web.answr.{WA_Empty, WebAnswer}
import org.slf4j.{Logger, LoggerFactory}

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


trait AppSsnnBoss {
	// Functional definition of a stateful web application
	def absorbAndAnswer(inEvt : WebEvent) : WebAnswer
}
class AppSsnnBoss_FakeImpl extends AppSsnnBoss {
	private lazy val mySssMgr : FakeSssMgr = new FakeSssMgr()
	private lazy val myUDM : FakeUserDataMgr = new FakeUserDataMgr()
	def absorbAndAnswer(inEvt : WebEvent) : WebAnswer = {
		new WA_Empty()
	}
}
trait AppSsnnBoss_Fndr {
	def findBoss : AppSsnnBoss
}
object AppSsnnBoss_Sngl extends AppSsnnBoss_Fndr {
	private lazy val ourBossImpl = new AppSsnnBoss_FakeImpl
	override def findBoss : AppSsnnBoss = ourBossImpl
}
// Note that WE_Empty and WE_Empty() are not the same, for match purposes.
// WE_Empty is a companion object that can be treated as a value.
class AppSsnnStMgr_ActImpl  extends Actor with ActorLogging {
	private def findAppSsnnBoss = AppSsnnBoss_Sngl.findBoss
	override def receive : Receive = {
		case wvb : WE_Empty => {
			log.warning("Matched wee binding : " + wvb)
			rcvEmpty(sender, wvb)
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
	private def rcvEmpty(sndr : ActorRef, empt : WE_Empty)  = {
		log.info("Processing empty inMsg: {}", empt)
		val emptyAnswr = new WA_Empty()
		sndr.tell(emptyAnswr, self)
	}
	private def rcvDomClick(sndr : ActorRef, dclk : WE_DomClick) : Unit = {
		log.info("Processing domClick: {}", dclk)
		val answr = domClkDummyAnswer(dclk)
		sndr.tell(answr, self)
// 		"domClick-sentAnswrAlrdy"
	}
	private def absorbAndAnswerDomClick (dclk : WE_DomClick) : WebAnswer = {
		val boss = findAppSsnnBoss
		val answr = boss.absorbAndAnswer(dclk)
		answr
	}
	private def domClkDummyAnswer(dclk : WE_DomClick) : WebAnswer = {
		val summTxt = "Dom Click rcvd" + dclk + " , thx.  Here's answer: ____ "
		val answrSumm = new WA_Empty() //  WA_Summary(summTxt)
		answrSumm
	}
}

object WbSssnStBossFactory {
	def launchWebEventProc (actrSys : ActorSystem, nmSfx : String): ActorRef = {
		val actrName = "wepBoss-" + nmSfx
		val wepActrRef = actrSys.actorOf(Props[AppSsnnStMgr_ActImpl], actrName)
		wepActrRef
	}
}
//////////////////////////////////////////////////////////////
//// Client Side starts here
//////////////////////////////////////////////////////////////

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