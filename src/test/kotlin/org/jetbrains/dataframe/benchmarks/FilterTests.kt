package org.jetbrains.dataframe.benchmarks

import org.jetbrains.kotlinx.dataframe.DataColumn
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataFrameBase
import org.jetbrains.kotlinx.dataframe.DataRowBase
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.filterFast
import org.jetbrains.kotlinx.dataframe.io.read
import org.jetbrains.kotlinx.dataframe.neq
import org.jetbrains.kotlinx.dataframe.typed
import org.junit.Ignore
import org.junit.Test
import kotlin.system.measureTimeMillis

class FilterTests {

    val path = "data/census.csv"
    val df = DataFrame.read(path)

    interface DataRecord {
        val Referer: String?
    }

    val DataFrameBase<DataRecord>.Referer: DataColumn<String?> @JvmName("DataRecord_Referer") get() = this["Referer"] as DataColumn<String?>
    val DataRowBase<DataRecord>.Referer: String? @JvmName("DataRecord_Referer") get() = this["Referer"] as String?

    val typed = df.typed<DataRecord>()

    val n = 100

    @Test
    @Ignore
    fun slow() {
        measureTimeMillis {
            for (i in 0..n)
                typed.filter { Referer != null }
        }.let { println(it) }
    }

    @Test
    @Ignore
    fun fast() {
        measureTimeMillis {
            for (i in 0..n)
                typed.filterFast { Referer neq null }
        }.let { println(it) }
    }
}
