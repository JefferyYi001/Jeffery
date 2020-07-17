# 第四章 Spark Streaming

处理流式数据，接近实时处理。

背压机制（Spark Streaming Backpressure）: 根据 JobScheduler 反馈作业的执行信息来动态调整 Receiver 数据接收率。通过属性 spark.streaming.backpressure.enabled 来控制是否启用backpressure 机制，默认值 false，即不启用。



## 4.1 Dstream 的创建

### 4.1.1 通过 socket 来创建

- 一般用于测试或学习
- 生产环境不会使用

```scala
object SocketDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("SocketDemo").setMaster("local[*]")
    val stc = new StreamingContext(conf, Seconds(3))

    val stream = stc.socketTextStream("hadoop103", 9999)
    val result = stream.flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)
    result.print

    stc.start()
    stc.awaitTermination()
  }
}
// Linux NetCat 启动命令：nc -k -l hadoop103 9999
```

### 4.1.2 通过 RDD 队列来创建

- 一般用于做压力测试
- 测试集群的计算能力

```scala
object QueueDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("QueueDemo").setMaster("local[*]")
    val stc = new StreamingContext(conf, Seconds(3))

    val queue = mutable.Queue[RDD[Int]]()
    val stream = stc.queueStream(queue, false)

    val result = stream.reduce(_ + _)
    result.print

    stc.start()

    while (true){
      val rdd = stc.sparkContext.parallelize(1 to 100)
      queue.enqueue(rdd)

      Thread.sleep(1000)
    }
    stc.awaitTermination()
  }
}
```

### 4.1.3 自定义 Receiver

```scala
package com.jeffery.sparkstreaming01

import java.io.{BufferedReader, InputStreamReader}
import java.net.Socket

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
 * @time 2020/5/16 - 20:30
 * @Version 1.0
 * @Author Jeffery Yi
 */
object MyReceiverTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("MyReceiverTest").setMaster("local[*]")
    val stc = new StreamingContext(conf, Seconds(3))

    val stream = stc.receiverStream(new MyReceiverStream("hadoop103", 9999))
    val result = stream.flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)
    result.print

    stc.start()
    stc.awaitTermination()
  }
}
// 默认 Receiver 的 StorageLevel 是 MEMORY_ONLY，因此有内存溢出风险
class MyReceiverStream(host: String, port: Int) extends Receiver[String](storageLevel = StorageLevel.MEMORY_ONLY) {
  var socket: Socket = _
  var reader: BufferedReader = _

  override def onStart(): Unit = {
    runInThread {
      try {
        socket = new Socket(host, port)
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream, "utf-8"))

        var line = reader.readLine()
        while (line != null) {
          store(line)
          line = reader.readLine()
        }
      } catch {
        case e => println(e.getMessage)
      } finally {
        restart("restarting receiver...")
      }
    }
  }

  override def onStop(): Unit = {
    if (reader != null) reader.close()
    if (socket != null) socket.close()
  }

  def runInThread(op: => Unit): Unit = {
    new Thread() {
      override def run(): Unit = op
    }.start()
  }
}
```



## 4.2 Kafka 数据源

### 4.2.1 简介

针对 Kafka 数据源，SparkStreaming 支持两种数据获取方式：Receiver + kafka API 高阶函数、直联模式。后者在 Spark 1.3 版本开始出现，较前者有以下3点优势：

（1）简化并行处理。直联模式下，SparkStreaming 端 RDD 的分区数与 Kafka 分区数量直接挂钩，各个分区之间进行一对一映射。

（2）高效。为了防止数据丢失，采用第一种方式需要预写日志（Write Ahead Log），效率大打折扣。而第二种方式没有 Receiver，只要 Kafka 对数据进行保留，SparkStreaming 可以从 Kafka 再次拉取已经丢失的数据。

