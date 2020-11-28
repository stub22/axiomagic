package axmgc.dmo.fin.fdtscn

import java.io.InputStream

import scala.io.BufferedSource

object RunFdtScn {
	def main(args: Array[String]): Unit = {
		println("Running a test fin-dat scan")
		val scanner = new FindatScanner {}
		scanner.readStuff
		println("End of main")
	}
}
trait FindatScanner {
	def openCpathTextResource(rpath : String, renc : String) : BufferedSource = {
		val cloader = getClass.getClassLoader
		val rstrm: InputStream = cloader.getResourceAsStream(rpath)
		val rIOSrc: BufferedSource = scala.io.Source.fromInputStream(rstrm, renc)
		rIOSrc
	}
	def processTextResource(rpath : String, renc : String, rproc : String => Unit) : Int = {
		val rIOSrc: BufferedSource = openCpathTextResource(rpath, renc)
		var lineCnt = 0
		for (line <- rIOSrc.getLines) {
			rproc(line)
		}
		rIOSrc.close
		lineCnt
	}

	def readStuff : Unit = {
		val rpath = "gdat/findat_md/all_types.csv"
		val wenc = "windows-1252"
		val u8enc = "utf-8"
		// val cloader = getClass.getClassLoader
		// val rstrm: InputStream = cloader.getResourceAsStream(rpath)
		// val rIOSrc: BufferedSource = scala.io.Source.fromInputStream(rstrm, wenc)
		/*
		val rIOSrc: BufferedSource = openCpathTextResource(rpath, wenc)
		var lineCnt = 0
		for (line <- rIOSrc.getLines) {
			println(line.toUpperCase)
			lineCnt += 1
		}
		rIOSrc.close
		 */
		val proc = (lnTxt : String) => {
			println(s"CoolProc found line: ${lnTxt}")
		}
		val lineCnt = processTextResource(rpath, wenc, proc)
		println(s"-------------------\nFinished processing ${lineCnt} lines")
	}

}
