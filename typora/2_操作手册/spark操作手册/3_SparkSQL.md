# 第三章 spark-SQL

`MapReduce` -> `hive`  为了解决`mr`程序编程困难.
`spark-core` -> `spark-sql` 为了解决`rdd`编程困难

## 3.1 spark-core 和 spark-SQL 的对比
`spark-core` : `rdd`

`spark-sql` : `DataFrame, DataSet`

## 3.2 使用 DataFrame 编程

1. 创建 DF
 - 使用数据源(json, jdbc, csv,..)
 - 从 rdd 转换过来
 - 通过专门查询 hive
2. 编程风格
  - SQL 风格(重点)
      写SQL语句
   ```scala
// 读取各种形式的数据
spark.read.
csv   format   jdbc   json   load   option   options   orc   parquet   schema   table   text   textFile  

// 临时表(内存级别)
  df.createTempView("user")
  df.createOrReplaceTempView("user")
  df.createGlobalTempView("person")  // 不常用
  
  val t1 = spark.sql("select * from person") // t1 类型为 DF
  spark.sql("select * from global_temp.person").show
  spark.newSession..sql("select * from global_temp.person").show

  spark.newSession // 开启新的会话
   ```

- DSL风格(了解)

  为了给那些不会SQL的人准备.

  ```scala
  df.printSchema // 查看 Schema 信息
  
  df.filter("salary > 3000").select("name", "salary").show 
  // select、filter 无先后顺序要求
  
  // 支持参数引用
  df.select($"name", $"salary"+1).filter("salary > 4000").show
  
  // 等价于 select salary, count(*) from user group by salary
  df.groupBy("salary").count.show 
  
  // select name, sum(salary) from user group by name
  df.groupBy("name").sum("salary").show
  ```

## 3.3 RDD 和 DF 的转换

RDD 可以转成 DF，DF 也可以转成 RDD。

### 3.3.1 RDD 转 DF

1. 在 rdd 中封装存储样例类的对象, 直接调用`rdd.toDF`

   ```scala
   object RDD2DF {
     def main(args: Array[String]): Unit = {
       val spark = SparkSession.builder()
         .master("local[2]")
         .appName("RDD2DF")
         .getOrCreate()
       import spark.implicits._
       val list = User(10, "zs") :: User(15, "ls") :: User(20, "ww") :: Nil
       val df: DataFrame = list.toDF
       df.show()
       spark.close()
     }
   }
   
   case class User(age: Int, name: String)
   ```

2. 如果不是样例类, 则需要手动指定列名`rdd.toDF(c1, c2, ...)`

   ```scala
   object RDD2DF2 {
     def main(args: Array[String]): Unit = {
       val spark = SparkSession.builder()
         .master("local[2]")
         .appName("RDD2DF2")
         .getOrCreate()
       /*    val df = spark.read.json("E:\\hiveinput\\employees.json")
           val t = df.createOrReplaceTempView("t")
           spark.sql("select * from t where salary >= 4000").show()*/
       import spark.implicits._
       val rdd = spark.sparkContext.parallelize(List((10, "a"), (20, "b"), (30, "c"), (40, "d")))
       val df = rdd.toDF("age", "name")
       df.printSchema()
       df.show()
       spark.close()
     }
   }
   ```

   注意: 要先导入隐式转换。隐式是存在于`SparkSession`对象中的。

   总结: 从简单 -> 复杂类型, 需要额外提供样例类，而且要隐式转换。

### 3.3.2 DF 转 RDD

```scala
object DF2RDD {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[2]")
      .appName("DF2RDD")
      .getOrCreate()
    val df: DataFrame = spark.read.json("E:\\hiveinput\\employees.json")
    df.printSchema()
    val rdd: RDD[Row] = df.rdd

    val rdd2 = rdd.map(row => {
      // index 传参，写死类型，需判断是否为空
      val name = if (row.isNullAt(0)) null else row.getString(0)
      val salary = if (row.isNullAt(1)) null else row.getLong(1)

      // index 传参，自动类型推导，没有则为 null
      val name = row.get(0)
      val salary = row.get(1)

      // key 值传参，指定泛型，没有自动赋默认值
      val name = row.getAs[String]("name")
      val salary = row.getAs[Long]("salary")
      (name, salary)
    })

    rdd2.collect.foreach(println)
    spark.close()
  }
}
```

关于 Row

