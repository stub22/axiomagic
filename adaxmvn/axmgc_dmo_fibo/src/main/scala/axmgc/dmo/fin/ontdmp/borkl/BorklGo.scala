package axmgc.dmo.fin.ontdmp.borkl

private trait BorklGo

trait ServONJ {
	def goServGo(kvliteMainArgs : Array[String]) : Unit = {
		oracle.kv.util.kvlite.KVLite.main(kvliteMainArgs)
	}
}

trait CliONJ {

}

trait BerkConf {

}