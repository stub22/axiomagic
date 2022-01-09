package axmgc.xpr.io.prqt

import java.time.{LocalDate, ZoneOffset}
import java.util.TimeZone

import com.github.mjakubowski84.parquet4s.{ParquetIterable, ParquetReader, ParquetWriter, Path, RowParquetRecord, Value, ValueCodecConfiguration}
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.{BINARY, INT32, INT64}
import org.apache.parquet.schema.Type.Repetition.{OPTIONAL, REQUIRED}
import org.apache.parquet.schema.{LogicalTypeAnnotation, MessageType, OriginalType, Types}


private trait WrapPrqt4S

case class DummyRec(dmrcId: String, dmName: String, created: java.sql.Timestamp)

trait WriteSomeStuff {

}
trait ReadSomeStuff

trait HasSchemaNamesAndVCC {
	val ID = "id"
	val Name = "name"
	val Birthday = "birthday"
	val SchemaName = "user_schema"
	val vcc = ValueCodecConfiguration(TimeZone.getTimeZone(ZoneOffset.UTC))
}
trait MakesUserDat extends HasSchemaNamesAndVCC {

	def mkUsrDat: List[RowParquetRecord] = List(
		(1L, "Alice", LocalDate.of(2000, 1, 1)),
		(2L, "Bob", LocalDate.of(1980, 2, 28)),
		(3L, "Cecilia", LocalDate.of(1977, 3, 15))
	).map { case (id, name, birthday) =>
		// 2021-12-26 updating versions of Scala and Parquet4s
		// naively replaced "empty" with "EmptyNoSchema", and "add" with "updated".
		RowParquetRecord.EmptyNoSchema
				.updated(ID, id, vcc)
				.updated(Name, name, vcc)
				.updated(Birthday, birthday, vcc)
	}
}
trait KnowsOurSchemaImplicitly extends HasSchemaNamesAndVCC {
	// write
	implicit val schema: MessageType = Types.buildMessage()
			.addField(Types.primitive(INT64, REQUIRED).as(LogicalTypeAnnotation.intType(64, true)).named(ID))
			.addField(Types.primitive(BINARY, OPTIONAL).as(LogicalTypeAnnotation.stringType()).named(Name))
			.addField(Types.primitive(INT32, OPTIONAL).as(LogicalTypeAnnotation.dateType()).named(Birthday))
			.named(SchemaName)

}
trait ReadsAndWritesPrqtWithOurSchema extends KnowsOurSchemaImplicitly {

	def writePrqtFile (udat : List[RowParquetRecord], fPath : String) : Unit = {
		val pPath = Path(fPath)
		ParquetWriter.writeAndClose(pPath, udat) // users.parquet
	}

	def readPrqtFile (fPath : String) : Unit = {
		val pPath = Path(fPath)
		//read
		val readData: ParquetIterable[RowParquetRecord] = ParquetReader.read[RowParquetRecord](pPath)
		// val x = readData
		try {
			readData.foreach { r =>  printRPR(r) }
		} finally readData.close()
	}
	def printRPR (rpr : RowParquetRecord) : Unit = {
		println(s"*** rpr=${rpr}")
		val rm: Map[String, Value] = rpr.toMap
		val rsq = rpr.toSeq
		println(s"field-seq=$rsq")
		rsq.foreach(pair => {
			val (n,v) = pair
			println(s"n=$n, v=$v, vclz=${v.getClass}")
		})
		val id = rpr.get[Long](ID, vcc)
		val name = rpr.get[String](Name, vcc)
		val birthday = rpr.get[LocalDate](Birthday, vcc)
		println(s"User[$ID=$id,$Name=$name,$Birthday=$birthday]")

	}

}