1. 其实可以把 RDD 看作 Row 的一个集合,  也有下标 0, 1, 2, 3。
2. 存在目的是存储数据。
3. 使用`row.getInt(0)  row.getString ...`转换后访问元素。

## 3.4 使用 DataSet 编程

相比于`DataFrame`，`DataSet`为强类型，更加安全。

> 注意: SQL 查询一般只用在弱类型的 df 上。(也可以用到 ds 上)
>
> ```scala
> ds.filter(_ > 30).show()
> ```

```scala
rdd和ds转换
rdd->ds(简单到复杂: 需要样例类)
	1. 在rdd中存储样例类
	2. rdd.toDS
ds->rdd
	ds.rdd   (与 df.rdd 用法十分相似)
// ds 中存放的就是对象
// df 中存放的是 Row
import spark.implicits._
    val list = User(10, "lisi") :: User(20, "zs") :: User(15, "ww") :: Nil
    val rdd = spark.sparkContext.parallelize(list)
    val ds: Dataset[User] = rdd.toDS()
    val df: DataFrame = rdd.toDF()
```

```scala
df -> ds
 1. 有样例类
 2. df.as[样例类]

ds -> df
	ds.toDF
// ds 不能自定义列名，只能使用 _1、_2 进行访问

object DSDF {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .master("local[2]")
      .appName("DSTest02")
      .getOrCreate()
    import spark.implicits._

    val df = spark.read.json("E:\\hiveinput\\employees.json")
    df.show()
    println("=================")
    val ds = df.as[People]
    ds.show()
  }
}

case class People(name: String, salary: Long)
```

三者之间的转换关系：

![image-20200514230540511](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200514230540511.png)



## 3.5 自定义函数

`UDF` 1进1出

`UDAF` 聚合函数  多进1出

`UDTF` 1进多出 (由`flatMap`实现) 

> hive: 如何自定义函数
>
> 定义类, 继承系统提供的函数, 打包, 放在 hive 的 lib 目录, 还需要注册...

### 3.5.1 UDF

`spark`中自定义 UDF 函数极其简单: `scala` 是函数式编程, 因此自定义函数直接使用匿名函数非常方便。

- `udf`

  ```scala
  // 匿名 UDF 函数最大支持 22 输入 1 输出
  spark.udf.register("myToUpper", (s: String) => s.toUpperCase)
  ```


### 3.5.2 UDAF

- `udaf`

  - `UserDefinedAggregateFunction`

    只能用在`sql`语句中。

  ```scala
  package com.spark.sql.sparksql02.udf
  
  import java.text.DecimalFormat
  
  import com.spark.sql.sparksql02.people
  import org.apache.spark.sql.{Row, SparkSession}
  import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
  import org.apache.spark.sql.types.{DataType, DoubleType, LongType, StringType, StructField, StructType}
  
  /**
   * @time 2020/5/14 - 14:25
   * @Version 1.0
   * @Author Jeffery Yi
   */
  object Udaf2 {
    def main(args: Array[String]): Unit = {
      val spark = SparkSession.builder()
        .master("local[2]")
        .appName("Udaf2")
        .getOrCreate()
  
      import spark.implicits._
      spark.udf.register("Avg", new MyAvg2)
      val df = spark.read.json("E:\\hiveinput\\employees.json")
      val ds = df.as[people]
      df.createOrReplaceTempView("t2")
      ds.createOrReplaceTempView("t3")
      spark.sql("select Avg(salary) from t2").show()
      spark.sql("select Avg(salary) from t3").show()
      spark.sql("select Avg(salary) from json.`E:\\hiveinput\\employees.json`").show
  
      spark.stop()
    }
  }
  
  class MyAvg2 extends UserDefinedAggregateFunction {
      // 输入参数，可以写多个
    override def inputSchema: StructType = StructType(StructField("ele", DoubleType) :: Nil)
  
    override def bufferSchema: StructType = StructType(StructField("sum", DoubleType) :: StructField("count", LongType) :: Nil)
  
    override def dataType: DataType = StringType
  
    override def deterministic: Boolean = true
  
    override def initialize(buffer: MutableAggregationBuffer): Unit = {
      buffer(0) = 0D
      buffer(1) = 0L
    }
  
    override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
      if (!input.isNullAt(0)){
        buffer(0) = buffer.getDouble(0) + input.getDouble(0)
        buffer(1) = buffer.getLong(1) + 1
      }
    }
  
    override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
  // 注意：buffer(0)、buffer(1) 只能赋值，不能直接参与运算；必须 get 到其中的值之后才能参与运算
      buffer1(0) = buffer1.getDouble(0) + buffer2.getDouble(0)
      buffer1(1) = buffer1.getLong(1) + buffer2.getLong(1)
    }
  
    override def evaluate(buffer: Row): Any = {
      new DecimalFormat(".00").format(buffer.getDouble(0) / buffer.getLong(1))
    }
  }
  
  ```

