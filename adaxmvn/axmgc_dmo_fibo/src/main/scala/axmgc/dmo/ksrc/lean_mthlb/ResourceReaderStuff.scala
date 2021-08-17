package axmgc.dmo.ksrc.lean_mthlb

import java.io.InputStream
import java.net.{URL, URLConnection}

import org.slf4j.{Logger, LoggerFactory}

import scala.io.Source

private trait ResourceReaderStuff

trait ResourceReadUtils {
	val myS4JLogger: Logger = LoggerFactory.getLogger(this.getClass)
	// 191 MB (200,695,650 bytes)
	// Naive read to array in 69s:  Read stream into char-array of len: 200695650
	def drainStreamToArr(rstrm : InputStream): Unit = {
		val isrc =	Source.fromInputStream(rstrm)
		val iarr: Array[Char] = isrc.toArray
		myS4JLogger.info(s"Read stream into char-array of len: ${iarr.length}")

	}
	def checkResourceLength(rpath : String ) : Long = {
		val rsrc: URL = getClass().getResource(rpath)
		myS4JLogger.info(s"Resource URL: ${rsrc}")
		val rconn: URLConnection = rsrc.openConnection()
		val rlen: Int = rconn.getContentLength()
		myS4JLogger.info(s"Resource length: ${rlen}")
		rlen
	}
}

