package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.ColumnsSelector
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.Usage.ColumnGroupName
import org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.Usage.ColumnSetName
import org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.Usage.PlainDslName
import org.jetbrains.kotlinx.dataframe.columns.ColumnGroup
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.ColumnsResolver
import org.jetbrains.kotlinx.dataframe.columns.SingleColumn
import org.jetbrains.kotlinx.dataframe.columns.toColumnSet
import org.jetbrains.kotlinx.dataframe.documentation.Indent
import org.jetbrains.kotlinx.dataframe.documentation.LineBreak
import org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate
import org.jetbrains.kotlinx.dataframe.impl.aggregation.toColumns
import org.jetbrains.kotlinx.dataframe.impl.columns.allColumnsExceptKeepingStructure
import org.jetbrains.kotlinx.dataframe.impl.columns.createColumnSet
import org.jetbrains.kotlinx.dataframe.util.ALL_COLS_EXCEPT
import org.jetbrains.kotlinx.dataframe.util.ALL_COLS_REPLACE
import org.jetbrains.kotlinx.dataframe.util.ALL_COLS_REPLACE_VARARG
import org.jetbrains.kotlinx.dataframe.util.COL_SELECT_DSL_EXCEPT
import org.jetbrains.kotlinx.dataframe.util.COL_SELECT_DSL_EXCEPT_REPLACE_RESOLVER
import org.jetbrains.kotlinx.dataframe.util.COL_SELECT_DSL_EXCEPT_REPLACE_SELECTOR
import org.jetbrains.kotlinx.dataframe.util.COL_SELECT_DSL_SINGLE_COL_EXCEPT
import org.jetbrains.kotlinx.dataframe.util.COL_SELECT_DSL_SINGLE_COL_EXCEPT_REPLACE_OTHERS
import org.jetbrains.kotlinx.dataframe.util.COL_SELECT_DSL_SINGLE_COL_EXCEPT_REPLACE_RESOLVER
import org.jetbrains.kotlinx.dataframe.util.COL_SELECT_DSL_SINGLE_COL_EXCEPT_REPLACE_SELECTOR
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KProperty

// region ColumnsSelectionDsl

public interface AllExceptColumnsSelectionDsl {

    /**
     * ## (All) (Cols) Except Usage
     *
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     * `columnSet: `[ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet]`<*>`
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `columnGroup: `[SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]`<`[DataRow][org.jetbrains.kotlinx.dataframe.DataRow]`<*>> | `[String][String]
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * `| `[KProperty][kotlin.reflect.KProperty]`<* | `[DataRow][org.jetbrains.kotlinx.dataframe.DataRow]`<*>>` | `[ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath]
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `colsSelector: `[ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector]
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `column: `[ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]` | `[String][String]
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * `| `[KProperty][kotlin.reflect.KProperty]`<*> | `[ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath]
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `columnNoAccessor: `[String][String]` | `[KProperty][kotlin.reflect.KProperty]`<*> | `[ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath]
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  `columnsResolver: `[ColumnsResolver][org.jetbrains.kotlinx.dataframe.columns.ColumnsResolver]
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### In the [ColumnsSelectionDsl][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl]:
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  [**allExcept**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept] **`{ `**[colsSelector][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnsSelectorDef]**` }`**
     *
     *  `|` [**allExcept**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept]**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]**`, ..)`**
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### On a [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet]:
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  [columnSet][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnSetDef]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;[**except**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except] `[`**` { `**`]` [columnsResolver][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnsResolverDef] `[`**` } `**`]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**except**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except] [column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**except**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnDef]**`, ..)`**
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### On a column group reference:
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  [columnGroup][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnGroupDef]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;.[**allColsExcept**][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.allColsExcept] **` { `**[colsSelector][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnsSelectorDef]**` } `**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**allColsExcept**][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.allColsExcept]**`(`**[columnNoAccessor][org.jetbrains.kotlinx.dataframe.documentation.UsageTemplateColumnsSelectionDsl.UsageTemplate.ColumnNoAccessorDef]**`, ..)`**
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */
    public interface Usage {

