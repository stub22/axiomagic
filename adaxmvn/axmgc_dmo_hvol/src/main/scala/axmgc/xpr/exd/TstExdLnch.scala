package axmgc.xpr.exd

object TstExdLnch {
	val flg_useSprkCntxt = true
	def main(args: Array[String]): Unit = {
		println("yowzanjobupe")
		val workingDir = System.getProperty("user.dir");
		println("user.dir=", workingDir)
		val ss = new SomeSpark {}
		if (flg_useSprkCntxt) {
			ss.runInCtx
		} else {
			ss.runInSession
		}
	}
}

import java.util.Random

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

trait SomeSpark {
	val pthTl_folkTxt = "lit_txt/pg15164.txt"
	val pthTl_aliceTxt = "lit_txt/pg_11_aiw_by_lc.txt"
	val srcTxtFldr = "axmgc_dmo_hvol/src/main/resources/"
	val folk_relFilePath = srcTxtFldr + pthTl_folkTxt
	val alice_relFilePath = srcTxtFldr + pthTl_aliceTxt
	lazy val skewed = new SimpleSkewedGroupByTest {}

	def runInCtx : Unit = {

		val sparkConf = new SparkConf()
				.setAppName("TrivialApp SparkContext")
				.setMaster("local[4]") // Delete this line when submitting to a cluster

		val sparkContext = new SparkContext(sparkConf);
		val numbersRDD = sparkContext.parallelize(Array(2,3,2,1))
		storyStats(sparkContext, folk_relFilePath)
		storyStats(sparkContext, alice_relFilePath)
		skewed.doTest(sparkContext)
	}
	def runInSession : Unit = {
		// Since Spark 2.0, SparkSession is a higher level entry point for Spark, including DataFrame APIs.
		val sprkSess = SparkSession.builder.master("local[4]").appName("TrivialApp SparkSession").getOrCreate()
		val sprkCtx = sprkSess.sparkContext
		storyStats(sprkCtx, folk_relFilePath)
		sprkSess.stop()
	}

	def storyStats(sparkContext : SparkContext, inFilePath : String) : Unit = {
		println("Defining rdd based on text file at: " + inFilePath)
		val textRDD : RDD[String] = sparkContext.textFile(inFilePath);
		val useRDD = textRDD
		val lineCnt = useRDD.count()
		println("Number of entries in RDD = " + lineCnt);
		val numAs = useRDD.filter(line => line.contains("a")).count()
		val numBs = useRDD.filter(line => line.contains("b")).count()
		println(s"numEnts = $lineCnt, numAs = $numAs, numBs = $numBs")
	}
}

/**
  * Copied from spark test cases, and modified
  *
  * Usage: SimpleSkewedGroupByTest [numMappers] [numKVPairs] [valSize] [numReducers] [ratio]
  */
trait SimpleSkewedGroupByTest {
	def doMain(args: Array[String]) {
		val spark = SparkSession
				.builder
				.appName("SimpleSkewedGroupByTest")
				.getOrCreate()

		val numMappers = if (args.length > 0) args(0).toInt else 2
		val numKVPairs = if (args.length > 1) args(1).toInt else 1000
		val valSize = if (args.length > 2) args(2).toInt else 1000
		val numReducers = if (args.length > 3) args(3).toInt else numMappers
		val ratio = if (args.length > 4) args(4).toInt else 5.0

		doTest(spark.sparkContext)

		spark.stop()
	}

	def doTest(sprkCtx : SparkContext, numMappers : Int = 2, numKVPairs : Int = 1000,
			   valSize : Int = 1000, numReducers : Int = 2, ratio : Float = 5.0F) : Unit = {
		println("Starting SimpleSkewedGroupByTest");
		val pairs1 = sprkCtx.parallelize(0 until numMappers, numMappers).flatMap { p =>
			val ranGen = new Random
			val result = new Array[(Int, Array[Byte])](numKVPairs)
			for (i <- 0 until numKVPairs) {
				val byteArr = new Array[Byte](valSize)
				ranGen.nextBytes(byteArr)
				val offset = ranGen.nextInt(1000) * numReducers
				if (ranGen.nextDouble < ratio / (numReducers + ratio - 1)) {
					// give ratio times higher chance of generating key 0 (for reducer 0)
					result(i) = (offset, byteArr)
				} else {
					// generate a key for one of the other reducers
					val key = 1 + ranGen.nextInt(numReducers-1) + offset
					result(i) = (key, byteArr)
				}
			}
			result
		}.cache
		println("SimpleSkewedGroupByTest:  Pairs have been told to cache, starting count.")
		// Enforce that everything has been calculated and in cache
		val cnt = pairs1.count
		println(s"SimpleSkewedGroupByTest: Count = $cnt")
		val grpd: RDD[(Int, Iterable[Array[Byte]])] = pairs1.groupByKey(numReducers)
		val grpdCnt : Long = grpd.count()
		println(s"SimpleSkewedGroupByTest grouped count: $grpdCnt")
	}
}