## 3.6 数据源

### 3.6.1 读

- 一种通用写法

  ```scala
  spark.read.format("json").load("examples/src/main/resources/people.json")
  ```

  `json` 表示文件格式

  `load` 表示路径，一次可以读一个目录下所有文件

  `format`默认格式为`parquet`，即格式为`parquet`时可以省略`format("json")`。

- 一种专用写法

  ```scala
  spark.read.json("路径")   // 等同于 foramt("json").load("路径")
  ```

正常情况, 如果要执行`sql`, 要先创建及临时表. 其实`spark-sql`支持直接在文件上执行`sql`

```scala
spark.sql("select * from json.`examples/src/main/resources/people.json`").show
```

### 3.6.2 写

- 一种通用的写

  ```scala
  df.write.format("json").save("./user")
  ```

  在写的时候, 如果路径已经存在, 则默认抛出异常。

  可以使用模式来控制:

  ```scala
  // mode 需跟在 read 或 write 后面
  .mode("error")  // 默认值
  "append"  //追加
  "overwrite"  // 覆盖
  "ignore"  // 如果文件存在忽略这次操作. 不存在, 则写入
  ```

- 一种专用的写

  ```scala
  df.write.mode("append").json("./user")
  ```

### 3.6.3 补充

```scala
// read 的输入格式为：DataFrameReader
val read: DataFrameReader = spark.read
// write 的输出格式为：DataFrameWriter
val writer: DataFrameWriter[Row] = df.write
```

## 3.7 JDBC 读写

### 3.7.1 JDBC 读

```scala
object JDBCReader {

  private val url = "jdbc:mysql://hadoop103:3306/test"
  private val user = "root"
  private val pwd = "root"

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
          .master("local[2]")
          .appName("JDBCReader")
          .getOrCreate()

    // 通用的读法
    val df1 = spark.read.format("jdbc")
      .options(Map(
        ("url", url),
        ("user", user),
        ("password", pwd),
        ("dbtable", "user01")
      ))
      .load()
    df1.show()
      
	// 专用的读法
    val prep = new Properties()
    prep.setProperty("user", user)
    prep.setProperty("password", pwd)

    val df2 = spark.read.jdbc(url, "user", prep)
    df2.show()
    spark.stop()
  }
}
```

### 3.7.2 JDBC 写

jdbc 写会自动建表，若表存在则默认报错，除非更改 mode。overwrite mode 实际上是先删再写（快速刷新就能看出来）。

```scala
object JDBCWriter {
  private val url = "jdbc:mysql://hadoop103:3306/test"
  private val user = "root"
  private val pwd = "root"
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
          .master("local[2]")
          .appName("JDBCWriter")
          .getOrCreate()

    val df1 = spark.read.json("E:\\hiveinput\\employees.json")
    // 通用写
    df1.write.mode(SaveMode.Overwrite)
      .format("jdbc")
      .option("url", url)
      .option("user", user)
      .option("password", pwd)
      .option("dbtable", "user01")
      .save()

    // 专用写
    val prep = new Properties()
    prep.setProperty("user", user)
    prep.setProperty("password", pwd)
    df1.write.mode("append")
        .jdbc(url, "user01", prep)
    spark.stop()
  }
}
```



## 3.8 spark 整合 hive

### 3.8.1 两种整合方式:

1. `hive on spark`

   `hive on mr, hive on tez(hive 3.0开始, 默认)`

   由几家比较大的公司联合开发，但是到目前应用人数极少, 还处在实验版本。主要是`hive的版本和spark`的兼容性问题比较大。

2. `spark on hive`(`spark`官方整出来)

   其实就是`spark-sql`对接 `hive` 数据。我们使用的官方的版本, 内部已经编译进去了`hive`。


### 3.8.2 spark on hive 的两种模式

