package org.jetbrains.dataframe

import org.jetbrains.dataframe.columns.*
import org.jetbrains.dataframe.impl.catchSilent
import org.jetbrains.dataframe.impl.columns.DataColumnInternal
import org.jetbrains.dataframe.impl.columns.toColumns
import org.jetbrains.dataframe.impl.columns.typed
import org.jetbrains.dataframe.io.toDataFrame
import java.math.BigDecimal
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.withNullability

public fun <T, C> DataFrame<T>.convert(selector: ColumnsSelector<T, C>): ConvertClause<T, C> = ConvertClause(this, selector)
public fun <T, C> DataFrame<T>.convert(vararg columns: KProperty<C>): ConvertClause<T, C> = convert { columns.toColumns() }
public fun <T> DataFrame<T>.convert(vararg columns: String): ConvertClause<T, Any?> = convert { columns.toColumns() }
public fun <T, C> DataFrame<T>.convert(vararg columns: ColumnReference<C>): ConvertClause<T, C> = convert { columns.toColumns() }

public data class ConvertClause<T, C>(val df: DataFrame<T>, val selector: ColumnsSelector<T, C>) {
    public inline fun <reified D> to(): DataFrame<T> = to(getType<D>())
}

public fun <T> ConvertClause<T, *>.to(type: KType): DataFrame<T> = to { it.convertTo(type) }

public inline fun <T, C, reified R> ConvertClause<T, C>.with(crossinline rowConverter: DataRow<T>.(C) -> R): DataFrame<T> =
    to { col -> df.newColumn(col.name()) { rowConverter(it, it[col]) } }

public fun <T, C> ConvertClause<T, C>.to(columnConverter: (DataColumn<C>) -> AnyCol): DataFrame<T> =
    df.replace(selector).with { columnConverter(it) }

public inline fun <reified C> AnyCol.convertTo(): DataColumn<C> = convertTo(getType<C>()) as DataColumn<C>

public fun AnyCol.convertToDateTime(): DataColumn<LocalDateTime> = convertTo()
public fun AnyCol.convertToDate(): DataColumn<LocalDate> = convertTo()
public fun AnyCol.convertToTime(): DataColumn<LocalTime> = convertTo()
public fun AnyCol.convertToInt(): DataColumn<Int> = convertTo()
public fun AnyCol.convertToString(): DataColumn<String> = convertTo()
public fun AnyCol.convertToDouble(): DataColumn<Double> = convertTo()

internal val convertersCache = mutableMapOf<Pair<KType, KType>, TypeConverter?>()

internal fun getConverter(from: KType, to: KType): TypeConverter? = convertersCache.getOrPut(from to to) { createConverter(from, to) }

public fun AnyCol.convertTo(newType: KType): AnyCol {
    val from = type
    val targetType = newType.withNullability(hasNulls)
    return when {
        from == newType -> this
        from.isSubtypeOf(newType) -> (this as DataColumnInternal<*>).changeType(targetType)
        else -> when (val converter = getConverter(from, newType)) {
            null -> when (from.classifier) {
                Any::class, Number::class, java.io.Serializable::class -> {
                    val values = values.map {
                        if (it == null) null else {
                            val clazz = it.javaClass.kotlin
                            val type = clazz.createStarProjectedType(false)
                            val conv = getConverter(type, newType) ?: error("Can't find converter from '$type' to '$newType'")
                            conv(it) ?: error("Can't convert '$it' to '$newType'")
                        }
                    }
                    DataColumn.create(name, values, targetType)
                }
                else -> error("Can't find converter from $from to $newType")
            }
            else -> {
                val values = values.map {
                    if (it == null) null
                    else converter(it) ?: error("Can't convert '$it' to '$newType'")
                }
                DataColumn.create(name, values, targetType)
            }
        }
    }
}

internal typealias TypeConverter = (Any) -> Any?

internal inline fun <T> convert(crossinline converter: (T) -> Any?): TypeConverter = { converter(it as T) }