        /** [**allExcept**][ColumnsSelectionDsl.allExcept] */
        public interface PlainDslName

        /** [**except**][ColumnsSelectionDsl.except] */
        public interface ColumnSetName

        /** .[**allColsExcept**][ColumnsSelectionDsl.allColsExcept] */
        public interface ColumnGroupName
    }

    /**
     * ## (All) (Cols) Except
     *
     * Exclude the given columns from the current selection.
     *
     * ### On [ColumnSets][ColumnSet]
     * This function can be explained the easiest on [ColumnSets][ColumnSet]. Let's say we want all
     * integer columns apart from `age` and `height`.
     *
     * We can do:
     *
     * `df.`[select][DataFrame.select]` { `[colsOf][colsOf]`<`[Int][Int]`>() `[except][ColumnSet.except]` { age `[and][ColumnsSelectionDsl.and]` height } }`
     *
     * Which will remove the [ColumnSet] created by `age `[and][ColumnsSelectionDsl.and]` height` from the [ColumnSet] created by [colsOf][colsOf]`<`[Int][Int]`>()`.
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     * This operation can also be done with columns that are from inside a [ColumnGroup].
     *
     * For instance:
     *
     * `df.`[select][DataFrame.select]` { `[colsAtAnyDepth][ColumnsSelectionDsl.colsAtAnyDepth]` { "a" `[in][String.contains]` it.`[name][DataColumn.name]`() } `[except][ColumnSet.except]` userData.age }`
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     * NOTE: If a column that needs to be removed appears multiple times, it is excepted
     * each time it is encountered (including inside [ColumnGroups][ColumnGroup]). You could say the receiver [ColumnSet]
     * is [simplified][ColumnsSelectionDsl.simplify] before the operation is performed:
     *
     * `(`[cols][ColumnsSelectionDsl.cols]`(userData, userData, userData.age, userData.age) `[except][ColumnSet.except]` userData.age) == (`[cols][ColumnsSelectionDsl.cols]`(userData) `[except][ColumnSet.except]` userData.age)`
     *
     * ### In the [ColumnsSelectionDsl][ColumnsSelectionDsl]
     * Instead of having to write [all][ColumnsSelectionDsl.all]`() `[except][ColumnsSelectionDsl.except]` { ... }` in the DSL,
     * you can use [allExcept][ColumnsSelectionDsl.allExcept]` { ... }` to achieve the same result.
     *
     * For example:
     *
     * `df.`[select][DataFrame.select]` { `[allExcept][ColumnsSelectionDsl.allExcept]` { userData.age `[and][ColumnsSelectionDsl.and]` height } }`
     *
     * ### On [ColumnGroups][ColumnGroup]
     * The variant of this function on [ColumnGroups][ColumnGroup] is a bit different as it operates on the columns
     * inside the group instead of in the DSL-scope. In other words, `myColGroup.`[allColsExcept][SingleColumn.allColsExcept]` { col } ` is
     * a shortcut for `myColGroup.`[select][ColumnsSelectionDsl.select]` { `[all][ColumnsSelectionDsl.all]`() `[except][ColumnSet]` col }`.
     *
     * Also note the name change, similar to [allCols][ColumnsSelectionDsl.allCols], this makes it clearer that your operating
     * on the columns in the group.
     *
     * ### Examples for this overload
     * {@getArg [ExampleArg]}
     *
     * {@getArg [ParamArg]}
     * @return A [ColumnSet] containing all columns in [this\] except the specified ones.
     */
    private interface CommonExceptDocs {

        /* Example argument */
        interface ExampleArg

        /* Parameter argument  */
        interface ParamArg
    }

    // region ColumnSet