1. 内置`hive`

   - 默认是使用的内置`hive`

   - 企业中一般不会使用这种内置`hive`

2. 接管外置的`hive`(重点)

   数据都在`hive`上, `spark`还要找到`hive`

   （1）把`hive-site.xml`拷贝到`spark`配置目录下(目的: 找到`hive`元数据)

   `tez`的配置去掉

   （2）拷贝`mysql`驱动到`/opt/module/spark-local/jars/`目录下

   （3）如果还有报错: 有些情况下, 需要拷贝`core-site.xml, hdfs-site.xml`

   （4）如果配置了 `lzo` 压缩，需要把相应的 `jar` 包拷贝至 `jars` 目录。

   ```shell
   cp /opt/module/hadoop-2.7.2/share/hadoop/common/hadoop-lzo-0.4.20.jar /opt/module/spark-local/jars/
   ```

   注意：由于在 `core-site.xml` 中定义了 lzo 的 codec，因此也要复制 `core-site.xml` 。

   注意：spark-shell 中的 show 默认显示 20条，show(num) 可以自定义显示条数。
   

### 3.8.3 数仓脚本优化

```scala
// 本地模式
hive=/opt/module/spark-local/bin/spark-sql
// yarn 模式
hive="/opt/module/spark-yarn/bin/spark-sql --master yarn --deploy-mode client"
```

注意:

- 在 yarn 模式下 `spark-sql、spark-shell` 只支持`--deploy-mode client` 不支持`--deploy-mode cluster`
- spark-SQL 不支持非严格模式切换，因此所有 SQL 语句都要符合严格模式语法。

### 3.8.4 spark 中使用 beeline 连接 hiveserver2

1. 启动 thrift 服务器

```shell
sbin/start-thriftserver.sh \
--master yarn \
--hiveconf hive.server2.thrift.bind.host=hadoop103 \
--hiveconf hive.server2.thrift.port=10000 \
# 不必担心端口冲突，hive 和 spark 两个端口都是 10000 即可
# hiveserver2 的本质也是 thriftserver
```

2. 启动 beeline 客户端

```bash
bin/beeline
# 然后输入
!connect jdbc:hive2://hadoop103:10000
# 然后按照提示输入用户名和密码
```

### 3.8.5 代码中访问 hive

1. `sparkSession` 需支持`hive`  

   ```scala
   .enableHiveSupport()
   ```

2. 添加依赖

   ```
   <dependency>
       <groupId>org.apache.spark</groupId>
       <artifactId>spark-hive_2.11</artifactId>
       <version>2.1.1</version>
   </dependency>
   ```

3. `copy hive-site.xml 到 resources`目录下（如有必要，拷贝 core-site 和 hdfs-site。但要注意的是 lzo 在 Win 下不能直接运行，需要打包到 linux 去执行）

#### 3.8.5.1 读 hive 表中的数据

```scala
object HiveTest {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "atguigu")
    val spark = SparkSession.builder()
      .master("local[*]")
      .appName("HiveTest")
      .enableHiveSupport()
      .getOrCreate()

    spark.sql("use gmall").show()
    spark.sql("select count(*) from ads_uv_count").show()

    spark.stop()
  }
}
```

#### 3.8.5.2 写 hive 表中的数据

```scala
object HiveWrite {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "atguigu")
    val spark = SparkSession.builder()
      .master("local[*]")
      .appName("HiveWrite")
      .enableHiveSupport()
      .config("spark.sql.warehouse.dir", "hdfs://hadoop102:9000/user/hive/warehouse")
      .getOrCreate()
    import spark.implicits._

//    spark.sql("create database test1128").show
    spark.sql("use test1128")
//    val df = spark.read.json("E:\\hiveinput\\employees.json")
    val df = List(("a", 1000L), ("b", 2000L)).toDF("n", "s")
    df.printSchema()

// 写法1：saveAsTable()  在保存的时候, 看列名, 只要列名一致, 顺序不重要, 能自动建表
//    df.write.mode("append").saveAsTable("user01")

// 写法2：insertInto() 不看列名, 只看顺序(类型), 要求表必须已经存在
//    df.write.mode("overwrite").insertInto("user01")

// 写法3：使用 hive 的 insert 语句
    spark.sql("insert into table user01 values('zs', 2500)")
    spark.sql("select * from user01").show()
    spark.stop()
  }
}
```

小结：

