package axmgc.xpr.io.prqt

/*
import com.github.mjakubowski84.parquet4s.{ParquetWriter} // ParquetStreams
import org.apache.parquet.hadoop.ParquetFileWriter
import org.apache.parquet.hadoop.metadata.CompressionCodecName

import org.apache.hadoop.conf.Configuration
*/

import java.time.{LocalDate, ZoneOffset}
import java.util.TimeZone
import com.github.mjakubowski84.parquet4s.{ParquetReader, ParquetWriter, RowParquetRecord, ValueCodecConfiguration}
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName.{BINARY, INT32, INT64}
import org.apache.parquet.schema.Type.Repetition.{OPTIONAL, REQUIRED}
import org.apache.parquet.schema.{LogicalTypeAnnotation, MessageType, OriginalType, Types}

import java.nio.file.Files


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
		RowParquetRecord.empty
				.add(ID, id, vcc)
				.add(Name, name, vcc)
				.add(Birthday, birthday, vcc)
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

		ParquetWriter.writeAndClose(fPath, udat) // users.parquet
	}

	def readPrqtFile (fPath : String) : Unit = {
		//read
		val readData = ParquetReader.read[RowParquetRecord](fPath)
		try {
			readData.foreach { record =>
				val id = record.get[Long](ID, vcc)
				val name = record.get[String](Name, vcc)
				val birthday = record.get[LocalDate](Birthday, vcc)
				println(s"User[$ID=$id,$Name=$name,$Birthday=$birthday]")
			}
		} finally readData.close()
	}
}