package axmgc.web.rsrc

import org.slf4j.{Logger, LoggerFactory}

private trait ResourcePathStuff

trait RsrcNms {
	protected lazy val myS4JLog : Logger = LoggerFactory.getLogger(this.getClass)

	import scala.io.Source

	private val pth_icNms = "gdat/rsrc_nms/icon_name_x3290.txt"

	private def readTxtLines(pthToRsrc: String): Seq[String] = {
		val src = Source.fromResource(pthToRsrc)
		val lineSeq = src.getLines().toSeq
		myS4JLog.info("Read {} lines", lineSeq.size)
		myS4JLog.info("First 5 lines:  {} ", lineSeq.take(5).toList)
		myS4JLog.info("Last 5 lines:  {} ", lineSeq.takeRight(5).toList)
		lineSeq
	}
	def readIcnNms(): Seq[String] = {
		readTxtLines(pth_icNms)
	}
}