```scala
写法1：saveAsTable() 看列名, 只要列名一致, 顺序不重要, 能自动建表。其 overwrite 会重新建表，调整表的列结构。
写法2：insertInto() 不看列名, 只看顺序(类型), 要求表必须已经存在。其 overwrite 只能清除表中数据。
```

## 3.9 SparkSQL 案例

```scala
object SQLApp {
  def main(args: Array[String]): Unit = {
    System.setProperty("HADOOP_USER_NAME", "atguigu")
    val spark = SparkSession.builder()
      .master("local[*]")
      .appName("SQLApp")
      .enableHiveSupport()
      .config("spark.sql.warehouse.dir", "hdfs://hadoop102:9000/user/hive/warehouse")
      // 在`spark-sql`里如果有`shuffle`, 那么`shuffle`后的分区默认是`200`
      .config("spark.sql.shuffle.partitions", 10)
      .getOrCreate()
      // 哪里发生了聚合，就去哪里分析聚合函数的需求
    spark.udf.register("cityRemark", new CityRemarkUDAF)
    // spark.sql("create database sparkpractice").show
    spark.sql("use sparkpractice")
    spark.sql(
      """
        |select area, product_name, city_name, product_id
        |from user_visit_action uva
        |join product_info pi
        |on uva.click_product_id = pi.product_id
        |join city_info ci
        |on uva.city_id = ci.city_id
        |""".stripMargin).createOrReplaceTempView("t1")
    spark.sql(
      """
        |select area, product_name, count(*) count, cityRemark(city_name) rmk
        |from t1
        |group by area, product_name
        |""".stripMargin).createOrReplaceTempView("t2")
    spark.sql(
      """
        |select area, product_name, count, rmk, rank() over (partition by area order by count desc) rk
        |from t2
        |""".stripMargin).createOrReplaceTempView("t3")
    spark.sql(
      """
        |select area, product_name, count, rmk
        |from t3
        |where rk <= 3
        |""".stripMargin)
        .coalesce(1)
        .write
        .mode(SaveMode.Overwrite)
        .saveAsTable("result")
//      .show(30, false) // 参数2: 是否截断. 如果内容太长, 默认为 true(截断) ...

    spark.stop()
  }
}

class CityRemarkUDAF extends UserDefinedAggregateFunction {
  // 输入格式
  override def inputSchema: StructType = StructType(StructField("city", StringType) :: Nil)
  // 缓存格式
  override def bufferSchema: StructType = StructType(StructField("map", MapType(StringType, LongType)) :: StructField("total", LongType) :: Nil)
  // 输出格式
  override def dataType: DataType = StringType
  // 输出一致性
  override def deterministic: Boolean = true
  // 初始化
  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = Map[String, Long]()
    buffer(1) = 0L
  }
  // 分区内聚合
  // 注意：buffer(0)、buffer(1) 只能赋值，不能直接参与运算；必须 get 到其中的值之后才能参与运算
  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    input match {
      case Row(city: String) =>
        buffer(1) = buffer.getLong(1) + 1L
        val map = buffer.getMap[String, Long](0)
        buffer(0) = map + (city -> (map.getOrElse(city, 0L) + 1L))
      case _ =>
    }
  }
  // 分区间聚合
  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    val map1 = buffer1.getMap[String, Long](0)
    val total1 = buffer1.getLong(1)

    val map2 = buffer2.getMap[String, Long](0)
    val total2 = buffer2.getLong(1)

    buffer1(1) = total1 + total2
    buffer1(0) = map1.foldLeft(map2) {
      case (map, (city, count)) =>
        map + (city -> (map.getOrElse(city, 0L) + count))
    }
  }

  override def evaluate(buffer: Row): Any = {
    val cityCount = buffer.getMap[String, Long](0)
    val total = buffer.getLong(1)
    val cityCountTop2 = cityCount.toList.sortBy(-_._2).take(2)
    val cityRemarkTop2 = cityCountTop2.map {
      case (city, count) =>
        CityRemark(city, count.toDouble / total)
    }
    val cityRemarks = cityRemarkTop2 :+ CityRemark("其他", cityRemarkTop2.foldLeft(1D)(_ - _.rate))
    cityRemarks.mkString(", ")
  }
}

case class CityRemark(city: String, rate: Double){
  private val f = new DecimalFormat(".00%")
  override def toString: String = {
    s"$city ${f.format(rate)}"
  }
}
```