    /**
     * ## (All) (Cols) Except
     *
     * Exclude the given columns from the current selection.
     *
     * ### On [ColumnSets][org.jetbrains.kotlinx.dataframe.columns.ColumnSet]
     * This function can be explained the easiest on [ColumnSets][org.jetbrains.kotlinx.dataframe.columns.ColumnSet]. Let's say we want all
     * integer columns apart from `age` and `height`.
     *
     * We can do:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.api.colsOf]`<`[Int][Int]`>() `[except][org.jetbrains.kotlinx.dataframe.columns.ColumnSet.except]` { age `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` height } }`
     *
     * Which will remove the [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] created by `age `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` height` from the [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] created by [colsOf][org.jetbrains.kotlinx.dataframe.api.colsOf]`<`[Int][Int]`>()`.
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     * This operation can also be done with columns that are from inside a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     *
     * For instance:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[colsAtAnyDepth][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsAtAnyDepth]` { "a" `[in][String.contains]` it.`[name][org.jetbrains.kotlinx.dataframe.DataColumn.name]`() } `[except][org.jetbrains.kotlinx.dataframe.columns.ColumnSet.except]` userData.age }`
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     * NOTE: If a column that needs to be removed appears multiple times, it is excepted
     * each time it is encountered (including inside [ColumnGroups][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]). You could say the receiver [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet]
     * is [simplified][org.jetbrains.kotlinx.dataframe.api.SimplifyColumnsSelectionDsl.simplify] before the operation is performed:
     *
     * `(`[cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]`(userData, userData, userData.age, userData.age) `[except][org.jetbrains.kotlinx.dataframe.columns.ColumnSet.except]` userData.age) == (`[cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]`(userData) `[except][org.jetbrains.kotlinx.dataframe.columns.ColumnSet.except]` userData.age)`
     *
     * ### In the [ColumnsSelectionDsl][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl]
     * Instead of having to write [all][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.all]`() `[except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]` { ... }` in the DSL,
     * you can use [allExcept][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept]` { ... }` to achieve the same result.
     *
     * For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { `[allExcept][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept]` { userData.age `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` height } }`
     *
     * ### On [ColumnGroups][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     * The variant of this function on [ColumnGroups][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] is a bit different as it operates on the columns
     * inside the group instead of in the DSL-scope. In other words, `myColGroup.`[allColsExcept][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.allColsExcept]` { col } ` is
     * a shortcut for `myColGroup.`[select][org.jetbrains.kotlinx.dataframe.api.SelectColumnsSelectionDsl.select]` { `[all][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.all]`() `[except][org.jetbrains.kotlinx.dataframe.columns.ColumnSet]` col }`.
     *
     * Also note the name change, similar to [allCols][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allCols], this makes it clearer that your operating
     * on the columns in the group.
     *
     * ### Examples for this overload
     * TODO
     *
     * @param [selector] A lambda in which you specify the columns that need to be
     *   excluded from the [ColumnSet]. The scope of the selector is the same as the outer scope.
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing all columns in [this] except the specified ones.
     */
    public infix fun <C> ColumnSet<C>.except(selector: () -> ColumnsResolver<*>): ColumnSet<C> =
        except(selector())

    public infix fun <C> ColumnSet<C>.except(other: ColumnsResolver<*>): ColumnSet<C> =
        exceptInternal(other)

    public fun <C> ColumnSet<C>.except(vararg other: ColumnsResolver<*>): ColumnSet<C> =
        except(other.toColumnSet())

    public infix fun <C> ColumnSet<C>.except(other: String): ColumnSet<C> =
        except(column<Any?>(other))

    public fun <C> ColumnSet<C>.except(vararg others: String): ColumnSet<C> =
        except(others.toColumnSet())

    public infix fun <C> ColumnSet<C>.except(other: KProperty<C>): ColumnSet<C> =
        except(column(other))

    public fun <C> ColumnSet<C>.except(vararg others: KProperty<C>): ColumnSet<C> =
        except(others.toColumnSet())

    public infix fun <C> ColumnSet<C>.except(other: ColumnPath): ColumnSet<C> =
        except(column<Any?>(other))

    public fun <C> ColumnSet<C>.except(vararg others: ColumnPath): ColumnSet<C> =
        except(others.toColumnSet())

    // endregion

    // region ColumnsSelectionDsl

