package axmgc.xpr.exd

object TstExdLnch {
	def main(args: Array[String]): Unit = {
		println("yowzanjobupe")
		val workingDir = System.getProperty("user.dir");
		println("user.dir=", workingDir)
		val ss = new SomeSpark {}
		ss.runBetter
	}
}

import java.util.Random

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

trait SomeSpark {
	val pthTl_storyTxt = "lit_txt/pg15164.txt"
	val srcTxtFldr = "axmgc_dmo_hvol/src/main/resources/"
	val rltvFilePath = srcTxtFldr + pthTl_storyTxt
	val inFilePath = rltvFilePath
	def runBetter : Unit = {
		val sparkConf = new SparkConf()
				.setAppName("Trivial App")
				.setMaster("local[4]")  // Delete this line when submitting to a cluster

		val sparkContext = new SparkContext(sparkConf);
		val numbersRDD = sparkContext.parallelize(Array(2,3,2,1))
		println("Defining rdd based on text file at: " + inFilePath)
		val textRDD : RDD[String] = sparkContext.textFile(inFilePath);
		val useRDD = textRDD
		val lineCnt = useRDD.count()
		println("Number of entries in RDD = " + lineCnt);
		val numAs = useRDD.filter(line => line.contains("a")).count()
		val numBs = useRDD.filter(line => line.contains("b")).count()
		println(s"numEnts = $lineCnt, numAs = $numAs, numBs = $numBs")
	}
	def runStuff : Unit = {
		val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
		val logData = spark.read.textFile(inFilePath).cache()
		val numAs = logData.filter(line => line.contains("a")).count()
		val numBs = logData.filter(line => line.contains("b")).count()
		println(s"Lines with a: $numAs, Lines with b: $numBs")
		spark.stop()
	}
}

/**
  * Usage: SimpleSkewedGroupByTest [numMappers] [numKVPairs] [valSize] [numReducers] [ratio]
  */
object SimpleSkewedGroupByTest {
	def main(args: Array[String]) {
		val spark = SparkSession
				.builder
				.appName("SimpleSkewedGroupByTest")
				.getOrCreate()

		val numMappers = if (args.length > 0) args(0).toInt else 2
		val numKVPairs = if (args.length > 1) args(1).toInt else 1000
		val valSize = if (args.length > 2) args(2).toInt else 1000
		val numReducers = if (args.length > 3) args(3).toInt else numMappers
		val ratio = if (args.length > 4) args(4).toInt else 5.0

		val pairs1 = spark.sparkContext.parallelize(0 until numMappers, numMappers).flatMap { p =>
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
		// Enforce that everything has been calculated and in cache
		pairs1.count

		println(s"RESULT: ${pairs1.groupByKey(numReducers).count}")

		spark.stop()
	}
}