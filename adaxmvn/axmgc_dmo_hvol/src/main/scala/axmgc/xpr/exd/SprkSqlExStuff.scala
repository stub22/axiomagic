package axmgc.xpr.exd

private trait SprkSqlExStuff


// This code copied and modified from:
// https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/sql/SparkSQLExample.scala

import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types._

object ModifiedSparkSQLExample {

	case class Person(name: String, age: Long)

	// Currently set by code to run in 'local' single-JVM test  mode
	// https://stackoverflow.com/questions/38008330/spark-error-a-master-url-must-be-set-in-your-configuration-when-submitting-a

	val origPeopleJsonPath = "examples/src/main/resources/people.json"
	val origPeopleTxtPath = "examples/src/main/resources/people.txt"
	// Path does not exist: file:/E:/_emnt/axio_git_clnz/agc_02/adaxmvn/examples/src/main/resources/people.json

	val hvolPathHead = "axmgc_dmo_hvol/"
	val src_sprkExRes = "src/main/resources/sprk_ex_res/"
	val peopleJsonPathTail  = "people.json"
	val hvolSrcPeopleJson = hvolPathHead + src_sprkExRes + peopleJsonPathTail
	val peopleTxtPathTail  = "people.txt"
	val hvolSrcPeopleTxt = hvolPathHead + src_sprkExRes + peopleTxtPathTail


	def main(args: Array[String]): Unit = {
		// $example on:init_session$
		val spark = SparkSession
				.builder()
				.appName("Spark SQL basic example")
				.config("spark.master", "local")
				.getOrCreate()



		// Runs OK including createOrReplaceTempView but fails on "createGlobalTempView" wanting HADOOP_HOME
		runBasicDataFrameExample(spark, hvolSrcPeopleJson, false)

		runDatasetCreationExample(spark, hvolSrcPeopleJson)
		runInferSchemaExample(spark, hvolSrcPeopleTxt)
		runProgrammaticSchemaExample(spark, hvolSrcPeopleTxt)

		spark.stop()
	}


	private def runBasicDataFrameExample(spark: SparkSession, jsonDataSrcPath : String, flag_doGTV : Boolean): Unit = {
		// $example on:create_df$
		val df = spark.read.json(jsonDataSrcPath)

		// Displays the content of the DataFrame to stdout
		df.show()
		// +----+-------+
		// | age|   name|
		// +----+-------+
		// |null|Michael|
		// |  30|   Andy|
		// |  19| Justin|
		// +----+-------+
		// $example off:create_df$

		// $example on:untyped_ops$
		// This import is needed to use the $-notation
		import spark.implicits._
		// Print the schema in a tree format
		df.printSchema()
		// root
		// |-- age: long (nullable = true)
		// |-- name: string (nullable = true)

		// Select only the "name" column
		df.select("name").show()
		// +-------+
		// |   name|
		// +-------+
		// |Michael|
		// |   Andy|
		// | Justin|
		// +-------+

		// Select everybody, but increment the age by 1
		df.select($"name", $"age" + 1).show()
		// +-------+---------+
		// |   name|(age + 1)|
		// +-------+---------+
		// |Michael|     null|
		// |   Andy|       31|
		// | Justin|       20|
		// +-------+---------+

		// Select people older than 21
		df.filter($"age" > 21).show()
		// +---+----+
		// |age|name|
		// +---+----+
		// | 30|Andy|
		// +---+----+

		// Count people by age
		df.groupBy("age").count().show()
		// +----+-----+
		// | age|count|
		// +----+-----+
		// |  19|    1|
		// |null|    1|
		// |  30|    1|
		// +----+-----+
		// $example off:untyped_ops$

		// $example on:run_sql$
		// Register the DataFrame as a SQL temporary view
		df.createOrReplaceTempView("people")

		val sqlDF = spark.sql("SELECT * FROM people")
		sqlDF.show()
		// +----+-------+
		// | age|   name|
		// +----+-------+
		// |null|Michael|
		// |  30|   Andy|
		// |  19| Justin|
		// +----+-------+
		// $example off:run_sql$

		// $example on:global_temp_view$
		// Register the DataFrame as a global temporary view
		/*
		STU notes that this is the first step to require HADOOP_HOME.
		See stack trace at bottom of file:
		 */
		if (flag_doGTV) {
			df.createGlobalTempView("people")

			// Global temporary view is tied to a system preserved database `global_temp`
			spark.sql("SELECT * FROM global_temp.people").show()
			// +----+-------+
			// | age|   name|
			// +----+-------+
			// |null|Michael|
			// |  30|   Andy|
			// |  19| Justin|
			// +----+-------+

			// Global temporary view is cross-session
			spark.newSession().sql("SELECT * FROM global_temp.people").show()
			// +----+-------+
			// | age|   name|
			// +----+-------+
			// |null|Michael|
			// |  30|   Andy|
			// |  19| Justin|
			// +----+-------+
			// $example off:global_temp_view$
		} else {
			println("Skipping Global-Temp-View stuff (which requires Hadoop Path)")
		}
	}

