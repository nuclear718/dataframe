package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.AnyCol
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.Column
import org.jetbrains.kotlinx.dataframe.ColumnSelector
import org.jetbrains.kotlinx.dataframe.ColumnsSelector
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.Many
import org.jetbrains.kotlinx.dataframe.RowColumnExpression
import org.jetbrains.kotlinx.dataframe.RowValueExpression
import org.jetbrains.kotlinx.dataframe.RowValueFilter
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnReference
import org.jetbrains.kotlinx.dataframe.columns.ColumnWithPath
import org.jetbrains.kotlinx.dataframe.emptyMany
import org.jetbrains.kotlinx.dataframe.impl.api.Parsers
import org.jetbrains.kotlinx.dataframe.impl.api.convertRowCellImpl
import org.jetbrains.kotlinx.dataframe.impl.api.convertRowColumnImpl
import org.jetbrains.kotlinx.dataframe.impl.api.convertToTypeImpl
import org.jetbrains.kotlinx.dataframe.impl.api.defaultTimeZone
import org.jetbrains.kotlinx.dataframe.impl.api.explodeImpl
import org.jetbrains.kotlinx.dataframe.impl.api.mergeRowsImpl
import org.jetbrains.kotlinx.dataframe.impl.api.splitDefault
import org.jetbrains.kotlinx.dataframe.impl.api.splitImpl
import org.jetbrains.kotlinx.dataframe.impl.api.toLocalDate
import org.jetbrains.kotlinx.dataframe.impl.api.toLocalDateTime
import org.jetbrains.kotlinx.dataframe.impl.api.tryParseImpl
import org.jetbrains.kotlinx.dataframe.impl.api.updateImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.toColumnSet
import org.jetbrains.kotlinx.dataframe.impl.columns.toColumns
import org.jetbrains.kotlinx.dataframe.impl.createTypeWithArgument
import org.jetbrains.kotlinx.dataframe.impl.getType
import org.jetbrains.kotlinx.dataframe.impl.headPlusArray
import org.jetbrains.kotlinx.dataframe.index
import org.jetbrains.kotlinx.dataframe.io.toDataFrame
import org.jetbrains.kotlinx.dataframe.pathOf
import org.jetbrains.kotlinx.dataframe.typeClass
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.reflect.KProperty
import kotlin.reflect.KType

// region update

public fun <T, C> DataFrame<T>.update(columns: ColumnsSelector<T, C>): UpdateClause<T, C> = UpdateClause(this, null, columns)
public fun <T, C> DataFrame<T>.update(columns: Iterable<ColumnReference<C>>): UpdateClause<T, C> = update { columns.toColumnSet() }
public fun <T> DataFrame<T>.update(vararg columns: String): UpdateClause<T, Any?> = update { columns.toColumns() }
public fun <T, C> DataFrame<T>.update(vararg columns: KProperty<C>): UpdateClause<T, C> = update { columns.toColumns() }
public fun <T, C> DataFrame<T>.update(vararg columns: ColumnReference<C>): UpdateClause<T, C> = update { columns.toColumns() }

public data class UpdateClause<T, C>(
    val df: DataFrame<T>,
    val filter: RowValueFilter<T, C>?,
    val columns: ColumnsSelector<T, C>
) {
    public fun <R : C> cast(): UpdateClause<T, R> = UpdateClause(df, filter as RowValueFilter<T, R>?, columns as ColumnsSelector<T, R>)
}

public fun <T, C> UpdateClause<T, C>.where(predicate: RowValueFilter<T, C>): UpdateClause<T, C> = copy(filter = filter and predicate)

public fun <T, C> UpdateClause<T, C>.at(rowIndices: Collection<Int>): UpdateClause<T, C> = where { index in rowIndices }
public fun <T, C> UpdateClause<T, C>.at(vararg rowIndices: Int): UpdateClause<T, C> = at(rowIndices.toSet())
public fun <T, C> UpdateClause<T, C>.at(rowRange: IntRange): UpdateClause<T, C> = where { index in rowRange }

