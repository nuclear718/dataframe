package org.jetbrains.kotlinx.dataframe

import org.jetbrains.kotlinx.dataframe.api.asDataColumn
import org.jetbrains.kotlinx.dataframe.api.concat
import org.jetbrains.kotlinx.dataframe.api.schema
import org.jetbrains.kotlinx.dataframe.api.typed
import org.jetbrains.kotlinx.dataframe.columns.BaseColumn
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup
import org.jetbrains.kotlinx.dataframe.columns.ColumnKind
import org.jetbrains.kotlinx.dataframe.columns.ColumnResolutionContext
import org.jetbrains.kotlinx.dataframe.columns.ColumnWithPath
import org.jetbrains.kotlinx.dataframe.columns.FrameColumn
import org.jetbrains.kotlinx.dataframe.columns.ValueColumn
import org.jetbrains.kotlinx.dataframe.impl.anyNull
import org.jetbrains.kotlinx.dataframe.impl.columns.ColumnGroupImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.FrameColumnImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.ValueColumnImpl
import org.jetbrains.kotlinx.dataframe.impl.columns.addPath
import org.jetbrains.kotlinx.dataframe.impl.columns.guessColumnType
import org.jetbrains.kotlinx.dataframe.impl.columns.toColumnKind
import org.jetbrains.kotlinx.dataframe.impl.getType
import org.jetbrains.kotlinx.dataframe.impl.splitByIndices
import org.jetbrains.kotlinx.dataframe.schema.DataFrameSchema
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.withNullability

/**
 * Column with type, name/path and values
 * Base interface for [ValueColumn] and [FrameColumn], but not for [ColumnGroup]
 * All extension functions that clash with [DataFrame] API (such as filter, forEach, map etc.) should be defined for this interface
 *
 * Although [ColumnGroup] doesn't implement this interface, but [ColumnGroupImpl] does, so you can cast any actual instance of [ColumnGroup] to [DataColumn]
 */
public interface DataColumn<out T> : BaseColumn<T> {

    public companion object {

        public fun <T> createValueColumn(name: String, values: List<T>, type: KType, defaultValue: T? = null): ValueColumn<T> = ValueColumnImpl(values, name, type, defaultValue)

        public fun <T> createColumnGroup(name: String, df: DataFrame<T>): ColumnGroup<T> = ColumnGroupImpl(df, name)

        public fun <T> createFrameColumn(
            name: String,
            df: DataFrame<T>,
            startIndices: Iterable<Int>,
            emptyToNull: Boolean
        ): FrameColumn<T> =
            FrameColumnImpl(name, df.splitByIndices(startIndices.asSequence(), emptyToNull).toList(), emptyToNull, lazy { df.schema() })

        public fun <T> createFrameColumn(
            name: String,
            groups: List<DataFrame<T>?>,
            hasNulls: Boolean? = null,
            schema: Lazy<DataFrameSchema>? = null
        ): FrameColumn<T> = FrameColumnImpl(name, groups, hasNulls, schema)

        public fun <T> createWithTypeInference(name: String, values: List<T>): DataColumn<T> = guessColumnType(name, values)

        public inline fun <reified T> create(name: String, values: List<T>, checkActualNulls: Boolean = false): DataColumn<T> {
            val type = if (checkActualNulls) getType<T>().withNullability(values.anyNull()) else getType<T>()
            return when (type.toColumnKind()) {
                ColumnKind.Value -> createValueColumn(name, values, type)
                ColumnKind.Group -> createColumnGroup(name, (values as List<AnyRow?>).concat()).asDataColumn().typed()
                ColumnKind.Frame -> createFrameColumn(name, values as List<AnyFrame?>, hasNulls = type.isMarkedNullable).asDataColumn().typed()
            }
        }

        public fun empty(): AnyCol = createValueColumn("", emptyList<Unit>(), getType<Unit>())
    }

    public fun hasNulls(): Boolean = type().isMarkedNullable

    override fun distinct(): DataColumn<T>

    override fun slice(range: IntRange): DataColumn<T>

    override fun slice(indices: Iterable<Int>): DataColumn<T>

    override fun slice(mask: BooleanArray): DataColumn<T>

    override fun rename(newName: String): DataColumn<T>

    override fun resolveSingle(context: ColumnResolutionContext): ColumnWithPath<T>? = this.addPath(context.df)

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): DataColumn<T> = super.getValue(thisRef, property) as DataColumn<T>

    public operator fun iterator(): Iterator<T> = values().iterator()
}

internal val AnyCol.type get() = type()
internal val AnyCol.kind get() = kind()
internal val AnyCol.hasNulls get() = hasNulls()
internal val AnyCol.typeClass get() = type.classifier as KClass<*>
