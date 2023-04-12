package org.jetbrains.kotlinx.dataframe.explainer

import com.beust.klaxon.JsonObject
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import org.jetbrains.kotlinx.dataframe.AnyCol
import org.jetbrains.kotlinx.dataframe.AnyFrame
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.RowValueFilter
import org.jetbrains.kotlinx.dataframe.api.ColumnsSelectionDsl
import org.jetbrains.kotlinx.dataframe.api.Convert
import org.jetbrains.kotlinx.dataframe.api.FormattedFrame
import org.jetbrains.kotlinx.dataframe.api.Gather
import org.jetbrains.kotlinx.dataframe.api.GroupBy
import org.jetbrains.kotlinx.dataframe.api.Merge
import org.jetbrains.kotlinx.dataframe.api.Pivot
import org.jetbrains.kotlinx.dataframe.api.PivotGroupBy
import org.jetbrains.kotlinx.dataframe.api.ReducedPivot
import org.jetbrains.kotlinx.dataframe.api.ReducedPivotGroupBy
import org.jetbrains.kotlinx.dataframe.api.Split
import org.jetbrains.kotlinx.dataframe.api.SplitWithTransform
import org.jetbrains.kotlinx.dataframe.api.Update
import org.jetbrains.kotlinx.dataframe.api.format
import org.jetbrains.kotlinx.dataframe.api.frames
import org.jetbrains.kotlinx.dataframe.api.into
import org.jetbrains.kotlinx.dataframe.api.print
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.jetbrains.kotlinx.dataframe.api.values
import org.jetbrains.kotlinx.dataframe.api.where
import org.jetbrains.kotlinx.dataframe.api.with
import org.jetbrains.kotlinx.dataframe.columns.ColumnSet
import org.jetbrains.kotlinx.dataframe.io.DataFrameHtmlData
import org.jetbrains.kotlinx.dataframe.io.DisplayConfiguration
import org.jetbrains.kotlinx.dataframe.io.sessionId
import org.jetbrains.kotlinx.dataframe.io.tableInSessionId
import org.jetbrains.kotlinx.dataframe.io.toHTML

private fun convertToHTML(dataframeLike: Any): DataFrameHtmlData {
    fun DataFrame<*>.toHTML() = toHTML(DisplayConfiguration(), getFooter = { "" })
    fun FormattedFrame<*>.toHTML1() = toHTML(DisplayConfiguration())

    return when (dataframeLike) {
        is Pivot<*> -> dataframeLike.frames().toDataFrame().toHTML()
        is ReducedPivot<*> -> dataframeLike.values().toDataFrame().toHTML()
        is PivotGroupBy<*> -> dataframeLike.frames().toHTML()
        is ReducedPivotGroupBy<*> -> dataframeLike.values().toHTML()
        is SplitWithTransform<*, *, *> -> dataframeLike.into().toHTML()
        is Merge<*, *, *> -> dataframeLike.into("merged").toHTML()
        is Gather<*, *, *, *> -> dataframeLike.into("key", "value").toHTML()
//        is Update<*, *> -> DataFrameHtmlData(body = "<p>${dataframeLike::class}</p>")
        is Update<*, *> -> dataframeLike.df.let {
            var it = it.format(dataframeLike.columns as ColumnsSelectionDsl<Any?>.(it: ColumnsSelectionDsl<Any?>) -> ColumnSet<*>)
            if (dataframeLike.filter != null) {
                it = it.where(dataframeLike.filter as RowValueFilter<Any?, Any?>)
            }
            it.with {
                background(rgb(152, 251, 152))
            }
        }
            .toHTML1()
        is Convert<*, *> -> DataFrameHtmlData(body = "<p>${dataframeLike::class}</p>")
        is FormattedFrame<*> -> dataframeLike.toHTML1()
        is GroupBy<*, *> -> dataframeLike.toDataFrame().toHTML()
        is AnyFrame -> dataframeLike.toHTML()
        is AnyCol -> dataframeLike.toDataFrame().toHTML()
        is DataRow<*> -> dataframeLike.toDataFrame().toHTML()
        is Split<*, *> -> dataframeLike.toDataFrame().toHTML()
//        is MoveClause<*, *>-> null
//        is RenameClause<*, *> -> null
//        is ReplaceClause<*, *> -> null
//        is GroupClause<*, *> -> null
//        is InsertClause<*> -> null
//        is FormatClause<*, *> -> null
        else -> throw IllegalArgumentException("Unsupported type: ${dataframeLike::class}")
    }
}