public infix fun <T, C> UpdateClause<T, C>.withRowCol(expression: RowColumnExpression<T, C, C>): DataFrame<T> = updateImpl { row, column, _ -> expression(row, column) }

public infix fun <T, C> UpdateClause<T, C>.with(expression: RowValueExpression<T, C, C>): DataFrame<T> = withExpression(expression)

public fun <T, C> UpdateClause<T, C>.asNullable(): UpdateClause<T, C?> = this as UpdateClause<T, C?>

public fun <T, C> UpdateClause<T, C>.withExpression(expression: RowValueExpression<T, C, C>): DataFrame<T> = updateImpl { row, _, value ->
    expression(row, value)
}

internal infix fun <T, C> RowValueFilter<T, C>?.and(other: RowValueFilter<T, C>): RowValueFilter<T, C> {
    if (this == null) return other
    val thisExp = this
    return { thisExp(this, it) && other(this, it) }
}

public fun <T, C> UpdateClause<T, C?>.notNull(): UpdateClause<T, C> = copy(filter = filter and { it != null }) as UpdateClause<T, C>

public fun <T, C> UpdateClause<T, C?>.notNull(expression: RowValueExpression<T, C, C>): DataFrame<T> = notNull().updateImpl { row, column, value ->
    expression(row, value)
}

public fun <T, C> DataFrame<T>.update(
    firstCol: ColumnReference<C>,
    vararg cols: ColumnReference<C>,
    expression: RowValueExpression<T, C, C>
): DataFrame<T> =
    update(*headPlusArray(firstCol, cols)).with(expression)

public fun <T, C> DataFrame<T>.update(
    firstCol: KProperty<C>,
    vararg cols: KProperty<C>,
    expression: RowValueExpression<T, C, C>
): DataFrame<T> =
    update(*headPlusArray(firstCol, cols)).with(expression)

public fun <T> DataFrame<T>.update(
    firstCol: String,
    vararg cols: String,
    expression: RowValueExpression<T, Any?, Any?>
): DataFrame<T> =
    update(*headPlusArray(firstCol, cols)).withExpression(expression)

public fun <T, C> UpdateClause<T, C>.withNull(): DataFrame<T> = asNullable().withValue(null)

public infix fun <T, C> UpdateClause<T, C>.withValue(value: C): DataFrame<T> = withExpression { value }

// endregion

// region convert

public fun <T, C> DataFrame<T>.convert(columns: ColumnsSelector<T, C>): ConvertClause<T, C> = ConvertClause(this, columns)
public fun <T, C> DataFrame<T>.convert(vararg columns: KProperty<C>): ConvertClause<T, C> = convert { columns.toColumns() }
public fun <T> DataFrame<T>.convert(vararg columns: String): ConvertClause<T, Any?> = convert { columns.toColumns() }
public fun <T, C> DataFrame<T>.convert(vararg columns: ColumnReference<C>): ConvertClause<T, C> = convert { columns.toColumns() }

public inline fun <T, C, reified R> DataFrame<T>.convert(
    firstCol: ColumnReference<C>,
    vararg cols: ColumnReference<C>,
    noinline expression: RowValueExpression<T, C, R>
): DataFrame<T> =
    convert(*headPlusArray(firstCol, cols)).with(expression)

public inline fun <T, C, reified R> DataFrame<T>.convert(
    firstCol: KProperty<C>,
    vararg cols: KProperty<C>,
    noinline expression: RowValueExpression<T, C, R>
): DataFrame<T> =
    convert(*headPlusArray(firstCol, cols)).with(expression)

public inline fun <T, reified R> DataFrame<T>.convert(
    firstCol: String,
    vararg cols: String,
    noinline expression: RowValueExpression<T, Any?, R>
): DataFrame<T> =
    convert(*headPlusArray(firstCol, cols)).with(expression)

public inline fun <T, C, reified R> ConvertClause<T, C?>.notNull(crossinline expression: RowValueExpression<T, C, R>): DataFrame<T> = with {
    if (it == null) null
    else expression(this, it)
}