    public fun <C> ColumnsSelectionDsl<C>.allExcept(selector: ColumnsSelector<C, *>): ColumnSet<*> =
        this.asSingleColumn().allColsExcept(selector)

    // no scoping issues, this can exist for legacy purposes
    public fun ColumnsSelectionDsl<*>.allExcept(vararg others: ColumnsResolver<*>): ColumnSet<*> =
        asSingleColumn().allColsExceptInternal(others.toColumnSet())

    public fun ColumnsSelectionDsl<*>.allExcept(vararg others: String): ColumnSet<*> =
        asSingleColumn().allColsExceptInternal(others.toColumnSet())

    public fun ColumnsSelectionDsl<*>.allExcept(vararg others: KProperty<*>): ColumnSet<*> =
        asSingleColumn().allColsExceptInternal(others.toColumnSet())

    public fun ColumnsSelectionDsl<*>.allExcept(vararg others: ColumnPath): ColumnSet<*> =
        asSingleColumn().allColsExceptInternal(others.toColumnSet())

    // endregion

    // region SingleColumn

    public fun <C> SingleColumn<DataRow<C>>.allColsExcept(selector: ColumnsSelector<C, *>): ColumnSet<*> =
        allColsExceptInternal(selector.toColumns())

    public fun SingleColumn<DataRow<*>>.allColsExcept(vararg others: String): ColumnSet<*> =
        allColsExceptInternal(others.toColumnSet())

    public fun SingleColumn<DataRow<*>>.allColsExcept(vararg others: KProperty<*>): ColumnSet<*> =
        allColsExceptInternal(others.toColumnSet())

    public fun SingleColumn<DataRow<*>>.allColsExcept(vararg other: ColumnPath): ColumnSet<*> =
        allColsExceptInternal(other.toColumnSet())

    // endregion

    // region String

    public fun String.allColsExcept(selector: ColumnsSelector<*, *>): ColumnSet<*> =
        columnGroup(this).allColsExcept(selector)

    public fun String.allColsExcept(vararg others: String): ColumnSet<*> =
        columnGroup(this).allColsExceptInternal(others.toColumnSet())

    public fun String.allColsExcept(vararg others: KProperty<*>): ColumnSet<*> =
        columnGroup(this).allColsExceptInternal(others.toColumnSet())

    public fun String.allColsExcept(vararg others: ColumnPath): ColumnSet<*> =
        columnGroup(this).allColsExceptInternal(others.toColumnSet())

    // endregion

    // region KProperty

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    // TODO: [KT-64092](https://youtrack.jetbrains.com/issue/KT-64092/OVERLOADRESOLUTIONAMBIGUITY-caused-by-lambda-argument)
    public fun <C> KProperty<C>.allColsExcept(selector: ColumnsSelector<C, *>): ColumnSet<*> =
        columnGroup(this).allColsExcept(selector)

    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("KPropertyDataRowAllColsExcept")
    public fun <C> KProperty<DataRow<C>>.allColsExcept(selector: ColumnsSelector<C, *>): ColumnSet<*> =
        columnGroup(this).allColsExcept(selector)

    public fun KProperty<*>.allColsExcept(vararg others: String): ColumnSet<*> =
        columnGroup(this).allColsExceptInternal(others.toColumnSet())

    public fun KProperty<*>.allColsExcept(vararg others: KProperty<*>): ColumnSet<*> =
        columnGroup(this).allColsExceptInternal(others.toColumnSet())

    public fun KProperty<*>.allColsExcept(vararg others: ColumnPath): ColumnSet<*> =
        columnGroup(this).allColsExceptInternal(others.toColumnSet())

    // endregion

    // region ColumnPath

    public fun ColumnPath.allColsExcept(selector: ColumnsSelector<*, *>): ColumnSet<*> =
        columnGroup(this).allColsExcept(selector)

    public fun ColumnPath.allColsExcept(vararg others: String): ColumnSet<*> =
        columnGroup(this).allColsExceptInternal(others.toColumnSet())

