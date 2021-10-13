package org.jetbrains.kotlinx.dataframe.api

import com.beust.klaxon.internal.firstNotNullResult
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.Many
import org.jetbrains.kotlinx.dataframe.asFrame
import org.jetbrains.kotlinx.dataframe.baseType
import org.jetbrains.kotlinx.dataframe.columnNames
import org.jetbrains.kotlinx.dataframe.columns.values
import org.jetbrains.kotlinx.dataframe.createTypeWithArgument
import org.jetbrains.kotlinx.dataframe.dataFrameOf
import org.jetbrains.kotlinx.dataframe.emptyDataFrame
import org.jetbrains.kotlinx.dataframe.guessColumnType
import org.jetbrains.kotlinx.dataframe.hasNulls
import org.jetbrains.kotlinx.dataframe.isGroup
import org.jetbrains.kotlinx.dataframe.toDataFrame
import org.jetbrains.kotlinx.dataframe.typed
import kotlin.reflect.KType
import kotlin.reflect.full.withNullability

@JvmName("unionRows")
public fun <T> Iterable<DataRow<T>?>.union(): DataFrame<T> = merge(map { it?.toDataFrame() ?: emptyDataFrame(1) }).typed()

public fun <T> Iterable<DataFrame<T>?>.union(): DataFrame<T> = merge(filterNotNull()).typed()

public fun <T> DataColumn<DataFrame<T>>.union(): DataFrame<T> = values.union().typed()

internal fun merge(dataFrames: List<AnyFrame>): AnyFrame {
    if (dataFrames.size == 1) return dataFrames[0]

    // collect column names preserving original order
    val columnNames = dataFrames
        .fold(emptyList<String>()) { acc, df -> acc + (df.columnNames() - acc) }

    val columns = columnNames.map { name ->

        val columns = dataFrames.map { it.tryGetColumn(name) }

        if (columns.all { it == null || it.isGroup() }) {
            val frames = columns.mapIndexed { index, col ->
                col?.asFrame() ?: emptyDataFrame(dataFrames[index].nrow())
            }
            val merged = merge(frames)
            DataColumn.create(name, merged)
        } else {
            var nulls = false
            val types = mutableSetOf<KType>()
            var manyNulls = false
            val defaultValue = columns.firstNotNullResult { it?.defaultValue() }
            var hasMany = false
            val list = columns.flatMapIndexed { index, col ->
                if (col != null) {
                    val type = col.type()
                    if (type.classifier == Many::class) {
                        val typeArgument = type.arguments[0].type
                        if (typeArgument != null) {
                            types.add(typeArgument)
                            if (!manyNulls) manyNulls = typeArgument.isMarkedNullable
                            hasMany = true
                        }
                    } else {
                        types.add(type)
                        if (!nulls) nulls = col.hasNulls
                    }
                    col.toList()
                } else {
                    val nrow = dataFrames[index].nrow()
                    if (!nulls && nrow > 0 && defaultValue == null) nulls = true
                    List(nrow) { defaultValue }
                }
            }

            val guessType = types.size > 1
            val baseType = baseType(types)
            val targetType = if (guessType || !hasMany) baseType.withNullability(nulls)
            else Many::class.createTypeWithArgument(baseType.withNullability(manyNulls))
            guessColumnType(name, list, targetType, guessType, defaultValue)
        }
    }
    return dataFrameOf(columns)
}

public operator fun <T> DataFrame<T>.plus(other: DataFrame<T>): DataFrame<T> = merge(listOf(this, other)).typed<T>()
public fun <T> DataFrame<T>.union(vararg other: DataFrame<T>): DataFrame<T> = merge(listOf(this) + other.toList()).typed<T>()