	private def runDatasetCreationExample(spark: SparkSession, jsonDataSrcPath : String): Unit = {
		import spark.implicits._
		// $example on:create_ds$
		// Encoders are created for case classes
		val caseClassDS = Seq(Person("Andy", 32)).toDS()
		caseClassDS.show()
		// +----+---+
		// |name|age|
		// +----+---+
		// |Andy| 32|
		// +----+---+

		// Encoders for most common types are automatically provided by importing spark.implicits._
		val primitiveDS = Seq(1, 2, 3).toDS()
		primitiveDS.map(_ + 1).collect() // Returns: Array(2, 3, 4)

		// DataFrames can be converted to a Dataset by providing a class. Mapping will be done by name
		val path = jsonDataSrcPath // "examples/src/main/resources/people.json"
		val peopleDS = spark.read.json(path).as[Person]
		peopleDS.show()
		// +----+-------+
		// | age|   name|
		// +----+-------+
		// |null|Michael|
		// |  30|   Andy|
		// |  19| Justin|
		// +----+-------+
		// $example off:create_ds$
	}

	private def runInferSchemaExample(spark: SparkSession, txtDataSrcPath : String): Unit = {
		// $example on:schema_inferring$
		// For implicit conversions from RDDs to DataFrames
		import spark.implicits._

		// Create an RDD of Person objects from a text file, convert it to a Dataframe
		val peopleDF = spark.sparkContext
				.textFile(txtDataSrcPath)
				.map(_.split(","))
				.map(attributes => Person(attributes(0), attributes(1).trim.toInt))
				.toDF()
		// Register the DataFrame as a temporary view
		peopleDF.createOrReplaceTempView("people")

		// SQL statements can be run by using the sql methods provided by Spark
		val teenagersDF = spark.sql("SELECT name, age FROM people WHERE age BETWEEN 13 AND 19")

		// The columns of a row in the result can be accessed by field index
		teenagersDF.map(teenager => "Name: " + teenager(0)).show()
		// +------------+
		// |       value|
		// +------------+
		// |Name: Justin|
		// +------------+

		// or by field name
		teenagersDF.map(teenager => "Name: " + teenager.getAs[String]("name")).show()
		// +------------+
		// |       value|
		// +------------+
		// |Name: Justin|
		// +------------+

		// No pre-defined encoders for Dataset[Map[K,V]], define explicitly
		implicit val mapEncoder = org.apache.spark.sql.Encoders.kryo[Map[String, Any]]
		// Primitive types and case classes can be also defined as
		// implicit val stringIntMapEncoder: Encoder[Map[String, Any]] = ExpressionEncoder()

		// row.getValuesMap[T] retrieves multiple columns at once into a Map[String, T]
		teenagersDF.map(teenager => teenager.getValuesMap[Any](List("name", "age"))).collect()
		// Array(Map("name" -> "Justin", "age" -> 19))
		// $example off:schema_inferring$
	}