（3）*Exactly-once* 语义。第一种方式使用 Kafka 的旧 API 从 Zookeeper 端获取偏移量数据，如果发生错误，容易出现多次消费的问题。第二种方式使用 Kafka 的新 API 直接从  Kafka 获取偏移量数据进行 checkpoint，这样如果发生的错误，SparkStreaming 可以从 checkpoint 恢复偏移量，以此实现 *Exactly-once* 语义。需要注意的是，SparkStreaming 直连模式只是做到了消费的时候的严格一次，如何向输出的也要严格一次需要开发者自己保证：① 输出系统是幂等 或者 ②输出系统支持事务。

三个语义:

1. 至多一次(高效, 数据丢失)
2. 正好一次(最理想. 额外的很多工作, 效率最低)
3. 至少一次(保证数据不丢失, 数据重复)

注：生产者、消费者都可以自动创建 topic，分区数为 /opt/module/kafka/config/server.properties 中定义的默认分区数。

### 4.2.2 Receiver 模式（08 版 API）

```scala
object ReceiverApi {
    def main(args: Array[String]): Unit = {
        val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("ReceiverApi")
        val ssc = new StreamingContext(conf, Seconds(3))

        // (k, v)  k默认是 null, v 才是真正的数据
        // key的用处: 决定数据的分区. 如果key null, 轮询的分区
        val sourceStream: ReceiverInputDStream[(String, String)] = KafkaUtils.createStream(
            ssc,
            "hadoop102:2181,hadoop103:2181,hadoop104:2181/mykafka",
            "jeffery",
            Map("spark1128" -> 2))

        sourceStream
            .map(_._2)
            .flatMap(_.split(" "))
            .map((_, 1))
            .reduceByKey(_ + _)
            .print

        ssc.start()
        ssc.awaitTermination()
    }
}
```

注意：使用 "KafkaUtils.createStream" 创建 Kafka 输入流，此 API 内部使用了 Kafka 客户端低阶API，不支持 offset 自动提交（提交到 zookeeper）。

解决方案1）通过 zookeeper 提供的API，自己编写代码，将 offset 提交到 zookeeper；服务启动时，从 zookeeper 读取 offset，并作为"KafkaUtils.createStream"的输入参数。
解决方案2）自己编写代码维护 offset，并将 offset 保存到 MongoDB 或者 redis。

### 4.2.3 Direct 模式 + CheckPoint（08 版 API）

```scala
// 解码器与反序列化作用类似，但解码器效率比反序列化要高
object DirectApiCheck {
  def createStc(): StreamingContext = {
    println("creating Stc...")
    val conf = new SparkConf().setAppName("DirectApiCheck").setMaster("local[*]")
    val stc: StreamingContext = new StreamingContext(conf, Seconds(3))
    stc.checkpoint("./ck1")

    val param = Map[String, String](
      "bootstrap.servers" -> "hadoop102:9092,hadoop103:9092,hadoop104:9092",
      "group.id" -> "jeffery"
    )

    val stream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      stc,
      param,
      Set("spark1128"))

    stream
      .map(_._2)
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)
      .print

    stc
  }

  def main(args: Array[String]): Unit = {
    val stc = StreamingContext.getActiveOrCreate("./ck1", createStc)
    stc.start()
    stc.awaitTermination()
  }
}
```



### 4.2.4  10 版 API 简介

10 版的 API 不再支持 receiver 的方式从 Kafka 拉取数据。

#### 4.2.4.1 重要参数

```scala
1. locationStrategy
（1）PreferConsistent：各 executor 间均等消费，满足大多数场景
（2）PreferBrokers：应用在 executor 和 broker 在同一台设备的场景
（3）PreferFixed：应用于数据发生严重倾斜的场景
2. ConsumerStrategies
对消费者的参数进行配置，一般通过 Subscribe 方法返回一个 ConsumerStrategies 实例。
```

#### 4.2.4.2 案例：实现 wordcount

