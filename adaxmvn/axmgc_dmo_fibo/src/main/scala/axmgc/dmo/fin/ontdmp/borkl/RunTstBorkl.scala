package axmgc.dmo.fin.ontdmp.borkl

object RunTstBorkl {
	//  java -jar lib/kvstore.jar kvlite -secure-config disable
	// WindowsCmdLineOperations.makeOwnerAccessOnly
	// -secure-config disable
	def main(args: Array[String]): Unit = {
		val bg = new ServONJ{}
		val goodArgs = List("-root", "onor_001", "-secure-config", "disable", "-admin-web-port", "8693").toArray

		bg.goServGo(goodArgs)
	}
}