	private def runProgrammaticSchemaExample(spark: SparkSession, txtDataSrcPath : String): Unit = {
		import spark.implicits._
		// $example on:programmatic_schema$
		// Create an RDD    		// orig path:   "examples/src/main/resources/people.txt"
		val peopleRDD = spark.sparkContext.textFile(txtDataSrcPath)

		// The schema is encoded in a string
		val schemaString = "name age"

		// Generate the schema based on the string of schema
		val fields = schemaString.split(" ")
				.map(fieldName => StructField(fieldName, StringType, nullable = true))
		val schema = StructType(fields)

		// Convert records of the RDD (people) to Rows
		val rowRDD = peopleRDD
				.map(_.split(","))
				.map(attributes => Row(attributes(0), attributes(1).trim))

		// Apply the schema to the RDD
		val peopleDF = spark.createDataFrame(rowRDD, schema)

		// Creates a temporary view using the DataFrame
		peopleDF.createOrReplaceTempView("people")

		// SQL can be run over a temporary view created using DataFrames
		val results = spark.sql("SELECT name FROM people")

		// The results of SQL queries are DataFrames and support all the normal RDD operations
		// The columns of a row in the result can be accessed by field index or by field name
		results.map(attributes => "Name: " + attributes(0)).show()
		// +-------------+
		// |        value|
		// +-------------+
		// |Name: Michael|
		// |   Name: Andy|
		// | Name: Justin|
		// +-------------+
		// $example off:programmatic_schema$
	}
}