public data class ConvertClause<T, C>(val df: DataFrame<T>, val columns: ColumnsSelector<T, C>) {
    public fun <R> cast(): ConvertClause<T, R> = ConvertClause(df, columns as ColumnsSelector<T, R>)

    public inline fun <reified D> to(): DataFrame<T> = to(getType<D>())
}

public fun <T> ConvertClause<T, *>.to(type: KType): DataFrame<T> = to { it.convertTo(type) }

public inline fun <T, C, reified R> ConvertClause<T, C>.with(noinline rowConverter: RowValueExpression<T, C, R>): DataFrame<T> =
    convertRowCellImpl(getType<R>(), rowConverter)

public inline fun <T, C, reified R> ConvertClause<T, C>.withRowCol(noinline expression: RowColumnExpression<T, C, R>): DataFrame<T> =
    convertRowColumnImpl(getType<R>(), expression)

public fun <T, C> ConvertClause<T, C>.to(columnConverter: DataFrame<T>.(DataColumn<C>) -> AnyCol): DataFrame<T> =
    df.replace(columns).with { columnConverter(df, it) }

public inline fun <reified C> AnyCol.convertTo(): DataColumn<C> = convertTo(getType<C>()) as DataColumn<C>

public fun AnyCol.toDateTime(): DataColumn<LocalDateTime> = convertTo()
public fun AnyCol.toDate(): DataColumn<LocalDate> = convertTo()
public fun AnyCol.toTime(): DataColumn<LocalTime> = convertTo()
public fun AnyCol.toInt(): DataColumn<Int> = convertTo()
public fun AnyCol.toStr(): DataColumn<String> = convertTo()
public fun AnyCol.toDouble(): DataColumn<Double> = convertTo()

public fun AnyCol.convertTo(newType: KType): AnyCol = convertToTypeImpl(newType)

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

// endregion

// region parse

public val DataFrame.Companion.parser: DataFrameParserOptions get() = Parsers

public interface DataFrameParserOptions {

    public fun addDateTimeFormat(format: String)
}

public fun DataColumn<String?>.tryParse(): DataColumn<*> = tryParseImpl()

public fun <T> DataFrame<T>.parse(): DataFrame<T> = parse { dfs() }

public fun <T> DataFrame<T>.parse(columns: ColumnsSelector<T, Any?>): DataFrame<T> = convert(columns).to {
    when {
        it.isFrameColumn() -> it.castTo<AnyFrame?>().parse()
        it.typeClass == String::class -> it.castTo<String?>().tryParse()
        else -> it
    }
}

public fun DataColumn<String?>.parse(): DataColumn<*> = tryParse().also { if (it.typeClass == String::class) error("Can't guess column type") }

@JvmName("tryParseAnyFrame?")
public fun DataColumn<AnyFrame?>.parse(): DataColumn<AnyFrame?> = map { it?.parse() }

// endregion

// region split

public fun <T, C> DataFrame<T>.split(columns: ColumnsSelector<T, C?>): Split<T, C> =
    SplitClause(this, columns)

public fun <T> DataFrame<T>.split(column: String): Split<T, Any> = split { column.toColumnAccessor() }
public fun <T, C> DataFrame<T>.split(column: ColumnReference<C?>): Split<T, C> = split { column }
public fun <T, C> DataFrame<T>.split(column: KProperty<C?>): Split<T, C> = split { column.toColumnAccessor() }

public interface Split<out T, out C>

public fun <T, C> Split<T, C>.by(
    vararg delimiters: String,
    trim: Boolean = true,
    ignoreCase: Boolean = false,
    limit: Int = 0
): SplitWithTransform<T, C, String> = with {
    it.toString().split(*delimiters, ignoreCase = ignoreCase, limit = limit).let {
        if (trim) it.map { it.trim() }
        else it
    }
}

public typealias ColumnNamesGenerator<C> = ColumnWithPath<C>.(extraColumnIndex: Int) -> String

