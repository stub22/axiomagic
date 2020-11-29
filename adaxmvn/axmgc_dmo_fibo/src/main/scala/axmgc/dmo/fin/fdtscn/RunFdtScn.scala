package axmgc.dmo.fin.fdtscn

import java.io.InputStream

import scala.collection.immutable.Seq
import scala.collection.mutable.{HashMap => SMHashMap, Map => SMMap}
import scala.io.BufferedSource

object RunFdtScn {
	def main(args: Array[String]): Unit = {
		println("Running a test fin-dat scan")
		val scanner = new FindatScanner {}
		scanner.readStuff
		println("End of main")
	}
}
trait FieldProc {
	def doProc(rowKey : String, fldArr : Array[String], flg_doDumps : Boolean) : Unit
}
abstract class FldXProc(xFlds : List[(Int,String)]) extends FieldProc {
	override def doProc(rowKey : String, fldArr: Array[String], flg_doDumps : Boolean): Unit = {
		// This can be done with a case, but...
		val mappedTups: Seq[(Int, String, String)] = xFlds.map(tup => {
			val (idx, nm) : (Int, String) = tup
			val xv = fldArr(idx)
			if (flg_doDumps) {
				println(s"idx=${idx}, nm=${nm}, xv=${xv}")
			}
			// saveOneField(rowKey, idx, nm, xv)
			(idx, nm, xv)
		})
		saveRow(rowKey, mappedTups)
	}
	def saveRow(rowKey : String, fseq : Seq[(Int, String, String)]): Unit
/*	= {
/		for (fld <- fseq) {
			saveOneField(rowKey, fld._1, fld._2, fld._3)
		}
	}
//	def saveOneField(rowKey : String, idx : Int, nm : String, vl : String) : Unit = {
//	}
 */
}
trait FindatScanner {
	def openCpathTextResource(rpath : String, renc : String) : BufferedSource = {
		val cloader = getClass.getClassLoader
		val rstrm: InputStream = cloader.getResourceAsStream(rpath)
		val rIOSrc: BufferedSource = scala.io.Source.fromInputStream(rstrm, renc)
		rIOSrc
	}
	def processTextResource(rpath : String, renc : String, rproc : (Int, String) => Unit) : Int = {
		val rIOSrc: BufferedSource = openCpathTextResource(rpath, renc)
		var lineCnt = 0
		for (line <- rIOSrc.getLines) {
			rproc(lineCnt, line)
			lineCnt += 1
		}
		rIOSrc.close
		lineCnt
	}
	private def dumpyProc(lnNum : Int, lnTxt : String, expectedCols : Int, dumpMod : Int, fp : FieldProc) : Unit = {
		val fields: Array[String] = lnTxt.split(",").map(_.trim)
		val fieldCnt = fields.length
		if (fields.length != expectedCols) {
			throw new Exception(s"Got unexpected fieldCount=${fieldCnt} at line ${lnNum}, txt=${lnTxt}")
		}
		val flg_doDumps = ((lnNum % dumpMod) == 0)

		if (flg_doDumps) {
			println(s"CoolProc at line ${lnNum} got txt: ${lnTxt}")
			val fldTxt = fields.mkString(".#.")
			println(s"Split into ${fieldCnt} fields: ${fldTxt}")
		}
		val rowKey = s"line_${lnNum}"
		fp.doProc(rowKey, fields, flg_doDumps)
	}
	def scanCsvTextResource(rpath : String, renc : String, expectedCols : Int, dumpMod : Int, fp : FieldProc): Unit = {
		val proc = (lnNum : Int, lnTxt : String) => dumpyProc(lnNum, lnTxt, expectedCols, dumpMod, fp)
		println(s"Opening resource at ${rpath}\n-------------------")
		val lineCnt = processTextResource(rpath, renc, proc)
		println(s"-------------------\nFinished processing ${lineCnt} lines in ${rpath}")
	}
	def readStuff : Unit = {

		val fldz1 = List[(Int,String)](0 -> "fld_00", 1 -> "fld_01", 2 -> "fld_02", 3 -> "fld_03")
		val fldz2 = List[(Int,String)](0 -> "fld_00", 1 -> "fld_01", 2 -> "fld_02")
/*
CoolProc at line 0 got txt:
0-6    Sponsor,Composite Ticker,Composite Name,Constituent Ticker,Constituent Name,Weighting,Identifier,
7-14   Date,Location,Exchange,Total Shares Held,Notional Value,Market Value,Sponsor Sector,Last Trade,
15-22   Currency,BloombergSymbol,BloombergExchange,NAICSSector,NAICSSubIndustry,Coupon,Maturity,Rating,
23-32    Type,SharesOutstanding,MarketCap,Earnings,PE Ratio,Face,eSignalTicker,TimeZone,DividendAmt,XDate,
DividendYield,RIC,IssueType,NAICSSector,NAICSIndustry,NAICSSubIndustry,CUSIP,ISIN,BBGID
 */
		val fldz3 = List[(Int,String)](0 -> "sponsor", 1 -> "agg_sym", 2 -> "agg_name", 3 -> "mbr_sym",
				4 -> "mbr_name", 5 -> "weight", 6-> "some_id", 7 -> "dt_rcvd", 8 -> "mbr_xloc", 9 -> "mbr_xchg", 10 -> "shares",
				11 -> "not_val", 12-> "mkt_val", 13-> "mbr_sect", 14-> "mbr_last", 15-> "mbr_last_curr",
				20 -> "mbr_coup", 21-> "mbr_maturity", 22-> "mbr_rating", 23->"mbr_type", 24->"mbr_shares", 25-> "mbr_mktCap")
		val fp1 = new FldXProc(fldz1.toList) {
			val symDir = new SMHashMap[String, (String, String)]
			override def saveRow(rowKey: String, fseq: Seq[(Int, String, String)]): Unit = {
				val sym = fseq(1)._3
				val flv = fseq(2)._3
				val desc = fseq(0)._3
				symDir.put(sym, (flv, desc))
			}
		}
		val fp2 = new FldXProc(fldz2.toList) {
			override def saveRow(rowKey: String, fseq: Seq[(Int, String, String)]): Unit = {}
		}
		val fp3 = new FldXProc(fldz3.toList) {
			override def saveRow(rowKey: String, fseq: Seq[(Int, String, String)]): Unit = {}
		}

		val r1path = "gdat/findat_md/all_types.csv"
		val r2path = "gdat/findat_md/all_comp_types.csv"
		val r3path = "gdat/findat_md/constit_dat.csv"

		val wenc = "windows-1252"
		val u8enc = "utf-8"

		scanCsvTextResource(r1path, wenc, 4, 100, fp1)
		scanCsvTextResource(r2path, wenc, 3, 100, fp2)
		scanCsvTextResource(r3path, wenc, 42, 10000, fp3)

		val sd = fp1.symDir
		println("=========================\nSymDir:\n======================== ")
		println(sd.toString)


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
	}

}
