package org.jetbrains.dataframe

import io.kotlintest.shouldBe
import org.jetbrains.dataframe.io.*
import org.junit.Ignore
import org.junit.Test
import kotlin.reflect.full.withNullability

class ReadTests {

    @Test
    @Ignore
    fun readCensus(){
        val df = readCSV("../jupyter notebooks/Kotlin/Census/cleanedCensus.csv")

        println(df.summary())
    }



    @Test
    fun parseJson1(){
        val json = """[
                {"a":1, "b":"text"},
                {"a":2, "b":5, "c":4.5}
            ]""".trimIndent()
        val df = TypedDataFrame.fromJsonStr(json)
        df.ncol shouldBe 3
        df.nrow shouldBe 2
        df["a"].type shouldBe getType<Int>()
        df["b"].type shouldBe getType<Comparable<*>>()
        df["c"].type shouldBe getType<Double?>()
    }

    @Test
    fun parseJson2(){
        val json = """[
                {"a":"text"},
                {"a":{"b":2}},
                {"a":[6,7,8]}
            ]""".trimIndent()
        val df = TypedDataFrame.fromJsonStr(json)
        println(df)
        df.ncol shouldBe 1
        df.nrow shouldBe 3
        val group = df["a"] as GroupedColumnBase<*>
        group.ncol shouldBe 3
        group["b"].type shouldBe getType<Int?>()
        group["value"].type shouldBe getType<String?>()
        group["array"].type shouldBe getType<List<Int>>()
    }

    @Test
    fun parseJson3(){
        val json = """[
                {"a":[3, 5]},
                {},
                {"a":[3.4, 5.6]}
            ]""".trimIndent()
        val df = TypedDataFrame.fromJsonStr(json)
        df.ncol shouldBe 1
        df.nrow shouldBe 3
        df["a"].type shouldBe getType<List<Number>>()
        df[1]["a"] shouldBe emptyList<Int>()
    }

    @Test
    fun parseJson4(){
        val json = """[
                {"a":[ {"b":2}, {"c":3} ]},
                {"a":[ {"b":4}, {"d":5} ]}
            ]""".trimIndent()
        val df = TypedDataFrame.fromJsonStr(json)
        df.ncol shouldBe 1
        df.nrow shouldBe 2
        println(df)
        val group = df["a"] as TableColumn<*>

    }
}