public interface SplitWithTransform<out T, out C, in R> {

    public fun intoRows(dropEmpty: Boolean = true): DataFrame<T>

    public fun inplace(): DataFrame<T>

    public fun inward(vararg names: String, extraNamesGenerator: ColumnNamesGenerator<C>? = null): DataFrame<T> = inward(names.toList(), extraNamesGenerator)

    public fun inward(names: Iterable<String>, extraNamesGenerator: ColumnNamesGenerator<C>? = null): DataFrame<T>
}

public class SplitClause<T, C>(
    public val df: DataFrame<T>,
    public val columns: ColumnsSelector<T, C?>
) : Split<T, C>

public inline fun <T, C, reified R> Split<T, C>.with(noinline splitter: (C) -> Iterable<R>): SplitWithTransform<T, C, R> = with(
    getType<R>(), splitter
)

@PublishedApi
internal fun <T, C, R> Split<T, C>.with(type: KType, splitter: (C) -> Iterable<R>): SplitWithTransform<T, C, R> {
    require(this is SplitClause<T, C>)
    return SplitClauseWithTransform(df, columns, false, type) {
        if (it == null) emptyMany() else splitter(it).toMany()
    }
}

public data class SplitClauseWithTransform<T, C, R>(
    val df: DataFrame<T>,
    val columns: ColumnsSelector<T, C?>,
    val inward: Boolean,
    val targetType: KType,
    val transform: (C) -> Iterable<R>
) : SplitWithTransform<T, C, R> {

    override fun intoRows(dropEmpty: Boolean): DataFrame<T> = df.explode(dropEmpty, columns)

    override fun inplace(): DataFrame<T> = df.convert(columns).convertRowCellImpl(Many::class.createTypeWithArgument(targetType)) { if (it == null) emptyMany() else transform(it).toMany() }

    override fun inward(names: Iterable<String>, extraNamesGenerator: ColumnNamesGenerator<C>?): DataFrame<T> = copy(inward = true).into(names.toList(), extraNamesGenerator)
}

public class FrameSplit<T, C>(
    public val df: DataFrame<T>,
    public val columns: ColumnSelector<T, DataFrame<C>?>
)

public fun <T, C, R> SplitWithTransform<T, C, R>.into(
    firstName: ColumnReference<*>,
    vararg otherNames: ColumnReference<*>
): DataFrame<T> =
    into(listOf(firstName.name()) + otherNames.map { it.name() })

public fun <T, C, R> SplitWithTransform<T, C, R>.intoMany(
    namesProvider: (ColumnWithPath<C>, numberOfNewColumns: Int) -> List<String>
): DataFrame<T> =
    splitImpl(this as SplitClauseWithTransform<T, C, R>, namesProvider)

public fun <T, C, R> SplitWithTransform<T, C, R>.into(
    vararg names: String,
    extraNamesGenerator: (ColumnWithPath<C>.(extraColumnIndex: Int) -> String)? = null
): DataFrame<T> = into(names.toList(), extraNamesGenerator)

public fun <T, C, R> SplitWithTransform<T, C, R>.into(
    names: List<String>,
    extraNamesGenerator: (ColumnWithPath<C>.(extraColumnIndex: Int) -> String)? = null
): DataFrame<T> = intoMany { col, numberOfNewCols ->
    if (extraNamesGenerator != null && names.size < numberOfNewCols) {
        names + (1..(numberOfNewCols - names.size)).map { extraNamesGenerator(col, it) }
    } else names
}

@JvmName("intoRowsTC")
public inline fun <T, C : Iterable<R>, reified R> Split<T, C>.intoRows(dropEmpty: Boolean = true): DataFrame<T> = with { it }.intoRows(dropEmpty)

@JvmName("intoRowsFrame")
public fun <T> Split<T, AnyFrame>.intoRows(dropEmpty: Boolean = true): DataFrame<T> = with { it.rows() }.intoRows(dropEmpty)