    public fun ColumnPath.allColsExcept(vararg others: KProperty<*>): ColumnSet<*> =
        columnGroup(this).allColsExceptInternal(others.toColumnSet())

    public fun ColumnPath.allColsExcept(vararg others: ColumnPath): ColumnSet<*> =
        columnGroup(this).allColsExceptInternal(others.toColumnSet())

    // endregion

    // region deprecated

    @Deprecated(
        message = COL_SELECT_DSL_SINGLE_COL_EXCEPT,
        replaceWith = ReplaceWith(COL_SELECT_DSL_SINGLE_COL_EXCEPT_REPLACE_SELECTOR),
        level = DeprecationLevel.WARNING,
    )
    public fun <C> SingleColumn<DataRow<C>>.except(selector: ColumnsSelector<C, *>): ColumnSet<*> =
        allColsExcept(selector)

    @Deprecated(
        message = COL_SELECT_DSL_SINGLE_COL_EXCEPT,
        replaceWith = ReplaceWith(COL_SELECT_DSL_SINGLE_COL_EXCEPT_REPLACE_RESOLVER),
        level = DeprecationLevel.WARNING,
    )
    public fun SingleColumn<DataRow<*>>.except(vararg others: ColumnsResolver<*>): ColumnSet<*> =
        allColsExcept { others.toColumnSet() }

    @Deprecated(
        message = COL_SELECT_DSL_SINGLE_COL_EXCEPT,
        replaceWith = ReplaceWith(COL_SELECT_DSL_SINGLE_COL_EXCEPT_REPLACE_OTHERS),
        level = DeprecationLevel.WARNING,
    )
    public fun SingleColumn<DataRow<*>>.except(vararg others: String): ColumnSet<*> =
        allColsExcept(*others)

    @Deprecated(
        message = COL_SELECT_DSL_SINGLE_COL_EXCEPT,
        replaceWith = ReplaceWith(COL_SELECT_DSL_SINGLE_COL_EXCEPT_REPLACE_OTHERS),
        level = DeprecationLevel.WARNING,
    )
    public fun SingleColumn<DataRow<*>>.except(vararg others: KProperty<*>): ColumnSet<*> =
        allColsExcept(*others)

    @Deprecated(
        message = COL_SELECT_DSL_SINGLE_COL_EXCEPT,
        replaceWith = ReplaceWith(COL_SELECT_DSL_SINGLE_COL_EXCEPT_REPLACE_OTHERS),
        level = DeprecationLevel.WARNING,
    )
    public fun SingleColumn<DataRow<*>>.except(vararg others: ColumnPath): ColumnSet<*> =
        allColsExcept(*others)

    @Deprecated(
        message = COL_SELECT_DSL_EXCEPT,
        replaceWith = ReplaceWith(COL_SELECT_DSL_EXCEPT_REPLACE_SELECTOR),
        level = DeprecationLevel.WARNING,
    )
    public fun <C> ColumnsSelectionDsl<C>.except(selector: ColumnsSelector<C, *>): ColumnSet<*> =
        allExcept(selector)

    @Deprecated(
        message = COL_SELECT_DSL_EXCEPT,
        replaceWith = ReplaceWith(COL_SELECT_DSL_EXCEPT_REPLACE_RESOLVER),
        level = DeprecationLevel.WARNING,
    )
    public fun ColumnsSelectionDsl<*>.except(vararg others: ColumnsResolver<*>): ColumnSet<*> =
        allExcept(*others)

    @Deprecated(
        message = COL_SELECT_DSL_EXCEPT,
        replaceWith = ReplaceWith(COL_SELECT_DSL_EXCEPT_REPLACE_RESOLVER),
        level = DeprecationLevel.WARNING,
    )
    public fun ColumnsSelectionDsl<*>.except(vararg others: String): ColumnSet<*> =
        allExcept(*others)

    @Deprecated(
        message = COL_SELECT_DSL_EXCEPT,
        replaceWith = ReplaceWith(COL_SELECT_DSL_EXCEPT_REPLACE_RESOLVER),
        level = DeprecationLevel.WARNING,
    )
    public fun ColumnsSelectionDsl<*>.except(vararg others: KProperty<*>): ColumnSet<*> =
        allExcept(*others)