internal fun createConverter(from: KType, to: KType): TypeConverter? {
    if (from.arguments.isNotEmpty() || to.arguments.isNotEmpty()) return null
    if (from.isMarkedNullable) {
        val res = createConverter(from.withNullability(false), to) ?: return null
        return { res(it) }
    }
    val fromClass = from.classifier as KClass<*>
    val toClass = to.classifier as KClass<*>

    if (fromClass == toClass) return { it }

    return when {
        fromClass == String::class -> Parsers[to]?.toConverter()
        toClass == String::class -> convert<Any> { it.toString() }
        fromClass == Number::class -> when (toClass) {
            Double::class -> convert<Number> { it.toDouble() }
            Int::class -> convert<Number> { it.toInt() }
            Float::class -> convert<Number> { it.toFloat() }
            Byte::class -> convert<Number> { it.toByte() }
            Short::class -> convert<Number> { it.toShort() }
            Long::class -> convert<Number> { it.toLong() }
            else -> null
        }
        fromClass == Int::class -> when (toClass) {
            Double::class -> convert<Int> { it.toDouble() }
            Float::class -> convert<Int> { it.toFloat() }
            Byte::class -> convert<Int> { it.toByte() }
            Short::class -> convert<Int> { it.toShort() }
            Long::class -> convert<Int> { it.toLong() }
            BigDecimal::class -> convert<Int> { it.toBigDecimal() }
            LocalDateTime::class -> convert<Int> { it.toLong().toLocalDateTime(defaultTimeZone) }
            LocalDate::class -> convert<Int> { it.toLong().toLocalDate(defaultTimeZone) }
            LocalTime::class -> convert<Int> { it.toLong().toLocalTime(defaultTimeZone) }
            else -> null
        }
        fromClass == Double::class -> when (toClass) {
            Int::class -> convert<Double> { it.toInt() }
            Float::class -> convert<Double> { it.toFloat() }
            Long::class -> convert<Double> { it.toLong() }
            BigDecimal::class -> convert<Double> { it.toBigDecimal() }
            else -> null
        }
        fromClass == Long::class -> when (toClass) {
            Double::class -> convert<Long> { it.toDouble() }
            Float::class -> convert<Long> { it.toFloat() }
            Byte::class -> convert<Long> { it.toByte() }
            Short::class -> convert<Long> { it.toShort() }
            Int::class -> convert<Long> { it.toInt() }
            BigDecimal::class -> convert<Long> { it.toBigDecimal() }
            LocalDateTime::class -> convert<Long> { it.toLocalDateTime(defaultTimeZone) }
            LocalDate::class -> convert<Long> { it.toLocalDate(defaultTimeZone) }
            LocalTime::class -> convert<Long> { it.toLocalTime(defaultTimeZone) }
            else -> null
        }
        fromClass == Float::class -> when (toClass) {
            Double::class -> convert<Float> { it.toDouble() }
            Long::class -> convert<Float> { it.toLong() }
            Int::class -> convert<Float> { it.toInt() }
            BigDecimal::class -> convert<Float> { it.toBigDecimal() }
            else -> null
        }
        else -> null
    }
}

internal fun Long.toLocalDateTime(zone: ZoneId) = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zone)
internal fun Long.toLocalDate(zone: ZoneId) = toLocalDateTime(zone).toLocalDate()
internal fun Long.toLocalTime(zone: ZoneId) = toLocalDateTime(zone).toLocalTime()

internal val defaultTimeZone = TimeZone.getDefault().toZoneId()

public fun <T> ConvertClause<T, *>.toInt(): DataFrame<T> = to<Int>()
public fun <T> ConvertClause<T, *>.toDouble(): DataFrame<T> = to<Double>()
public fun <T> ConvertClause<T, *>.toFloat(): DataFrame<T> = to<Float>()
public fun <T> ConvertClause<T, *>.toStr(): DataFrame<T> = to<String>()
public fun <T> ConvertClause<T, *>.toLong(): DataFrame<T> = to<Long>()
public fun <T> ConvertClause<T, *>.toBigDecimal(): DataFrame<T> = to<BigDecimal>()

public fun <T> ConvertClause<T, *>.toDate(zone: ZoneId = defaultTimeZone): DataFrame<T> = to { it.toLocalDate(zone) }
public fun <T> ConvertClause<T, *>.toTime(zone: ZoneId = defaultTimeZone): DataFrame<T> = to { it.toLocalTime(zone) }
public fun <T> ConvertClause<T, *>.toDateTime(zone: ZoneId = defaultTimeZone): DataFrame<T> = to { it.toLocalDateTime(zone) }

public fun <T, C> ConvertClause<T, Many<Many<C>>>.toDataFrames(containsColumns: Boolean = false): DataFrame<T> =
    to { it.toDataFrames(containsColumns) }

internal class StringParser<T : Any>(val type: KType, val parse: (String) -> T?) {
    fun toConverter(): TypeConverter = { parse(it as String) }
}