private fun convertToDescription(dataframeLike: Any): String {
    return when (dataframeLike) {
        is AnyFrame -> dataframeLike.let { "DataFrame: rowsCount = ${it.rowsCount()}, columnsCount = ${it.columnsCount()}" }
        is Pivot<*> -> "Pivot"
        is ReducedPivot<*> -> "ReducedPivot"
        is PivotGroupBy<*> -> "PivotGroupBy"
        is ReducedPivotGroupBy<*> -> "ReducedPivotGroupBy"
        is SplitWithTransform<*, *, *> -> "SplitWithTransform"
        is Split<*, *> -> "Split"
//        is MoveClause<*, *> -> "Move"
//        is RenameClause<*, *> -> "Rename"
//        is ReplaceClause<*, *> -> "Replace"
//        is GroupClause<*, *> -> "Group"
//        is InsertClause<*> -> "Insert"
//        is FormatClause<*, *> -> "Format"
        is Merge<*, *, *> -> "Merge"
        is Gather<*, *, *, *> -> "Gather"
        is Update<*, *> -> "Update"
        is Convert<*, *> -> "Convert"
        is FormattedFrame<*> -> "FormattedFrame"
        is GroupBy<*, *> -> "GroupBy"
        is DataRow<*> -> "DataRow"
        else -> "TODO"
    }.escapeHTML()
}

annotation class TransformDataFrameExpressions

fun main() {
    File("build/dataframes")
        .walkTopDown()
        .filter {
            it.nameWithoutExtension.startsWith("org.jetbrains")
        }
        // org.ClassName.functionName_properties
        // <dataFrame src="org.jetbrains.kotlinx.dataframe.samples.api.Modify.addDfs.html"/>
        .groupBy {
            it.nameWithoutExtension.substringBefore("_")
        }
        .mapValues { (name, files) ->
            val target = File("../docs/StardustDocs/snippets")
            val original = files
                .firstOrNull { it.nameWithoutExtension.contains("properties") }
                ?: files.first()
            original.copyTo(File(target, "$name.html"), overwrite = true)
        }
}

object PluginCallback {
//    val strings = mutableListOf<String>()
//    val names = mutableListOf<String>()
//    val dfs = mutableListOf<String>()

    var i = AtomicInteger(0)
    val names = mutableMapOf<String, List<String>>()
    val expressionsByStatement = mutableMapOf<Int, List<Expression>>()

    data class Expression(
        val source: String,
        val containingClassFqName: String?,
        val containingFunName: String?,
        val df: Any
    )

    fun start() {
        expressionsByStatement.clear()
    }

    fun save() {
        if (i.get() == 0) return
        sessionId = 0
        tableInSessionId = 0
        var output = DataFrameHtmlData.tableDefinitions() + DataFrameHtmlData(
            style = """
                body {
                    font-family: "JetBrains Mono",SFMono-Regular,Consolas,"Liberation Mono",Menlo,Courier,monospace;
                }       
                
                :root {
                    color: #19191C;
                    background-color: #fff;
                }
                
                :root[theme="dark"] {
                    background-color: #19191C;
                    color: #FFFFFFCC
                }
                
                details details {
                    margin-left: 20px; 
                }
                
                summary {
                    padding: 6px;
                }
            """.trimIndent()
        )

        // make copy to avoid concurrent modification exception
        val statements = expressionsByStatement.toMap()
        when (statements.size) {
            0 -> TODO("wtf")
            1 -> {
                output += expressionOutputs(statements.values.single(), open = false)
            }
            else -> {
                statements.forEach { (index, expressions) ->
                    var details: DataFrameHtmlData = expressionOutputs(expressions, open = true)

                    details = details.copy(
                        body =
                        """
                        <details>
                        <summary>${expressions.joinToString(".") { it.source }
                            .also {
                                if (it.length > 95) TODO("expression is too long ${it.length}. better to split sample in multiple snippets")
                            }
                            .escapeHTML()}</summary>
                        ${details.body}
                        </details>
                        <br>
                        """.trimIndent()
                    )

                    output += details
                }
            }
        }
        val input = expressionsByStatement.values.first().first()
        val name = "${input.containingClassFqName}.${input.containingFunName}"
        val destination = File("build/dataframes").also {
            it.mkdirs()
        }
        output.writeHTML(File(destination, "$name.html"))
    }

