# Kotlin DataFrame: data manipulation framework
[![Kotlin](https://img.shields.io/badge/kotlin-1.4.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)

Kotlin DataFrame is a framework for in-memory data manipulation
* Supports hierarchical data layouts
* Reads CSV and JSON
* Provides statically typed API for data manipulation
* Tracks column nullability 
* Generates extension properties for typed data access in Jupyter notebooks

Inspired by [krangl](https://github.com/holgerbrandl/krangl) and [pandas](https://pandas.pydata.org/)

See [API reference](docs/reference.md) for a list of  supported operations 

## Key entities
* `DataColumn` is a named list of values
* `DataFrame` consists of `DataColumns` with unique names and equal size
* `DataRow` is a single row of `DataFrame` and provides a single value for every `DataColumn`
* `DataFrame` can be optionally typed by `DataSchema` to provide typed data access via extension properties  
* `DataSchema` is an interface that describes a single row of `DataFrame`
* `DataColumn` can be one of three types:
    * `MapColumn`: every element of the column is `DataRow`
    * `FrameColumn`: every element of the column is `DataFrame`
    * `ValueColumn`: otherwise 

## Typed data access

DataFrame comes with three levels of API for data access

### Strings

String column names are the easiest way to access data in DataFrame: 
```kotlin
val df = DataFrame.read("titanic.csv")
df.filter { it["survived"] as Boolean }.groupBy("city").max("age")
```
For more complicated expressions this API may lead to code mess with plenty type casts:
```kotlin
df.filter { (it["surived"] as Boolean) && (it["home"] as String).endsWith("NY") && (it["age"] as Int?) in 10..20 }
```  
And solution is...

### Column Accessors

For frequently accessed columns type casting can be reduced by `ColumnAccessors`:   
```kotlin
val survived by column<Boolean>()
val home by column<String>()
val age by column<Int?>()
```
Now columns can be accessed in a type-safe way:
```kotlin
df.filter { it[survived] && it[home].endsWith("NY") && it[age] in 10..20 }
```
or simplier, using 'invoke' operator of column accessors:
```kotlin
df.filter { survived() && home().endsWith("NY") && age() in 10..20 }
```

### Extension properties
Within Jupyter Kernel there is even more straightforward way to access data. 
After every new REPL line execution all new global variables of type DataFrame are analyzed and extension properties 
for data access are generated:
```kotlin
val df = DataFrame.read("titanic.csv")
```
Now data can be accessed just by "." member accesor, with full completion support:
```kotlin
df.filter { it.survived && it.home.endsWith("NY") && it.age in 10..20 }
```
"it" can be ommited:    
```kotlin
df.filter { survived && home.endsWith("NY") && age in 10..20 }
```