```scala
object NewApiDemo {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("NewApiDemo").setMaster("local[*]")
    val stc = new StreamingContext(conf, Seconds(3))

    val topics = Array("spark1128")
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "hadoop102:9092,hadoop103:9092,hadoop104:9092",
      "key.deserializer" -> classOf[StringDeserializer], // key的反序列化器
      "value.deserializer" -> classOf[StringDeserializer], // value的反序列化器
      "group.id" -> "jeffery",
      "auto.offset.reset" -> "latest", // 每次从最新的位置开始读
      "enable.auto.commit" -> (true: lang.Boolean) // 自动提交kafka的offset
    )

    val stream = KafkaUtils.createDirectStream(
      stc,
      locationStrategy = LocationStrategies.PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    val result = stream
      .map(_.value)
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)
    result.print

    stc.start()
    stc.awaitTermination()
  }
}
```



## 4.3 转换操作

### 4.3.1 无状态的转换

这个转换仅仅针对当前批次，批次之间没有关系。和`RDD`算子重名的算子都是无状态的算子。

#### 4.3.1.1 transform 算子

流是由`RDD`组成的，transform 可以得到每个批次内的那个`RDD`。由于流的算子不够丰富, 没有`RDD`多；可以通过这个方法得到`RDD`，然后对`RDD`进行操作。 

```scala
object TransformTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("TransformTest2").setMaster("local[*]")
    val stc = new StreamingContext(conf, Seconds(3))
    val stream = stc.socketTextStream("hadoop103", 9999)
    // 把对流的操作, 转换成对RDD操作.
    val result = stream.transform(rdd => {
      rdd.flatMap(_.split(" ")).map((_, 1)).reduceByKey(_ + _)
    })
    result.print

    stc.start()
    stc.awaitTermination()
  }
}
```

#### 4.3.1.4 foreachRDD 算子

把流的操作转换成操作 RDD，与前者的区别是返回值类型。

### 4.3.2 有状态的转换

#### 4.3.2.1 updateStateByKey

作用：能够跨批次进行操作，实现批次间的聚合。案例如下：

```scala
// 1. updateStateByKey 不能自动推导类型，必须在变量后注明类型，否则编译不通过。
// scala 开发中报错有时候是缺泛型，不能自动推导，加上泛型就好了。
// 2. updateStateByKey 需要设置 checkpoint 用于批次间聚合。框架会自动清理 checkpoint 中过期的文件。其重载方法还支持指定分区数、自定义分区器。


object UpdateStateTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("UpdateStateTest").setMaster("local[*]")
    val stc = new StreamingContext(conf, Seconds(3))
    stc.checkpoint("./ck5")

    val stream = stc.socketTextStream("hadoop103", 9999)
    val result = stream.flatMap(_.split(" "))
      .map((_, 1))
      .updateStateByKey((seq: Seq[Int], opt: Option[Int]) => {
        Some(seq.sum + opt.getOrElse(0))
      })
    result.print

    stc.start()
    stc.awaitTermination()
  }
}
```

#### 4.3.2.2 reduceByKeyAndWindow

为批次间的聚合增加一个窗口。案例如下：

```scala
object WindowTest1 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("WindowTest1").setMaster("local[*]")
    val stc = new StreamingContext(conf, Seconds(3))
    stc.checkpoint("./ck1")

    val lineStream = stc.socketTextStream("hadoop103", 9999)
    val result = lineStream
      .flatMap(_.split(" "))
      .map((_, 1))
      // 写法1：注意 slideDuration 需要参数名传参，否则会引发歧义导致报错
      .reduceByKeyAndWindow(_ + _, Seconds(9), slideDuration = Seconds(6))
      // 写法2：采用了优化处理，将每次不需要计算的中间部分 checkpoint，需要设置 checkpoint。当 windowDuration > slideDuration 时才有意义。
      // 为防止 (a,0) 的情况出现，需要定义 filterFunc = _._2 > 0
      .reduceByKeyAndWindow(_ + _, (now, pre) => now - pre, Seconds(9), filterFunc = _._2 > 0)
    result.print

    stc.start()
    stc.awaitTermination()
  }
}
```

