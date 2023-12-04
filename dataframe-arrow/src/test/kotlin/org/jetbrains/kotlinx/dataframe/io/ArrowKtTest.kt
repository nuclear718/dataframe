package org.jetbrains.kotlinx.dataframe.io

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.apache.arrow.memory.RootAllocator
import org.apache.arrow.vector.TimeStampMicroVector
import org.apache.arrow.vector.TimeStampMilliVector
import org.apache.arrow.vector.TimeStampNanoVector
import org.apache.arrow.vector.TimeStampSecVector
import org.apache.arrow.vector.VectorSchemaRoot
import org.apache.arrow.vector.ipc.ArrowFileWriter
import org.apache.arrow.vector.ipc.ArrowStreamWriter
import org.apache.arrow.vector.types.FloatingPointPrecision
import org.apache.arrow.vector.types.TimeUnit
import org.apache.arrow.vector.types.pojo.ArrowType
import org.apache.arrow.vector.types.pojo.Field
import org.apache.arrow.vector.types.pojo.FieldType
import org.apache.arrow.vector.types.pojo.Schema
import org.apache.arrow.vector.util.Text
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.NullabilityOptions
import org.jetbrains.kotlinx.dataframe.api.add
import org.jetbrains.kotlinx.dataframe.api.columnOf
import org.jetbrains.kotlinx.dataframe.api.convertToBoolean
import org.jetbrains.kotlinx.dataframe.api.copy
import org.jetbrains.kotlinx.dataframe.api.dataFrameOf
import org.jetbrains.kotlinx.dataframe.api.map
import org.jetbrains.kotlinx.dataframe.api.pathOf
import org.jetbrains.kotlinx.dataframe.api.remove
import org.jetbrains.kotlinx.dataframe.api.toColumn
import org.jetbrains.kotlinx.dataframe.exceptions.TypeConverterNotFoundException
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URL
import java.nio.channels.Channels
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale
import kotlin.reflect.typeOf

internal class ArrowKtTest {

    fun testResource(resourcePath: String): URL = ArrowKtTest::class.java.classLoader.getResource(resourcePath)!!

    fun testArrowFeather(name: String) = testResource("$name.feather")

    fun testArrowIPC(name: String) = testResource("$name.ipc")

    @Test
    fun testReadingFromFile() {
        val feather = testArrowFeather("data-arrow_2.0.0_uncompressed")
        val df = DataFrame.readArrowFeather(feather)
        val a by columnOf("one")
        val b by columnOf(2.0)
        val c by listOf(
            mapOf(
                "c1" to Text("inner"),
                "c2" to 4.0,
                "c3" to 50.0,
            ) as Map<String, Any?>
        ).toColumn()
        val d by columnOf("four")
        val expected = dataFrameOf(a, b, c, d)
        df shouldBe expected
    }