/*

22/01/24 20:14:07 INFO ShutdownHookManager:
Deleting directory C:\Users\texpe\AppData\Local\Temp\spark-306311c6-294c-49c1-8f59-f53f0f64f620

Exception in thread "main" java.lang.RuntimeException: java.io.FileNotFoundException: java.io.FileNotFoundException: HADOOP_HOME and hadoop.home.dir are unset. -see https://wiki.apache.org/hadoop/WindowsProblems
	at org.apache.hadoop.util.Shell.getWinUtilsPath(Shell.java:736)
	at org.apache.hadoop.util.Shell.getSetPermissionCommand(Shell.java:271)
	at org.apache.hadoop.util.Shell.getSetPermissionCommand(Shell.java:287)
	at org.apache.hadoop.fs.RawLocalFileSystem.setPermission(RawLocalFileSystem.java:978)
	at org.apache.hadoop.fs.RawLocalFileSystem.mkOneDirWithMode(RawLocalFileSystem.java:660)
	at org.apache.hadoop.fs.RawLocalFileSystem.mkdirsWithOptionalPermission(RawLocalFileSystem.java:700)
	at org.apache.hadoop.fs.RawLocalFileSystem.mkdirs(RawLocalFileSystem.java:672)
	at org.apache.hadoop.fs.ChecksumFileSystem.mkdirs(ChecksumFileSystem.java:788)
	at org.apache.spark.sql.catalyst.catalog.InMemoryCatalog.liftedTree1$1(InMemoryCatalog.scala:121)
	at org.apache.spark.sql.catalyst.catalog.InMemoryCatalog.createDatabase(InMemoryCatalog.scala:118)
	at org.apache.spark.sql.internal.SharedState.externalCatalog$lzycompute(SharedState.scala:153)
	at org.apache.spark.sql.internal.SharedState.externalCatalog(SharedState.scala:140)
	at org.apache.spark.sql.internal.SharedState.globalTempViewManager$lzycompute(SharedState.scala:170)
	at org.apache.spark.sql.internal.SharedState.globalTempViewManager(SharedState.scala:168)
	at org.apache.spark.sql.internal.BaseSessionStateBuilder.$anonfun$catalog$2(BaseSessionStateBuilder.scala:151)
	at org.apache.spark.sql.catalyst.catalog.SessionCatalog.globalTempViewManager$lzycompute(SessionCatalog.scala:119)
	at org.apache.spark.sql.catalyst.catalog.SessionCatalog.globalTempViewManager(SessionCatalog.scala:119)
	at org.apache.spark.sql.catalyst.catalog.SessionCatalog.getRawGlobalTempView(SessionCatalog.scala:661)
	at org.apache.spark.sql.execution.command.CreateViewCommand.$anonfun$run$2(views.scala:144)
	at org.apache.spark.sql.execution.command.ViewHelper$.createTemporaryViewRelation(views.scala:627)
	at org.apache.spark.sql.execution.command.CreateViewCommand.run(views.scala:147)
	at org.apache.spark.sql.execution.command.ExecutedCommandExec.sideEffectResult$lzycompute(commands.scala:75)
	at org.apache.spark.sql.execution.command.ExecutedCommandExec.sideEffectResult(commands.scala:73)
	at org.apache.spark.sql.execution.command.ExecutedCommandExec.executeCollect(commands.scala:84)
	at org.apache.spark.sql.execution.QueryExecution$$anonfun$eagerlyExecuteCommands$1.$anonfun$applyOrElse$1(QueryExecution.scala:110)
	at org.apache.spark.sql.execution.SQLExecution$.$anonfun$withNewExecutionId$5(SQLExecution.scala:103)
	at org.apache.spark.sql.execution.SQLExecution$.withSQLConfPropagated(SQLExecution.scala:163)
	at org.apache.spark.sql.execution.SQLExecution$.$anonfun$withNewExecutionId$1(SQLExecution.scala:90)
	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:775)
	at org.apache.spark.sql.execution.SQLExecution$.withNewExecutionId(SQLExecution.scala:64)
	at org.apache.spark.sql.execution.QueryExecution$$anonfun$eagerlyExecuteCommands$1.applyOrElse(QueryExecution.scala:110)
	at org.apache.spark.sql.execution.QueryExecution$$anonfun$eagerlyExecuteCommands$1.applyOrElse(QueryExecution.scala:106)
	at org.apache.spark.sql.catalyst.trees.TreeNode.$anonfun$transformDownWithPruning$1(TreeNode.scala:481)
	at org.apache.spark.sql.catalyst.trees.CurrentOrigin$.withOrigin(TreeNode.scala:82)
	at org.apache.spark.sql.catalyst.trees.TreeNode.transformDownWithPruning(TreeNode.scala:481)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.org$apache$spark$sql$catalyst$plans$logical$AnalysisHelper$$super$transformDownWithPruning(LogicalPlan.scala:30)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.transformDownWithPruning(AnalysisHelper.scala:267)
	at org.apache.spark.sql.catalyst.plans.logical.AnalysisHelper.transformDownWithPruning$(AnalysisHelper.scala:263)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.transformDownWithPruning(LogicalPlan.scala:30)
	at org.apache.spark.sql.catalyst.plans.logical.LogicalPlan.transformDownWithPruning(LogicalPlan.scala:30)
	at org.apache.spark.sql.catalyst.trees.TreeNode.transformDown(TreeNode.scala:457)
	at org.apache.spark.sql.execution.QueryExecution.eagerlyExecuteCommands(QueryExecution.scala:106)
	at org.apache.spark.sql.execution.QueryExecution.commandExecuted$lzycompute(QueryExecution.scala:93)
	at org.apache.spark.sql.execution.QueryExecution.commandExecuted(QueryExecution.scala:91)
	at org.apache.spark.sql.Dataset.<init>(Dataset.scala:219)
	at org.apache.spark.sql.Dataset$.$anonfun$ofRows$1(Dataset.scala:91)
	at org.apache.spark.sql.SparkSession.withActive(SparkSession.scala:775)
	at org.apache.spark.sql.Dataset$.ofRows(Dataset.scala:88)
	at org.apache.spark.sql.Dataset.withPlan(Dataset.scala:3734)
	at org.apache.spark.sql.Dataset.createGlobalTempView(Dataset.scala:3325)
	at axmgc.xpr.exd.ModifiedSparkSQLExample$.runBasicDataFrameExample(SprkSqlExStuff.scala:123)
	at axmgc.xpr.exd.ModifiedSparkSQLExample$.main(SprkSqlExStuff.scala:29)
	at axmgc.xpr.exd.ModifiedSparkSQLExample.main(SprkSqlExStuff.scala)
Caused by: java.io.FileNotFoundException: java.io.FileNotFoundException: HADOOP_HOME and hadoop.home.dir are unset. -see https://wiki.apache.org/hadoop/WindowsProblems
 */