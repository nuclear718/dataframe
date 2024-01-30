package org.jetbrains.kotlinx.dataframe.aggregation

import org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl
import org.jetbrains.kotlinx.dataframe.api.pathOf
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.SingleColumn
import org.jetbrains.kotlinx.dataframe.impl.aggregation.ConfiguredAggregateColumn

public interface ColumnsForAggregateSelectionDsl<out T> : ColumnsSelectionDsl<T> {

    public infix fun <C> ColumnSet<C>.default(defaultValue: C): ColumnSet<C> =
        ConfiguredAggregateColumn.withDefault(this, defaultValue)

    public infix fun <C> SingleColumn<C>.default(defaultValue: C): SingleColumn<C> =
        ConfiguredAggregateColumn.withDefault(this, defaultValue).single()

    public fun path(vararg names: String): ColumnPath = ColumnPath(names.asList())

    public infix fun <C> ColumnSet<C>.into(name: String): ColumnSet<C> = ConfiguredAggregateColumn.withPath(this, pathOf(name))

    public infix fun <C> SingleColumn<C>.into(name: String): SingleColumn<C> =
        ConfiguredAggregateColumn.withPath(this, pathOf(name)).single()

    public infix fun <C> ColumnSet<C>.into(path: ColumnPath): ColumnSet<C> = ConfiguredAggregateColumn.withPath(this, path)

    public infix fun <C> SingleColumn<C>.into(path: ColumnPath): SingleColumn<C> =
        ConfiguredAggregateColumn.withPath(this, path).single()
}