```scala
object WindowTest2 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("WindowTest1").setMaster("local[*]")
    val stc = new StreamingContext(conf, Seconds(3))
    // stc.checkpoint("./ck1")
    // 直接给 DStream 分配窗口, 将来所有的操作, 都是基于窗口
    // 不支持 checkpoint 优化
    val lineStream = stc.socketTextStream("hadoop103", 9999).window(Seconds(9), Seconds(6))
    val result = lineStream
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)
    result.print

    stc.start()
    stc.awaitTermination()
  }
}
```



## 4.4 输出操作

### 4.4.1 输出到文件

```scala
// 每个周期存一个目录，目录名为 前缀-时间戳-后缀，目录中存放输出的内容
lineStream.saveAsTextFiles("word", "log")
```

### 4.4.2 输出到 JDBC

使用 `foreachRDD`，将 `DStream` 转化为 `RDD` 进行输出。

#### 4.4.2.1 方法1：加载驱动

```scala
object ForeachRddTest01 {
  val driver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://hadoop103:3306/test"
  val user = "root"
  val pw = "root"
  val sql = "insert into wordcount values(?, ?)"

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("ForeachRddTest").setMaster("local[*]")
    val stc = new StreamingContext(conf, Seconds(3))

    val lineStream = stc.socketTextStream("hadoop103", 9999)
    val wordCountStream = lineStream
      .flatMap(_.split(" "))
      .map((_, 1))
      .reduceByKey(_ + _)

    // 把 wordCount 数据写入到 mysql 中
    // 须保证表已经建好
    wordCountStream.(rdd => {
      // 也是把流的操作转换成操作 RDD.
      // 向外部存储写入数据
      rdd.foreachPartition(it => {
        // 获取连接
        Class.forName(driver)
        val conn = DriverManager.getConnection(url, user, pw)
        // 写入
        it.foreach{
          case (word, count) =>
            val ps = conn.prepareStatement(sql)
            ps.setString(1, word)
            ps.setInt(2, count)
            ps.execute()
            // 关闭连接
            ps.close()
        }
        conn.close()
      })
    })

    stc.start()
    stc.awaitTermination()
  }
}

```

#### 4.4.2.2 方法2：SparkSQL

```scala
object ForeachRddTest02 {
  val driver = "com.mysql.jdbc.Driver"
  val url = "jdbc:mysql://hadoop103:3306/test"
  val user = "root"
  val pw = "root"

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("ForeachRddTest").setMaster("local[*]")
    val stc = new StreamingContext(conf, Seconds(3))
    stc.checkpoint("./ck4")
    
    val lineStream = stc.socketTextStream("hadoop103", 9999)
    val wordCountStream = lineStream
      .flatMap(_.split(" "))
      .map((_, 1))
      .updateStateByKey((seq: Seq[Int], opt: Option[Int]) => {
        Some(seq.sum + opt.getOrElse(0))
      })
    // 创建 SparkSession
    val spark = SparkSession
      .builder()
      .config(stc.sparkContext.getConf)
      .getOrCreate()
    import spark.implicits._
    wordCountStream.foreachRDD(rdd => {
    // 将 RDD 转为 DF
    // DS 也可以，因为 DS 是 DF 的一般式
      val df = rdd.toDF("word", "count")
    // 写入 MySQL 数据库
      df.write.mode("overwrite").format("jdbc")
        .option("url", url)
        .option("user", user)
        .option("password", pw)
        .option("dbtable", "wordcount")
        .save()
    })

    stc.start()
    stc.awaitTermination()
  }
}
```



## 4.5 项目小技巧

1. 时间格式化

   ```scala
     private val date = new Date(ts)
     dayString = new SimpleDateFormat("yyyy-MM-dd").format(date)
     hmString = new SimpleDateFormat("HH:mm").format(date)
   ```

2. json 字符串转换

   ```scala
   import org.json4s.DefaultFormats
   val hmStr = Serialization.write(it.toMap)(DefaultFormats)
   ```

3. 工作中禁止直接复制粘贴代码，而是要将共同的代码抽象出来封装为 trait。

4. 开发中不要把常量写死，而是从配置文件中读取常量值。



















