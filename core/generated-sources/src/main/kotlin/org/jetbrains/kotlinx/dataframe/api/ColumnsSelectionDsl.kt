package org.jetbrains.kotlinx.dataframe.api

import org.jetbrains.kotlinx.dataframe.ColumnsSelector
import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.columns.ColumnPath
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.columns.ColumnsResolver
import org.jetbrains.kotlinx.dataframe.columns.SingleColumn
import org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl
import org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate
import org.jetbrains.kotlinx.dataframe.documentation.Indent
import org.jetbrains.kotlinx.dataframe.documentation.LineBreak
import org.jetbrains.kotlinx.dataframe.documentation.SelectingColumns
import org.jetbrains.kotlinx.dataframe.impl.DataFrameReceiver
import org.jetbrains.kotlinx.dataframe.impl.columns.ColumnsList
import org.jetbrains.kotlinx.dataframe.util.COL_SELECT_DSL_LIST_DATACOLUMN_GET
import org.jetbrains.kotlinx.dataframe.util.COL_SELECT_DSL_LIST_DATACOLUMN_GET_REPLACE
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KProperty

/** [Columns Selection DSL][ColumnsSelectionDsl] */
internal interface ColumnsSelectionDslLink

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <T> ColumnsSelectionDsl<T>.asSingleColumn(): SingleColumn<DataRow<T>> = this as SingleColumn<DataRow<T>>

/**
 * [DslMarker] for [ColumnsSelectionDsl] to prevent accessors being used across scopes for nested
 * [ColumnsSelectionDsl.select] calls.
 */
@DslMarker
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
public annotation class ColumnsSelectionDslMarker

/**
 * ## Columns Selection DSL
 * Select or express columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl].
 * (Any (combination of) [Access API][org.jetbrains.kotlinx.dataframe.documentation.AccessApi]).
 *
 * This DSL is initiated by a [Columns Selector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] lambda,
 * which operates in the context of the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] and
 * expects you to return a [SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn] or [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] (so, a [ColumnsResolver][org.jetbrains.kotlinx.dataframe.columns.ColumnsResolver]).
 * This is an entity formed by calling any (combination) of the functions
 * in the DSL that is or can be resolved into one or more columns.
 *
 * #### NOTE:
 * While you can use the [String API][org.jetbrains.kotlinx.dataframe.documentation.AccessApi.StringApi] and [KProperties API][org.jetbrains.kotlinx.dataframe.documentation.AccessApi.KPropertiesApi]
 * in this DSL directly with any function, they are NOT valid return types for the
 * [Columns Selector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] lambda. You'd need to turn them into a [ColumnReference][org.jetbrains.kotlinx.dataframe.columns.ColumnReference] first, for instance
 * with a function like [`col("name")`][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col].
 *
 * ### Check out: [Columns Selection DSL Grammar][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.DslGrammar]
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;
 *
 * [See Column Selectors on the documentation website.](https://kotlin.github.io/dataframe/columnselectors.html)
 *
 * #### For example:
 *
 * `df.`[select][DataFrame.select]` { length `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` age }`
 *
 * `df.`[select][DataFrame.select]` { `[cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]`(1..5) }`
 *
 * `df.`[select][DataFrame.select]` { `[colsOf][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsOf]`<`[Double][Double]`>() }`
 *
 *
 *
 *
 */
