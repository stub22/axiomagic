package axmgc.dmo.fin.ontdmp.borkl

object RunTstBorkl {
	//  java -jar lib/kvstore.jar kvlite -secure-config disable
	// WindowsCmdLineOperations.makeOwnerAccessOnly
	// -secure-config disable
	def main(args: Array[String]): Unit = {
		val bg = new ServONJ{}
		val goodArgs = List("-root", "onor_001", "-secure-config", "disable", "-admin-web-port", "8693").toArray

		bg.goServGo(goodArgs)
		println("goServGo returned, now we will sleep 5S and then try to connect")
		Thread.sleep(5000L)
		println("Back from sleep")
		val cc = new CliONJ {}
		cc.testCliConn()
		println("Finished testCliConn")
		println("End of Main")
	}

}