public fun AnyCol.toLocalDate(zone: ZoneId = defaultTimeZone): DataColumn<LocalDate> = when (typeClass) {
    Long::class -> typed<Long>().map { it.toLocalDate(zone) }
    Int::class -> typed<Int>().map { it.toLong().toLocalDate(zone) }
    else -> convertTo(getType<LocalDate>()).typed()
}

public fun AnyCol.toLocalDateTime(zone: ZoneId = defaultTimeZone): DataColumn<LocalDateTime> = when (typeClass) {
    Long::class -> typed<Long>().map { it.toLocalDateTime(zone) }
    Int::class -> typed<Int>().map { it.toLong().toLocalDateTime(zone) }
    else -> convertTo(getType<LocalDateTime>()).typed()
}

public fun AnyCol.toLocalTime(zone: ZoneId = defaultTimeZone): DataColumn<LocalTime> = when (typeClass) {
    Long::class -> typed<Long>().map { it.toLocalDateTime(zone).toLocalTime() }
    Int::class -> typed<Int>().map { it.toLong().toLocalDateTime(zone).toLocalTime() }
    else -> convertTo(getType<LocalTime>()).typed()
}

public fun <T> DataColumn<Many<Many<T>>>.toDataFrames(containsColumns: Boolean = false): DataColumn<AnyFrame> =
    map { it.toDataFrame(containsColumns) }

internal object Parsers : DataFrameParserOptions {

    private val formatterDateTime = DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral(' ')
        .append(DateTimeFormatter.ISO_LOCAL_TIME).toFormatter()

    private val formatters = mutableListOf(
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        formatterDateTime
    )

    override fun addDateTimeFormat(format: String) { formatters.add(DateTimeFormatter.ofPattern(format)) }

    private fun String.toLocalDateTimeOrNull(): LocalDateTime? {
        for (format in formatters) {
            catchSilent { LocalDateTime.parse(this, format) }?.let { return it }
        }
        return null
    }

    private fun String.toBooleanOrNull() =
        when (uppercase(Locale.getDefault())) {
            "T" -> true
            "TRUE" -> true
            "YES" -> true
            "F" -> false
            "FALSE" -> false
            "NO" -> false
            else -> null
        }

    private fun String.toLocalDateOrNull(): LocalDate? =
        try {
            LocalDate.parse(this)
        } catch (_: Throwable) {
            null
        }

    private fun String.toLocalTimeOrNull(): LocalTime? =
        try {
            LocalTime.parse(this)
        } catch (_: Throwable) {
            null
        }

    private fun String.parseDouble() =
        when (uppercase(Locale.getDefault())) {
            "NAN" -> Double.NaN
            "INF" -> Double.POSITIVE_INFINITY
            "-INF" -> Double.NEGATIVE_INFINITY
            "INFINITY" -> Double.POSITIVE_INFINITY
            "-INFINITY" -> Double.NEGATIVE_INFINITY
            else -> toDoubleOrNull()
        }

    inline fun <reified T : Any> stringParser(noinline body: (String) -> T?) = StringParser(getType<T>(), body)

    val All = listOf(
        stringParser { it.toIntOrNull() },
        stringParser { it.toLongOrNull() },
        stringParser { it.parseDouble() },
        stringParser { it.toBooleanOrNull() },
        stringParser { it.toBigDecimalOrNull() },
        stringParser { it.toLocalDateOrNull() },
        stringParser { it.toLocalTimeOrNull() },
        stringParser { it.toLocalDateTimeOrNull() }
    )

    private val parsersMap = All.associateBy { it.type }

    val size: Int = All.size

    operator fun get(index: Int): StringParser<*> = All[index]

    operator fun get(type: KType): StringParser<*>? = parsersMap.get(type)

    operator fun <T : Any> get(type: KClass<T>): StringParser<*>? = parsersMap.get(type.createStarProjectedType(false))

    inline fun <reified T : Any> get(): StringParser<T>? = get(getType<T>()) as? StringParser<T>
}

internal fun <T : Any> DataColumn<String?>.parse(parser: StringParser<T>): DataColumn<T?> {
    val parsedValues = values.map {
        it?.let {
            parser.parse(it) ?: throw Exception("Couldn't parse '$it' to type ${parser.type}")
        }
    }
    return DataColumn.create(name(), parsedValues, parser.type.withNullability(hasNulls)) as DataColumn<T?>
}