@JvmName("inplaceTC")
public inline fun <T, C : Iterable<R>, reified R> Split<T, C>.inplace(): DataFrame<T> = with { it }.inplace()

public inline fun <T, C : Iterable<R>, reified R> Split<T, C>.inward(
    vararg names: String,
    noinline extraNamesGenerator: ColumnNamesGenerator<C>? = null
): DataFrame<T> =
    with { it }.inward(names.toList(), extraNamesGenerator)

public inline fun <T, C : Iterable<R>, reified R> Split<T, C>.into(
    vararg names: String,
    noinline extraNamesGenerator: ColumnNamesGenerator<C>? = null
): DataFrame<T> =
    with { it }.into(names.toList(), extraNamesGenerator)

@JvmName("intoTC")
public fun <T> Split<T, String>.into(
    vararg names: String,
    extraNamesGenerator: (ColumnWithPath<String>.(extraColumnIndex: Int) -> String)? = null
): DataFrame<T> =
    with { it.splitDefault() }.into(names.toList(), extraNamesGenerator)

// endregion

// region merge

public class MergeClause<T, C, R>(
    public val df: DataFrame<T>,
    public val selector: ColumnsSelector<T, C>,
    public val transform: (Iterable<C>) -> R
)

public fun <T, C> DataFrame<T>.merge(selector: ColumnsSelector<T, C>): MergeClause<T, C, Iterable<C>> = MergeClause(this, selector, { it })

public inline fun <T, C, reified R> MergeClause<T, C, R>.into(columnName: String): DataFrame<T> = into(pathOf(columnName))

public inline fun <T, C, reified R> MergeClause<T, C, R>.into(columnPath: ColumnPath): DataFrame<T> {
    val grouped = df.move(selector).under(columnPath)
    val res = grouped.convert { getColumnGroup(columnPath) }.with {
        transform(it.values().toMany() as Iterable<C>)
    }
    return res
}

public fun <T, C, R> MergeClause<T, C, R>.asStrings(): MergeClause<T, C, String> = by(", ")
public fun <T, C, R> MergeClause<T, C, R>.by(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "..."
): MergeClause<T, C, String> =
    MergeClause(df, selector) { it.joinToString(separator = separator, prefix = prefix, postfix = postfix, limit = limit, truncated = truncated) }

public inline fun <T, C, R, reified V> MergeClause<T, C, R>.by(crossinline transform: (R) -> V): MergeClause<T, C, V> = MergeClause(df, selector) { transform(this@by.transform(it)) }

// endregion

// region explode

public fun <T> DataFrame<T>.explode(dropEmpty: Boolean = true): DataFrame<T> = explode(dropEmpty) { all() }
public fun <T> DataFrame<T>.explode(vararg columns: Column, dropEmpty: Boolean = true): DataFrame<T> = explode(dropEmpty) { columns.toColumns() }
public fun <T> DataFrame<T>.explode(vararg columns: String, dropEmpty: Boolean = true): DataFrame<T> = explode(dropEmpty) { columns.toColumns() }
public fun <T> DataFrame<T>.explode(dropEmpty: Boolean = true, selector: ColumnsSelector<T, *>): DataFrame<T> = explodeImpl(dropEmpty, selector)

// endregion

// region mergeRows

public fun <T> DataFrame<T>.mergeRows(vararg columns: String, dropNulls: Boolean = false): DataFrame<T> = mergeRows(dropNulls) { columns.toColumns() }
public fun <T> DataFrame<T>.mergeRows(vararg columns: Column, dropNulls: Boolean = false): DataFrame<T> = mergeRows(dropNulls) { columns.toColumns() }
public fun <T, C> DataFrame<T>.mergeRows(vararg columns: KProperty<C>, dropNulls: Boolean = false): DataFrame<T> = mergeRows(dropNulls) { columns.toColumns() }
public fun <T, C> DataFrame<T>.mergeRows(dropNulls: Boolean = false, columns: ColumnsSelector<T, C>): DataFrame<T> = mergeRowsImpl(dropNulls, columns)

// endregion
