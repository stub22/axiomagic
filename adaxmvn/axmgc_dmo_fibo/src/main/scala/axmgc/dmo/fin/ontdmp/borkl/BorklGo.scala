package axmgc.dmo.fin.ontdmp.borkl

import oracle.kv.{KVStoreConfig, KVStoreFactory, Version}

private trait BorklGo

trait ServONJ {
	def goServGo(kvliteMainArgs : Array[String]) : Unit = {
		oracle.kv.util.kvlite.KVLite.main(kvliteMainArgs)
	}
}

trait CliONJ {
	def testCliConn() : Unit = {
		val dfltStoreName = "kvstore"
		val otherStoreName = "kvs_borkl_29"
		val storeName = dfltStoreName
		val hostName = "localhost"
		val hostPort = "5000"
		println ("Making kvsConf")
		val kvsConf = new KVStoreConfig(storeName, hostName + ":" + hostPort)
		println ("Connecting kvsCli")
		val kvsCli =  KVStoreFactory.getStore(kvsConf);
		println ("Got kvsCli:" + kvsCli)

		import oracle.kv.Key
		import oracle.kv.Value
		import oracle.kv.ValueVersion
		val keyString = "Hello"
		val valueString = "Big Data World!"

		val goodKey: Key = Key.createKey(keyString)
		val goodVal: Value = Value.createValue(valueString.getBytes)

		val putVV: Version = kvsCli.put(goodKey, goodVal)
		println("putVV: " + putVV)

		val gotVV: ValueVersion = kvsCli.get(goodKey)

		println(keyString + " " + new String(gotVV.getValue.getValue))
		kvsCli.close()
	}
}

/*
Opened existing kvlite store with config:
-root onor_001 -store kvstore -host sqdgyrct -port 5000 -secure-config disable -restore-from-snapshot null -admin-web-port 8693
goServGo returned, now we will sleep 5S and then try to connect
Back from sleep
Making kvsConf
Connecting kvsCli
Exception in thread "main" java.lang.IllegalArgumentException: Specified store name, kvs_borkl_29, does not match store name at specified host/port, kvstore
	at oracle.kv.impl.api.RequestDispatcherImpl.<init>(RequestDispatcherImpl.java:445)
	at oracle.kv.impl.api.RequestDispatcherImpl.<init>(RequestDispatcherImpl.java:371)
	at oracle.kv.impl.api.RequestDispatcherImpl.createForClient(RequestDispatcherImpl.java:344)
	at oracle.kv.KVStoreFactory.getStoreInternal(KVStoreFactory.java:253)
	at oracle.kv.KVStoreFactory.getStore(KVStoreFactory.java:135)
	at oracle.kv.KVStoreFactory.getStore(KVStoreFactory.java:72)
	at axmgc.dmo.fin.ontdmp.borkl.CliONJ.testCliConn(BorklGo.scala:22)
	at axmgc.dmo.fin.ontdmp.borkl.CliONJ.testCliConn$(BorklGo.scala:14)
	at axmgc.dmo.fin.ontdmp.borkl.RunTstBorkl$$anon$2.testCliConn(RunTstBorkl.scala:15)
	at axmgc.dmo.fin.ontdmp.borkl.RunTstBorkl$.main(RunTstBorkl.scala:16)
	at axmgc.dmo.fin.ontdmp.borkl.RunTstBorkl.main(RunTstBorkl.scala)
 */

trait BerkConf {

}