    @Deprecated(
        message = COL_SELECT_DSL_EXCEPT,
        replaceWith = ReplaceWith(COL_SELECT_DSL_EXCEPT_REPLACE_RESOLVER),
        level = DeprecationLevel.WARNING,
    )
    public fun ColumnsSelectionDsl<*>.except(vararg others: ColumnPath): ColumnSet<*> =
        allExcept(*others)

    @Deprecated(
        message = ALL_COLS_EXCEPT,
        replaceWith = ReplaceWith(ALL_COLS_REPLACE),
        level = DeprecationLevel.ERROR,
    )
    public fun SingleColumn<DataRow<*>>.allColsExcept(other: ColumnsResolver<*>): ColumnSet<*> =
        allColsExcept { other }

    @Deprecated(
        message = ALL_COLS_EXCEPT,
        replaceWith = ReplaceWith(ALL_COLS_REPLACE_VARARG),
        level = DeprecationLevel.ERROR,
    )
    public fun SingleColumn<DataRow<*>>.allColsExcept(vararg others: ColumnsResolver<*>): ColumnSet<*> =
        allColsExcept { others.toColumnSet() }

    @Deprecated(
        message = ALL_COLS_EXCEPT,
        replaceWith = ReplaceWith(ALL_COLS_REPLACE),
        level = DeprecationLevel.ERROR
    )
    public fun String.allColsExcept(other: ColumnsResolver<*>): ColumnSet<*> =
        allColsExcept { other }

    @Deprecated(
        message = ALL_COLS_EXCEPT,
        replaceWith = ReplaceWith(ALL_COLS_REPLACE_VARARG),
        level = DeprecationLevel.ERROR,
    )
    public fun String.allColsExcept(vararg others: ColumnsResolver<*>): ColumnSet<*> =
        allColsExcept { others.toColumnSet() }

    @Deprecated(
        message = ALL_COLS_EXCEPT,
        replaceWith = ReplaceWith(ALL_COLS_REPLACE),
        level = DeprecationLevel.ERROR,
    )
    public fun KProperty<*>.allColsExcept(other: ColumnsResolver<*>): ColumnSet<*> =
        allColsExcept { other }

    @Deprecated(
        message = ALL_COLS_EXCEPT,
        replaceWith = ReplaceWith(ALL_COLS_REPLACE_VARARG),
        level = DeprecationLevel.ERROR,
    )
    public fun KProperty<*>.allColsExcept(vararg others: ColumnsResolver<*>): ColumnSet<*> =
        allColsExcept { others.toColumnSet() }
    @Deprecated(
        message = ALL_COLS_EXCEPT,
        replaceWith = ReplaceWith(ALL_COLS_REPLACE),
        level = DeprecationLevel.ERROR,
    )
    public fun ColumnPath.allColsExcept(other: ColumnsResolver<*>): ColumnSet<*> =
        allColsExcept { other }

    @Deprecated(
        message = ALL_COLS_EXCEPT,
        replaceWith = ReplaceWith(ALL_COLS_REPLACE_VARARG),
        level = DeprecationLevel.ERROR,
    )
    public fun ColumnPath.allColsExcept(vararg others: ColumnsResolver<*>): ColumnSet<*> =
        allColsExcept { others.toColumnSet() }

    // endregion

    @Suppress("UNCHECKED_CAST")
    private fun <C> ColumnSet<C>.exceptInternal(other: ColumnsResolver<*>): ColumnSet<C> =
        createColumnSet { context ->
            val resolvedCols = this.resolve(context)
            val resolvedColsToExcept = other.resolve(context)
            resolvedCols.allColumnsExceptKeepingStructure(resolvedColsToExcept)
        } as ColumnSet<C>

    private fun SingleColumn<DataRow<*>>.allColsExceptInternal(other: ColumnsResolver<*>): ColumnSet<Any?> =
        selectInternal { all().exceptInternal(other) }
}

// endregion