    private fun expressionOutputs(
        expressions: List<Expression>,
        open: Boolean,
    ): DataFrameHtmlData {
//        val attribute = if (open) " open" else ""
        val attribute = ""
        var data = DataFrameHtmlData()
        if (expressions.size < 2) error("Sample without output or input (i.e. function returns some value)")
        for ((i, expression) in expressions.withIndex()) {
            when (i) {
                0 -> {
                    val table = convertToHTML(expression.df)
                    val description = table.copy(
                        body = """
                                    <details$attribute>
                                    <summary>Input ${convertToDescription(expression.df)}</summary>
                                     ${table.body}
                                    </details>
                        """.trimIndent()
                    )
                    data += description
                }

                expressions.lastIndex -> {
                    val table = convertToHTML(expression.df)
                    val description = table.copy(
                        body = """
                                    <details$attribute>
                                    <summary>Output ${convertToDescription(expression.df)}</summary>
                                     ${table.body}
                                    </details>
                        """.trimIndent()
                    )
                    data += description
                }

                else -> {
                    val table = convertToHTML(expression.df)
                    val description = table.copy(
                        body = """
                                    <details>
                                    <summary>Step $i: ${convertToDescription(expression.df)}</summary>
                                     ${table.body}
                                    </details>
                        """.trimIndent()
                    )
                    data += description
                }
            }
        }
        return data
    }

    var action: (String, String, Any, String, String?, String?, String?, Int) -> Unit =
        { source, name, df, id, receiverId, containingClassFqName, containingFunName, statementIndex ->
            i.incrementAndGet()
            expressionsByStatement.compute(statementIndex) { _, list ->
                val element = Expression(source, containingClassFqName, containingFunName, df)
                list?.plus(element) ?: listOf(element)
            }
            //        strings.add(string)
            //        names.add(name)
            // Can be called with the same name multiple times, need to aggregate samples by function name somehow?
            // save schema
            val path = "$containingClassFqName.$containingFunName.html"
            // names.compute(path) {  }
            //        dfs.add(path)
            if (df is AnyFrame) {
                println(source)
//                df.print()
                println(id)
                println(receiverId)
            } else {
                println(df::class)
            }
            File("build/out").let {
                val json = JsonObject(
                    mapOf(
                        "string" to source,
                        "name" to name,
                        "path" to path,
                        "id" to id,
                        "receiverId" to receiverId,
                    )
                ).toJsonString()
                it.appendText(json)
                it.appendText(",\n")
            }
            println(path)
            if (df is AnyFrame) {
                df.print()
            } else {
                println(df::class)
            }
            //        convertToHTML(df).writeHTML(File("build/dataframes/$path"))
        }

    fun doAction(
        string: String,
        name: String,
        df: Any,
        id: String,
        receiverId: String?,
        containingClassFqName: String?,
        containingFunName: String?,
        statementIndex: Int
    ) {
        action(string, name, df, id, receiverId, containingClassFqName, containingFunName, statementIndex)
    }
}

internal fun String.escapeHTML(): String {
    val str = this
    return buildString {
        for (c in str) {
            when {
                c.code > 127 || c == '\'' || c == '\\' -> {
                    append("&#")
                    append(c.code)
                    append(';')
                }
//                c == '<' -> append("&lt;")
//                c == '>' -> append("&gt;")
                c == '"' -> append("&quot;")
                c == '<' -> append("&amp;lt;")
                c == '>' -> append("&amp;gt;")
//                c == '"' -> append("&amp;quot;")
                c == '&' -> append("&amp;")
                else -> {
                    append(c)
                }
            }
        }
    }
}
