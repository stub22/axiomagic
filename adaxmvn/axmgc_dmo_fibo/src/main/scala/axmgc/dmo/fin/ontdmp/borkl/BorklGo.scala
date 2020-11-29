package axmgc.dmo.fin.ontdmp.borkl

import oracle.kv.table.{Row, Table}
import oracle.kv.{KVStore, KVStoreConfig, KVStoreFactory, StatementResult, Version}

private trait BorklGo

trait ServONJ {
	def goServGo(kvliteMainArgs : Array[String]) : Unit = {
		oracle.kv.util.kvlite.KVLite.main(kvliteMainArgs)
	}
}

trait CliONJ {
	def testCliConn() : Unit = {
		// Adapted from Oracle-SDK hello-world example :   HelloBigDataWorld.java
		val dfltStoreName = "kvstore"
		val otherStoreName = "kvs_borkl_29"
		val storeName = dfltStoreName
		val hostName = "localhost"
		val hostPort = "5000"
		println ("Making kvsConf")
		val kvsConf = new KVStoreConfig(storeName, hostName + ":" + hostPort)
		println ("Connecting kvsCli")
		val kvsCli: KVStore =  KVStoreFactory.getStore(kvsConf);
		println ("Got kvsCli:" + kvsCli)

		import oracle.kv.{Key, Value, Version, ValueVersion}
		val keyString = "Hello"
		val valueString = "Big Data World!"

		val goodKey: Key = Key.createKey(keyString)
		val goodVal: Value = Value.createValue(valueString.getBytes)

		val putVV: Version = kvsCli.put(goodKey, goodVal)
		println("putVV: " + putVV)

		val gotVV: ValueVersion = kvsCli.get(goodKey)

		println(keyString + " " + new String(gotVV.getValue.getValue))

		val mkTb = new MkKVTables {}
		mkTb.tstTableStorage(kvsCli)
		kvsCli.close()
	}
}

trait MkKVTables {
	val tblMkCmd =  "CREATE TABLE IF NOT EXISTS simpleUsers  " +
				"(firstName STRING, " +
				" lastName STRING, " +
				" userID INTEGER, " +
				" PRIMARY KEY (userID))"

	val tblDropCmd ="DROP TABLE IF EXISTS simpleUsers"

	def tstTableStorage(kvCli : KVStore) : Unit = {
		val drpRslt: StatementResult = kvCli.executeSync(tblDropCmd)

		val creRslt: StatementResult = kvCli.executeSync(tblMkCmd)

		val kvTblApi = kvCli.getTableAPI

		val tblCli: Table =  kvTblApi.getTable("simpleUsers")

		/* Insert row */
		val oneRow: Row = tblCli.createRow
		oneRow.put("userID", 1)
		oneRow.put("firstName", "Alex")
		oneRow.put("lastName", "Robertson")
		val putRslt: Version = kvTblApi.put(oneRow, null, null)


	}
}
/*
This client works for Stu using Version 20.2.21, but we cannot find the sql.jar in earlier versions.
Those do contain kvclient.jar, but we have not figured out how to launch its kv admin interface
as a console client, so we are sticking with this SQL jobber for now.

https://docs.oracle.com/en/database/other-databases/nosql-database/18.1/sqlfornosql/introduction-sql-shell.html

SQLDeveloper also works for browsing the schema.

It looks like kvclient.jar probably works fine on the classpath for compiling + running example code.
(We have that capability already on our maven classpath).

CONSOLE_DIR      /c/_root/_japp/orcl_kvcli_20221
CONSOLE_PROMPT   java -jar lib/sql.jar
sql-> help
sql-> connect -host localhost -port 5000 -name kvstore
Connected to kvstore at localhost:5000.
sql-> show tables

sql-> select * from simpleUsers WHERE firstName = 'Alex';
{"firstName":"Alex","lastName":"Robertson","userID":1}


More API info:
https://docs.oracle.com/en/database/other-databases/nosql-database/18.1/java-driver-table/developing.html

 */

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