    @Test
    fun testReadingAllTypesAsEstimated() {
        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(testArrowFeather("test.arrow"), NullabilityOptions.Infer),
            expectedNullable = false,
            hasNulls = false,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(testArrowIPC("test.arrow"), NullabilityOptions.Infer),
            expectedNullable = false,
            hasNulls = false,
        )

        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(testArrowFeather("test.arrow"), NullabilityOptions.Checking),
            expectedNullable = true,
            hasNulls = false,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(testArrowIPC("test.arrow"), NullabilityOptions.Checking),
            expectedNullable = true,
            hasNulls = false,
        )

        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(testArrowFeather("test.arrow"), NullabilityOptions.Widening),
            expectedNullable = true,
            hasNulls = false,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(testArrowIPC("test.arrow"), NullabilityOptions.Widening),
            expectedNullable = true,
            hasNulls = false,
        )
    }

    @Test
    fun testReadingAllTypesAsEstimatedWithNulls() {
        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(
                testArrowFeather("test-with-nulls.arrow"),
                NullabilityOptions.Infer,
            ),
            expectedNullable = true,
            hasNulls = true,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(
                testArrowIPC("test-with-nulls.arrow"),
                NullabilityOptions.Infer,
            ),
            expectedNullable = true,
            hasNulls = true,
        )

        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(
                testArrowFeather("test-with-nulls.arrow"),
                NullabilityOptions.Checking,
            ),
            expectedNullable = true,
            hasNulls = true,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(
                testArrowIPC("test-with-nulls.arrow"),
                NullabilityOptions.Checking,
            ),
            expectedNullable = true,
            hasNulls = true,
        )

        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(
                testArrowFeather("test-with-nulls.arrow"),
                NullabilityOptions.Widening,
            ),
            expectedNullable = true,
            hasNulls = true,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(
                testArrowIPC("test-with-nulls.arrow"),
                NullabilityOptions.Widening,
            ),
            expectedNullable = true,
            hasNulls = true,
        )
    }

    @Test
    fun testReadingAllTypesAsEstimatedNotNullable() {
        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(
                testArrowFeather("test-not-nullable.arrow"),
                NullabilityOptions.Infer,
            ),
            expectedNullable = false,
            hasNulls = false,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(
                testArrowIPC("test-not-nullable.arrow"),
                NullabilityOptions.Infer,
            ),
            expectedNullable = false,
            hasNulls = false,
        )

        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(
                testArrowFeather("test-not-nullable.arrow"),
                NullabilityOptions.Checking,
            ),
            expectedNullable = false,
            hasNulls = false,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(
                testArrowIPC("test-not-nullable.arrow"),
                NullabilityOptions.Checking,
            ),
            expectedNullable = false,
            hasNulls = false,
        )

        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(
                testArrowFeather("test-not-nullable.arrow"),
                NullabilityOptions.Widening,
            ),
            expectedNullable = false,
            hasNulls = false,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(
                testArrowIPC("test-not-nullable.arrow"),
                NullabilityOptions.Widening,
            ),
            expectedNullable = false,
            hasNulls = false,
        )
    }

    @Test
    fun testReadingAllTypesAsEstimatedNotNullableWithNulls() {
        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(
                testArrowFeather("test-illegal.arrow"),
                NullabilityOptions.Infer,
            ),
            expectedNullable = true,
            hasNulls = true,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(
                testArrowIPC("test-illegal.arrow"),
                NullabilityOptions.Infer,
            ),
            expectedNullable = true,
            hasNulls = true
        )

        shouldThrow<IllegalArgumentException> {
            assertEstimations(
                exampleFrame = DataFrame.readArrowFeather(
                    testArrowFeather("test-illegal.arrow"),
                    NullabilityOptions.Checking,
                ),
                expectedNullable = false,
                hasNulls = true,
            )
        }
        shouldThrow<IllegalArgumentException> {
            assertEstimations(
                exampleFrame = DataFrame.readArrowIPC(
                    testArrowIPC("test-illegal.arrow"),
                    NullabilityOptions.Checking,
                ),
                expectedNullable = false,
                hasNulls = true,
            )
        }

        assertEstimations(
            exampleFrame = DataFrame.readArrowFeather(
                testArrowFeather("test-illegal.arrow"),
                NullabilityOptions.Widening,
            ),
            expectedNullable = true,
            hasNulls = true,
        )
        assertEstimations(
            exampleFrame = DataFrame.readArrowIPC(
                testArrowIPC("test-illegal.arrow"),
                NullabilityOptions.Widening,
            ),
            expectedNullable = true,
            hasNulls = true,
        )
    }

    @Test
    fun testWritingGeneral() {
        fun assertEstimation(citiesDeserialized: DataFrame<*>) {
            citiesDeserialized["name"] shouldBe citiesExampleFrame["name"]
            citiesDeserialized["affiliation"] shouldBe citiesExampleFrame["affiliation"]
            citiesDeserialized["is_capital"] shouldBe citiesExampleFrame["is_capital"]
            citiesDeserialized["population"] shouldBe citiesExampleFrame["population"]
            citiesDeserialized["area"] shouldBe citiesExampleFrame["area"]
            citiesDeserialized["settled"].type() shouldBe typeOf<LocalDate>() // cities["settled"].type() refers to FlexibleTypeImpl(LocalDate..LocalDate?) and does not match typeOf<LocalDate>()
            citiesDeserialized["settled"].values() shouldBe citiesExampleFrame["settled"].values()
            citiesDeserialized["page_in_wiki"].type() shouldBe typeOf<String>() // cities["page_in_wiki"].type() is URI, not supported by Arrow directly
            citiesDeserialized["page_in_wiki"].values() shouldBe
                citiesExampleFrame["page_in_wiki"].values().map { it.toString() }
        }

        val testFile = File.createTempFile("cities", "arrow")
        citiesExampleFrame.writeArrowFeather(testFile)
        assertEstimation(DataFrame.readArrowFeather(testFile))

        val testByteArray = citiesExampleFrame.saveArrowIPCToByteArray()
        assertEstimation(DataFrame.readArrowIPC(testByteArray))
    }

    @Test
    fun testWritingBySchema() {
        val testFile = File.createTempFile("cities", "arrow")
        citiesExampleFrame.arrowWriter(Schema.fromJSON(citiesExampleSchema)).use { it.writeArrowFeather(testFile) }
        val citiesDeserialized = DataFrame.readArrowFeather(testFile, NullabilityOptions.Checking)
        citiesDeserialized["population"].type() shouldBe typeOf<Long?>()
        citiesDeserialized["area"].type() shouldBe typeOf<Float>()
        citiesDeserialized["settled"].type() shouldBe typeOf<LocalDateTime>()
        shouldThrow<IllegalArgumentException> { citiesDeserialized["page_in_wiki"] }
        citiesDeserialized["film_in_youtube"] shouldBe
            DataColumn.createValueColumn(
                name = "film_in_youtube",
                values = arrayOfNulls<String>(citiesExampleFrame.rowsCount()).asList(),
            )
    }

    @Test
    fun testWidening() {
        val warnings = ArrayList<ConvertingMismatch>()
        val testRestrictWidening = citiesExampleFrame.arrowWriter(
            Schema.fromJSON(citiesExampleSchema),
            ArrowWriter.Mode.STRICT
        ) { warning -> warnings.add(warning) }.use { it.saveArrowFeatherToByteArray() }
        warnings.shouldContain(ConvertingMismatch.WideningMismatch.RejectedColumn("page_in_wiki"))
        shouldThrow<IllegalArgumentException> { DataFrame.readArrowFeather(testRestrictWidening)["page_in_wiki"] }

        val testAllowWidening = citiesExampleFrame.arrowWriter(
            Schema.fromJSON(citiesExampleSchema),
            ArrowWriter.Mode(
                restrictWidening = false,
                restrictNarrowing = true,
                strictType = true,
                strictNullable = true,
            )
        ).use { it.saveArrowFeatherToByteArray() }
        DataFrame.readArrowFeather(testAllowWidening)["page_in_wiki"].values() shouldBe
            citiesExampleFrame["page_in_wiki"]
                .values()
                .map { it.toString() }
    }

    @Test
    fun testNarrowing() {
        val frameWithoutRequiredField = citiesExampleFrame.copy().remove("settled")

        frameWithoutRequiredField.arrowWriter(
            Schema.fromJSON(citiesExampleSchema),
            ArrowWriter.Mode.STRICT
        ).use {
            shouldThrow<ConvertingException> { it.saveArrowFeatherToByteArray() }
        }

        val warnings = ArrayList<ConvertingMismatch>()
        val testAllowNarrowing = frameWithoutRequiredField.arrowWriter(
            targetSchema = Schema.fromJSON(citiesExampleSchema),
            mode = ArrowWriter.Mode(
                restrictWidening = true,
                restrictNarrowing = false,
                strictType = true,
                strictNullable = true
            )
        ) { warning -> warnings.add(warning) }.use { it.saveArrowFeatherToByteArray() }
        warnings.shouldContain(ConvertingMismatch.NarrowingMismatch.NotPresentedColumnIgnored("settled"))
        shouldThrow<IllegalArgumentException> { DataFrame.readArrowFeather(testAllowNarrowing)["settled"] }
    }

    @Test
    fun testStrictType() {
        val frameRenaming = citiesExampleFrame.copy().remove("settled")
        val frameWithIncompatibleField =
            frameRenaming.add(
                frameRenaming["is_capital"].map { value -> value ?: false }
                    .rename("settled")
                    .convertToBoolean()
            )

        frameWithIncompatibleField.arrowWriter(
            Schema.fromJSON(citiesExampleSchema),
            ArrowWriter.Mode.STRICT
        ).use {
            shouldThrow<ConvertingException> { it.saveArrowFeatherToByteArray() }
        }

        val warnings = ArrayList<ConvertingMismatch>()
        val testLoyalType = frameWithIncompatibleField.arrowWriter(
            Schema.fromJSON(citiesExampleSchema),
            ArrowWriter.Mode(
                restrictWidening = true,
                restrictNarrowing = true,
                strictType = false,
                strictNullable = true
            )
        ) { warning -> warnings.add(warning) }.use { it.saveArrowFeatherToByteArray() }
        warnings.map { it.toString() }.shouldContain(
            ConvertingMismatch.TypeConversionNotFound.ConversionNotFoundIgnored(
                "settled",
                TypeConverterNotFoundException(
                    typeOf<Boolean>(),
                    typeOf<kotlinx.datetime.LocalDateTime?>(),
                    pathOf("settled")
                )
            ).toString()
        )
        DataFrame.readArrowFeather(testLoyalType)["settled"].type() shouldBe typeOf<Boolean>()
    }

    @Test
    fun testStrictNullable() {
        val frameRenaming = citiesExampleFrame.copy().remove("settled")
        val frameWithNulls = frameRenaming.add(
            DataColumn.createValueColumn(
                "settled",
                arrayOfNulls<LocalDate>(frameRenaming.rowsCount()).asList(),
            )
        )

        frameWithNulls.arrowWriter(
            Schema.fromJSON(citiesExampleSchema),
            ArrowWriter.Mode.STRICT
        ).use {
            shouldThrow<ConvertingException> { it.saveArrowFeatherToByteArray() }
        }

        val warnings = ArrayList<ConvertingMismatch>()
        val testLoyalNullable = frameWithNulls.arrowWriter(
            Schema.fromJSON(citiesExampleSchema),
            ArrowWriter.Mode(
                restrictWidening = true,
                restrictNarrowing = true,
                strictType = true,
                strictNullable = false,
            )
        ) { warning -> warnings.add(warning) }.use { it.saveArrowFeatherToByteArray() }
        warnings.shouldContain(ConvertingMismatch.NullableMismatch.NullValueIgnored("settled", 0))
        DataFrame.readArrowFeather(testLoyalNullable)["settled"].type() shouldBe typeOf<LocalDateTime?>()
        DataFrame.readArrowFeather(testLoyalNullable)["settled"].values() shouldBe arrayOfNulls<LocalDate>(frameRenaming.rowsCount()).asList()
    }

    @Test
    fun testParsing() {
        val columnStringDot = columnOf("12.345", "67.890")
        val columnStringComma = columnOf("12,345", "67,890")
        val frameString = dataFrameOf("columnDot", "columnComma")(columnStringDot, columnStringComma)
        val columnDoubleFraction = columnOf(12.345, 67.890)
        val columnDoubleRound = columnOf(12345.0, 67890.0)
        val targetType = FieldType.notNullable(ArrowType.FloatingPoint(FloatingPointPrecision.DOUBLE))
        val targetSchema = Schema(
            listOf(
                Field("columnDot", targetType, emptyList()),
                Field("columnComma", targetType, emptyList()),
            )
        )

        val currentLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("en-US"))
            val serializedAsUs = frameString.arrowWriter(targetSchema).saveArrowFeatherToByteArray()
            DataFrame.readArrowFeather(serializedAsUs) shouldBe dataFrameOf("columnDot", "columnComma")(
                columnDoubleFraction,
                columnDoubleRound
            )
            Locale.setDefault(Locale.forLanguageTag("ru-RU"))
            val serializedAsRu = frameString.arrowWriter(targetSchema).saveArrowFeatherToByteArray()
            DataFrame.readArrowFeather(serializedAsRu) shouldBe dataFrameOf("columnDot", "columnComma")(
                columnDoubleFraction,
                columnDoubleFraction
            )
        } finally {
            Locale.setDefault(currentLocale)
        }
    }

    @Test
    fun testBigStringColumn() {
        val dataFrame = dataFrameOf(bigStringColumn)
        val data = dataFrame.saveArrowFeatherToByteArray()
        DataFrame.readArrowFeather(data) shouldBe dataFrame
    }

    @Test
    fun testTimeStamp(){
        val dates = listOf(
            LocalDateTime.of(2023, 11, 23, 9, 30, 25),
            LocalDateTime.of(2015, 5, 25, 14, 20, 13),
            LocalDateTime.of(2013, 6, 19, 11, 20, 13)
        )

        val dataFrame = dataFrameOf(
            "ts_nano" to dates,
            "ts_micro" to dates,
            "ts_milli" to dates,
            "ts_sec" to dates
        )

        DataFrame.readArrowFeather(writeArrowTimestamp(dates)) shouldBe dataFrame
        DataFrame.readArrowIPC(writeArrowTimestamp(dates, true)) shouldBe dataFrame
    }

    private fun writeArrowTimestamp(dates: List<LocalDateTime>, streaming: Boolean = false): ByteArray {
        RootAllocator().use { allocator ->
            val timeStampMilli = Field(
                "ts_milli",
                FieldType.nullable(ArrowType.Timestamp(TimeUnit.MILLISECOND, null)),
                null
            )

            val timeStampMicro = Field(
                "ts_micro",
                FieldType.nullable(ArrowType.Timestamp(TimeUnit.MICROSECOND, null)),
                null
            )

            val timeStampNano = Field(
                "ts_nano",
                FieldType.nullable(ArrowType.Timestamp(TimeUnit.NANOSECOND, null)),
                null
            )

            val timeStampSec = Field(
                "ts_sec",
                FieldType.nullable(ArrowType.Timestamp(TimeUnit.SECOND, null)),
                null
            )
            val schemaTimeStamp = Schema(
                listOf(timeStampNano, timeStampMicro, timeStampMilli, timeStampSec)
            )
            VectorSchemaRoot.create(schemaTimeStamp, allocator).use { vectorSchemaRoot ->
                val timeStampMilliVector = vectorSchemaRoot.getVector("ts_milli") as TimeStampMilliVector
                val timeStampNanoVector = vectorSchemaRoot.getVector("ts_nano") as TimeStampNanoVector
                val timeStampMicroVector = vectorSchemaRoot.getVector("ts_micro") as TimeStampMicroVector
                val timeStampSecVector = vectorSchemaRoot.getVector("ts_sec") as TimeStampSecVector
                timeStampMilliVector.allocateNew(dates.size)
                timeStampNanoVector.allocateNew(dates.size)
                timeStampMicroVector.allocateNew(dates.size)
                timeStampSecVector.allocateNew(dates.size)

                dates.forEachIndexed { index, localDateTime ->
                    val instant = localDateTime.toInstant(ZoneOffset.UTC)
                    timeStampNanoVector[index] = instant.toEpochMilli() * 1_000_000L + instant.nano
                    timeStampMicroVector[index] = instant.toEpochMilli() * 1_000L
                    timeStampMilliVector[index] = instant.toEpochMilli()
                    timeStampSecVector[index] = instant.toEpochMilli() / 1_000L
                }
                vectorSchemaRoot.setRowCount(dates.size)
                val bos = ByteArrayOutputStream()
                bos.use { out ->
                    val arrowWriter = if (streaming) {
                        ArrowStreamWriter(vectorSchemaRoot, null, Channels.newChannel(out))
                    } else {
                        ArrowFileWriter(vectorSchemaRoot, null, Channels.newChannel(out))
                    }
                    arrowWriter.use { writer ->
                        writer.start()
                        writer.writeBatch()
                    }
                }
                return bos.toByteArray()
            }
        }
    }
}