@ColumnsSelectionDslMarker
public interface ColumnsSelectionDsl<out T> : /* SingleColumn<DataRow<T>> */
    ColumnSelectionDsl<T>,

    // first {}, firstCol()
    FirstColumnsSelectionDsl,
    // last {}, lastCol()
    LastColumnsSelectionDsl,
    // single {}, singleCol()
    SingleColumnsSelectionDsl,

    // col(name), col(5), [5]
    ColColumnsSelectionDsl,
    // valueCol(name), valueCol(5)
    ValueColColumnsSelectionDsl,
    // frameCol(name), frameCol(5)
    FrameColColumnsSelectionDsl,
    // colGroup(name), colGroup(5)
    ColGroupColumnsSelectionDsl,

    // cols {}, cols(), cols(colA, colB), cols(1, 5), cols(1..5), [{}]
    ColsColumnsSelectionDsl,

    // colA.."colB"
    ColumnRangeColumnsSelectionDsl,

    // valueCols {}, valueCols()
    ValueColsColumnsSelectionDsl,
    // frameCols {}, frameCols()
    FrameColsColumnsSelectionDsl,
    // colGroups {}, colGroups()
    ColGroupsColumnsSelectionDsl,
    // colsOfKind(Value, Frame) {}, colsOfKind(Value, Frame)
    ColsOfKindColumnsSelectionDsl,

    // all(Cols), allAfter(colA), allBefore(colA), allFrom(colA), allUpTo(colA)
    AllColumnsSelectionDsl,
    // colsAtAnyDepth {}, colsAtAnyDepth()
    ColsAtAnyDepthColumnsSelectionDsl,
    // colsInGroups {}, colsInGroups()
    ColsInGroupsColumnsSelectionDsl,
    // take(5), takeLastCols(2), takeLastWhile {}, takeColsWhile {}
    TakeColumnsSelectionDsl,
    // drop(5), dropLastCols(2), dropLastWhile {}, dropColsWhile {}
    DropColumnsSelectionDsl,

    // select {}, TODO due to String.invoke conflict this cannot be moved out of ColumnsSelectionDsl
    SelectColumnsSelectionDsl,
    // except(), allExcept {}, allColsExcept {}
    AllExceptColumnsSelectionDsl,

    // nameContains(""), colsNameContains(""), nameStartsWith(""), childrenNameEndsWith("")
    ColumnNameFiltersColumnsSelectionDsl,
    // withoutNulls(), colsWithoutNulls()
    WithoutNullsColumnsSelectionDsl,
    // distinct()
    DistinctColumnsSelectionDsl,
    // none()
    NoneColumnsSelectionDsl,
    // colsOf<>(), colsOf<> {}
    ColsOfColumnsSelectionDsl,
    // simplify()
    SimplifyColumnsSelectionDsl,
    // filter {}
    FilterColumnsSelectionDsl,
    // colSet and colB
    AndColumnsSelectionDsl,
    // colA named "colB", colA into "colB"
    RenameColumnsSelectionDsl,
    // expr {}
    ExprColumnsSelectionDsl {

    /**
     * ## [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] Grammar
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     * [(What is this notation?)][org.jetbrains.kotlinx.dataframe.documentation.DslGrammar]
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### Definitions:
     *  `columnGroupReference: `[String][String]` | `[KProperty][kotlin.reflect.KProperty]`<*>`
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * | `[ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath]
     *
     *  `colSelector: `[ColumnSelector][org.jetbrains.kotlinx.dataframe.ColumnSelector]
     *
     *  `colsSelector: `[ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector]
     *
     *  `column: `[ColumnAccessor][org.jetbrains.kotlinx.dataframe.columns.ColumnAccessor]` | `[String][String]
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * `| `[KProperty][kotlin.reflect.KProperty]`<*> | `[ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath]
     *
     *  `columnGroup: `[SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]`<`[DataRow][org.jetbrains.kotlinx.dataframe.DataRow]`<*>> | `[String][String]
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     * `| `[KProperty][kotlin.reflect.KProperty]`<* | `[DataRow][org.jetbrains.kotlinx.dataframe.DataRow]`<*>>` | `[ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath]
     *
     *  `columnNoAccessor: `[String][String]` | `[KProperty][kotlin.reflect.KProperty]`<*> | `[ColumnPath][org.jetbrains.kotlinx.dataframe.columns.ColumnPath]
     *
     *  `columnOrSet: `[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]` | `[columnSet][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnSetDef]
     *
     *  `columnSet: `[ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet]`<*>`
     *
     *  `columnsResolver: `[ColumnsResolver][org.jetbrains.kotlinx.dataframe.columns.ColumnsResolver]
     *
     *  `condition: `[ColumnFilter][org.jetbrains.kotlinx.dataframe.ColumnFilter]
     *
     *  `expression: `[Column Expression][org.jetbrains.kotlinx.dataframe.documentation.ColumnExpression]
     *
     *  `ignoreCase: `[Boolean][Boolean]
     *
     *  `index: `[Int][Int]
     *
     *  `indexRange: `[IntRange][IntRange]
     *
     *  `infer: `[Infer][org.jetbrains.kotlinx.dataframe.api.Infer]
     *
     *  `kind: `[ColumnKind][org.jetbrains.kotlinx.dataframe.columns.ColumnKind]
     *
     *  `kType: `[KType][kotlin.reflect.KType]
     *
     *  `name: `[String][String]
     *
     *  `number: `[Int][Int]
     *
     *  `regex: `[Regex][Regex]
     *
     *  `singleColumn: `[SingleColumn][org.jetbrains.kotlinx.dataframe.columns.SingleColumn]`<`[DataRow][org.jetbrains.kotlinx.dataframe.DataRow]`<*>>
     *
     *  `T: Column type`
     *
     *  `text: `[String][String]
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### What can be called directly in the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl]:
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  [column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef] [**..**][org.jetbrains.kotlinx.dataframe.api.ColumnRangeColumnsSelectionDsl.rangeTo] [column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]
     *
     *  `|` **`this`**`/`**`it`**[**`[`**][cols][column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]**`,`**` .. `[**`]`**][cols]
     *
     *  `|` **`this`**`/`**`it`**[**`[`**][cols]**`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`**[**`]`**][cols]
     *
     *  `|` [**all**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.all]**`()`**
     *
     *  `|` **`all`**`(`[**Before**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsBefore]`|`[**After**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allAfter]`|`[**From**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsFrom]`|`[**UpTo**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsUpTo]`)` `(` **`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]**`)`** `|` **`{`** [colSelector][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnSelectorDef] **`}`** `)`
     *
     *  `|` [**allExcept**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept] **`{ `**[colsSelector][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnsSelectorDef]**` }`**
     *
     *  `|` [**allExcept**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allExcept]**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]**`,`**` ..`**`)`**
     *
     *  `|` [columnOrSet][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnOrColumnSetDef] [**and**][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` [ `**`{`**` ] `[columnOrSet][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnOrColumnSetDef]` [ `**`}`**` ] `
     *
     *  `|` [columnOrSet][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnOrColumnSetDef].[**and**][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and] **`(`**`|`**`{ `**[columnOrSet][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnOrColumnSetDef]**` }`**`|`**`)`**
     *
     *  `|` `(`
     *  [**col**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col]
     *  `|` [**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]
     *  `|` [**frameCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol]
     *  `|` [**colGroup**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup]
     *  `)[`**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnTypeDef]**`>`**`]`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]` | `[index][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexDef]**`)`**
     *
     * `|` `(`
     *  [**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]
     *  `|` [**valueCols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCols]
     *  `|` [**frameCols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]
     *  `|` [**colGroups**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]
     *  `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  `|` [**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]`[`**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnTypeDef]**`>`**`]`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]**`,`**` .. | `[index][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexDef]**`,`**` .. | `[indexRange][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexRangeDef]**`)`**
     *
     *  `|` [**colsAtAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsAtAnyDepth]` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  `|` [**colsInGroups**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsInGroups]` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]
     *
     *  `|` [**colsOf**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsOf]**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnTypeDef]**`>`**` [` **`(`**[kType][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.KTypeDef]**`)`** `] [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  `|` [**colsOfKind**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]**`(`**[kind][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnKindDef]**`,`**` ..`**`)`**` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  `|` [**drop**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.drop]`(`[**Last**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.dropLast]`)`**`(`**[number][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.NumberDef]**`)`**
     *
     *  `|` [**drop**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.dropWhile]`(`[**Last**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.dropLastWhile]`)`[**While**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.dropWhile]**` { `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`**
     *
     *  `|` [**expr**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]**`(`**`[`[name][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.NameDef]**`,`**`][`[infer][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.InferDef]`]`**`)`** **`{ `**[expression][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnExpressionDef]**` }`**
     *
     *  `|` `(`
     *  [**first**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.first]
     *  `|` [**last**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.last]
     *  `|` [**single**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.single]
     *  `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  `|` [**nameContains**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.nameContains]**`(`**[text][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.TextDef]`[`**`,`** [ignoreCase][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IgnoreCaseDef]`] | `[regex][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.RegexDef]**`)`**
     *
     *  `|` 
     * **name**`(`[**Starts**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.nameStartsWith]`|`[**Ends**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.nameEndsWith]`)`**`With`****`(`**[text][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.TextDef]`[`**`,`** [ignoreCase][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IgnoreCaseDef]`]`**`)`**
     *
     *  `|` [column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef] [**named**][org.jetbrains.kotlinx.dataframe.api.RenameColumnsSelectionDsl.named]`/`[**into**][org.jetbrains.kotlinx.dataframe.api.RenameColumnsSelectionDsl.into] [column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]
     *
     *  `|` [column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]`(`.[**named**][org.jetbrains.kotlinx.dataframe.api.RenameColumnsSelectionDsl.named]`|`.[**into**][org.jetbrains.kotlinx.dataframe.api.RenameColumnsSelectionDsl.into]`)`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]**`)`**
     *
     *  `|` [**none**][org.jetbrains.kotlinx.dataframe.api.NoneColumnsSelectionDsl.none]**`()`**
     *
     *  `|` [**take**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.take]`(`[**Last**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.takeLast]`)`**`(`**[number][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.NumberDef]**`)`**
     *
     *  `|` [**take**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.takeWhile]`(`[**Last**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.takeLastWhile]`)`[**While**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.takeWhile]**` { `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`**
     *
     *  `|` [**withoutNulls**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.withoutNulls]**`()`**
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### What can be called on a [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet]:
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  [columnSet][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnSetDef]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;[**`[`**][ColumnsSelectionDsl.col][index][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexDef][**`]`**][ColumnsSelectionDsl.col]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**`[`**][cols][index][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexDef]**`,`**` .. | `[indexRange][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexRangeDef][**`]`**][cols]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**`[`**][cols]**`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`**[**`]`**][cols]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**all**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.all]**`()`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .**`all`**`(`[**Before**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsBefore]`|`[**After**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allAfter]`|`[**From**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsFrom]`|`[**UpTo**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsUpTo]`)` `(` **`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]**`)`** `|` **`{`** [condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef] **`}`** `)`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**and**][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and] **`(`**`|`**`{ `**[columnOrSet][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnOrColumnSetDef]**` }`**`|`**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` `(`
     *  .[**col**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col]
     *  `|` .[**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]
     *  `|` .[**frameCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol]
     *  `|` .[**colGroup**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup]
     *  `)`**`(`**[index][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` `(`
     *  .[**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]
     *  `|` .[**valueCols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCols]
     *  `|` .[**frameCols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]
     *  `|` .[**colGroups**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]
     *  `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]**`(`**[index][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexDef]**`,`**` .. | `[indexRange][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexRangeDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsAtAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsAtAnyDepth]` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsInGroups**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsInGroups]` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsOf**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsOf]**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnTypeDef]**`>`**` [` **`(`**[kType][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.KTypeDef]**`)`** `] [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsOfKind**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]**`(`**[kind][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnKindDef]**`,`**` ..`**`)`**` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**distinct**][org.jetbrains.kotlinx.dataframe.api.DistinctColumnsSelectionDsl.distinct]**`()`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**drop**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.drop]`(`[**Last**][org.jetbrains.kotlinx.dataframe.columns.ColumnSet.dropLast]`)`**`(`**[number][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.NumberDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**drop**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.dropWhile]`(`[**Last**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.dropLastWhile]`)`[**While**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.dropWhile]**` { `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**except**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except] `[`**` { `**`]` [columnsResolver][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnsResolverDef] `[`**` } `**`]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**except**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except] [column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**except**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]**`,`**` ..`**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**filter**][org.jetbrains.kotlinx.dataframe.api.FilterColumnsSelectionDsl.filter]**` {`** [condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef] **`}`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` `(`
     *  .[**first**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.first]
     *  `|` .[**last**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.last]
     *  `|` .[**single**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.single]
     *  `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .**name**`(`[**Starts**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.nameStartsWith]`|`[**Ends**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.nameEndsWith]`)`**`With`****`(`**[text][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.TextDef]`[`**`,`** [ignoreCase][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IgnoreCaseDef]`]`**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**nameContains**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.nameContains]**`(`**[text][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.TextDef]`[`**`,`** [ignoreCase][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IgnoreCaseDef]`] | `[regex][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.RegexDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**simplify**][org.jetbrains.kotlinx.dataframe.api.SimplifyColumnsSelectionDsl.simplify]**`()`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**take**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.take]`(`[**Last**][org.jetbrains.kotlinx.dataframe.columns.ColumnSet.takeLast]`)`**`(`**[number][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.NumberDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**take**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.takeWhile]`(`[**Last**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.takeLastWhile]`)`[**While**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.takeWhile]**` { `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**withoutNulls**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.withoutNulls]**`()`**
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  ### What can be called on a [Column Group (reference)][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnGroupDef]:
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *  [columnGroup][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnGroupDef]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**`[`**][cols][column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]**`,`**` ..`[**`]`**][cols]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**`[`**][cols]**`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`**[**`]`**][cols]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|`[**` {`**][ColumnsSelectionDsl.select] [colsSelector][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnsSelectorDef] [**`}`**][ColumnsSelectionDsl.select]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**allCols**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allCols]**`()`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .**`allCols`**`(`[**Before**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsBefore]`|`[**After**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.allAfter]`|`[**From**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsFrom]`|`[**UpTo**][org.jetbrains.kotlinx.dataframe.api.AllColumnsSelectionDsl.allColsUpTo]`)` `(` **`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]**`)`** `|` **`{`** [colSelector][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnSelectorDef] **`}`** `)`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**allColsExcept**][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.allColsExcept] **` { `**[colsSelector][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnsSelectorDef]**` } `**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**allColsExcept**][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.allColsExcept]**`(`**[columnNoAccessor][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnNoAccessorDef]**`,`**` ..`**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**and**][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and] **`(`**`|`**`{ `**[columnOrSet][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnOrColumnSetDef]**` }`**`|`**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`| (`
     *  .[**col**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.col]
     *  `|` .[**valueCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCol]
     *  `|` .[**frameCol**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.frameCol]
     *  `|` .[**colGroup**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroup]
     *  `)[`**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnTypeDef]**`>`**`]`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]` | `[index][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` `(`
     *   .[**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]
     *   `|` .[**valueCols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.valueCols]
     *   `|` .[**frameCols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]
     *   `|` .[**colGroups**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]
     *   `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**cols**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols]`[`**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnTypeDef]**`>`**`]`**`(`**[column][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnDef]**`,`**` .. | `[index][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexDef]**`,`**` .. | `[indexRange][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IndexRangeDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsAtAnyDepth**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsAtAnyDepth]` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsInGroups**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsInGroups]` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .**colsName**`(`[**Starts**][org.jetbrains.kotlinx.dataframe.api.ColumnNameFiltersColumnsSelectionDsl.colsNameStartsWith]`|`[**Ends**][org.jetbrains.kotlinx.dataframe.api.ColumnNameFiltersColumnsSelectionDsl.colsNameEndsWith]`)`**`With`****`(`**[text][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.TextDef]`[`**`,`** [ignoreCase][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IgnoreCaseDef]`]`**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsNameContains**][org.jetbrains.kotlinx.dataframe.api.ColumnNameFiltersColumnsSelectionDsl.colsNameContains]**`(`**[text][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.TextDef]`[`**`,`** [ignoreCase][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.IgnoreCaseDef]`] | `[regex][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.RegexDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsOfKind**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colGroups]**`(`**[kind][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnKindDef]**`,`**` ..`**`)`**` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**colsWithoutNulls**][org.jetbrains.kotlinx.dataframe.api.WithoutNullsColumnsSelectionDsl.colsWithoutNulls]**`()`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**drop**][org.jetbrains.kotlinx.dataframe.api.DropColumnsSelectionDsl.dropCols]`(`[**Last**][org.jetbrains.kotlinx.dataframe.api.DropColumnsSelectionDsl.dropLastCols]`)`[**Cols**][org.jetbrains.kotlinx.dataframe.api.DropColumnsSelectionDsl.dropCols]**`(`**[number][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.NumberDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**drop**][org.jetbrains.kotlinx.dataframe.api.DropColumnsSelectionDsl.dropColsWhile]`(`[**Last**][org.jetbrains.kotlinx.dataframe.api.DropColumnsSelectionDsl.dropLastColsWhile]`)`[**ColsWhile**][org.jetbrains.kotlinx.dataframe.api.DropColumnsSelectionDsl.dropColsWhile]**` { `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**exceptNew**][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.exceptNew] **` { `**[colsSelector][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnsSelectorDef]**` } EXPERIMENTAL!`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` [**exceptNew**][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.exceptNew]**`(`**[columnNoAccessor][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnNoAccessorDef]**`,`**` ..`**`) EXPERIMENTAL!`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` `(`
     *  .[**firstCol**][org.jetbrains.kotlinx.dataframe.api.FirstColumnsSelectionDsl.firstCol]
     *  `|` .[**lastCol**][org.jetbrains.kotlinx.dataframe.api.LastColumnsSelectionDsl.lastCol]
     *  `|` .[**singleCol**][org.jetbrains.kotlinx.dataframe.api.SingleColumnsSelectionDsl.singleCol]
     *  `) [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**select**][org.jetbrains.kotlinx.dataframe.api.SelectColumnsSelectionDsl.select]**` {`** [colsSelector][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnsSelectorDef] **`}`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**take**][org.jetbrains.kotlinx.dataframe.api.TakeColumnsSelectionDsl.takeCols]`(`[**Last**][org.jetbrains.kotlinx.dataframe.api.TakeColumnsSelectionDsl.takeLastCols]`)`[**Cols**][org.jetbrains.kotlinx.dataframe.api.TakeColumnsSelectionDsl.takeCols]**`(`**[number][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.NumberDef]**`)`**
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;`|` .[**take**][org.jetbrains.kotlinx.dataframe.api.TakeColumnsSelectionDsl.takeColsWhile]`(`[**Last**][org.jetbrains.kotlinx.dataframe.api.TakeColumnsSelectionDsl.takeLastColsWhile]`)`[**ColsWhile**][org.jetbrains.kotlinx.dataframe.api.TakeColumnsSelectionDsl.takeColsWhile]**` { `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`**
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *  [singleColumn][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.SingleColumnDef]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;.[**colsOf**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsOf]**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnTypeDef]**`>`**` [` **`(`**[kType][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.KTypeDef]**`)`** `] [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
     *
     *  
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     *  [columnGroupReference][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnGroupNoSingleColumnDef]
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;.[**colsOf**][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.colsOf]**`<`**[T][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ColumnTypeDef]**`>(`**[kType][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.KTypeDef]**`)`** ` [` **`{ `**[condition][org.jetbrains.kotlinx.dataframe.documentation.DslGrammarTemplateColumnsSelectionDsl.DslGrammarTemplate.ConditionDef]**` }`** `]`
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
     *
     *
     *
     */
    public interface DslGrammar

    /**
     * Invokes the given [ColumnsSelector] using this [ColumnsSelectionDsl].
     */
    public operator fun <C> ColumnsSelector<T, C>.invoke(): ColumnsResolver<C> =
        this@invoke(this@ColumnsSelectionDsl, this@ColumnsSelectionDsl)

    /**
     * ## Deprecated: Columns by Index Range from List of Columns
     * Helper function to create a [ColumnSet] from a list of columns by specifying a range of indices.
     *
     * ### Deprecated
     *
     * Deprecated because it's too niche. Let us know if you have a good use for it!
     */
    @Deprecated(
        message = COL_SELECT_DSL_LIST_DATACOLUMN_GET,
        replaceWith = ReplaceWith(COL_SELECT_DSL_LIST_DATACOLUMN_GET_REPLACE),
        level = DeprecationLevel.WARNING,
    )
    public operator fun <C> List<DataColumn<C>>.get(range: IntRange): ColumnSet<C> =
        ColumnsList(subList(range.first, range.last + 1))

    // region select
    // NOTE: due to invoke conflicts these cannot be moved out of the interface

    /**
     * ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than the [cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols] filter, because now all
     * operations of the DSL are at your disposal.
     *
     * The scope of the new DSL instance is relative to
     * the [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] you are selecting from.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * ### Check out: [Grammar][org.jetbrains.kotlinx.dataframe.api.SelectColumnsSelectionDsl.Grammar]
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { myColGroup.`[select][SingleColumn.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { myColGroup `[{][SingleColumn.select]` colA `[and][ColumnsSelectionDsl.and]` colB `[}][SingleColumn.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.allColsExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    public operator fun <C, R> SingleColumn<DataRow<C>>.invoke(selector: ColumnsSelector<C, R>): ColumnSet<R> =
        select(selector)

    /**
     * ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than the [cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols] filter, because now all
     * operations of the DSL are at your disposal.
     *
     * The scope of the new DSL instance is relative to
     * the [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] you are selecting from.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * ### Check out: [Grammar][org.jetbrains.kotlinx.dataframe.api.SelectColumnsSelectionDsl.Grammar]
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { Type::myColGroup.`[select][KProperty.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { DataSchemaType::myColGroup `[`{`][KProperty.select]` colA `[and][ColumnsSelectionDsl.and]` colB `[`}`][KProperty.select]` }`
     *
     * ## NOTE: 
     * If you get a warning `CANDIDATE_CHOSEN_USING_OVERLOAD_RESOLUTION_BY_LAMBDA_ANNOTATION`, you
     * can safely ignore this. It is caused by a workaround for a bug in the Kotlin compiler
     * ([KT-64092](https://youtrack.jetbrains.com/issue/KT-64092/OVERLOADRESOLUTIONAMBIGUITY-caused-by-lambda-argument)).
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.allColsExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    @Suppress("INAPPLICABLE_JVM_NAME")
    @JvmName("KPropertyDataRowInvoke")
    public operator fun <C, R> KProperty<DataRow<C>>.invoke(selector: ColumnsSelector<C, R>): ColumnSet<R> =
        select(selector)

    /**
     * ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than the [cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols] filter, because now all
     * operations of the DSL are at your disposal.
     *
     * The scope of the new DSL instance is relative to
     * the [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] you are selecting from.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * ### Check out: [Grammar][org.jetbrains.kotlinx.dataframe.api.SelectColumnsSelectionDsl.Grammar]
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { Type::myColGroup.`[select][KProperty.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { DataSchemaType::myColGroup `[`{`][KProperty.select]` colA `[and][ColumnsSelectionDsl.and]` colB `[`}`][KProperty.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.allColsExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    public operator fun <C, R> KProperty<C>.invoke(selector: ColumnsSelector<C, R>): ColumnSet<R> =
        columnGroup(this).select(selector)

    /**
     * ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than the [cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols] filter, because now all
     * operations of the DSL are at your disposal.
     *
     * The scope of the new DSL instance is relative to
     * the [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] you are selecting from.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * ### Check out: [Grammar][org.jetbrains.kotlinx.dataframe.api.SelectColumnsSelectionDsl.Grammar]
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { "myColGroup".`[select][String.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { "myColGroup" `[{][String.select]` colA `[and][ColumnsSelectionDsl.and]` colB `[}][String.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.allColsExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    public operator fun <R> String.invoke(selector: ColumnsSelector<*, R>): ColumnSet<R> =
        select(selector)

    /**
     * ## Select from [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]
     *
     * Perform a selection of columns using the [Columns Selection DSL][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl] on
     * any [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup]. This is more powerful than the [cols][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.cols] filter, because now all
     * operations of the DSL are at your disposal.
     *
     * The scope of the new DSL instance is relative to
     * the [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] you are selecting from.
     *
     * The [invoke][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.invoke] operator is overloaded to work as a shortcut for this method.
     *
     * ### Check out: [Grammar][org.jetbrains.kotlinx.dataframe.api.SelectColumnsSelectionDsl.Grammar]
     *
     * #### For example:
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { myColGroup.`[select][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` { someCol `[and][org.jetbrains.kotlinx.dataframe.api.AndColumnsSelectionDsl.and]` `[colsOf][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "myGroupCol" `[{][kotlin.String.select]` "colA" and `[expr][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.expr]` { 0 } `[}][kotlin.String.select]` }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { "pathTo"["myGroupCol"].`[select][org.jetbrains.kotlinx.dataframe.columns.ColumnPath.select]` { "colA" and "colB" } }`
     *
     * `df.`[select][org.jetbrains.kotlinx.dataframe.DataFrame.select]` { it["myGroupCol"].`[asColumnGroup][org.jetbrains.kotlinx.dataframe.DataColumn.asColumnGroup]`()`[() {][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` "colA" and "colB" `[}][org.jetbrains.kotlinx.dataframe.columns.SingleColumn.select]` }`
     *
     * #### Examples for this overload:
     *
     * `df.`[select][DataFrame.select]` { "pathTo"["myColGroup"].`[select][ColumnPath.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { "pathTo"["myColGroup"] `[{][ColumnPath.select]` colA `[and][ColumnsSelectionDsl.and]` colB `[}][ColumnPath.select]` }`
     *
     * `df.`[select][DataFrame.select]` { `[pathOf][pathOf]`("pathTo", "myColGroup").`[select][ColumnPath.select]` { someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() } }`
     *
     * `df.`[select][DataFrame.select]` { `[pathOf][pathOf]`("pathTo", "myColGroup")`[() {][ColumnPath.select]` someCol `[and][ColumnsSelectionDsl.and]` `[colsOf][SingleColumn.colsOf]`<`[String][String]`>() `[}][ColumnPath.select]` }`
     *
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;
     *
     *
     * See also [except][org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl.except]/[allExcept][org.jetbrains.kotlinx.dataframe.api.AllExceptColumnsSelectionDsl.allColsExcept] for the inverted operation of this function.
     *
     * @param [selector] The [ColumnsSelector][org.jetbrains.kotlinx.dataframe.ColumnsSelector] to use for the selection.
     * @receiver The [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup] to select from.
     * @throws [IllegalArgumentException] If [this] is not a [ColumnGroup][org.jetbrains.kotlinx.dataframe.columns.ColumnGroup].
     * @return A [ColumnSet][org.jetbrains.kotlinx.dataframe.columns.ColumnSet] containing the columns selected by [selector].
     * @see [SingleColumn.except]
     */
    public operator fun <R> ColumnPath.invoke(selector: ColumnsSelector<*, R>): ColumnSet<R> =
        select(selector)

    // endregion
}
