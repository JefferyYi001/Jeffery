一 流的Source

## 1.1 从集合摄入

示例：

```scala
    val stream = env
      .fromCollection(List(
        SensorReading("sensor_1", 1347718199, 35.80018327300259),
        SensorReading("sensor_6", 1347718199, 13.402984393403084),
        SensorReading("sensor_7", 1347718199, 6.720945201171228),
        SensorReading("sensor_10", 1347718199, 38.101067604893444)
      ))
```

## 1.2 从文件摄入

示例：

```scala
    val stream = env.readTextFile(filePath)
```

## 1.3 从Kafka消息队列摄入

示例：

`kafka消费者配置`

```properties
bootstrap.servers=hadoop102:9092,hadoop103:9092,hadoop104:9092
group.id=childWen
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=org.apache.kafka.common.serialization.StringSerializer
#消费策略：每次都从最新位置开始消费数据
auto.offset.reset=latest
```

`Flink-Kafka 依赖`

```xml
		<dependency>
			<groupId>org.apache.flink</groupId>
			<artifactId>flink-connector-kafka-0.11_2.11</artifactId>
			<version>1.10.0</version>
		</dependency>
```

`工具类 读取配置文件参数`

```scala
package com.atguigu.common

import java.util.Properties

/**
 * @Classname PropertyUtil
 * @Description TODO
 *              Date ${Date} 22:36
 * @Create by childwen
 */
object PropertyUtil {

  def getProperty(fileName: String, propertyName: String) = {
    // 1. 读取文件内容
    val inputStream = PropertyUtil.getClass.getClassLoader.getResourceAsStream(fileName)
    val properties = new Properties()
    properties.load(inputStream)
    // 2. 根据属性名得到属性值
    properties.getProperty(propertyName)
  }

  def getProperties(fileName: String) = {
    // 1. 读取文件内容
    val inputStream = PropertyUtil.getClass.getClassLoader.getResourceAsStream(fileName)
    val properties = new Properties()
    properties.load(inputStream)
    properties
    /*    // 2. 根据属性名得到属性值
        properties.getProperty(propertyName)
        val pro = new Properties()*/
  }

}

```

代码：

```scala
    //从kafka创建数据源
    val stream = env.addSource(new FlinkKafkaConsumer011[String](
      "flink1128",
      new SimpleStringSchema(),
      PropertyUtil.getProperties("kafkaConsumerconfig.properties")
    ))
```

## 1.4 从自定义数据源摄入

`自定义数据源`

```scala
import java.util.Calendar

import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext

import scala.util.Random

// 传感器id，时间戳，温度
case class SensorReading(id: String, timestamp: Long, temperature: Double)

// 需要extends RichParallelSourceFunction, 泛型为SensorReading
class SensorSource
  extends RichParallelSourceFunction[SensorReading] {

  // flag indicating whether source is still running.
  // flag: 表示数据源是否还在正常运行
  var running: Boolean = true

  // run()函数连续的发送SensorReading数据，使用SourceContext
  // 需要override
  override def run(srcCtx: SourceContext[SensorReading]): Unit = {

    // initialize random number generator
    // 初始化随机数发生器
    val rand = new Random()
    // look up index of this parallel task
    // 查找当前运行时上下文的任务的索引
    val taskIdx = this.getRuntimeContext.getIndexOfThisSubtask

    // initialize sensor ids and temperatures
    // 初始化10个(温度传感器的id, 温度值)元组
    var curFTemp = (1 to 10).map {
      // nextGaussian产生高斯随机数
      i => ("sensor_" + (taskIdx * 10 + i), 65 + (rand.nextGaussian() * 20))
    }

    // emit data until being canceled
    // 无限循环，产生数据流
    while (running) {

      // update temperature
      // 更新温度
      curFTemp = curFTemp.map(t => (t._1, t._2 + (rand.nextGaussian() * 0.5)) )
      // get current time
      // 获取当前时间戳
      val curTime = Calendar.getInstance.getTimeInMillis

      // emit new SensorReading
      // 发射新的传感器数据, 注意这里srcCtx.collect
      curFTemp.foreach(t => srcCtx.collect(SensorReading(t._1, curTime, t._2)))

      // wait for 100 ms
      Thread.sleep(100)
    }

  }

  // override cancel函数
  override def cancel(): Unit = {
    running = false
  }

}
```

代码：

```scala
val sensorData: DataStream[SensorReading] = env
      .addSource(new SensorSource)
```

## 1.5 从基本类型元素摄入

```scala
    val stream = env.fromElements("hello",
      "hello wrold",
      "deng zhi wen",
      "deng zhi wen"
    )
```

# 二 流的Transfrom

## 2.1 基本转换算子

基本转换算子会针对流中的每一个单独的时间做处理，也就是说每一个输入数据都会产生一个输出数据。单值转换，数据分割，数据过滤，都是基本的转换操作例子。

### 2.1.1 MAP

#### MAP基本介绍

`map`算子通过调用`DataStream.map()`来指定。`map`算子的使用将会产生一条新的数据流。它会将每一个输入的事件传送到一个用户自定义的mapper，这个mapper只返回一个输出事件，这个输出事件和输入事件的类型可能不一样。如图展示了一个map算子，这个map将每一个正方形转化成了圆形。

![img](https://confucianzuoyuan.github.io/flink-tutorial/images/spaf_0501.png)

#### MAP基本使用

`MapFunction`的类型与输入事件和输出事件的类型相关，可以通过实现`MapFunction`接口来定义。接口包含`map()`函数，这个函数将一个输入事件恰好转换为一个输出事件。

##### 使用匿名函数

```scala
//1 使用匿名函数实现
//    stream.map(sensor => sensor.id).print()
```

##### 使用匿名子类

```scala
    //2 使用匿名子类实现
//    stream.map(new MapFunction[SensorReading, String] {
//      override def map(t: SensorReading): String = t.id
//    }).print()
```

##### 使用实现接口

```scala
    //3 使用MapFunction实现子类实现
    stream.map(new MyMapFunction).print()

class MyMapFunction extends MapFunction[SensorReading, String] {
  override def map(t: SensorReading): String = t.id
}
```

### 2.1.2 FILTER

#### FILTER基本介绍

`filter`转换算子通过在每个输入事件上对一个布尔条件进行求值来过滤掉一些元素，然后将剩下的元素继续发送。一个`true`的求值结果将会把输入事件保留下来并发送到输出，而如果求值结果为`false`，则输入事件会被抛弃掉。我们通过调用`DataStream.filter()`来指定流的`filter`算子，`filter`操作将产生一条新的流，其类型和输入流中的事件类型是一样的。如图展示了只产生白色方框的`filter`操作。

![img](https://confucianzuoyuan.github.io/flink-tutorial/images/spaf_0502.png)

布尔条件可以使用函数、FilterFunction接口或者匿名函数来实现。FilterFunction中的泛型是输入事件的类型。定义的`filter()`方法会作用在每一个输入元素上面，并返回一个布尔值。

#### FILTER基本使用

和MAP类似，可以选择匿名函数或实现类来操作。

```scala
package com.atguigu.day02.transfrom

import com.atguigu.day02.source.{SensorReading, SensorSource}
import org.apache.flink.api.common.functions.FilterFunction
import org.apache.flink.streaming.api.scala._

/**
 * @Classname FilterExample
 * @Description TODO
 *              Date ${Date} 10:30
 * @Create by childwen
 */
object FilterExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream = env.addSource(new SensorSource)
    //1 匿名函数
    //    stream.filter(sensor => "sensor_10" == sensor.id).print()

    // 匿名子类
    //    stream.filter(new FilterFunction[SensorReading] {
    //      override def filter(t: SensorReading): Boolean = "sensor_10" == t.id
    //    }).print()

    //实现类
    stream.filter(new MyFilterFunction).print()
    env.execute()
  }

}

class MyFilterFunction extends FilterFunction[SensorReading] {
  override def filter(t: SensorReading): Boolean = "sensor_1" == t.id
}

```

### 2.1.3 FLATMAP

#### FLATMAP基本介绍

`flatMap`算子和`map`算子很类似，不同之处在于针对每一个输入事件`flatMap`可以生成0个、1个或者多个输出元素。事实上，`flatMap`转换算子是`filter`和`map`的泛化。所以`flatMap`可以实现`map`和`filter`算子的功能。图5-3展示了`flatMap`如何根据输入事件的颜色来做不同的处理。如果输入事件是白色方框，则直接输出。输入元素是黑框，则复制输入。灰色方框会被过滤掉。

![img](https://confucianzuoyuan.github.io/flink-tutorial/images/spaf_0503.png)

flatMap算子将会应用在每一个输入事件上面。对应的`FlatMapFunction`定义了`flatMap()`方法，这个方法返回0个、1个或者多个事件到一个`Collector`集合中，作为输出结果。

#### FLATMAP基本使用

```scala
package com.atguigu.day02.transfrom

import com.atguigu.day02.source.{SensorReading, SensorSource}
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

/**
 * @Classname FlatMapExample
 * @Description TODO
 *              Date ${Date} 10:50
 * @Create by childwen
 */
object FlatMapExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)
    //1 默认flatMap方法会对传入的参数进行foreach遍历，取出每一个元素收集发送
    stream.flatMap(sensor => sensor.id)
    //      .print()
    //2 使用匿名子类实现map效果
    stream.flatMap(new FlatMapFunction[SensorReading, String] {
      override def flatMap(t: SensorReading, collector: Collector[String]): Unit =
        collector.collect(t.id)
    })
    //      .print()
    //3 使用实现子类实现过滤效果
    stream.flatMap(new MyFlatMap).print()
    env.execute()
  }

}

class MyFlatMap extends FlatMapFunction[SensorReading, String] {
  override def flatMap(t: SensorReading, collector: Collector[String]): Unit = {
    if ("sensor_1" == t.id) collector.collect(t.id)
    else if ("sensor_2" == t.id) {
      collector.collect(s"${t.id}--->使用flatMap实现复制的效果1")
      collector.collect(s"${t.id}--->使用flatMap实现复制的效果2")
    }
    else if ("sensor_3" == t.id){
      t.id.foreach(char => {
        val str = char.toString
        collector.collect(str)
        collector.collect("使用foreach实现源码flatMap的效果")
      })
    }
  }
}

```

## 2.2 键控流转换算子

很多流处理程序的一个基本要求就是要能对数据进行分组，分组后的数据共享某一个相同的属性。DataStream API提供了一个叫做`KeyedStream`的抽象，**此抽象会从逻辑上对DataStream进行分区，分区后的数据拥有同样的`Key`值，分区后的流互不相关。**

针对KeyedStream的状态转换操作可以读取数据或者写入数据到当前事件Key所对应的状态中。这表明拥有同样Key的所有事件都可以访问同样的状态，也就是说所有这些事件可以一起处理。

> 要小心使用状态转换操作和基于Key的聚合操作。如果Key的值越来越多，例如：Key是订单ID，我们必须及时清空Key所对应的状态，以免引起内存方面的问题。

**KeyedStream可以使用map，flatMap和filter算子来处理。**

### 2.2.1 基本介绍

keyBy通过指定key来将DataStream转换成KeyedStream。基于不同的key，流中的事件将被分配到不同的分区中去。**所有具有相同key的事件将会在接下来的操作符的同一个子任务槽中进行处理。拥有不同key的事件可以在同一个任务中处理。但是算子只能访问当前事件的key所对应的状态。**

如图5-4所示，把输入事件的颜色作为key，黑色的事件输出到了一个分区，其他颜色输出到了另一个分区。

![img](https://confucianzuoyuan.github.io/flink-tutorial/images/spaf_0504.png)

`keyBy()`方法接收一个参数，这个参数指定了key或者keys，有很多不同的方法来指定key。我们将在后面讲解。

### 2.2.2 基本使用

下面的代码声明了`id`这个字段为SensorReading流的key。

```scala
    val readings: DataStream[SensorReading] = ...
    val keyed: KeyedStream[SensorReading, String] = readings
      .keyBy(r => r.id)
```

匿名函数`r => r.id`抽取了传感器读数SensorReading的id值。

## 2.3 滚动聚合算子

#### 2.3.1 基本介绍

滚动聚合算子由`KeyedStream`调用，并生成一个聚合以后的DataStream，例如：sum，minimum，maximum。一个滚动聚合算子会为每一个观察到的key保存一个聚合的值。针对每一个输入事件，算子将会更新保存的聚合结果，并发送一个带有更新后的值的事件到下游算子。滚动聚合不需要用户自定义函数，但需要接受一个参数，这个参数指定了在哪一个字段上面做聚合操作。DataStream API提供了以下滚动聚合方法。

> 滚动聚合算子只能用在滚动窗口，不能用在滑动窗口。

- sum()：在输入流上对指定的字段做滚动相加操作。
- min()：在输入流上对指定的字段求最小值。
- max()：在输入流上对指定的字段求最大值。
- minBy()：在输入流上针对指定字段求最小值，并返回包含当前观察到的最小值的事件。
- maxBy()：在输入流上针对指定字段求最大值，并返回包含当前观察到的最大值的事件。

滚动聚合算子无法组合起来使用，每次计算只能使用一个单独的滚动聚合算子。

> 滚动聚合操作会对每一个key都保存一个状态。因为状态从来不会被清空，所以我们在使用滚动聚合算子时只能使用在含有有限个key的流上面。

#### 2.3.2 基本使用

下面的例子根据第一个字段来对类型为`Tuple3[Int, Int, Int]`的流做分流操作，然后针对第二个字段做滚动求和操作。

```scala
    val inputStream: DataStream[(Int, Int, Int)] = env.fromElements(
      (1, 2, 2), (2, 3, 1), (2, 2, 4), (1, 5, 3))
    
    val resultStream: DataStream[(Int, Int, Int)] = inputStream
      .keyBy(0) 
      .sum(1) 
```

在这个例子里面，输入流根据第一个字段来分流，然后在第二个字段上做计算。对于key 1，输出结果是(1,2,2),(1,7,2)。对于key 2，输出结果是(2,3,1),(2,5,1)。第一个字段是key，第二个字段是求和的数值，第三个字段未定义。

#### 2.3.3 REDUCE算子

##### 基本介绍

reduce算子是滚动聚合的泛化实现。它将一个ReduceFunction应用到了一个KeyedStream上面去。reduce算子将会把每一个输入事件和当前已经reduce出来的值做聚合计算。reduce操作不会改变流的事件类型。输出流数据类型和输入流数据类型是一样的。

reduce函数可以通过实现接口ReduceFunction来创建一个类。ReduceFunction接口定义了`reduce()`方法，此方法接收两个输入事件，输入一个相同类型的事件。

> reduce作为滚动聚合的泛化实现，同样也要针对每一个key保存状态。因为状态从来不会清空，所以我们需要将reduce算子应用在一个有限key的流上。

```scala
// T: the element type
ReduceFunction[T]
    > reduce(T, T): T
```
##### 基本使用

```scala
package com.atguigu.day02.keybyAndReduce

import org.apache.flink.streaming.api.scala._

/**
 * @Classname KeyByExapmple
 * @Description TODO
 *              Date ${Date} 11:48
 * @Create by childwen
 */
object KeyByAndReduceExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream = env.fromElements(("en", List("tea")),
      ("fr", List("vin")),
      ("en", List("cake"))
    )
    //对key分组，相同的key的值进行聚合操作
    stream
      //逻辑上将流按key的Hash分成了不同的分支。
      .keyBy(_._1)
      //r1表示流的上一时刻状态，r2表示流的当前状态
      .reduce((r1, r2) => (r1._1, r1._2 ::: r2._2))
      .print()
    env.execute()
  }

}

```

## 2.4 多流转换算子

许多应用需要摄入多个流并将流合并处理，还可能需要将一条流分割成多条流然后针对每一条流应用不同的业务逻辑。接下来，我们将讨论DataStream API中提供的能够处理多条输入流或者发送多条输出流的操作算子。

### 2.4.1 UNION

#### 基本介绍

DataStream.union()方法将两条或者多条DataStream合并成一条具有与输入流相同类型的输出DataStream。接下来的转换算子将会处理输入流中的所有元素。图5-5展示了union操作符如何将黑色和白色的事件流合并成一个单一输出流。

![img](https://confucianzuoyuan.github.io/flink-tutorial/images/spaf_0505.png)

事件合流的方式为FIFO方式。操作符并不会产生一个特定顺序的事件流。union操作符也不会进行去重。每一个输入事件都被发送到了下一个操作符。

#### 基本使用

```scala
package com.atguigu.day02.flowtransfrom

import com.atguigu.day02.source.{SensorReading, SensorSource}
import org.apache.flink.api.common.functions.FilterFunction


/**
 * @Classname UnionExample
 * @Description TODO
 *              Date ${Date} 14:27
 * @Create by childwen
 */
object UnionExample {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream1 = env
      .addSource(new SensorSource)
      .filter(new FilterFunction[SensorReading] {
        override def filter(t: SensorReading): Boolean = "sensor_1" == t.id
      })

    val stream2 = env.addSource(new SensorSource)
      .filter("sensor_2" == _.id)

    val stream3 = env.addSource(new SensorSource)
      .filter(new MyFilterFunction)

    val unionStream = stream1.union(stream2, stream3)

    unionStream.print

    env.execute()

  }

  class MyFilterFunction extends FilterFunction[SensorReading] {
    override def filter(t: SensorReading): Boolean = "sensor_3" == t.id
  }

}

```

### 2.4.2 CONNECT -> COMAP和COFLATMAP

#### 基本介绍

联合两条流的事件是非常常见的流处理需求。例如监控一片森林然后发出高危的火警警报。报警的Application接收两条流，一条是温度传感器传回来的数据，一条是烟雾传感器传回来的数据。当两条流都超过各自的阈值时，报警。

DataStream API提供了`connect`操作来支持以上的应用场景。`DataStream.connect()`方法接收一条`DataStream`，然后返回一个`ConnectedStreams`类型的对象，这个对象表示了两条连接的流。

```scala
    // first stream
    val first: DataStream[Int] = ...
    // second stream
    val second: DataStream[String] = ...
    
    // connect streams
    val connected: ConnectedStreams[Int, String] = first.connect(second)
```

#### 基本使用

**ConnectedStreams提供了`map()`和`flatMap()`方法，分别需要接收类型为`CoMapFunction`和`CoFlatMapFunction`的参数。**

以上两个函数里面的泛型是第一条流的事件类型和第二条流的事件类型，以及输出流的事件类型。还定义了两个方法，每一个方法针对一条流来调用。`map1()`和`flatMap1()`会调用在第一条流的元素上面，`map2()`和`flatMap2()`会调用在第二条流的元素上面。

```scala
    // IN1: 第一条流的事件类型
    // IN2: 第二条流的事件类型
    // OUT: 输出流的事件类型
    CoMapFunction[IN1, IN2, OUT]
        > map1(IN1): OUT
        > map2(IN2): OUT
    
    CoFlatMapFunction[IN1, IN2, OUT]
        > flatMap1(IN1, Collector[OUT]): Unit
        > flatMap2(IN2, Collector[OUT]): Unit
```

> 函数无法选择读某一条流。我们是无法控制函数中的两个方法的调用顺序的。当一条流中的元素到来时，将会调用相对应的方法。

对两条流做连接查询通常需要这两条流基于某些条件被确定性的路由到操作符中相同的并行实例里面去。在默认情况下，connect()操作将不会对两条流的事件建立任何关系，所以两条流的事件将会随机的被发送到下游的算子实例里面去。这样的行为会产生不确定性的计算结果，显然不是我们想要的。为了针对ConnectedStreams进行确定性的转换操作，connect()方法可以和keyBy()或者broadcast()组合起来使用。我们首先看一下keyBy()的示例。

```scala
    val one: DataStream[(Int, Long)] = ...
    val two: DataStream[(Int, String)] = ...
    
    // keyBy two connected streams
    val keyedConnect1: ConnectedStreams[(Int, Long), (Int, String)] = one
      .connect(two)
      .keyBy(0, 0) // key both input streams on first attribute
    
    // alternative: connect two keyed streams
    val keyedConnect2: ConnectedStreams[(Int, Long), (Int, String)] = one
      .keyBy(0)
      .connect(two.keyBy(0))
```

无论使用keyBy()算子操作ConnectedStreams还是使用connect()算子连接两条KeyedStreams，**connect()算子会将两条流的含有相同Key的所有事件都发送到相同的算子实例。**两条流的key必须是一样的类型和值，就像SQL中的JOIN。在connected和keyed stream上面执行的算子有访问keyed state的权限。

下面的例子展示了如何连接一条DataStream和广播过的流。

```scala
val first: DataStream[(Int, Long)] = ...
val second: DataStream[(Int, String)] = ...

// connect streams with broadcast
val keyedConnect: ConnectedStreams[(Int, Long), (Int, String)] = first
  // broadcast second input stream
  .connect(second.broadcast())
```
一条被广播过的流中的所有元素将会被复制然后发送到下游算子的所有并行实例中去。未被广播过的流仅仅向前发送。所以两条流的元素显然会被连接处理。

### 2.4.3 SPLIT和SELECT

#### 基本介绍

Split是Union的反函数。Split将输入的流分成两条或者多条流。每一个输入的元素都可以被路由到0、1或者多条流中去。所以，split可以用来过滤或者复制元素。图5-6展示了split操作符将所有的白色事件都路由到同一条流中去了，剩下的元素去往另一条流。

![img](https://confucianzuoyuan.github.io/flink-tutorial/images/spaf_0506.png)

DataStream.split()方法接受`OutputSelector`类型，此类型定义了输入流中的元素被分配到哪个名字的流中去。`OutputSelector`定义了`select()`方法，此方法将被每一个元素调用，并返回`java.lang.Iterable[String]`类型的数据。返回的`String`类型的值将指定元素将被路由到哪一条流。

#### 基本使用

将一条整数流分成了不同的流，大的整数一条流，小的整数一条流。

```scala
val inputStream: DataStream[(Int, String)] = ...

val splitted: SplitStream[(Int, String)] = inputStream
  .split(t => if (t._1 > 1000) Seq("large") else Seq("small"))

val large: DataStream[(Int, String)] = splitted.select("large")
val small: DataStream[(Int, String)] = splitted.select("small")
val all: DataStream[(Int, String)] = splitted.select("small", "large")
```

> 不推荐使用split方法，推荐使用Flink的侧输出（side-output）特性。

## 2.5 分布式转换算子

分布式转换算子操作定义了事件如何分配到不同的任务中去。当我们使用DataStream API来编写程序时，系统将自动的选择数据分区策略，然后根据操作符的语义和设置的并行度将数据路由到正确的地方去。有些时候，我们需要在应用程序的层面控制分区策略，或者自定义分区策略。例如，如果我们知道会发生数据倾斜，那么我们想要针对数据流做负载均衡，将数据流平均发送到接下来的操作符中去。又或者，应用程序的业务逻辑可能需要一个算子所有的并行任务都需要接收同样的数据。再或者，我们需要自定义分区策略的时候。

> keyBy()方法不同于分布式转换算子。所有的分布式转换算子将产生DataStream数据类型。而keyBy()产生的类型是KeyedStream，它拥有自己的keyed state。

### 2.5.1 Random

随机数据交换由`DataStream.shuffle()`方法实现。shuffle方法将数据随机的分配到下游算子的并行任务中去。

### 2.5.2 Round-Robin

`rebalance()`方法使用Round-Robin负载均衡算法将输入流平均分配到随后的并行运行的任务中去。图5-7为round-robin分布式转换算子的示意图。

### 2.5.3 Rescale

`rescale()`方法使用的也是round-robin算法，但只会将数据发送到接下来的并行运行的任务中的一部分任务中。本质上，当发送者任务数量和接收者任务数量不一样时，rescale分区策略提供了一种轻量级的负载均衡策略。如果接收者任务的数量是发送者任务的数量的倍数时，rescale操作将会效率更高。

`rebalance()`和`rescale()`的根本区别在于任务之间连接的机制不同。 `rebalance()`将会针对所有发送者任务和所有接收者任务之间建立通信通道，而`rescale()`仅仅针对每一个任务和下游算子的一部分子并行任务之间建立通信通道。rescale的示意图为图5-7。

![img](https://confucianzuoyuan.github.io/flink-tutorial/images/spaf_0507.png)

### 2.5.4 Broadcast

`broadcast()`方法将输入流的所有数据复制并发送到下游算子的所有并行任务中去。

### 2.5.5 Global

`global()`方法将所有的输入流数据都发送到下游算子的第一个并行任务中去。这个操作需要很谨慎，因为将所有数据发送到同一个task，将会对应用程序造成很大的压力。

### 2.5.6 Custom

当Flink提供的分区策略都不适用时，我们可以使用`partitionCustom()`方法来自定义分区策略。这个方法接收一个`Partitioner`对象，这个对象需要实现分区逻辑以及定义针对流的哪一个字段或者key来进行分区。下面的例子将一条整数流做partition，使得所有的负整数都发送到第一个任务中，剩下的数随机分配。

```scala
    val numbers: DataStream[(Int)] = ...
    numbers.partitionCustom(myPartitioner, 0)
    
//实现Partitioner
    object myPartitioner extends Partitioner[Int] {
      val r = scala.util.Random
    
      override def partition(key: Int, numPartitions: Int): Int = {
        if (key < 0) 0 else r.nextInt(numPartitions)
      }
    }
```

## 2.6 实现UDF函数类

更细粒度的控制流，Flink暴露了所有udf函数的接口(实现方式为接口或者抽象类)。例如MapFunction, FilterFunction, ProcessFunction等等。

例子实现了FilterFunction接口

```scala
    class FilterFilter extends FilterFunction[String] {
      override def filter(value: String): Boolean = {
        value.contains("flink")
      }
    }
    
    val flinkTweets = tweets.filter(new FlinkFilter)
```

还可以将函数实现成匿名类

```scala
    val flinkTweets = tweets.filter(
      new RichFilterFunction[String] {
        override def filter(value: String): Boolean = {
          value.contains("flink")
        }
      }
    )
```

我们filter的字符串“flink”还可以当作参数传进去。

```scala
    val tweets: DataStream[String] = ...
    val flinkTweets = tweets.filter(new KeywordFilter("flink"))
    
    class KeywordFilter(keyWord: String) extends FilterFunction[String] {
      override def filter(value: String): Boolean = {
        value.contains(keyWord)
      }
    }
```

## 2.7 匿名函数

匿名函数可以实现一些简单的逻辑，但无法实现一些高级功能，例如访问状态等等。

```scala
    val tweets: DataStream[String] = ...
    val flinkTweets = tweets.filter(_.contains("flink"))
```

## 2.8 富函数

我们经常会有这样的需求：在函数处理数据之前，需要做一些初始化的工作；或者需要在处理数据时可以获得函数执行上下文的一些信息；以及在处理完数据时做一些清理工作。而DataStream API就提供了这样的机制。

DataStream API提供的所有转换操作函数，都拥有它们的“富”版本，并且我们在使用常规函数或者匿名函数的地方来使用富函数。例如下面就是富函数的一些例子，可以看出，只需要在常规函数的前面加上`Rich`前缀就是富函数了。

- RichMapFunction
- RichFlatMapFunction
- RichFilterFunction
- …

当我们使用富函数时，我们可以实现两个额外的方法：

- open()方法是rich function的初始化方法，当一个算子例如map或者filter被调用之前open()会被调用。open()函数通常用来做一些只需要做一次即可的初始化工作。
- close()方法是生命周期中的最后一个调用的方法，通常用来做一些清理工作。

另外，getRuntimeContext()方法提供了函数的RuntimeContext的一些信息，例如函数执行的并行度，当前子任务的索引，当前子任务的名字。同时还它还包含了访问**分区状态**的方法。下面看一个例子：

实现RichFlatMapFunction富函数

```scala
    class MyFlatMap extends RichFlatMapFunction[Int, (Int, Int)] {
      var subTaskIndex = 0
    
      override def open(configuration: Configuration): Unit = {
        subTaskIndex = getRuntimeContext.getIndexOfThisSubtask
        // 做一些初始化工作
        // 例如建立一个和HDFS的连接
      }
    
      override def flatMap(in: Int, out: Collector[(Int, Int)]): Unit = {
        if (in % 2 == subTaskIndex) {
          out.collect((subTaskIndex, in))
        }
      }
    
      override def close(): Unit = {
        // 清理工作，断开和HDFS的连接。
      }
    }
```

# 三 流的Sink

## 3.1 基本介绍

Flink没有类似于spark中foreach方法，让用户进行迭代的操作。所有对外的输出操作都要利用Sink完成。最后通过类似如下方式完成整个任务最终输出操作。

例如：

```scala
stream.addSink(new MySink(xxxx))
```

官方提供了一部分的框架的sink。除此以外，需要用户自定义实现sink。

## 3.2 KafkaSink

### 3.2.1 maven配置文件

Flink-Kafka版本为0.11

```xml
    <dependency>
      <groupId>org.apache.flink</groupId>
      <artifactId>flink-connector-kafka-0.11_2.11</artifactId>
      <version>1.10.0</version>
    </dependency>
```



### 3.2.2 工具类读取kafka配置

```scala
package com.atguigu.common

import java.util.Properties

/**
 * @Classname PropertyUtil
 * @Description TODO
 *              Date ${Date} 22:36
 * @Create by childwen
 */
object PropertyUtil {

  def getProperty(fileName: String, propertyName: String) = {
    // 1. 读取文件内容
    val inputStream = PropertyUtil.getClass.getClassLoader.getResourceAsStream(fileName)
    val properties = new Properties()
    properties.load(inputStream)
    // 2. 根据属性名得到属性值
    properties.getProperty(propertyName)
  }

  def getProperties(fileName: String) = {
    // 1. 读取文件内容
    val inputStream = PropertyUtil.getClass.getClassLoader.getResourceAsStream(fileName)
    val properties = new Properties()
    properties.load(inputStream)
    properties
    /*    // 2. 根据属性名得到属性值
        properties.getProperty(propertyName)
        val pro = new Properties()*/
  }

}

```

### 3.2.3 kafka消费者配置文件

```properties
bootstrap.servers=hadoop102:9092,hadoop103:9092,hadoop104:9092
group.id=childWen
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=org.apache.kafka.common.serialization.StringSerializer
#消费策略：每次都从最新位置开始消费数据
auto.offset.reset=latest
```

### 3.2.4 kafka生产者配置文件

```properties
bootstrap.servers=hadoop102:9092,hadoop103:9092,hadoop104:9092
key.serializer=org.apache.kafka.common.serialization.StringSerializer
value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

### 3.2.5 Kafka生产者向Kafka发送数据

```scala
package com.atguigu.day02.sink

import com.atguigu.common.PropertyUtil
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
 * @Classname KafkaProducer
 * @Description TODO
 *              Date ${Date} 22:33
 * @Create by childwen
 */
object KafkaProducer {
  val producer =
    new KafkaProducer[String, String](PropertyUtil.getProperties("kafkaProducerconfig.properties"))

  def sendToKafka(topic: String): Unit = {
    val record = new ProducerRecord[String, String](topic, "hello world")
    producer.send(record)
    producer.close()
  }

  def main(args: Array[String]): Unit = {
    sendToKafka("flink1128")
  }

}

```

### 3.2.6 Flink向Kafka发送数据

```scala
package com.atguigu.day02.sink

import com.atguigu.common.PropertyUtil
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}

/**
 * @Classname KafkaSinkExample
 * @Description TODO
 *              Date ${Date} 22:44
 * @Create by childwen
 */
object KafkaSinkExample {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    //从kafka创建数据源
    val stream = env.addSource(new FlinkKafkaConsumer011[String](
      "flink1128",
      new SimpleStringSchema(),
      PropertyUtil.getProperties("kafkaConsumerconfig.properties")
    ))

    //向kafka推送数据
    stream.addSink(
      new FlinkKafkaProducer011[String](
        "hadoop102:9092,hadoop103:9092,hadoop104:9092",
        "flink1128",
        new SimpleStringSchema()
      )
    )

    stream.print
    env.execute()
  }

}

```

## 3.3 RedisSink

### 3.3.1 maven配置文件

```xml
    <dependency>
      <groupId>org.apache.bahir</groupId>
      <artifactId>flink-connector-redis_2.11</artifactId>
      <version>1.0</version>
    </dependency>
```

### 3.3.2 RedisSink

```scala
package com.atguigu.day03.sink

import com.atguigu.day02.source.{SensorReading, SensorSource}
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}


/**
 * @Classname RedisSinkExample
 * @Description TODO
 *              Date ${Date} 8:53
 * @Create by childwen
 */
object RedisSinkExample {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream = env.addSource(new SensorSource)
    //redis主机信息
    val conf = new FlinkJedisPoolConfig.Builder().setHost("hadoop102").build()

    stream.addSink(new RedisSink[SensorReading](conf, new MyRedisMapper))

    env.execute()
  }

}

case class MyRedisMapper() extends RedisMapper[SensorReading] {

  override def getCommandDescription: RedisCommandDescription = {
    //如果返回的是HSET命令，附加key就是hashTable的表名
//    new RedisCommandDescription(RedisCommand.HSET, "sensor")
    //如果返回的是LPUSH命令那么key值就是getKeyFromData返回的key
    new RedisCommandDescription(RedisCommand.LPUSH)
  }

  //向redis中发送数据时的key
  override def getKeyFromData(t: SensorReading): String = {
    "list"
//    t.id
  }

  //向redis中发送数据时的key
  override def getValueFromData(t: SensorReading): String = t.temperature.toString
}

```

## 3.4 ElasticSearch

### 3.4.1 maven配置文件

```xml
    <dependency>
      <groupId>org.apache.flink</groupId>
      <artifactId>flink-connector-elasticsearch6_2.11</artifactId>
      <version>1.10.0</version>
    </dependency>
```

### 3.4.2 ElasticSearchSink

```scala
package com.atguigu.day03.sink

import java.util

import com.atguigu.day02.source.{SensorReading, SensorSource}
import org.apache.flink.api.common.functions.RuntimeContext
import org.apache.flink.streaming.connectors.elasticsearch.{ElasticsearchSinkFunction, RequestIndexer}
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink
import org.apache.http.HttpHost
import org.elasticsearch.client.Requests

/**
 * @Classname ElasticSearchSinkExample
 * @Description TODO
 *              Date ${Date} 9:37
 * @Create by childwen
 */
object ElasticSearchSinkExample {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream = env.addSource(new SensorSource)

    // 1 初始化es主机和端口
    val httpHosts = new util.ArrayList[HttpHost]()
    httpHosts.add(new HttpHost("hadoop102", 9200))

    // 2将数据写入es
    val ESSink = new ElasticsearchSink.Builder[SensorReading](
      //es的主机名和端口
      httpHosts,
      //匿名对象,定义如何将输入写入es中
      new ElasticsearchSinkFunction[SensorReading] {
        override def process(t: SensorReading, runtimeContext:
        RuntimeContext, requestIndexer: RequestIndexer): Unit = {
          //哈希表的key为string,value为string
          val json = new util.HashMap[String, String]()
          json.put("date", t.toString)
          //构建一个写入es的请求
          val indexRequest = Requests
            .indexRequest()
            .index("sensor")
            .`type`("flink1128")
            .source(json)
          requestIndexer.add(indexRequest)
        }

      }
    )

    //定义按批发送数据
    ESSink.setBulkFlushMaxActions(10)

    stream.addSink(ESSink.build())

    env.execute()
  }

}

```

## 3.5 JdbcSink

### 3.5.1 maven配置文件

```xml
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <version>5.1.44</version>
    </dependency>
```

### 3.5.2 自定义类实现RichSinkFunction完成JdbcSink

```scala
package com.atguigu.day03.sink

import java.sql.{Connection, DriverManager, PreparedStatement}

import com.atguigu.day02.source.{SensorReading, SensorSource}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}

/**
 * @Classname JDBCSinkExample
 * @Description TODO
 *              Date ${Date} 10:24
 * @Create by childwen
 */
object JDBCSinkExample {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream = env.addSource(new SensorSource)

    stream.addSink(new MyJdbcSink)

    env.execute()

    env.execute()
  }

  class MyJdbcSink extends RichSinkFunction[SensorReading] {
      //提交深度
    var count = 0
    var coon: Connection = _
    val url = "jdbc:mysql://hadoop102:3306/test"
    val user = "root"
    val psd = "123123"
    //插入语句
    var insertStr: PreparedStatement = _
    //更新语句
    var updateStr: PreparedStatement = _

    override def open(parameters: Configuration): Unit = {
      coon = DriverManager.getConnection(
        url,
        user,
        psd
      )

      insertStr = coon.prepareStatement("INSERT INTO temperatures (sensor,temp) VALUES (?,?)")

      updateStr = coon.prepareStatement("UPDATE temperatures SET temp = ? WHERE sensor = ?")
    }

    //执行SQL语句
    override def invoke(value: SensorReading, context: SinkFunction.Context[_]): Unit = {

      if (count < 3) {
        updateStr.setDouble(1, value.temperature)
        updateStr.setString(2, value.id)
        println(count)
        if (updateStr.getUpdateCount == 0) {
          insertStr.setString(1, value.id)
          insertStr.setDouble(2, value.temperature)
          insertStr.addBatch()
          count += 1
        }else{
          updateStr.addBatch()
          count += 1
        }
      } else {
        updateStr.executeBatch()
        insertStr.executeBatch()
        count = 0
      }

    }

    //结束生命周期
    override def close(): Unit = {
      updateStr.executeBatch()
      insertStr.executeBatch()
      insertStr.close()
      updateStr.close()
      coon.close()
    }
  }

}

```

# 四 窗口操作

## 4.1 基本介绍

窗口操作是流处理程序中很常见的操作。窗口操作允许我们**在无限流上的一段有界区间上面做聚合之类的操作。**而我们使用基于时间的逻辑来定义区间。窗口操作符提供了一种将数据放进一个桶，并根据桶中的数据做计算的方法。例如，我们可以将事件放进5分钟的滚动窗口中，然后计数。

> 无限流转化成有限数据的方法：使用窗口。

开窗的目的是为了聚合，常规操作是先分流开窗然后聚合。

WindowStream不能直接打印。

## 4.2 如何定义窗口？

Window算子可以在keyed stream或者nokeyed stream上使用。

创建一个Window算子，需要指定两个部分：

1. `window assigner`定义了流的元素如何分配到window中。

   window assigner将会产生一条WindowedStream(或者AllWindowedStream，如果是nonkeyed DataStream的话)

2. window function用来处理WindowedStream(AllWindowedStream)中的元素。

下面的代码说明了如何使用窗口操作符。

```scala
    stream
      .keyBy(...)
      .window(...)  // 指定window assigner
      .reduce/aggregate/process(...) // 指定window function
    
    stream
      .windowAll(...) // 指定window assigner
      .reduce/aggregate/process(...) // 指定window function
```

## 4.3 窗口的分配及触发

### 4.3.1 窗口的分配

窗口分配器将会根据事件的事件时间或处理时间来将事件分配到对应的窗口中去，窗口包含开始时间和结束时间这两个时间戳。

### 4.3.2 窗口的触发

所有的窗口分配器都包含一个默认的触发器：

- 对于事件时间：当水位线超过窗口结束时间，触发窗口的求值操作。

  > 也叫逻辑时钟，向量时钟。

- 对于处理时间：当机器时间超过窗口结束时间，触发窗口的求值操作。

  > 墙上时钟。

> 需要注意的是：当处于某个窗口的第一个事件到达的时候，这个窗口才会被创建。Flink不会对空窗口求值。

Flink创建的窗口类型是`TimeWindow`，包含开始时间和结束时间，区间是左闭右开的，也就是说包含开始时间戳，不包含结束时间戳。

### 4.3.3 窗口清空时间

![image-20200710081844875](D:\ProgramFiles\Typora\图片备份\image-20200710081844875.png)

窗口最大关闭时间 + 允许最大等待时间

## 4.4 滚动窗口

### 4.4.1 基本介绍

滚动窗口将数据按照固定的窗口长度对数据进行切分。

- 时间对齐
- 窗口长度固定
- 窗口之间不重叠

![img](https://confucianzuoyuan.github.io/flink-tutorial/images/spaf_0601.png)

## 4.5 滑动窗口

### 4.5.1 基本介绍

对于滑动窗口，我们需要指定窗口的大小和滑动的步长，当滑动步长小于窗口大小时，窗口将会出现重叠，而元素会被分配到不止一个窗口中。当滑动步长大于窗口大小时，一些元素可能不会被分配到任何窗口中，会被直接丢弃。

![img](https://confucianzuoyuan.github.io/flink-tutorial/images/spaf_0602.png)

- 窗口长度固定
- 窗口之间可以有重叠

## 4.6 会话窗口（Flink特有）

### 4.6.1 基本介绍

会话窗口不可能重叠，并且会话窗口的大小也不是固定的。不活跃的时间长度定义了会话窗口的界限。不活跃的时间是指这段时间没有元素到达。下图展示了元素如何被分配到会话窗口。

![img](https://confucianzuoyuan.github.io/flink-tutorial/images/spaf_0603.png)

由于会话窗口的开始时间和结束时间取决于接收到的元素，所以窗口分配器无法立即将所有的元素分配到正确的窗口中去。相反，会话窗口分配器最开始时先将每一个元素分配到它自己独有的窗口中去，窗口开始时间是这个元素的时间戳，窗口大小是session gap的大小。接下来，会话窗口分配器会将出现重叠的窗口合并成一个窗口。

- 由一系列事件组合一个。一段时间没有接受到新数据就会生成新的窗口
- 时间不对齐

## 4.7 窗口计算函数

### 4.7.1 基本介绍

window functions定义了窗口中数据的计算逻辑。有两种计算逻辑：

1. 增量聚合函数(Incremental aggregation functions)：当一个事件被添加到窗口时，触发函数计算，并且更新window的状态(单个值)。最终聚合的结果将作为输出。ReduceFunction和AggregateFunction是增量聚合函数。
2. 全窗口函数(Full window functions)：这个函数将会收集窗口中所有的元素，可以做一些复杂计算。ProcessWindowFunction是window function。

### 4.7.2 增量聚合函数

增量聚合函数何时发射？

> 窗口闭合时发射数据。

#### 1）AggregateFunction

##### 基本介绍

```scala
public interface AggregateFunction<IN, ACC, OUT>
  extends Function, Serializable {

  // 初始化累加器
  ACC createAccumulator();

  // 累加器逻辑
  ACC add(IN value, ACC accumulator);

  // 累加器输出类型
  OUT getResult(ACC accumulator);

  // 累加器合并，用在会话窗口
  ACC merge(ACC a, ACC b);
}
```
IN是输入元素的类型，ACC是累加器的类型，OUT是输出元素的类型。

##### 基本使用

```scala
package com.atguigu.day03.window

import com.atguigu.day02.source.SensorSource
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.windowing.time.Time

/**
 * @Classname AvgWindowExample
 * @Description TODO
 *              Date ${Date} 14:09
 * @Create by childwen
 */
object AvgWindowExample {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream = env.addSource(new SensorSource)
    stream
      .map(r => (r.id, r.temperature))
      .keyBy(0)
      .timeWindow(Time.seconds(5))
      .aggregate(new MyAggregate)
      .print()

    env.execute()
  }

  class MyAggregate extends AggregateFunction[(String, Double), (String, Double, Long), (String, Double)] {
    //初始化累加器
    override def createAccumulator(): (String, Double, Long) = ("", 0.0, 0L)

    //累加器逻辑
    override def add(in: (String, Double), acc: (String, Double, Long)): (String, Double, Long) = {
      val id = in._1
      val sum = in._2 + acc._2
      val count = acc._3 + 1
      (id, sum, count)
    }

    //窗口关闭时返回的累加器结果
    override def getResult(acc: (String, Double, Long)): (String, Double) = {
      val id = acc._1
      val avg = acc._2 / acc._3
      (id, avg)
    }

    //累加器合并
    override def merge(acc: (String, Double, Long), acc1: (String, Double, Long)): (String, Double, Long) = {
      val id = acc._1
      val sum = acc._2 + acc1._2
      val count = acc._3 + acc1._3
      (id, sum, count)
    }
  }

}

```

#### 2）ReduceFunction 

##### 基本介绍

示例：对窗口中的元组`Tuple3(string,double,double)`求最大值和最小值

```scala
  class MyReduceFunction extends ReduceFunction[(String, Double, Double)]() {
    override def reduce(value1: (String, Double, Double), value2: (String, Double, Double)): (String, Double, Double) = {
      val id = value1._1
      val min = value1._2.min(value2._2)
      val max = value1._3.max(value2._3)
      (id, min, max)
    }
  }
```

##### 基本使用

```scala
object MinTemperatureExample {
  def main(args: Array[String]): Unit = {


    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)

    stream
      .map(r => (r.id, r.temperature, format(r.timestamp)))
      //分流
      .keyBy(0)
      //对每一条流开一个窗口,长度为10 滑动距离为5
      .timeWindow(Time.seconds(10), Time.seconds(5))
      //聚合操作，def reduce(function: (T, T) => T): DataStream[T]
      //返回值和输入的类型必须保持一致，这里我们舍弃了r1和r2的温度，只聚合保留了一个最小值作为累加器
      .reduce((r1, r2) => (r1._1, r1._2.min(r2._2), r2._3))
      .print

    env.execute()
  }
}
```



### 4.7.3 全窗口聚合

#### ProcessWindowFunction

##### 基本介绍

一些业务场景，我们需要收集窗口内所有的数据进行计算，例如计算窗口数据的中位数，或者计算窗口数据中出现频率最高的值。这样的需求，使用ReduceFunction和AggregateFunction就无法实现了。这个时候就需要ProcessWindowFunction了。

接口定义

```scala
    public abstract class ProcessWindowFunction<IN, OUT, KEY, W extends Window>
      extends AbstractRichFunction {
      
      // Evaluates the window
      void process(KEY key, Context ctx, Iterable<IN> vals, Collector<OUT> out)
        throws Exception;
    
      // Deletes any custom per-window state when the window is purged
      public void clear(Context ctx) throws Exception {}
    
      // The context holding window metadata
      public abstract class Context implements Serializable {
        // Returns the metadata of the window
        public abstract W window();
    
        // Returns the current processing time
        public abstract long currentProcessingTime();
    
        // Returns the current event-time watermark
        public abstract long currentWatermark();
    
        // State accessor for per-window state
        public abstract KeyedStateStore windowState();
    
        // State accessor for per-key global state
        public abstract KeyedStateStore globalState();
    
        // Emits a record to the side output identified by the OutputTag.
        public abstract <X> void output(OutputTag<X> outputTag, X value);
      }
    }
```

process()方法接受的参数为：window的key，Iterable迭代器包含窗口的所有元素，Collector用于输出结果流。Context参数和别的process方法一样。而ProcessWindowFunction的Context对象还可以访问window的元数据(窗口开始和结束时间)，当前处理时间和水位线，per-window state和per-key global state，side outputs。

- per-window state: 用于保存一些信息，这些信息可以被process()访问，只要process所处理的元素属于这个窗口。
- per-key global state: 同一个key，也就是在一条KeyedStream上，不同的window可以访问per-key global state保存的值。

##### 基本使用

计算5s滚动窗口中的最低和最高的温度。输出的元素包含了(流的Key, 最低温度, 最高温度, 窗口结束时间)。

```scala

val minMaxTempPerWindow: DataStream[MinMaxTemp] = sensorData
  .keyBy(_.id)
  .timeWindow(Time.seconds(5))
  .process(new HighAndLowTempProcessFunction)

//样例类
case class MinMaxTemp(id: String, min: Double, max: Double, endTs: Long)

//自定义类实现ProcessWindowFunction
//泛型指定 [IN,OUT,KEY,Window]
class HighAndLowTempProcessFunction
  extends ProcessWindowFunction[SensorReading,
    MinMaxTemp, String, TimeWindow] {
  override def process(key: String,
                       ctx: Context,
                       vals: Iterable[SensorReading],
                       out: Collector[MinMaxTemp]): Unit = {
    val temps = vals.map(_.temperature)
    val windowEnd = ctx.window.getEnd

    out.collect(MinMaxTemp(key, temps.min, temps.max, windowEnd))
  }
}
```

### 4.7.4 增量聚合 + 全窗口聚合

#### 基本介绍

1）我们还可以将ReduceFunction/AggregateFunction和ProcessWindowFunction结合起来使用。ReduceFunction/AggregateFunction做增量聚合，ProcessWindowFunction提供更多的对数据流的访问权限。

2）如果只使用ProcessWindowFunction(底层的实现为将事件都保存在ListState中)，将会非常占用空间。

3）使用增量聚合分配到某个窗口的元素将被提前聚合，而当窗口的trigger触发时，也就是窗口收集完数据关闭时，将会把聚合结果发送到ProcessWindowFunction中，这时Iterable参数将会只有一个值，就是前面聚合的值。

#### 基本使用 Aggregate+全量

示例：给定一个5s的窗口，求出最小值最大值和窗口结束时间。

```scala
package com.atguigu.day03.window

import com.atguigu.day02.source.{SensorReading, SensorSource}
import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

/**
 * @Classname AggAndProcessFromWindowExample
 * @Description TODO
 *              Date ${Date} 13:27
 * @Create by childwen
 */
object AggAndProcessFromWindowExample {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)
    stream
      .keyBy(_.id)
      .timeWindow(Time.seconds(5))
      .aggregate(new Agg, new TempProcess)
      .print()

    env.execute()
  }

  //增量聚合
  class Agg extends AggregateFunction[SensorReading, (String, Double, Double), TempMaxMin] {
    override def createAccumulator(): (String, Double, Double) = ("", Double.MaxValue, Double.MinValue)

    override def add(in: SensorReading, acc: (String, Double, Double)): (String, Double, Double) = {
      val id = in.id
      val min = in.temperature.min(acc._2)
      val max = in.temperature.max(acc._3)
      (id, min, max)
    }

    override def getResult(acc: (String, Double, Double)): TempMaxMin = {
      TempMaxMin(acc._1, acc._2, acc._3)
    }

    override def merge(acc: (String, Double, Double), acc1: (String, Double, Double)): (String, Double, Double) = {
      (acc._1, acc._2.min(acc1._2), acc._3.max(acc1._3))
    }
  }

    
  //样例类
  case class TempMaxMin(id: String, min: Double, max: Double, var endTime: Long = 0L)





  //全量聚合
  class TempProcess extends ProcessWindowFunction[TempMaxMin, TempMaxMin, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[TempMaxMin],
                         out: Collector[TempMaxMin]): Unit = {
      val head = elements.head
      val endTime = context.window.getEnd
      head.endTime = endTime
      out.collect(head)
    }
  }

}

```

#### 基本使用 Reduce+全量

示例：给定一个5s的窗口，求出最小值最大值和窗口结束时间。

```scala
package com.atguigu.day03.window


import com.atguigu.day02.source.SensorSource
import com.atguigu.day03.window.AggAndProcessFromWindowExample.TempMaxMin
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector


/**
 * @Classname ReduceAndProcessFromWindowExample
 * @Description TODO
 *              Date ${Date} 16:10
 * @Create by childwen
 */
object ReduceAndProcessFromWindowExample {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)
    stream
      .map(r => (r.id, r.temperature, r.temperature))
      .keyBy(_._1)
      .timeWindow(Time.seconds(5))
      //使用reduce包裹增量聚合和全量聚合
      .reduce(
        (r1:(String,Double,Double), r2:(String,Double,Double)) => (r1._1, r1._2.min(r2._2), r1._3.max(r1._3)),
        new WindowProcess
      )
      .print

    env.execute()
  }

  class WindowProcess extends ProcessWindowFunction[(String, Double, Double), TempMaxMin, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[(String, Double, Double)],
                         out: Collector[TempMaxMin]): Unit = {
      val head = elements.head
      out.collect(TempMaxMin(head._1, head._2, head._3, context.window.getEnd))
    }
  }

}

```

## 4.8 自定义窗口操作符

### 4.8.1 关于WindowAssigner

Flink内置的window operators分配器已经已经足够应付大多数应用场景。尽管如此，如果我们需要实现一些复杂的窗口逻辑，例如：可以发射早到的事件或者碰到迟到的事件就更新窗口的结果，或者窗口的开始和结束决定于特定事件的接收。

DataStream API暴露了接口和方法来自定义窗口操作符。

- 自定义窗口分配器
- 自定义窗口计算触发器(trigger)
- 自定义窗口数据清理功能(evictor)

当一个事件来到窗口操作符，首先将会传给WindowAssigner来处理。**WindowAssigner决定了事件将被分配到哪些窗口**。如果窗口不存在，WindowAssigner将会创建一个新的窗口。

### 4.8.2 关于Trigger

如果一个window operator接受了一个增量聚合函数作为参数，例如ReduceFunction或者AggregateFunction，新到的元素将会立即被聚合，而聚合结果result将存储在window中。如果window operator没有使用增量聚合函数，那么新元素将被添加到ListState中，ListState中保存了所有分配给窗口的元素。

当新元素被添加到窗口时，这个新元素同时也被传给了window的trigger，trigger定义了window何时准备好求值（FIRE），何时window被清空（PURGE）。trigger可以基于window被分配的元素和注册的定时器来对窗口所有元素求值或者在特定事件清空window中所有的元素。



每次调用触发器都会产生一个TriggerResult来决定窗口接下来发生什么。TriggerResult可以取以下结果：

- CONTINUE：什么都不做
- FIRE：如果window operator有ProcessWindowFunction这个参数，将会调用这个ProcessWindowFunction。如果窗口仅有增量聚合函数(ReduceFunction或者AggregateFunction)作为参数，那么当前的聚合结果将会被发送。窗口的state不变。
- PURGE：窗口所有内容包括窗口的元数据都将被丢弃。
- FIRE_AND_PURGE：先对窗口进行求值，再将窗口中的内容丢弃。

#### 案例1

下面的例子展示了一个触发器在窗口结束时间之前触发。当第一个事件被分配到窗口时，这个触发器注册了一个定时器，定时时间为水位线之前一秒钟。当定时事件执行，将会注册一个新的定时事件，这样，这个触发器每秒钟最多触发一次。

```scala
package com.atguigu.day5

import java.sql.Timestamp

import com.atguigu.day2.{SensorReading, SensorSource}
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.{Trigger, TriggerResult}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

object TriggerExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env
      .addSource(new SensorSource)
      .filter(r => r.id.equals("sensor_2"))
      .keyBy(_.id)
      .timeWindow(Time.seconds(10))
      .trigger(new OneSecondIntervalTrigger)
      .process(new WindowResult)

    stream.print()
    env.execute()
  }

  class WindowResult extends ProcessWindowFunction[SensorReading, String, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[SensorReading], out: Collector[String]): Unit = {
      out.collect("传感器ID为 " + key + " 的传感器窗口中元素的数量是 " + elements.size)
    }
  }

  class OneSecondIntervalTrigger extends Trigger[SensorReading, TimeWindow] {
    // 来一条数据调用一次
    override def onElement(element: SensorReading, timestamp: Long, window: TimeWindow, ctx: Trigger.TriggerContext): TriggerResult = {
      val firstSeen = ctx.getPartitionedState(
        new ValueStateDescriptor[Boolean]("first-seen", Types.of[Boolean])
      )

      // 如果firstSeen为false，也就是当碰到第一条元素的时候
      if (!firstSeen.value()) {
        // 假设第一条事件来的时候，机器时间是1234ms，t是多少？t是2000ms
        val t = ctx.getCurrentProcessingTime + (1000 - (ctx.getCurrentProcessingTime % 1000))
        ctx.registerProcessingTimeTimer(t) // 在2000ms注册一个定时器
        ctx.registerProcessingTimeTimer(window.getEnd) // 在窗口结束时间注册一个定时器
        firstSeen.update(true)
      }

      TriggerResult.CONTINUE
    }

    override def onEventTime(time: Long, window: TimeWindow, ctx: Trigger.TriggerContext): TriggerResult = {
      TriggerResult.CONTINUE
    }

    // 注册的定时器的回调函数
    override def onProcessingTime(time: Long, window: TimeWindow, ctx: Trigger.TriggerContext): TriggerResult = {
      println("回调函数触发时间：" + new Timestamp(time))
      if (time == window.getEnd) {
          //当时间等于窗口关闭时间时，其实系统已经关闭该窗口了。所以这个地方执行时，窗口已经不存在了。
        TriggerResult.FIRE_AND_PURGE
      } else {
        val t = ctx.getCurrentProcessingTime + (1000 - (ctx.getCurrentProcessingTime % 1000))
        if (t < window.getEnd) {
          ctx.registerProcessingTimeTimer(t)
        }
        TriggerResult.FIRE
      }
    }

    override def clear(window: TimeWindow, ctx: Trigger.TriggerContext): Unit = {
      // SingleTon, 单例模式，只会被初始化一次
      val firstSeen = ctx.getPartitionedState(
        new ValueStateDescriptor[Boolean]("first-seen", Types.of[Boolean])
      )
      firstSeen.clear()
    }
  }
}
```

> 这里要注意两个地方：清空state和merging合并触发器。

#### 案例2 

使用处理时间，每秒钟统计一次当前窗口中元素的个数

```scala
package com.atguigu.day05.tigger

import com.atguigu.day02.source.{SensorReading, SensorSource}
import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.{Trigger, TriggerResult}
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

/**
 * @Classname TrigerExample
 * @Description TODO
 *              Date ${Date} 11:31
 * @Create by childwen
 */
object TriggerExample {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    val stream = env
      .addSource(new SensorSource)
    stream
      .keyBy(_.id)
      .timeWindow(Time.seconds(5))
      .trigger(new OneSecondIntervalTrigger)
      .process(new MyWindowProcess)
      .print

    env.execute()
  }

  class MyWindowProcess extends ProcessWindowFunction[(SensorReading), String, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[SensorReading], out: Collector[String]): Unit = {
      out.collect(s"传感器ID为$key 的窗口内元素个数：${elements.size}")
    }
  }

  class OneSecondIntervalTrigger extends Trigger[SensorReading, TimeWindow] {
    //对每一个元素都调用一次
    override def onElement(element: SensorReading, timestamp: Long, window: TimeWindow, ctx: Trigger.TriggerContext): TriggerResult = {
      //定义状态变量
      val firstSeen = ctx.getPartitionedState(
        new ValueStateDescriptor[Boolean](
          "first-seen", Types.of[Boolean]
        )
      )
      if (!firstSeen.value()) {
        //注册一个一秒钟以后的定时器
        val t = ctx.getCurrentProcessingTime + (1000 - (ctx.getCurrentProcessingTime % 1000))
        ctx.registerProcessingTimeTimer(t)
        //窗口结束时注册一个定时器
        ctx.registerProcessingTimeTimer(window.getEnd)
        //更新状态的值
        firstSeen.update(true)
      }
      TriggerResult.CONTINUE
    }

    override def onProcessingTime(time: Long, window: TimeWindow, ctx: Trigger.TriggerContext): TriggerResult = {

      if (time == window.getEnd) {
        //如果当前时间等于窗口结束时间先计算窗口，然后丢弃窗口
        TriggerResult.FIRE_AND_PURGE
      } else {
        //获取当前时间的1秒后
        val t = ctx.getCurrentProcessingTime + (1000 - (ctx.getCurrentProcessingTime % 1000))
        //继续注册一个一秒后的定时器
        if (t < window.getEnd) ctx.registerProcessingTimeTimer(t)

        TriggerResult.FIRE
      }
    }

    override def onEventTime(time: Long, window: TimeWindow, ctx: Trigger.TriggerContext): TriggerResult = TriggerResult.CONTINUE

    override def clear(window: TimeWindow, ctx: Trigger.TriggerContext): Unit = {
      //状态变量是单例模式。
      //从不同的作用域获取状态变量可以通过直接创建对象来获取，单例模式。
      val firstSeen = ctx.getPartitionedState(
        new ValueStateDescriptor[Boolean](
          "first-seen", Types.of[Boolean]
        )
      )
      firstSeen.clear()
    }
  }

}

```

### 4.8.3 关于清理器(EVICTORS)

evictor可以在window function求值之前或者之后移除窗口中的元素。

**Evictor的接口定义：**

```scala
    public interface Evictor<T, W extends Window>
        extends Serializable {
      void evictBefore(
        Iterable<TimestampedValue<T>> elements,
        int size,
        W window,
        EvictorContext evictorContext);
    
      void evictAfter(
        Iterable<TimestampedValue<T>> elements,
        int size,
        W window,
        EvictorContext evictorContext);
    
      interface EvictorContext {
    
        long getCurrentProcessingTime();
    
        long getCurrentWatermark();
      }
    }
```

evictBefore()和evictAfter()分别在window function计算之前或者之后调用。Iterable迭代器包含了窗口所有的元素，size为窗口中元素的数量，window object和EvictorContext可以访问当前处理时间和水位线。可以对Iterator调用remove()方法来移除窗口中的元素。

evictor也经常被用在GlobalWindow上，用来清除部分元素，而不是将窗口中的元素全部清空。

# 五 Flink名词理解

## 水位线是什么？

Watermark 是事件时间域中衡量输入完成进度的一种时间概念。换句话说，在处理使用事件时间属性的数据流时，Watermark 是系统测量数据处理进度的一种方法。假如当前系统的 watermark 为时间 T，那么系统认为所有事件时间小于 T 的消息都已经到达，即系统任务它不会再接收到事件时间小于 T 的消息了。有了 Watermark，系统就可以确定使用事件时间的窗口是否已经完成。但是 Watermark 只是一种度量指标，系统借由它来评估当前的进度，并不能完全保证不会出现小于当前 Watermark 的消息。对于这种消息，即“迟到”的消息，需要进行特殊的处理。这也就是前面所说的流处理系统面临的 **How** 的问题，即如何处理迟到的消息，从而修正已经输出的计算结果。

总结：

- Watermark是一种衡量EventTime进展的机制，可以设定延迟触发。
- Watermark是用于处理乱序时间的，而正确的处理乱序时间，通常用Watermark机制结合window来实现。
- 数据流中的Watermark用于表示timestamp小于Watermark的数据都已经到达了，因此window的执行也是由Watermark触发的。
- Watermark用来让程序自己平衡延迟和结果正确性。
- 水位线只是衡量数据迟没迟到，进入窗口的范围无关系

## 窗口存在的意义是什么？

**聚合类的处理** Flink可以每来一个消息就处理一次，但是有时我们需要做一些聚合类的处理，例如：在过去的1分钟内有多少用户点击了我们的网页。所以Flink引入了窗口概念。

 窗口的作用为了周期性的获取数据。就是把传入的原始数据流切分成多个buckets，所有计算都在单一的buckets中进行。窗口（window）就是从 Streaming 到 Batch 的一个桥梁。

窗口带来的问题是什么？

**带来的问题**：聚合类处理带来了新的问题，比如乱序/延迟。其解决方案就是 Watermark / allowLateNess / sideOutPut 这一组合拳。

**Watermark** 的作用是防止 数据乱序 / 指定时间，内获取不到全部数据。

**allowLateNess** 是将窗口关闭时间再延迟一段时间。

**sideOutPut **是最后兜底操作，当指定窗口已经彻底关闭后，就会把所有过期延迟数据放到侧输出流，让用户决定如何处理。

总结起来就是说

```java
Windows -----> Watermark -----> allowLateNess -----> sideOutPut 
    
用Windows把流数据分块处理，用Watermark确定什么时候不再等待更早的数据/触发窗口进行计算，用allowLateNess 将窗口关闭时间再延迟一段时间。用sideOutPut 最后兜底把数据导出到其他地方。
```

## 并行度、任务槽、分流的概念

![并行度、任务槽和KeyedStream的概念](D:\ProgramFiles\Typora\图片备份\并行度、任务槽和KeyedStream的概念.jpg)

# 六 Process Function类

![image-20200613091920579](D:\ProgramFiles\Typora\图片备份\image-20200613091920579.png)

## 6.1 基本介绍

我们之前学习的转换算子是无法访问事件的时间戳信息和水位线信息的。而这在一些应用场景下，极为重要。例如MapFunction这样的map转换算子就无法访问时间戳或者当前事件的事件时间。

基于此，DataStream API提供了一系列的Low-Level转换算子。可以访问时间戳、水位线以及注册定时事件。还可以输出特定的一些事件，例如超时事件等。Process Function用来构建事件驱动的应用以及实现自定义的业务逻辑(使用之前的window函数和转换算子无法实现)。例如，Flink-SQL就是使用Process Function实现的。

Flink提供了8个Process Function：

- ProcessFunction	**(no-key)**

- KeyedProcessFunction**(基于keyBy)**

- CoProcessFunction**(基于connect)**

- ProcessJoinFunction**(基于间隔的join)**

- BroadcastProcessFunction

- KeyedBroadcastProcessFunction

- ProcessWindowFunction**(窗口)**

- ProcessAllWindowFunction


所有的Process Function都继承自RichFunction接口，所以都有open()、close()和getRuntimeContext()等方法。

## 6.2 方法介绍

- processElement(v: IN, ctx: Context, out: Collector[OUT]), 流中的每一个元素都会调用这个方法，调用结果将会放在Collector数据类型中输出。Context可以访问元素的时间戳，元素的key，以及TimerService时间服务。Context还可以将结果输出到别的流(side outputs)。
- onTimer(timestamp: Long, ctx: OnTimerContext, out: Collector[OUT])是一个回调函数。当之前注册的定时器触发时调用。参数timestamp为定时器所设定的触发的时间戳。Collector为输出结果的集合。OnTimerContext和processElement的Context参数一样，提供了上下文的一些信息，例如firing trigger的时间信息(事件时间或者处理时间)。
- Context和OnTimerContext所持有的TimerService对象拥有以下方法:
  - `currentProcessingTime(): Long` 返回当前处理时间
  - `currentWatermark(): Long` 返回当前水位线的时间戳
  - `registerProcessingTimeTimer(timestamp: Long): Unit` 会注册当前key的processing time的timer。当processing time到达定时时间时，触发timer。
  - `registerEventTimeTimer(timestamp: Long): Unit` 会注册当前key的event time timer。当水位线大于等于定时器注册的时间时，触发定时器执行回调函数。
  - `deleteProcessingTimeTimer(timestamp: Long): Unit` 删除之前注册处理时间定时器。如果没有这个时间戳的定时器，则不执行。
  - `deleteEventTimeTimer(timestamp: Long): Unit` 删除之前注册的事件时间定时器，如果没有此时间戳的定时器，则不执行。



  

## 6.1 开窗ProcessWindowFunction

- 抽象类ProcessWindowFunction的顶级父类是RichFunction

- 在窗口结束时，将窗口内所有的元素收集到一个迭代器中。
- 可以获取当前算子的时间
- 如果是使用事件时间还可以获取当前水位线
- 可以获取当前窗口关闭时间
- 有open和close方法，可以用来初始化和关闭资源
- 侧输出

##  6.2 分流KeyedProcessFunction

- 顶级父类RichFunction

- 对流中的每一个元素调用一次该函数。
- 可以获取当前算子的处理时间和事件时间
- 可以注册定时器
- 可以获取当前分流的key值
- 有open和 close方法，可以用来初始化资源和关闭资源
- 侧输出

## 6.3 不开窗不分流ProcessFunction

- 顶级父类RichFunction

- 对流中的每一个元素调用一次该函数。
- 可以获取当前算子的处理时间和事件时间
- 可以注册定时器
- 有open和 close方法，可以用来初始化资源和关闭资源
- 侧输出

## 6.4 connect的CoProcessFunction

- 顶级父类RichFunction

- 对合并前的每一条流中的每一个元素调用一次该函数。
- 可以获取当前算子的处理时间和事件时间
- 可以注册定时器
- 有open和 close方法，可以用来初始化资源和关闭资源
- 侧输出

## 6.5 总结

1）在窗口上使用ProcessWindowFunction函数时，方法调用是在窗口关闭时调用。并返回一个迭代器，迭代器中会收集窗口内的所有数据，没有定时器，因为窗口关闭以后就没有存在的意义了，可以简单理解为一个定时器的快速实现类。

2）process函数的通用特性

- 都可以获取到一个timeservice，通过timeservice可以获取到当前算子的处理时间和事件时间，或者注册定时器。
- 都可以重写定时器onTime
- 都支持侧输出

# 七 时间语义

## 7.1 时间语义基本介绍

Event Time：表示事件创建的时间。

Ingestion Time：表示数据进入Flink的时间。

Processing Time：**执行操作算子的本地系统时间**，与机器相关。



## 7.2 事件时间和水位线的基本使用

### 7.2.1 设置使用事件时间

直接在代码中，对执行环境调用setStreamTimeCharacteristic，设置流的时间特性为事件时间。

**具体时间的提取，还需要从数据中提取时间戳。**

```scala
//从调用时刻开始给env创建的每一个stream追加时间特性
env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
```

> 默认使用处理时间。

### 7.2.2 水位线的计算方式

水位线 = 事件时间 - 最大延迟时间

### 7.2.3 水位线的引入

EventTime的使用一定要指定数据源中的时间戳。

调用assignTimestampAndWatermarks方法，传入一个`BoundedOutOfDernessTimestampExtractor`，就可以指定watermark。

#### 1）产生带延迟时间的水位线

```scala
// 抽取时间戳和插入水位线
// 插入水位线的操作一定要紧跟source算子
.assignTimestampsAndWatermarks(
  // 最大延迟时间设置为5s
  new BoundedOutOfOrdernessTimestampExtractor[(String, Long)](Time.seconds(5)) {
    override def extractTimestamp(element: (String, Long)): Long = element._2
  }
)
```

#### 2）产生不包含延迟时间的水位线

```scala
stream
      .map(line => {
        val arr = line.split("\\s")
        (arr(0), arr(1).toLong * 1000)
      })
      //插入水位线
      .assignAscendingTimestamps(_._2)
```

#### 3）自定义类周期型的生成watermark

```scala
package com.atguigu.day04.watermark

import java.sql.Timestamp

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

/**
 * @Classname PeriodIncWaterMark
 * @Description TODO
 *              Date ${Date} 9:27
 * @Create by childwen
 */
object PeriodIncWaterMark {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream = env.socketTextStream("hadoop102", 9999, '\n')

    stream
      .map(line => {
        val arr = line.split("\\s")
        (arr(0), arr(1).toLong * 1000)
      })
      //设置水位线 延迟5s
      .assignTimestampsAndWatermarks(new MyAssTimestamp)
      .keyBy(_._1)
      //滚动窗口 10s滚动一次
      .timeWindow(Time.seconds(10))
      //窗口全量函数
      .process(new MyProcessWindow)
      .print

    env.execute()
  }

  class MyAssTimestamp extends AssignerWithPeriodicWatermarks[(String, Long)] {
    //最大延迟时间
    val bound = 5000L
    var maxTs = Long.MinValue + bound

    override def getCurrentWatermark: Watermark = {
      //水位线 = 观察到的最大时间 - 最大延迟时间
      new Watermark(maxTs - bound)
    }

    //产生水位线的函数，默认每200ms就调用一次
    //每进来一个元素就调用一次
    override def extractTimestamp(element: (String, Long), previousElementTimestamp: Long): Long = {
      maxTs = maxTs.max(element._2)
      element._2
    }
  }

  class MyProcessWindow extends ProcessWindowFunction[(String, Long), String, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[(String, Long)], out: Collector[String]): Unit = {
      out.collect(s"当前水位线 ${new Timestamp(context.currentWatermark)} 窗口关闭时间 ${new Timestamp(context.currentProcessingTime)} 窗口有效元素个数 ${elements.size}")
    }
  }


}

```

#### 4）自定义类不规则的生成水位线

有时候输入流中会包含一些用于指示系统进度的特殊元组或标记。Flink为此类情形以及可根据输入元素生成水位线的情形提供了`AssignerWithPunctuatedWatermarks`接口。该接口中的`checkAndGetNextWatermark()`方法会在针对每个事件的`extractTimestamp()`方法后立即调用。它可以决定是否生成一个新的水位线。如果该方法返回一个非空、且大于之前值的水位线，算子就会将这个新水位线发出。

**没有时间周期规律，可打断的生成watermark**

```scala
class PunctuatedAssigner
  extends AssignerWithPunctuatedWatermarks[SensorReading] {
  val bound: Long = 60 * 1000

  // 每来一条数据就调用一次
  override def checkAndGetNextWatermark(r: SensorReading,
                                        extractedTS: Long): Watermark = {
    if (r.id == "sensor_1") {
      // 抽取的时间戳 - 最大延迟时间
      new Watermark(extractedTS - bound)
    } else {
      null
    }
  }

  // 每来一条数据就调用一次
  override def extractTimestamp(r: SensorReading,
                                previousTS: Long): Long = {
    r.timestamp
  }
}
```



### 7.2.4 水位线的刷新策略

默认是200ms刷新一次，也可以手动在env中进行设置。

```scala
// 系统每隔一分钟的机器时间插入一次水位线
    env.getConfig.setAutoWatermarkInterval(60000)
```



### 7.2.5 注意事项

1）分流以后的对keyedStream的所有算子操作都是针对某一条以key为分支的流进行操作，比如在keyby以后对每一条流使用process全量函数，该函数将会作用来每一条流上。

**但是水位线的触发是全局的。**

2）waterMark必须单调递增，以确保任务事件时钟是在向前推进而不是在后退。

3）watermark的值与数据的时间戳相关。

4）水位线只是一个衡量的标准，在有窗口时通过水位线来关闭窗口。在无窗口时，水位线是衡量数据是否迟到的标准。

## 7.3 水位线的传递

![image-20200613133129958](D:\ProgramFiles\Typora\图片备份\image-20200613133129958.png)

#### 7.3.1 基本介绍

在流进行connect时，合并以后的流水位线生成公式：`Min(a1,a2,a3)`，当水位线更新时会进行计划，将计算出来的结果值广播给下游。

在处理离线数据时，使用事件时间当数据源的所有数据都读取完毕时，系统会向下发送一条无限大的水位线以确保触发所有的窗口关闭。

#### 7.3.2 案例示范

通过socket创建两条流，使用connect连接以后用CoProcess函数，分别查看两条流的水位线对比。

```scala
package com.atguigu.day04.selfstudy

import java.sql.Timestamp

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.util.Collector

/**
 * @Classname ProcessTest
 * @Description TODO
 *              Date ${Date} 16:03
 * @Create by childwen
 */
object ProcessTest {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    //设置流的时间特性为事件时间
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream1 = env
      .socketTextStream("hadoop102", 9999, '\n')
      .map(line => {
        val arr = line.split("\\s")
        (arr(0), arr(1).toLong * 1000L)
      })
      .assignAscendingTimestamps(_._2)
      .keyBy(_._1)

    val stream2 = env
      .socketTextStream("hadoop102", 9998, '\n')
      .map(line => {
        val arr = line.split("\\s")
        (arr(0), arr(1).toLong * 1000L)
      })
      .assignAscendingTimestamps(_._2)
      .keyBy(_._1)
    //流的连接
    stream1.connect(stream2)
    //使用CoProcess函数打印两条流的水位线变化
        .process(new MyConnectCoProcess)
        .print

    env.execute()
  }

  class MyConnectCoProcess extends CoProcessFunction[(String,Long),(String,Long),String]{
    //该方法对流1中的每一条元素都调用一次
    override def processElement1(value: (String, Long),
                                 ctx: CoProcessFunction[(String, Long), (String, Long),
                                   String]#Context, out: Collector[String]): Unit = {
      out.collect(s"第一条流调用process方法时水位线 ${ctx.timerService().currentWatermark()}")

    }

    override def processElement2(value: (String, Long),
                                 ctx: CoProcessFunction[(String, Long), (String, Long), String]#Context,
                                 out: Collector[String]): Unit = {
      out.collect(s"第二条流调用process方法时的水位线 ${ctx.timerService().currentWatermark()}")
    }
  }

}

```

#### 7.3.3 总结

水位线传递时，我们的关注点一定要放在上游当前最小的水位线。

# 八 侧输出

## 8.1 基本介绍

大部分的DataStream API的算子输出是单一输出，也就是某种类型的流。除了split算子，可以将一条流分成多条流，这些流的数据类型也都相同。process function的side output功能可以产生多条流，并且这些流的数据类型可以不一样。一个side output可以定义为OutputTag[x]对象，X是输出流的数据类型，process function可以通过Context对象发射一个事件到一个或者多个side outputs。

将数据输出到侧输出：

```scala
//定义一个侧输出标签
lazy val freezingAlarmOutput = new OutputTag[String]("freezing-1")
//将数据输出到侧输出标签对象
 ctx.output(freezingAlarmOutput, s"${value.id}温度小于60华摄氏度")
```



调用侧输出流：

```scala
stream
//创建一个侧输出Output标签输入对应标签的id
.getSideOutput(new OutputTag[String]("freezing-1"))
```



## 8.2 基本使用

需求：将温度低于32F的传感器ID输出到侧输出流

```scala
package com.atguigu.day04.selfstudy

import com.atguigu.day02.source.{SensorReading, SensorSource}
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.{OutputTag, _}
import org.apache.flink.util.Collector

/**
 * @Classname SideOutputTest
 * @Description TODO
 *              Date ${Date} 21:29
 * @Create by childwen
 */
object SideOutputTest {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env.addSource(new SensorSource)
    val processStream = stream
      .process(new MySideProcess)
    //小于32F输出
    val sideOutput1 = processStream.getSideOutput(new OutputTag[String]("freezing-1")).print
    //大于32F输出
    val sideOutput2 = processStream.getSideOutput(new OutputTag[String]("freezing-2")).print
    env.execute()
  }

  class MySideProcess extends ProcessFunction[SensorReading, String] {
    //定义一个侧输出标签
    lazy val freezingAlarmOutput = new OutputTag[String]("freezing-1")
    lazy val freezingAlarmOutput1 = new OutputTag[String]("freezing-2")

    override def processElement(value: SensorReading,
                                ctx: ProcessFunction[SensorReading, String]#Context,
                                out: Collector[String]): Unit = {
      //对每个元素进行判断，如果小于32F就输出到侧输出标签
      if (value.temperature < 60) {
        ctx.output(freezingAlarmOutput, s"${value.id}温度小于60华摄氏度")
      } else {
        ctx.output(freezingAlarmOutput1, s"${value.id}温度大于60华摄氏度")
      }
      out.collect(value.id)
    }
  }

}

```

# 九 状态变量

## 9.1 基本介绍

状态变量也是一个单例对象。

## 9.2 基本使用

### 在类中创建状态变量

使用getRuntimeContext

```scala
    //用来存储最近一次的温度
    //当保存检查点的时候，也可以配置保存到状态后端
    //默认状态后端是内存，也可以配置hdfs等为状态后端
    //懒加载，当运行到process方法的时候才会惰性赋值
    //状态变量只会被初始化一次
    //根据last-temp这个名字到状态后端去查找，如果状态后端中没有就会初始化
    //如果在状态后端中存在last-temp的状态变量直接懒加载
    //默认值是0.0
    lazy val lastTemp = getRuntimeContext.getState(
      new ValueStateDescriptor[Double](
        "last-temp",
        Types.of[Double]
      )
    )
```

### 在函数内部创建状态变量

使用getPartitionedState

```scala
      //定义状态变量
      val firstSeen = ctx.getPartitionedState(
        new ValueStateDescriptor[Boolean](
          "first-seen", Types.of[Boolean]
        )
      )
```

# 十 基于时间的双流Join

## 10.1 基于时间间隔的Join

```scala
package com.atguigu.day05.doublejoin

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

/**
 * @Classname IntervalJoinExample
 * @Description TODO
 *              Date ${Date} 13:46
 * @Create by childwen
 */
object IntervalJoinExample {

  // 用户点击日志
  case class UserClickLog(userID: String,
                          eventTime: String,
                          eventType: String,
                          pageID: String)

  // 用户浏览日志
  case class UserBrowseLog(userID: String,
                           eventTime: String,
                           eventType: String,
                           productID: String,
                           productPrice: String)

  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    //需求对两条流进行时间的间隔join
    val clickStream = env.fromElements(
      UserClickLog("user_2", "1300", "click", "page_1"), // 相对于right的上下界(900, 1300)
      UserClickLog("user_2", "2000", "click", "page_1")
    )
      .assignAscendingTimestamps(_.eventTime.toLong)
      .keyBy(_.userID)

    val browseStream = env.fromElements(
      UserBrowseLog("user_2", "1000", "browse", "product_1", "10"), // 相对于left的上下界(1000, 1600)
      UserBrowseLog("user_2", "1300", "browse", "product_1", "10"), // (1300, 2100)
      UserBrowseLog("user_2", "1301", "browse", "product_1", "10"), // (1301, 2101)
      UserBrowseLog("user_2", "1302", "browse", "product_1", "10") // (1302, 2102)
    )
      .assignAscendingTimestamps(_.eventTime.toLong)
      .keyBy(_.userID)

    //相对于clickStream的上下界
    clickStream.intervalJoin(browseStream)
      .between(Time.milliseconds(-600), Time.milliseconds(602))
      .process(new MyIntervalJoin)
      .print()
    env.execute()
  }

  class MyIntervalJoin extends ProcessJoinFunction[UserClickLog, UserBrowseLog, String] {
    override def processElement(left: UserClickLog, right: UserBrowseLog, ctx: ProcessJoinFunction[UserClickLog, UserBrowseLog, String]#Context, out: Collector[String]): Unit = {
      out.collect(left + "===>" + right)
    }
  }

}

```

## 10.2 基于窗口的Join

```scala
input1.join(input2)
  .where(...)       // 为input1指定键值属性
  .equalTo(...)     // 为input2指定键值属性
  .window(...)      // 指定WindowAssigner
  [.trigger(...)]   // 选择性的指定Trigger
  [.evictor(...)]   // 选择性的指定Evictor
  .apply(...)       // 指定JoinFunction
```
```scala
package com.atguigu.day05.doublejoin

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time

/**
 * @Classname WindowJoin
 * @Description TODO
 *              Date ${Date} 18:14
 * @Create by childwen
 */
object WindowJoin {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val stream1 = env.fromElements(
      (1, 999L), (1, 1000L), (2, 999L), (2, 1000L)
    )
      .assignAscendingTimestamps(_._2)
//      .keyBy(_._1)

    val stream2 = env.fromElements(
      (1, 1001L), (1, 1005L), (2, 2001L), (2, 2002L)
    )
      .assignAscendingTimestamps(_._2)
//      .keyBy(_._1)

    //仅在同一个窗口范围内，key相同的进行join
    //如果offset未设置，从0开始
    stream1.join(stream2)
      .where(_._1)
      .equalTo(_._1)
      //2秒滚动窗口
      .window(TumblingEventTimeWindows.of(Time.seconds(2)))
      .apply((r1, r2) => r1 + "----->" + r2)
      .print()

    env.execute()
  }

}

```

> 注意，对划分窗口后的数据流进行Join可能会产生意想不到的语义。例如，假设你为执行Join操作的算子配置了1小时的滚动窗口，那么一旦来自两个输入的元素没有被划分到同一窗口，它们就无法Join在一起，即使二者彼此仅相差1秒钟。

# 十一 处理迟到元素

## 11.1 什么是迟到元素？

迟到的元素是指当这个元素来到时，这个元素所对应的窗口已经计算完毕了(也就是说水位线已经没过窗口结束时间了)。这说明迟到这个特性只针对事件时间。

- 直接抛弃迟到的元素
- 将迟到的元素发送到另一条流中去
- 可以更新窗口已经计算完的结果，并发出计算结果。

## 11.2 抛弃迟到元素

抛弃迟到的元素是event time window operator的默认行为。也就是说一个迟到的元素不会创建一个新的窗口。

process function可以通过比较迟到元素的时间戳和当前水位线的大小来很轻易的过滤掉迟到元素。

## 11.3 将迟到的元素发送到另一条流中去

windowProcessFunction

> 如果是ProcessFunction使用水位线来判断是否迟到，然后将迟到元素发送到侧输出标签即可。

```scala
package com.atguigu.day06

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

/**
 * @Classname SideOutput
 * @Description TODO
 *              Date ${Date} 18:28
 * @Create by childwen
 */
object SideOutput {
  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    val stream = env.socketTextStream("hadoop102", 9999, '\n')
    val readings = stream
      .map(line => {
        val arr = line.split("\\s")
        (arr(0), arr(1).toLong * 1000L)
      })
      .assignAscendingTimestamps(_._2)
      .keyBy(_._1)
      .timeWindow(Time.seconds(5))
      //迟到元素输出到侧输出标签
      .sideOutputLateData(new OutputTag[(String, Long)]("late"))
      .process(new SideProcess)


    readings.print()
    //从流中获取侧输出流
    readings.getSideOutput(new OutputTag[(String, Long)]("late")).print

    env.execute()
  }

  class SideProcess extends ProcessWindowFunction[(String, Long), String, String, TimeWindow] {
    override def process(key: String, context: Context, elements: Iterable[(String, Long)], out: Collector[String]): Unit = {
      out.collect(s"${context.window.getStart}到${context.window.getEnd}关闭！！！当前窗口元素个数${elements.size}")
    }
  }

}

```

> 元素的迟到与否，和窗口的关闭相关。窗口关闭了，只代表元素无法进入到对应窗口，但实际上元素本身是进入流中的不受水位线的限制。

## 11.4 等待迟到元素并更新窗口计算结果

由于存在迟到的元素，所以已经计算出的窗口结果是不准确和不完全的。我们可以使用迟到元素更新已经计算完的窗口结果。

如果我们要求一个operator支持重新计算和更新已经发出的结果，就需要在第一次发出结果以后也要保存之前所有的状态。但显然我们不能一直保存所有的状态，肯定会在某一个时间点将状态清空，而一旦状态被清空，结果就再也不能重新计算或者更新了。而迟到的元素只能被抛弃或者发送到侧输出流。

window operator API提供了方法来明确声明我们要等待迟到元素。当使用event-time window，我们可以指定一个时间段叫做allowed lateness。window operator如果设置了allowed lateness，这个window operator在水位线没过窗口结束时间时也将不会删除窗口和窗口中的状态。窗口会在一段时间内(allowed lateness设置的)保留所有的元素。

**当迟到元素在allowed lateness时间内到达时，这个迟到元素会被实时处理并发送到触发器(trigger)。当水位线没过了窗口结束时间+allowed lateness时间时，窗口会被删除**，并且所有后来的迟到的元素都会被丢弃。

```scala
package com.atguigu.day6

import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

//a 1
//a 2
//a 1
//a 2
//a 4
//a 10
//a 1
//a 1
//a 13
//a 1
object UpdateWindowResultWithLateElement {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.setParallelism(1)

    val stream = env
      .socketTextStream("localhost", 9999, '\n')
      .map(line => {
        val arr = line.split(" ")
        (arr(0), arr(1).toLong * 1000L)
      })
      .assignTimestampsAndWatermarks(
        new BoundedOutOfOrdernessTimestampExtractor[(String, Long)](Time.seconds(5)) {
          override def extractTimestamp(element: (String, Long)): Long = element._2
        }
      )
      .keyBy(_._1)
      .timeWindow(Time.seconds(5))
      .allowedLateness(Time.seconds(5))
      .process(new UpdatingWindowCountFunction)

    stream.print()
    env.execute()
  }

  class UpdatingWindowCountFunction extends ProcessWindowFunction[(String, Long), String, String, TimeWindow] {
    // process和processElement的区别？
    // processElement用于KeyedProcessFunction中，也就是没有开窗口的流，来一条元素调用一次
    // process函数用于ProcessWindowFunction中，水位线超过窗口结束时间时调用一次
    override def process(key: String, context: Context, elements: Iterable[(String, Long)], out: Collector[String]): Unit = {
      val count = elements.size

      // 基于窗口的状态变量，仅当前窗口可见
      // 默认值是false
      val isUpdate = context.windowState.getState(
        new ValueStateDescriptor[Boolean]("is-update", Types.of[Boolean])
      )

      if (!isUpdate.value()) {
        out.collect("当水位线超过窗口结束时间的时候，窗口第一次触发计算！元素数量是 " + count + " 个！")
        isUpdate.update(true)
      } else {
        // 迟到元素到来以后，更新窗口的计算结果
        out.collect("迟到元素来了！元素数量是 " + count + " 个！")
      }
    }
  }
}
```

## 11.5 注意事项

1）Flink中当开窗时，迟到是否与水位线是否没过窗口相关。不开窗时，和水位线相关。

2）窗口关闭等待看水位线

3）窗口真关闭看水位线 > 窗口结束时间 + 最大允许迟到时间

4）当数据来了以后，落在窗口的范围只和事件时间有关，即在开窗时，事件时间决定归属在哪个窗口范围内。

5）**在开窗时，水位线决定窗口关闭时间。事件时间决定数据所在的窗口范围。**

6）sideOutputLateData收集的是未能进入窗口的元素。

> ```scala
> // process和processElement的区别？
> // processElement用于KeyedProcessFunction中，也就是没有开窗口的流，来一条元素调用一次
> // process函数用于ProcessWindowFunction中，水位线超过窗口结束时间时调用一次
> ```

# 十二 Flink状态管理

## 12.1 Flink中状态分类

Flink中状态分为rowState和ManagerState两种状态类型。

rowState可以让用户自己实现定制各种接口来完成对状态的保存和本地化，ManagerState是由Flink帮忙管理状态。

ManagerState又分为：

- 算子状态（Operatior State）
  键控状态（Keyed State）
  状态后端（State Backends）

## 12.2 状态基本介绍

![image-20200616214040292](D:\ProgramFiles\Typora\图片备份\image-20200616214040292.png)

1）由一个任务维护，并且用来计算某个结果的所有数据，都属于这个任务的状态。
2）可以认为状态就是一个本地变量，可以被任务的业务逻辑访问。
3）Flink 会进行状态管理，包括状态一致性、故障处理以及高效存储和访问，以便开发人员可以专注于应用程序的逻辑。

> 在 Flink 中，状态始终与特定算子相关联
> 为了使运行时的 Flink 了解算子的状态，算子需要预先注册其状态

只针对当前算子的叫无状态算子。

4）**个人理解**

> ![image-20200618091333183](D:\ProgramFiles\Typora\图片备份\image-20200618091333183.png)

比如说上述代码，这个变量count也可以实现共享变量读取的问题，但是它不是一个状态。状态不仅仅是一个本地变量，还涉及到故障恢复（比如出现故障时还能从运行时上下文读取上一次状态继续运行），一致性，以及共享变量高效的读取和存储。



**5）算子状态和键控状态的区别**：作用范围不同

> 算子状态的作用范围限定在当前算子任务，也就是当前分区slot，局限在当前任务。  
>
> 键控状态是按照keyBy之后的key划分的流来访问当前状态，只有key相同的流才能访问和修改当前状态（定时器也一样），即不同的key会访问不同的状态。



## 12.3 算子状态

### 12.3.1 算子状态基本介绍

![image-20200616214356132](D:\ProgramFiles\Typora\图片备份\image-20200616214356132.png)



1）算子状态的作用范围限定为算子任务，由**同一并行任务所输入的所有数据都可以访问到相同的状态**
2）状态对于同一任务而言是共享的
3）算子状态不能由相同或不同算子的另一个任务访问

4）在代码中使用算子状态需要实现ListCheckPoint实现快照保存和故障恢复。

### 12.3.2 算子状态分类

1）列表状态（List state）
将状态表示为一组数据的列表

2）联合列表状态（Union list state）
也将状态表示为数据的列表。它与常规列表状态的区别在于，在发生故障时，或者从保存点（savepoint）启动应用程序时如何恢复

3）广播状态（Broadcast state）
如果一个算子有多项任务，而它的每项任务状态又都相同，那么这种特殊情况最适合应用广播状态。

### 12.3.3 基本使用

实现算子状态的保存需要继承ListCheckpointed，实现快照保存和故障恢复方法，泛型传入状态的类型。

> 算子状态的状态保存为什么是返回一个List？
>
> 因为我们算子状态的作用范围是一个状态被一个算子任务共享，在实际生产中有时会出现需要增加并行度的情况，如果说返回的不是一个List状态列表会导致任务间的状态分配不均匀。

```scala
package com.atguigu.test

import java.util

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.streaming.api.checkpoint.ListCheckpointed

/**
 * @Classname OperatorState
 * @Description TODO
 *              Date ${Date} 9:32
 * @Create by childwen
 */
object OperatorState {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(4)

    val stream = env.fromElements(0, 0, 0, 0, 0, 0,0,0)
    stream
      .map(new MyOperatorState)
      .print.setParallelism(1)
    env.execute()
  }

  class MyOperatorState extends RichMapFunction[Int, Int] with ListCheckpointed[A] {
    var count: Int = 0

    override def map(value: Int): Int = {
      count += 1
      count
    }

    //快照保存
    override def snapshotState(checkpointId: Long, timestamp: Long): util.List[A] = {
      val list = new util.ArrayList[A]()
      list.add(A(count))
      list
    }

    //故障恢复
    override def restoreState(state: util.List[A]): Unit = {
      import scala.collection.JavaConversions._
      for(ele <- state){
        count = count + ele.a
      }
    }
  }

  case class A(a:Int)

}

```





## 12.4 键控状态

![image-20200618082311845](D:\ProgramFiles\Typora\图片备份\image-20200618082311845.png)

### 12.4.1 键控状态基本介绍



1）键控状态是根据输入数据中定义的键key来维护和访问的。

2）Flink 为每个 key 维护一个状态实例，并将具有相同键的所有数据，都分配到同一个算子任务中，这个任务会维护和处理这个 key 对应的状态。

3）**当任务处理一条数据时，它会自动将状态的访问范围限定为当前数据的 key**。



### 12.4.2 键控状态的分类

1）值状态（Value state）
将状态表示为单个的值

2）列表状态（List state）
将状态表示为一组数据的列表

3）映射状态（Map state）
将状态表示为一组 Key-Value 对

4）聚合状态（Reducing state & Aggregating State）
将状态表示为一个用于聚合操作的列表

### 12.4.3 定义键控状态的两种方式

##### 第一种定义方式

使用getRuntimeContext声明一个键控状态

```scala
    //定义一个状态变量
    lazy val state = getRuntimeContext.getState(
      new ValueStateDescriptor[Boolean]("state",classOf[Boolean])
    )
```

**为什么要懒加载？**

因为在类加载时，getRuntimeContext运行时上下文还没有加载完成。所以需要加lazy！！！

##### 第二种定义方式

使用在open中使用getRuntimeContext声明，就可以避免第一种加载时的问题。

````scala
    //定义一个全局变量，所有函数都可访问
private var lastTemp: ValueState[Double] = _

    override def open(parameters: Configuration): Unit = {
      lastTemp = getRuntimeContext.getState(
        new ValueStateDescriptor[Double]("last-temp", classOf[Double])
      )
````



### 12.4.5 基于窗口的状态变量

**基于窗口的状态变量，仅当前窗口可见**

```scala
  class UpdatingWindowCountFunction extends ProcessWindowFunction[(String, Long), String, String, TimeWindow] {
    // process和processElement的区别？
    // processElement用于KeyedProcessFunction中，也就是没有开窗口的流，来一条元素调用一次
    // process函数用于ProcessWindowFunction中，水位线超过窗口结束时间时调用一次
    override def process(key: String, context: Context, elements: Iterable[(String, Long)], out: Collector[String]): Unit = {
      val count = elements.size

      // 基于窗口的状态变量，深入理解计算机系统 算法导论 计算机程序的构造与解释
      // 默认值是false
      val isUpdate = context.windowState.getState(
        new ValueStateDescriptor[Boolean]("is-update", Types.of[Boolean])
      )

      if (!isUpdate.value()) {
        out.collect("当水位线超过窗口结束时间的时候，窗口第一次触发计算！元素数量是 " + count + " 个！")
        isUpdate.update(true)
      } else {
        // 迟到元素到来以后，更新窗口的计算结果
        out.collect("迟到元素来了！元素数量是 " + count + " 个！")
      }
    }
  }
```



### 12.4.6 键控状态的基本API

keyed state仅可用于KeyedStream。Flink支持以下数据类型的状态变量：

- ValueState[T]保存单个的值，值的类型为T。

  ```scala
  lazy val myValueState: ValueState[Double] = getRuntimeContext.getState( new ValueStateDescriptor[Double]("myValue", classOf[Double]) )
  ```

  

  - get操作: ValueState.value()
  - set操作: ValueState.update(value: T)

  

- ListState[T]保存一个列表，列表里的元素的数据类型为T。基本操作如下：

  ```scala
  lazy val myListState: ListState[String] = getRuntimeContext.getListState( new ListStateDescriptor[String]("myList", classOf[String]) )
  ```

  

  - ListState.add(value: T)
  - ListState.addAll(values: java.util.List[T])
  - ListState.get()返回Iterable[T]
  - ListState.update(values: java.util.List[T])

- MapState[K, V]保存Key-Value对。

  ```scala
  lazy val myMapState: MapState[String, Int] = getRuntimeContext.getMapState( new MapStateDescriptor[String, Int]("myMap", classOf[String], classOf[Int]) )
  ```

  

  - MapState.get(key: K)
  - MapState.put(key: K, value: V)
  - MapState.contains(key: K)
  - MapState.remove(key: K)

- ReducingState[T]：类型不变同reduce

    ```scala
      lazy val myReducingState: ReducingState[SensorReading] = getRuntimeContext.getReducingState( new ReducingStateDescriptor[SensorReading]("myReduce", new MyReduceFunc, classOf[SensorReading]) )
    ```

    

    **在使用add需要定义一个reduce函数。本质就是累加**

- AggregatingState[I, O]：输入输出可以是不同类型。

State.clear()是清空操作。

### 12.4.7 注意事项

1）在使用键控状态时使用get获取状态的方式并不是值拷贝，而是获取到一个状态句柄。通过状态句柄可以修改基于相同键中的同一个状态。

# 十三 状态后端

## 13.1 什么是状态后端？

 1）数据流每传入一条数据，有状态的算子任务都会读取和更新状态。

2）由于有效的状态访问对于处理数据的低延迟至关重要（对比本地和内存的区别），因此**每个并行任务都会在本地维护其状态，以确保快速的访问状态。**

3）状态的存储、访问以及维护，由一个**可插入**的组件决定，这个组件就叫做状态后端。

4）状态后端主要负责两件事。

> ① 本地的状态管理
>
> ② 将检查点状态写入远程存储

## 13.2 三类状态后端基本介绍

### 13.2.1 MemoryStateBackend

**内存级的状态后端**

- 保存方式：会将键控状态作为内存中的对象进行管理，将它们存储在 TaskManager 的 JVM 堆上，而将 checkpoint 存储在 JobManager 的内存中
- 特点：快速、低延迟，但不稳定

### 13.2.2 FsStateBackend

- 保存方式：将 checkpoint 存到远程的持久化文件系统（FileSystem）上，而对于本地状态，跟 MemoryStateBackend 一样，也会存在 TaskManager 的 JVM 堆上。
- 特点：同时拥有内存级的本地访问速度，和更好的容错保证。

### 13.2.3 RocksDBStateBackend

- 保存方式：将所有状态序列化后，存入本地的 RocksDB 中存储。
- 在做checkPoint时还可以提供增量同步的策略，相比文件系统的后端管理效率较高。

## 13.3 三类状态后端的配置

### 1）在代码中配置

Flink在内部给我们提供的backend有内存级别和FS级别和RocksDB。

setStateBackend目前是被弃用状态，但是新的API目前还用不了。

**MemoryStateBackend内存级别** 

```scala
env.setStateBackend(new MemoryStateBackend())
```

**FsStateBacked级别** （建议配置）

```scala
env.setStateBackend(new FsStateBackend("path路径"))
```

**RocksDBStateBackend**

需要导入依赖	

```scala
    //true为开启增量保存状态,默认是false。每次磁盘到本地是开启的是全量同步策略，在数据量很大时建议设置为true
    env.setStateBackend(new RocksDBStateBackend("path",true))
```

> ```xml
>         <dependency>
>             <groupId>org.apache.flink</groupId>
>             <artifactId>flink-statebackend-rocksdb_2.11</artifactId>
>             <version>1.10.0</version>
>         </dependency>
> ```

> 特点：可以配置增量同步

### 2）在Flink配置文件中配置

## 13.4 开启检查点保存状态

```scala
    // 每个10s钟做一次保存检查点的操作
    env.enableCheckpointing(10000L)
    env.setStateBackend(new FsStateBackend("file:///Users/yuanzuo/Desktop/flink-tutorial/FlinkSZ1128/checkpoint"))
```

> 指定触发检查点的时间间隔指的是插入barrier的时间间隔。

## 13.5 案例

将ListState保存到到FsSystem文件系统中

```scala
package com.atguigu.day6

import com.atguigu.day2.{SensorReading, SensorSource}
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.configuration.Configuration
import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector

import scala.collection.mutable.ListBuffer

object ListStateExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    // 每个10s钟做一次保存检查点的操作
    env.enableCheckpointing(10000L)
    env.setStateBackend(new FsStateBackend("file:///Users/yuanzuo/Desktop/flink-tutorial/FlinkSZ1128/checkpoint"))

    val stream = env
      .addSource(new SensorSource)
      .filter(_.id.equals("sensor_1"))
      .keyBy(_.id)
      .process(new MyKeyedProcess)

    stream.print()
    env.execute()
  }

  class MyKeyedProcess extends KeyedProcessFunction[String, SensorReading, String] {

    var listState: ListState[SensorReading] = _

    var timerTs: ValueState[Long] = _

    override def open(parameters: Configuration): Unit = {
      // 初始化一个列表状态变量
      listState = getRuntimeContext.getListState(
        new ListStateDescriptor[SensorReading]("list-state", Types.of[SensorReading])
      )

      timerTs = getRuntimeContext.getState(
        new ValueStateDescriptor[Long]("timer", Types.of[Long])
      )
    }

    override def processElement(value: SensorReading, ctx: KeyedProcessFunction[String, SensorReading, String]#Context, out: Collector[String]): Unit = {
      listState.add(value) // 将到来的传感器数据添加到列表状态变量
      if (timerTs.value() == 0L) {
        val ts = ctx.timerService().currentProcessingTime() + 10 * 1000L
        ctx.timerService().registerProcessingTimeTimer(ts)
        timerTs.update(ts)
      }
    }

    override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, SensorReading, String]#OnTimerContext, out: Collector[String]): Unit = {
      val list: ListBuffer[SensorReading] = ListBuffer() // 初始化一个空列表
      import scala.collection.JavaConversions._ // 必须导入
      // 将列表状态变量的数据都添加到列表中
      for (r <- listState.get()) {
        list += r
      }
      listState.clear() // gc列表状态变量

      out.collect("列表状态变量中的元素数量有 " + list.size + " 个")
      timerTs.clear()
    }
  }
}
```

## 13.6 将HDFS配置为状态后端

### 在IDEA中配置

#### 13.6.1 添加依赖

```xml
            <dependency>
                <groupId>org.apache.hadoop</groupId>
                <artifactId>hadoop-client</artifactId>
                <version>2.8.3</version>
    <!--            <scope>provided</scope>-->
            </dependency>
```

#### 13.6.2 配置hdfs-site.xml文件

添加

```properties
        <property>
            <name>dfs.permissions</name>
            <value>false</value>
        </property>
```

> 添加以后需要重启hdfs文件系统

#### 13.6.3 添加本地文件夹和hdfs文件的映射



### 全局配置

flink可以通过flink-conf.yaml 配置原因全局配置state backend。

```xml
# The backend that will be used to store operator state checkpoints
state.backend: filesystem
# Directory for storing checkpoints
state.checkpoints.dir: hdfs://namenode:40010/flink/checkpoints
```



# 十四 Keyed State状态编程

## 14.1 基本介绍

利用状态来保留数据，不等于全局变量只是保存了一个当前的状态，比如keyBy以后，保存了一个同一key上的流的共享状态。

## 14.2 基本使用

需求：前后两次温度差值大于指定值时，进行报警提示。

使用RichFlatMapFunction获取状态实现。

（1）使用 ValueState 和 RichFlatMap 函数获取状态实现。

```scala
object ValueStateInFlatMap {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env
      .addSource(new SensorSource)
      .keyBy(_.id)
      .flatMap(new TemperatureAlert(1.7))

    stream.print()
    env.execute()
  }

  class TemperatureAlert(val diff: Double) extends RichFlatMapFunction[SensorReading, (String, Double, Double)] {
    private var lastTemp: ValueState[Double] = _
    // 尽管不是 keyedProcessFunction，但状态是 keyedState，也必须应用在 keyBy 之后
    override def open(parameters: Configuration): Unit = {
      lastTemp = getRuntimeContext.getState(
        new ValueStateDescriptor[Double]("last-temp", classOf[Double])
      )
    }

    override def flatMap(value: SensorReading, out: Collector[(String, Double, Double)]): Unit = {
      val last = lastTemp.value()
      val tempDiff = (value.temperature - last).abs // 差值的绝对值
      if (tempDiff > diff) {
        out.collect((value.id, value.temperature,tempDiff))
      }
      lastTemp.update(value.temperature)
    }
  }
}
```

（2）使用 flatMapWithState 函数实现。

```scala
object FlatMapWithStateExample {
  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val stream = env
      .addSource(new SensorSource)
      .keyBy(_.id)
      // 泛型的第一个参数是输出的元素的类型，第二个参数是状态变量的类型
      .flatMapWithState[(String, Double, Double), Double] {
            // 传入一个偏函数，第一个变量时输入元素，第二个变量是经过 Option 封装的状态
            // 偏函数的返回值是输出与经过 Option 封装的状态构成的元组
        case (in: SensorReading, None) => {
          (List.empty, Some(in.temperature))
        }
        case (r: SensorReading, lastTemp: Some[Double]) => {
          val tempDiff = (r.temperature - lastTemp.get).abs
          if (tempDiff > 1.5) {
            (List((r.id, r.temperature, tempDiff)), Some(r.temperature))
          } else {
            (List.empty, Some(r.temperature))
          }
        }
      }
    stream.print()
    env.execute()
  }
}
// mapWithState、filterWithState 同样适用。
// flatMapWithState、mapWithState、filterWithState 为 keyedStream 独有方法
// （较其父类 DataStream）。
```



## 14.3 注意事项

1）什么函数可以使用getRuntimeContext来定义状态？只有ProcessFunction吗？

> 只要是RichFunction都可以使用getRuntimeContext来定义状态。
>
> ProcessFunction的顶级父类归属于RichFunction。

2）关于无状态算子

>  比如说普通的map函数是没有状态的，无法从map函数中获取状态，比如getRuntimeContext，但是使用RichMapFunction就可以从map操作中获取状态。

3）mapWithState和flatMapWithState两个带状态的map，其本质就是RichMap和RichFlatMap的简写版。

4）flatMapWithState函数解析：

传入输入类型和状态变量，返回输出类型和状态变量。

(IN,state)=>(OUT,state)

FlatMapWithState只能定义在keyedStream之后。

**mapState、filterState、flatMapState是keyedStream独有。**

**Flink 提供了非常灵活的状态 API，可以认为所有的算子都可以有状态。**

5）map/ filter/ flatmap 本来就是无状态的，但是可以通过实现 RichFunction，获取其上下文，从而对状态进行操作。除此之外，还可以使用 flatMapWithState、mapWithState、filterWithState。

6）reduce/ aggregate/ window(window+窗口函数) 本来就是有状态的，由 flink 底层直接管理，当然也可以实现 RichFunction 自定义状态。

7）ProcessFunction 是一类特殊的函数类，是 process 方法的参数，也实现了 RichFunction 接口，是一类特殊的富函数，ProcessFunction如果不定义状态默认是无状态的。

8）DataStream/ KeyedStream/ ConnectedStream/ WindowedStream 等等都可以调用 process 方法，传入的是不同的 ProcessFunction。

9）有状态的流失计算，针对每个算子都是可以去定义的。主要使用RichFunction去实现，RichFunction除了有open和close以外还可以获取运行时上下文。可以将定义好的状态句柄取出来，将状态当做一个本地变量来存取。

10）**比如说reduceFunction聚合函数，系统内部会帮我们保存上一个元素的状态。但是如果我们的逻辑不仅想要上一次的状态还需要额外多的状态就需要我们自己实现RichFunction保存更多的状态，这就是Flink底层帮我们管理的状态。**

#  十五 Flink容错机制

## 15.1 一致性检查点

### 15.1.1 什么是一致性检查点？

1）所有任务的状态快照组合在一起的快照才叫一个checkPoint

> Flink一致性检查点存储的是一组状态，也是Flink故障恢复机制的核心，本质就是做一份快照。
>
> 注意：Fink中的一致性检查点不是保存一份数据，而是保存当前时间点的一份状态。
>
> 这里需要注意状态和数据的区别。

2）在某个时间点做一份快照

> 这个时间点是所有任务都恰好处理完同一相同输入数据的时候，并不是说随便找个时间点Flink说：唉，大家都停一下，我来拍张照。

### 15.1.2 一致性检查点基本流程

![image-20200618120212909](D:\ProgramFiles\Typora\图片备份\image-20200618120212909.png)

1）个人理解

> 比如说上图中，
>
> 由InputStream数据源输入数据，Source中我们会保存一个状态，该状态定义为偏移量。当前偏移量为5，表示已经输入到第五个数据了。假设前面的数据分别是：1、2、3、4、5
>
> 数据遇到聚合算子时，保存了两个状态一个是sum_even，另外一个是sum_odd
>
> 当前sum_even状态已经保存状态到第5个输入数据了。
>
> 当前sum_odd状态也已经保存状态到第5个输入数据了。
>
> 此时所有的算子都已经处理完成相同的输入数据，Flink的checkPoint就会做一份快照，维护一份当前时刻的状态信息，这个信息可以被写入到远端也可以写入到内存。

**2）把什么保存成状态是由我们自己决定的。**



## 15.2 从检查点恢复状态

![image-20200618150615443](D:\ProgramFiles\Typora\图片备份\image-20200618150615443.png)

### 15.2.1 如何从检查点恢复状态？

如上所述，在执行流应用程序期间，Flink会定期保存状态的一致性检查点。

如果发生故障，Flink将会使用最近的检查点来一致恢复应用程序的状态，并重新启动处理流程。

> 恢复状态对外部系统的要求：可以回滚，将数据重新定位到故障之前的状态，看起来好像从来没有发生过故障一样。

### 15.2.2 从检查点恢复状态基本流程

如上图所示

> 当offset状态保存到第7个元素。
>
> sun_evnet处理完第7个元素。
>
> sum_odd在等待处理第7个元素时，发生故障数据丢失。
>
> ![image-20200618151924527](D:\ProgramFiles\Typora\图片备份\image-20200618151924527.png)
>
> 此时Flink将会使用最近的检查点来一致恢复应用程序的状态，并重新启动处理流程。
>
> ![image-20200618151946314](D:\ProgramFiles\Typora\图片备份\image-20200618151946314.png)
>
> 从checkPoint中读取上一次的状态，将状态重置。
>
> 从检查点重新启动应用程序后，其内部状态与检查点完成时的状态完全相同。
>
> ![image-20200618152045614](D:\ProgramFiles\Typora\图片备份\image-20200618152045614.png)
>
> 当状态重置完成后开始重新消费并处理检查点到发生故障之间的所有数据。

### 15.2.3 小结

1）这种检查点的保存和恢复机制可以为应用程序状态提供“精确一次”（exactly-once）的一致性，因为所有算子都会保存检查点并恢复其所有状态，这样一来所有的输入流就都会被重置到检查点完成时的位置。

2）如果要实现精确一次，前提是输入端必须要满足能够将数据重新定位到之前的样子。

3）为什么要在所有算子同时处理完同一数据的时刻做快照？

> 为了保证数据的一致性，因为在数据的处理过程中会有很多的不确定性如果不这么做会导致数据的丢失或重复计算的问题。

## 15.3 Flink底层如何做checkPoint？

将检查点的保存和数据的处理分开而不是等到所有的检查点完成才处理后续的数据，

在保存检查点时无需暂停整个应用，这样就可以将整个保存检查点的效率提升到最高。

（可联想平时拍照）。

> 只需要收到标记以后就去保存一下状态，最后在将所有的状态合并。
>
> 类似于拍照时，谁先把手头上的事做完了谁就先去拍，最后将所有人的照片P到一起即可。

底层基于一个分布式算法Chandy-Lamport

## 15.4 检查点算法

### 15.4.1 基本介绍

基于Chandy-Lamport算法的分布式快照。

- 该算法的特点就是将检查点的保存和数据的处理分离开，在保存检查点时不暂停整个应用。

### 15.4.2 检查点算法执行流程

- 如下当前Flink初始状态

![image-20200618170614776](D:\ProgramFiles\Typora\图片备份\image-20200618170614776.png)

> barrier也叫分界线或者屏障。
>
> Source：发送数据。
>
> Sum even：偶数累加。
>
> Sum odd：奇数累加。
>
> 现在是一个有两个输入流的应用程序，用并行的两个 Source 任务来读取
>
> source确定由那个数据用来做checkPoint广播barrier，**当后面的任务接收到barrier时，就会认为前面的数据都已经处理完成了。**
>
> 现在是一个有两个输入流的应用程序，用并行的两个 Source 任务来读取

- JobManager启动checkPoint

![image-20200618170644160](D:\ProgramFiles\Typora\图片备份\image-20200618170644160.png)

> 可以看出从图一到图二，黄②和蓝②已经被写入到sink系统了，这里可以看出我们在做checkPoint时不影响当前流中应用的执行，体现了Chandy-Lamport分布式快照在做检查点时不暂停应用的特性。
>
> JobManager 会向每个 source 任务发送一条带有新检查点 ID 的消息，通过这种方式来启动检查点。
>
> 注：如果在向下游传输时，下游还没有读取到barrier时发生故障，那么这次checkPoint就算作一次失败的checkPoint，恢复会从最近一次的检查点进行恢复。只有所有的任务都接收到barrier这次的检查点操作才算成功。

- 发送barrier

![image-20200618171042997](D:\ProgramFiles\Typora\图片备份\image-20200618171042997.png)

见到一个barrier就向检查点保存一份状态，同时向JobManager发送信息，告知已经接受到barrier。

> 在source保存蓝③和黄④时默认的保存方式是采用同步策略如果是异步策略同时保存时还可以保持计算，在保存时会开启一个线程，后续的数据需要等到状态完成以后才继续执行。

在source保存了状态以后，barrier继续向下走，如果此时第一个分区发送的barrier蓝2先到了，但是第二个分区发送的黄④和barrier黄2还没到。这时能将Sum even4这个状态保存起来吗？

> 当然是不可以的，所以当蓝色的barrier到了以后，sum even4得等着。

- 分界线对齐

![image-20200618171401660](D:\ProgramFiles\Typora\图片备份\image-20200618171401660.png)

如上所述，如果此时蓝2的barrier到了Sum even4，但是黄色的barrier还没处理完。此时蓝④到了，可以进行状态累加吗？

> 当然是不可以的，同一算子的状态保存必须要等到所有barrier到齐，任务开始保存状态，才可以进行后面的数据运算。
>
> 对于barrier已经到达的分区，继续到达的数据会被缓存

检查点算法精髓就在于barrier如何正确对齐，

![image-20200618172404454](D:\ProgramFiles\Typora\图片备份\image-20200618172404454.png)

>  当所有barrier对齐以后，任务就将其状态保存到状态后端的检查点中，然后向JobManager通知接收到barrier以后将 barrier 继续向下游转发就可以继续处理数据了。

![image-20200618172559181](D:\ProgramFiles\Typora\图片备份\image-20200618172559181.png)

> 向下游转发检查点 barrier 后，任务继续正常的数据处理。
>
> 在处理后续任务时，先从之前因barrier等待被缓存的缓存区中读取数据进行计算，在计算后续来的数据。

> 当所有的任务包括Sink任务也有可能有自己的状态，都接收到barrier以后将状态保存到checkPoint以后，通知JobManager，JobManager对收到的信息进行校验，将所有的快照进行拼接。接下来发生故障，直接从保存的快照中恢复。

### 小结

1）barrier用于分隔不同的checkPoint，对于每个任务而言，收到barrier就意味着要开始做state的保存。

2）算法中需要对不同上游分区发来的barrier，**进行对齐**。

3）checkPoint存储位置由state backend决定，一般是放在持久化存储空间（fs或者rocksDB）

4）JobManager触发一个checkPoint操作，会把checkPoint中所有任务状态的拓扑结构保存下来。

5）barrier和watermark类似，都可以看作一个插入数据流中的特殊数据结构。

> checkPoint在数据处理上跟watermark是两种机制，checkPoint只负责保存状态，至于watermark是使用时间语义用来处理乱序数据。

## 15.5 保存点

1）Flink 还提供了可以自定义的镜像保存功能，就是保存点（savepoints）

2）原则上，创建保存点使用的算法与检查点完全相同，因此保存点可以认为就是具有一些额外元数据的检查点。

3）Flink不会自动创建保存点，因此用户（或者外部调度程序）必须明确地触发创建操作。

4）保存点是一个强大的功能。除了故障恢复外，保存点可以用于：有计划的手动备份，更新应用程序，版本迁移，暂停和重启应用，等等。

5）相比较之下checkPoint就是故障恢复，保证容错没有savePoints这么灵活。

## 15.6 反压机制

1）在一个Flink应用中算子和算子之间有一个网络缓冲区，Flink根据不同的并行任务中的数据进行分配credit信任度，根据百分比来分配向分区发送的数据。

2）并行度之间可以互相通信，根据数据量的大小调整缓冲区信任度大小。

3）反压机制指的就是Flink使用网络传输的方式逐级倒退，将反压的压力传递回去一直到source。

> 既然是流式的处理，下面满了上面自然满了。

## 15.6 检查点的常用配置

```scala
    //每个10S钟做一次保存检查点操作（头到头）
    env.enableCheckpointing(1000L)
    //设置检查点模式默认是精准一次
   env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    //检查点超时时间，超过该时间则认为检查点失败，丢弃当前checkPoint
    env.getCheckpointConfig.setCheckpointTimeout(5000L)
    //两次barrier之间的最小触发间隔（尾到头）
    env.getCheckpointConfig.setMinPauseBetweenCheckpoints(500L)
    //true代表故障以后从checkPoint恢复，false代表故障以后从savePoint中恢复，默认为true
    env.getCheckpointConfig.setPreferCheckpointForRecovery(true)
    //允许当前checkPoint允许失败次数,0代表不允许失败
    env.getCheckpointConfig.setTolerableCheckpointFailureNumber(0)
    //设置状态后端存储类型
    env.setStateBackend(new FsStateBackend("file:\\E:\\IdeaPro\\Flink1128\\check"))
```

## 15.7 保存点基本使用

### 15.7.1 依赖

```xml
<!--SavePoint依赖-->
		<dependency>
			<groupId>org.apache.flink</groupId>
			<artifactId>flink-state-processor-api_2.11</artifactId>
			<version>1.10.0</version>
			<scope>provided</scope>
		</dependency>
```

### 15.7.1 代码

```scala
    import org.apache.flink.streaming.api.scala._
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val bEnv      = ExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)

    val savepoint = Savepoint.load(bEnv, "", new MemoryStateBackend())
//    savepoint.readKeyedState()
    env.execute()
  }
```

## 15.8 重启策略

```scala
    //尝试重启次数，间隔重启时间
    env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3,10000L))
```

> 导包时冲突可以使用包别名解决。

## 15.9 小结

1）Flink中的checkPoint保存的是所有任务状态的快照

> 这个状态要求是所有任务都处理完同一个数据之后的状态。

2）Flink checkPoint算法

> 基于Chandy - Lamport算法的分布式快照

3）Flink checkPoint中重要的概念

> barrier用于分隔不同的checkPoint，对于每个任务而言，收到barrier就意味着要开始做state的保存，算法中需要对不同上游分区发来的barrier进行对齐。

4）checkPoint存储位置由stateBacked决定

> 一般放在远程持久化存储空间（fs或rocksDB）
>
> jobmanager触发一个checkPoint操作，会把checkPoint中所有任务的拓扑结构保存下来。

5）barrier和watermark类似，都可以看作一个插入数据流中的特殊数据结构

> barrier在数据处理上跟watermark是两套机制，完全没有关系。

# 十六 Flink状态一致性

## 16.1 什么是状态一致性？

- 有状态的流处理，内部每个算子任务都可以有自己的状态。
- 对于流处理器内部来说，所谓的状态一致性，其实就是我们所说的计算结果要保证正确。
- 一条数据都不应该丢失，也不应该重复计算。
- 在遇到故障时可以恢复状态，恢复以后的重新计算结果也应该是完全正确的。

> 简单来说就是结果要正确。
>
> 对于Flink有状态的流式计算，所谓的状态一致性就是说假如发生故障最后的结果要是正确的。
>
> 对于一致性而言，就是计算结果要正确。出现故障时，我们要保证最终的状态一致。

## 16.2 状态一致性的分类

- AT-MOST-ONCE 最多一次
- AT-LEAST-ONCE 至少一次
- EXACTLY-ONCE 精准一次

## 16.3 一致性检查点

- Flink使用了一种轻量级快照机制-**检查点（checkPoint）**来保证exactly-once语义。

- 有状态流应用的一致检查点，其实就是：所有任务的状态，在某个时间点的一份拷贝（一份快照）。而这个时间点，应该是所有任务都恰好处理完一个相同的输入数据的时候。
- 应用状态的一致检查点，是 Flink故障恢复机制的核心。

## 16.4 端到端的一致性

#### 16.4.1 基本介绍

目前我们看到的一致性保证都是由流处理器实现的，也就是说都是在 Flink 流处理器内部保证的；而在真实应用中，流处理应用除了流处理器以外还包含了数据源（例如 Kafka）和输出到持久化系统。

> 所谓端到端的一致性不仅仅要考虑Flink内部的一致性，还需要考虑数据源和中间处理过程数据的顺序不会乱，而且到输出端的数据不会丢失，只写入一次。

端到端的一致性保证，意味着结果的正确性贯穿了整个流处理应用的始终；每一个组件都保证了它自己的一致性。

整个端到端的一致性级别取决于所有组件中一致性最弱的组件。

#### 16.4.2 不同Source和Sink的一致性保证

![img](https://note.youdao.com/yws/public/resource/ee3f39dbfe2792f3e1d74b32e1ce526f/xmlnote/A51CD57D6CA240969F15228369D6DEB2/18306)

## 16.5 如何实现端到端的精确一次保证？

- Flink内部保证：checkPoint
- Source端：可重新定位数据读取的位置。
- Sink端：从故障恢复时，数据不会重复写入外部系统，即支持幂等写入或事物写入。



## 16.6 Flink幂等写入和事务写入基本介绍

### 16.6.1 幂等写入

幂等写入是指一个操作，可以重复执行很多次，但结果只会改变一次，也就是说后面的结果就不起作用了。譬如说redis，ES等KV类型数据库。

> 幂等在状态恢复写入的过程中会有短暂的数据不一致状态。
>
> 可以保证最终的状态一致性。

### 16.6.2 事务写入

#### 基本介绍

Flink构建的事务对应着checkPoint，等到checkPoint真正完成的时候，才把所有对应的结果写入到Sink系统中，可以通过预写日志或两阶段提交来实现。

> 将两个barrier之间的所有数据写入缓存，如果成功一次性提交，失败撤回事务。

#### 预写日志方式（Write-Ahead-Log，WAL）

把结果数据当成状态保存+批量缓存，然后在收到checkPoint完成的通知时一次性写入Sink系统。

每次都需要等到checkPoint完成再去拉取内容，类似批处理按批写入的方式。相当于在Flink和Sink端实现了一个事务，只有FlinkcheckPoint完成Sink端才会去拉取数据。

DataStream API提供了一个模板类：GenericWriteAheadSink，来实现这种事务型Sink。

> 优点：简单易于实现，由于数据在状态后端已经做好了缓存所以无论什么Sink系统，都可以用这种方式一批搞定。
>
> 缺点：对Sink端的实时性有影响，如果在预写日志写入到外部系统时如果发生故障二次写入会导致数据重复。

#### 两阶段提交（Two-Phase-Commit，2PC）

对于每个checkPoint，Sink任务（外部系统）会启动一个事务，并将接下来所有接收到的数据添加到事务里。

- 注：每来一条就写入一次（预提交），利用的是下游系统的事务，需要下游系统支持事务。

将数据写入到外部Sink系统，但不提交它们，这时只是预提交。

当Sink收到checkPoint完成的通知时，它才正式提交事务，将结果真正的写入。

两阶段提交这种方式真正实现了精准一次exactly-once，它**需要一个提供事务支持的外部sink系统。Flink提供了TwoPhaseCommitSinkFunction 接口。**

> **2PC对外部sink系统的要求**
>
> 1）外部Sink系统必须提供事务支持，或者Sink任务必须能够模拟外部系统上的事务。
>
> 2）在checkPoint的间隔期间里，必须能够开启一个事务并接受数据写入。
>
> 3）在没有收到JobManager发送的checkPoint完成通知之前，事务必须是“等待提交”的状态。
>
> 4）Sink任务必须能够在进程失败后恢复事务。
>
> 5）提交事务必须是幂等操作。
>
> 6）在故障恢复的情况下，这可能需要一些时间。如果这个时候 sink 系统关闭事务（例如超时），那么未提交的数据就会丢失。

> **预写日志和事务写入的区别**
>
> 预写日志写入是将两次barrier的数据全部缓存起来，等到检查点操作完成在一批写入。
>
> 两阶段提交是在写入时加入事务，Sink继续写入，如果故障全部撤销。
>
> 一个checkPoint对应一个事务。 



## 16.7 Flink + Kafka 端到端的状态一致性保证

### 16.7.1 基本介绍

Flink内部：利用checkPoint机制，将状态存盘。发生故障的时候可以恢复，保证内部状态的一致性。

Source：使用kafkaConsumer作为Source可以将偏移量保存下来，如果后续任务出现了任务故障，恢复的时候可以由连接器重置偏移量，重新消费 数据，保证一致性。

Sink：Kafka Producer作为sink，采用两阶段提交，需要实现TwoPhaseCommitSinkFunction 接口。

> flink的kafkaProceder011（sink端）实现了两阶段提交，提交和保存快照的一系列方法。
>
> 从该类的源码中可以看到具体实现。

### 16.7.2 具体流程

![image-20200619092634264](D:\ProgramFiles\Typora\图片备份\image-20200619092634264.png)

- JobManager协调各个TaskManager进行checkPoint存储。
- checkPoint保存在StateBackend中，默认StateBackend是内存级别的，也可以更改状态后端级别。

![image-20200620205542222](D:\ProgramFiles\Typora\图片备份\image-20200620205542222.png)

- 当checkPoint启动时，JobManager会将检查点分界线（barrier）注入数据流。
- barrier会在算子间传递下去。

![image-20200620205550947](D:\ProgramFiles\Typora\图片备份\image-20200620205550947.png)

- 每个算子会对当前的状态做个快照，保存到状态后端。
- checkPoint机制可以保证内部的一致性。

![image-20200620205652764](D:\ProgramFiles\Typora\图片备份\image-20200620205652764.png)



- 每个内部的transfrom任务遇到barrier时，都会把状态存到checkPoint里，并通知JobManager。
- Sink任务首先把数据写入外部Kafka，这些数据都属于预提交事务，遇到barrier时，把状态保存到状态后端，并开启新的预提交事务。

> 当Sink遇到一个barrier时，会把当前的状态快照信息保存到状态后端，不影响后续的数据处理继续预提交。

![image-20200620205918988](D:\ProgramFiles\Typora\图片备份\image-20200620205918988.png)



- 当所有算子任务的快照完成，也就是这次的checkPoint完成时，JobManager会向所有任务发通知，确认这次checkPoint完成。
- Sink任务收到确认通知，正式提交之前的事务，Kafka中未确认数据改为已确认。



### 16.7.3 Exactly-once 两阶段提交步骤

- 第一条数据来了之后，开启一个 kafka 的事务（transaction），正常写入 kafka 分区日志但标记为未提交，这就是“预提交”
- jobmanager 触发 checkpoint 操作，barrier 从 source 开始向下传递，遇到 barrier 的算子将状态存入状态后端，并通知 jobmanager
- sink 连接器收到 barrier，保存当前状态，存入 checkpoint，通知 jobmanager，并开启下一阶段的事务，用于提交下个检查点的数据
- jobmanager 收到所有任务的通知，发出确认信息，表示 checkpoint 完成
- sink 任务收到 jobmanager 的确认信息，正式提交这段时间的数据
- 外部kafka关闭事务，提交的数据可以正常消费了。

### 16.7.4 kafkaSink连接Kafka系统的注意事项

如果checkPoint做的时间太长，而外部提交事务的超时时间已经关了。就会造成数据丢失，

kafkaSink超时时间是1小时。

外部事务超时时间是15分钟，建议将外部超时时间设置成和checkPoint超时时间一致。

在FlinkKafka中有不同的时间语义可以通过semantic设置。

如果需要开启两阶段提交，checkPoint的触发时间间隔和实时性的要求相关。

> 可以设置到微秒级别。

semantic 默认是 AT_LEAST_ONCE，需更改为 EXACTLY_ONCE。

uncommitted 状态数据不能被消费。因此 consumer 的消费级别必须是 READ_COMMITTED。

事务超时时间要配置合适。（连接超时时间默认 1 小时，事务超时时间默认 15min，超时后会关闭事务）

# 十七 FlinkTable和SQL

## 17.1 Table API 和 Flink SQL 是什么？

- Flink本身是批流统一的处理框架，所以Table API和SQL，就是批流统一的上层处理API。目前功能尚未完善，处于活跃的开发阶段。
- Table API是一套内嵌在Java和Scala语言中的查询API，它允许我们以非常直观的方式，组合来自一些关系运算符的查询（比如select、filter和join）。而对于Flink SQL，就是直接可以在代码中写SQL，来实现一些查询（Query）操作。Flink的SQL支持，基于实现了SQL标准的Apache Calcite（Apache开源SQL解析工具）
- 无论输入是批输入还是流式输入，在这两套API中，指定的查询都具有相同的语义，得到相同的结果。

![image-20200621185247524](D:\ProgramFiles\Typora\图片备份\image-20200621185247524.png)

## 17.2 基本程序结构

### 17.2.1 代码流程

```scala
val tableEnv = ...     // 创建表的执行环境

// 创建一张表，用于读取数据
tableEnv.connect(...).createTemporaryTable("inputTable")

// 注册一张表，用于把计算结果输出
tableEnv.connect(...).createTemporaryTable("outputTable")

// 通过 Table API 查询算子，得到一张结果表
val result = tableEnv.from("inputTable").select(...)

// 通过 SQL查询语句，得到一张结果表
val sqlResult  = tableEnv.sqlQuery("SELECT ... FROM inputTable ...")

// 将结果表写入输出表中
result.insertInto("outputTable")

```

### 17.2.2 依赖

```xml
		<dependency>
			<groupId>org.apache.flink</groupId>
			<artifactId>flink-table-api-scala-bridge_2.11</artifactId>
			<version>1.10.0</version>
			<!--   <scope>provided</scope>-->
		</dependency>

		<dependency>
			<groupId>org.apache.flink</groupId>
			<artifactId>flink-table-planner_2.11</artifactId>
			<version>1.10.0</version>
			<!--   <scope>provided</scope>-->
		</dependency>

		<dependency>
			<groupId>org.apache.flink</groupId>
			<artifactId>flink-table-planner-blink_2.11</artifactId>
			<version>1.10.0</version>
			<!--   <scope>provided</scope>-->
		</dependency>

		<dependency>
			<groupId>org.apache.flink</groupId>
			<artifactId>flink-table-common</artifactId>
			<version>1.10.0</version>
			<!--   <scope>provided</scope>-->
		</dependency>

		<dependency>
			<groupId>org.apache.flink</groupId>
			<artifactId>flink-csv</artifactId>
			<version>1.10.0</version>
		</dependency>
```

> Table API和SQL需要引入的依赖有两个：planner和bridge。
>
> flink-table-planner：planner计划器，是table API最主要的部分，提供了运行时环境和生成程序执行计划的planner；
>
> flink-table-api-scala-bridge：bridge桥接器，主要负责table API和 DataStream/DataSet API的连接支持，按照语言分java和scala。
>
> 这里的两个依赖，是IDE环境下运行需要添加的；如果是生产环境，lib目录下默认已经有了planner，就只需要有bridge就可以了。
>
> 当然，如果想使用用户自定义函数，或是跟kafka做连接，需要有一个SQL client，这个包含在flink-table-common里。

### 17.2.3 两种planner（old & blink）的区别

1. **批流统一：Blink 将批处理作业，视为流式处理的特殊情况。**所以，blink 不支持表和
DataSet 之间的转换，批处理作业将不转换为DataSet 应用程序，而是跟流处理一样，转
换为DataStream 程序来处理。
2. 因为批流统一，Blink planner 也不支持BatchTableSource，而使用有界的StreamTable-
Source 代替。
3. **Blink planner 只支持全新的目录，不支持已弃用的ExternalCatalog。**
4. 旧planner 和Blink planner 的FilterableTableSource 实现不兼容。旧的planner 会把PlannerExpressions
下推到filterableTableSource 中，而blink planner 则会把Expressions 下
推。
5. **基于字符串的键值配置选项仅适用于Blink planner。**
6. PlannerConfig 在两个planner 中的实现不同。
7. Blink planner 会将多个sink 优化在一个DAG 中（仅在TableEnvironment 上受支持，而
在StreamTableEnvironment 上不受支持）。而旧planner 的优化总是将每一个sink 放在
一个新的DAG 中，其中所有DAG 彼此独立。
8. 旧的planner 不支持目录统计，而Blink planner 支持。



## 17.3 关于TableEnv

创建表的执行环境，需要将 flink 流处理的执行环境传入，TableEnvironment 是 flink 中集成 Table API 和 SQL 的核心概念，所有对表的操作都基于 TableEnvironment 。

注册 Catalog--->在 Catalog 中注册表--->执行 SQL 查询--->注册用户自定义函数（UDF）

表环境（TableEnvironment）是flink 中集成Table API & SQL 的核心概念。它负责:

• 注册catalog
• 在内部catalog 中注册表
• 执行SQL 查询
• 注册用户自定义函数
• 将DataStream 或DataSet 转换为表
• 保存对ExecutionEnvironment 或StreamExecutionEnvironment 的引用

#### 创建表环境

表环境的创建有三种方式

- 基于env

- 基于env + settings

  > 衍生有BlinkPlanner模式和OladPlanner模式
  >
  > Stream处理模式和Batch处理模式
  >
  > 建议使用Blink + Stream模式

- 基于TableConfig

```scala
    //1.基于env创建表环境
    val tableEnv1 = StreamTableEnvironment.create(env)

    //2.基于env + settings创建表环境
    val settings = EnvironmentSettings
      .newInstance()
      //        .inBatchMode()  批处理模式
      .inStreamingMode() // 流处理模式
      .useBlinkPlanner() // 使用BlinkPlanner开发
      //      .useOldPlanner()  //使用旧版本的Planner开发
      .build()
    val tableEnv2 = StreamTableEnvironment.create(env, settings)

    //3.基于env + TableConfig创建表环境这里不做概述
```

## 17.4 关于Table

- TableEnvironment 可以注册目录 Catalog，并可以基于 Catalog 注册表。
- 表（Table）是由一个“标识符”（identifier）来指定的，由3部分组成：Catalog名、数据库（database）名和对象名
- 表可以是常规的，也可以是虚拟的（视图，View）。
- 常规表（Table）一般可以用来描述外部数据，比如文件、数据库表或消息队列的数据，也可以直接从 DataStream转换而来。
- 视图（View）可以从现有的表中创建，通常是 table API 或者 SQL 查询的一个结果集。

#### 创建Table

##### 基于connectorDescriptor创建

- 基于TableEnvironment 的`.connect() `方法，连接外部系统，并调用 `.createTemporaryTable() `方法，在 Catalog 中注册表

- 连接文件系统

  ```scala
      tableEnv
        .connect(new FileSystem().path("E:\\IdeaPro\\Flink1128\\src\\main\\resources\\output1.txt"))
        .withFormat(new Csv()) 
        .withSchema(			
          new Schema()
            .field("userID", DataTypes.STRING())
            //.field("temp",DataTypes.DOUBLE())
            .field("count", DataTypes.BIGINT())
  
        )
        .inRetractMode()//修改模式为撤回
        .createTemporaryTable("outputTable") //在内存中创建临时表
  
  ```

- 连接kafka系统

  kafka 的连接器flink-kafka-connector 中，1.10 版本的已经提供了Table API 的
  支持。我们可以在connect 方法中直接传入一个叫做Kafka 的类，这就是kafka 连接器的描
  述器ConnectorDescriptor。

  ```scala
  tableEnv
  .connect(
  new Kafka()
  .version("0.11") // 定义kafka 的版本
  .topic("sensor") // 定义主题
  .property("zookeeper.connect", "localhost:2181")
  .property("bootstrap.servers", "localhost:9092")
  )
  .withFormat(new Csv())
  .withSchema(
  new Schema()
  .field("id", DataTypes.STRING())
  .field("timestamp", DataTypes.BIGINT())
  .field("temperature", DataTypes.DOUBLE())
  )
  .createTemporaryTable("kafkaInputTable")
  ```

  当然也可以连接到ElasticSearch、MySql、HBase、Hive 等外部系统，实现方式基本上是类似
  的。

//TableEnv读取内存中的表创建Table
      val resTable: Table = tableEnv.from("outputTable")

  ```
  
  ```scala
  .connect(...)    // 定义表的数据来源，和外部系统建立连接
  .withFormat(...)    // 定义数据格式化方法
.withSchema(...)    // 定义表结构
  .createTemporaryTable("MyTable")    // 创建临时表
  ```

  > 可以创建 Table 来描述文件数据，它可以从文件中读取，或者将数据写入文件


##### 基于DataStream创建

- 基于DataStream转换成Table，进而方便地调用 Table API 做转换操作

  ```scala
      //通过表执行环境将流转换成表,基于样例类类中的字段指定别名,一一对应可以更改字段的位置
      val sensorTable: Table = tableEnv.fromDataStream(dataSteam, 'id, 'timestamp as 'ts, 'temperature as 'temp)
  
      //通过表执行环境将流转换成表,基于样例类类中的字段名称访问
      val sensorTable1 = tableEnv.fromDataStream(dataSteam, 'id,'temperature)
  
      //直接通过流中的样例类转换
      val sensorTable2 = tableEnv.fromDataStream(dataSteam)
  
      sensorTable.toAppendStream[(String,Long,Double)].print()
      sensorTable1.toAppendStream[(String,Double)].print()
      sensorTable2.toAppendStream[(String,Long,Double)].print()
  ```

> 对于一个 DataStream，可以直接转换成 Table，进而方便地调用 Table API 做转换操作。
>
> 默认转换后的 Table schema 和 DataStream 中的字段定义一一对应，也可以单独指定出来。
>
> DataStream 中的数据类型，与表的 Schema 之间的对应关系，可以有两种：基于字段名称，或者基于字段位置
>
> 基于名称（name-based）
>
> ```scala
> val sensorTable = tableEnv.fromDataStream(dataStream, 
>                   'timestamp as 'ts, 'id as 'myId, 'temperature)
> 
> ```
>
> 基于位置（position-based）
>
> ```scala
> val sensorTable = tableEnv.fromDataStream(dataStream, 'myId, 'ts)
> 
> ```
>
> 

#### 创建临时视图

##### 基于 DataStream 创建临时视图

```scala
tableEnv.createTemporaryView("sensorView", dataStream)
```

```scala
tableEnv.createTemporaryView("sensorView", 
        dataStream, 'id, 'temperature, 'timestamp as 'ts)

```

##### 基于Table创建临时视图

```scala
tableEnv.createTemporaryView("sensorView", sensorTable)
```



#### 表的查询

##### 基于Table API查询

- Table API 是集成在 Scala 和 Java 语言内的查询 API
- Table API 基于代表“表”的 Table 类，并提供一整套操作处理的方法 API；这些方法会返回一个新的 Table 对象，表示对输入表应用转换操作的结果
- 有些关系型转换操作，可以由多个方法调用组成，构成链式调用结构

```scala
val sensorTable: Table = tableEnv.from("inputTable")

val aggResultTable = sensorTable
					.groupBy('id)
					.select('id, 'id.count as 'count)

```

这里Table API 里指定的字段，前面加了一个单引号'，这是Table API 中定义的Expression
类型的写法，可以很方便地表示一个表中的字段。
字段可以直接全部用双引号引起来，也可以用半边单引号+ 字段名的方式。以后的代码中，
一般都用后一种形式。

##### 基于SQL 查询

- Flink 的 SQL 集成，基于实现 了SQL 标准的 Apache Calcite
- 在 Flink 中，用常规字符串来定义 SQL 查询语句
- SQL 查询的结果，也是一个新的 Table

```scala
val resultSqlTable: Table = tableEnv
   .sqlQuery("select id, temperature from sensorTable where id ='sensor_1'")
```

#### 输出表

- 表的输出，是通过将数据写入 TableSink 来实现的
- TableSink 是一个通用接口，可以支持不同的文件格式、存储数据库和消息队列

- 输出表最直接的方法，就是通过 Table.insertInto() 方法将一个 Table 写入注册过的 TableSink 中

  ```scala
  //通过connect连接到外部系统创建外部表
  tableEnv.connect(...)
  .createTemporaryTable("outputTable")
  
  //通过DataStream转换得到表，向外部表写入数据
  val resultSqlTable: Table = ...
  resultTable.insertInto("outputTable")
  
  ```

  示例：输出到文件

  ```scala
  tableEnv.connect(
      new FileSystem().path("output.txt")
                  ) // 定义到文件系统的连接
  .withFormat(new Csv()) 
    .withSchema(new Schema()
                .field("id", DataTypes.STRING())
                .field("temp", DataTypes.Double())
               ) 
    .createTemporaryTable("outputTable")     // 创建临时表
  resultTable.insertInto("outputTable")    // 输出表
  
  ```

### 流和表的字段转换

如果使用样例类，直接将流传入样例类中的字段一一对应。

基于名称对应

如果想自定义一些字段或者想调整字段顺序定义时间语义，只需要将里面对应的名称放在指定的位置即可。

基于位置对应

如果只是更改别名，但是不改变顺序，和样例类中的字段一一对应即可。



表直接连接外部系统输出到注册好的TableSink

> TableSink支持不同的连接器，向外部系统写入数据。
>
> 通过Table.insertinto方法



FlinkConnector连接器

https://ci.apache.org/projects/flink/flink-docs-release-1.10/dev/table/connect.html#jdbc-connector

kafka只支持新版本的Csv格式。

## 17.5 关于更新模式

在流处理过程中，表的处理并不像传统定义的那样简单。
对于流式查询（Streaming Queries），需要声明如何在（动态）表和外部连接器之间执行转换。
与外部系统交换的消息类型，由更新模式（update mode）指定。

Flink Table API 中的更新模式有以下三种：
1. 追加模式（Append Mode）
在追加模式下，表（动态表）和外部连接器只交换插入（Insert）消息。
2. 撤回模式（Retract Mode）
在撤回模式下，表和外部连接器交换的是：添加（Add）和撤回（Retract）消息。
• 插入（Insert）会被编码为添加消息；
• 删除（Delete）则编码为撤回消息；
• 更新（Update）则会编码为，已更新行（上一行）的撤回消息，和更新行（新行）的添
加消息。
在此模式下，不能定义key，这一点跟upsert 模式完全不同。

3. Upsert（更新插入）模式
在Upsert 模式下，动态表和外部连接器交换Upsert 和Delete 消息。
这个模式需要一个唯一的key，通过这个key 可以传递更新消息。为了正确应用消息，外部
连接器需要知道这个唯一key 的属性。
• 插入（Insert）和更新（Update）都被编码为Upsert 消息；
• 删除（Delete）编码为Delete 信息。
这种模式和Retract 模式的主要区别在于，Update 操作是用单个消息编码的，所以效率会更
高。



## 17.6 动态表和连续查询

### 动态表基本介绍

*动态表*是Flink的Table API和SQL对流数据的支持的核心概念。与代表批处理数据的静态表相反，动态表随时间而变化。可以像静态批处理表一样查询它们。查询动态表会产生一个*连续查询*。连续查询永远不会终止并产生动态表作为结果。查询会不断更新其（动态）结果表，以反映其（动态）输入表上的更改。本质上，对动态表的连续查询与定义实例化视图的查询非常相似。

重要的是要注意，连续查询的结果在语义上始终等效于在输入表的快照上以批处理方式执行的同一查询的结果。

下图显示了流，动态表和连续查询之间的关系：

![image-20200621151258717](D:\ProgramFiles\Typora\图片备份\image-20200621151258717.png)

> 1. 流将转换为动态表。
> 2. 在动态表上评估连续查询，生成新的动态表。
> 3. 生成的动态表将转换回流。

**注意：**动态表首先是一个逻辑概念。动态表在查询执行期间不一定（完全）实现。

### 连续查询基本介绍

#### 不开窗的连续查询

在动态表上评估连续查询，并生成一个新的动态表作为结果。与批处理查询相反，连续查询永远不会终止并根据其输入表的更新来更新其结果表。在任何时间点，连续查询的结果在语义上都等同于在输入表的快照上以批处理模式执行同一查询的结果。

第一个查询是一个简单的`GROUP-BY COUNT`聚合查询。它将字段中的`clicks`表分组，`user`并计算访问的URL数量。下图显示了随着`clicks`表中其他行的更新，如何随时间评估查询。

![image-20200621152032014](D:\ProgramFiles\Typora\图片备份\image-20200621152032014.png)

1. 仅追加（Append-only）流
仅通过插入（Insert）更改，来修改的动态表，可以直接转换为“仅追加” 流。这个流中发出
的数据，就是动态表中新增的每一行。
2. 撤回（Retract）流
Retract 流是包含两类消息的流，添加（Add）消息和撤回（Retract）消息。
动态表通过将INSERT 编码为add 消息、DELETE 编码为retract 消息、UPDATE 编码为被更
改行（前一行）的retract 消息和更新后行（新行）的add 消息，转换为retract 流。

![image-20200628225039368](D:\ProgramFiles\Typora\图片备份\image-20200628225039368.png)

3. Upsert（更新插入）流
Upsert 流包含两种类型的消息：Upsert 消息和delete 消息。转换为upsert 流的动态表，需要
有唯一的键（key）。
通过将INSERT 和UPDATE 更改编码为upsert 消息，将DELETE 更改编码为DELETE 消息，
就可以将具有唯一键（Unique Key）的动态表转换为流。
下图显示了将动态表转换为upsert 流的过程。

![image-20200628225058383](D:\ProgramFiles\Typora\图片备份\image-20200628225058383.png)

这些概念我们之前都已提到过。需要注意的是，在代码里将动态表转换为DataStream 时，仅
支持Append 和Retract 流。而向外部系统输出动态表的TableSink 接口，则可以有不同的实
现，比如之前我们讲到的ES，就可以有Upsert 模式。

#### 基于窗口的连续查询

第二个查询与第一个查询类似，但是在对`clicks`表进行计数之前，除了对`user`属性进行分组外，它还在小时滚动窗口上对表进行分组（在对URL进行计数之前（基于窗口的基于时间的计算基于特殊的时间属性，稍后将进行讨论） ）。同样，该图显示了在不同时间点的输入和输出，以可视化动态表的变化性质。

![image-20200621152217821](D:\ProgramFiles\Typora\图片备份\image-20200621152217821.png)

> 和以前一样，输入表`clicks`显示在左侧。该查询每小时连续计算结果并更新结果表。clicks表包含四行，`cTime`在`12:00:00`和`12:59:59`之间带有时间戳（）。该查询从该输入计算两个结果行（每个对应一个`user`），并将它们附加到结果表中。对于`13:00:00`和`13:59:59`之间的下一个窗口，该`clicks`表包含三行，这将导致另外两行附加到结果表中。结果表将更新，因为`clicks`随着时间的推移会添加更多行。

## 17.7 时间特性

### 17.7.1 基本介绍

基于时间的操作（比如Table API 和SQL 中窗口操作），需要定义相关的时间语义和时间数
据来源的信息。所以，Table 可以提供一个逻辑上的时间字段，用于在表处理程序中，指示
时间和访问相应的时间戳。
时间属性，可以是每个表schema 的一部分。一旦定义了时间属性，它就可以作为一个字段
引用，并且可以在基于时间的操作中使用。
时间属性的行为类似于常规时间戳，可以访问，并且进行计算。

> 通常建议使用流直接转换。
>
> 使用schema转换时，需要考虑外部系统是否支持时间语义的转换。

###  17.7.2 处理时间（Processing Time）

#### 1）基本介绍

处理时间语义下，允许表处理程序根据机器的本地时间生成
结果。它是时间的最简单概念。它既不需要提取时间戳，也不需要生成watermark。

定义处理时间属性有三种方法：

- 在DataStream 转化时直接指定；

- 在定义Table Schema 时指定；

- 在创建表的DDL 中指定。

#### 2）基本使用

1. DataStream 转化成Table 时指定
    由DataStream 转换成表时，可以在后面指定字段名来定义Schema。在定义Schema 期间，可
    以使用.proctime，定义处理时间字段。
    注意，这个proctime 属性只能通过附加逻辑字段，来扩展物理schema。因此，只能在schema
    定义的末尾定义它。
    代码如下：

  ```scala
  // 定义好DataStream
  val inputStream: DataStream[String] = env.readTextFile("\\sensor.txt")
  val dataStream: DataStream[SensorReading] = inputStream
  .map(data => {
  val dataArray = data.split(",")
  SensorReading(dataArray(0), dataArray(1).toLong, dataArray(2).toDouble)
  })
  // 将DataStream 转换为Table，并指定时间字段
  val sensorTable = tableEnv
  .fromDataStream(dataStream, 'id, 'temperature, 'timestamp, 'pt.proctime)
  ```

2. 定义Table Schema 时指定
   这种方法其实也很简单，只要在定义Schema 的时候，加上一个新的字段，并指定成proctime
   就可以了。

   ```scala
   tableEnv
   .connect(
   new FileSystem().path("..\\sensor.txt"))
   .withFormat(new Csv())
   .withSchema(
   new Schema()
   .field("id", DataTypes.STRING())
   .field("timestamp", DataTypes.BIGINT())
   .field("temperature", DataTypes.DOUBLE())
   .field("pt", DataTypes.TIMESTAMP(3)).proctime() // 指定pt 字段为处理时间
   ) // 定义表结构
   .createTemporaryTable("inputTable") // 创建临时表
   ```

3. 创建表的DDL 中指定
   在创建表的DDL 中，增加一个字段并指定成proctime，也可以指定当前的时间字段。
   代码如下：

   ```scala
   val sinkDDL: String =
   """
   |create table dataTable (
   | id varchar(20) not null,
   | ts bigint,
   | temperature double,
   | pt AS PROCTIME()
   |) with (
   | 'connector.type' = 'filesystem',
   | 'connector.path' = 'file:///D:\\..\\sensor.txt',
   | 'format.type' = 'csv'
   |)
   """.stripMargin
   tableEnv.sqlUpdate(sinkDDL) // 执行DDL
   ```

   注意：运行这段DDL，必须使用Blink Planner。

### 17.7.3 事件时间（Event Time）

#### 1）基本介绍

事件时间语义，允许表处理程序根据每个记录中包含的时间生成结果。这样即使在有乱序事件或者延迟事件时，也可以获得正确的结果。
为了处理无序事件，并区分流中的准时和迟到事件；Flink 需要从事件数据中，提取时间戳，
并用来推进事件时间的进展（watermark）。

1. DataStream 转化成Table 时指定
    在DataStream 转换成Table，schema 的定义期间，使用.rowtime 可以定义事件时间属性。注
    意，必须在转换的数据流中分配时间戳和watermark。
    在将数据流转换为表时，有两种定义时间属性的方法。根据指定的.rowtime 字段名是否存在
    于数据流的架构中，timestamp 字段可以：
    • 作为新字段追加到schema
    • 替换现有字段
    在这两种情况下，定义的事件时间戳字段，都将保存DataStream 中事件时间戳的值。
    代码如下：

  ```scala
  val inputStream: DataStream[String] = env.readTextFile("\\sensor.txt")
  
  val dataStream: DataStream[SensorReading] = inputStream
  .map(data => {
  val dataArray = data.split(",")
  SensorReading(dataArray(0), dataArray(1).toLong, dataArray(2).toDouble)
  })
  .assignAscendingTimestamps(_.timestamp * 1000L)
  
  // 将DataStream 转换为Table，并指定时间字段
  val sensorTable = tableEnv
  .fromDataStream(dataStream, 'id, 'timestamp.rowtime, 'temperature)
  // 或者，直接追加字段
  val sensorTable2 = tableEnv
  .fromDataStream(dataStream, 'id, 'temperature, 'timestamp, 'rt.rowtime)
  ```

  2. 定义Table Schema 时指定

    这种方法只要在定义Schema 的时候，将事件时间字段，并指定成rowtime 就可以了。
    代码如下：

  ```scala
  tableEnv
  .connect(new FileSystem().path("sensor.txt"))
  .withFormat(new Csv())
  .withSchema(
  new Schema()
  .field("id", DataTypes.STRING())
  .field("timestamp", DataTypes.BIGINT())
  .rowtime(
  new Rowtime()
      .timestampsFromField("timestamp") // 从字段中提取时间戳
      .watermarksPeriodicBounded(1000) // watermark 延迟1 秒
  )
  .field("temperature", DataTypes.DOUBLE())
  ) // 定义表结构
  .createTemporaryTable("inputTable") // 创建临时表
  ```

  3. 创建表的DDL 中指定

    事件时间属性，是使用CREATE TABLE DDL 中的WARDMARK 语句定义的。watermark 语
    句，定义现有事件时间字段上的watermark 生成表达式，该表达式将事件时间字段标记为事
    件时间属性。
    代码如下：
    
    ```scala
    val sinkDDL: String =
    """
    |create table dataTable (
    | id varchar(20) not null,
    | ts bigint,
    | temperature double,
    | rt AS TO_TIMESTAMP( FROM_UNIXTIME(ts) ),
    | watermark for rt as rt - interval '1' second
    |) with (
    | 'connector.type' = 'filesystem',
    | 'connector.path' = 'file:///D:\\..\\sensor.txt',
    | 'format.type' = 'csv'
    |)
    """.stripMargin
    
    tableEnv.sqlUpdate(sinkDDL) // 执行DDL
    ```
    
    这里FROM_UNIXTIME 是系统内置的时间函数，用来将一个整数（秒数）转换成“YYYYMM-
    DD hh:mm:ss” 格式（默认，也可以作为第二个String 参数传入）的日期时间字符串（date
    time string）；然后再用TO_TIMESTAMP 将其转换成Timestamp。



# 十八 Flink实时项目

18.1 在大数据场景下遇到上亿条数据的去重需求时，使用布隆过滤器实现去重。

- 三个哈希
- 可能存在
- 一定不存在

18.2 双流join

```scala
package com.atguigu.need3

import org.apache.flink.api.common.state.ValueStateDescriptor
import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.co.CoProcessFunction
import org.apache.flink.streaming.api.scala.{OutputTag, _}
import org.apache.flink.util.Collector

/**
 * @Classname DoubleJoin
 * @Description TODO
 *              Date ${Date} 14:35
 * @Create by childwen
 */
object DoubleJoin {

  // 订单支付事件
  case class OrderEvent(orderId: String,
                        eventType: String,
                        eventTime: Long)

  // 第三方支付事件，例如微信，支付宝
  case class PayEvent(orderId: String,
                      eventType: String,
                      eventTime: Long)

  //侧输出标签，用来接收没有匹配的时间
  val unmatchedOrders = new OutputTag[String]("unmatchedOrders")
  val unmatchedPays = new OutputTag[String]("unmatchedPays")

  def main(args: Array[String]): Unit = {

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setParallelism(1)
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val orders = env
      .fromElements(
        OrderEvent("order_1", "pay", 2000L),
        OrderEvent("order_2", "pay", 5000L),
        OrderEvent("order_3", "pay", 6000L)
      )
      .assignAscendingTimestamps(_.eventTime)
      .keyBy(_.orderId)

    val pays = env
      .fromElements(
        PayEvent("order_1", "weixin", 7000L),
        PayEvent("order_2", "weixin", 8000L),
        PayEvent("order_4", "weixin", 9000L)
      )
      .assignAscendingTimestamps(_.eventTime)
      .keyBy(_.orderId)

    val doubleStream = orders.connect(pays)
    //双流Join，核心思想，两条流的数据相对而言必然有一个先到一个后到
    val resStream = doubleStream
      .process(new DoubleProcess)

    resStream.print
    resStream.getSideOutput(unmatchedOrders).print()
    resStream.getSideOutput(unmatchedPays).print()

    env.execute()
  }

  class DoubleProcess extends CoProcessFunction[OrderEvent, PayEvent, String] {
    //订单状态
    lazy val orderState = getRuntimeContext.getState(
      new ValueStateDescriptor[OrderEvent](
        "order", Types.of[OrderEvent]
      )
    )
    //支付状态
    lazy val payState = getRuntimeContext.getState(
      new ValueStateDescriptor[PayEvent](
        "pay", Types.of[PayEvent]
      )
    )

    override def processElement1(value: OrderEvent, ctx: CoProcessFunction[OrderEvent, PayEvent, String]#Context, out: Collector[String]): Unit = {
      //先匹配支付,匹配不到将自己存入状态等待
      val pay = payState.value()
      if (pay == null) {
        //还没收到支付信息,将自己缓存起来
        orderState.update(value)
        val t = ctx.timerService().currentWatermark() + 5000L
        println("注册时的水位线" + ctx.timerService().currentWatermark())
        //注册一个5秒钟后的定时器，等待五秒钟再次匹配。
        ctx.timerService().registerEventTimeTimer(t)
      } else {
        //支付信息已经到了，清空状态发送数据给下游
        payState.clear()
        out.collect(s"订单${value.orderId}已经完成支付。")
      }
    }

    override def processElement2(value: PayEvent, ctx: CoProcessFunction[OrderEvent, PayEvent, String]#Context, out: Collector[String]): Unit = {

      //先匹配订单,匹配不到将自己存入状态等待
      val order = orderState.value()
      if (order == null) {
        payState.update(value)
        val t = ctx.timerService().currentWatermark() + 5000L
        //注册一个5秒钟后的定时器，等待五秒钟再次匹配。
        ctx.timerService().registerEventTimeTimer(t)
      } else {
        //支付信息已经到了，清空状态发送数据给下游
        orderState.clear()
        out.collect(s"订单${value.orderId}已经完成支付。")
      }
    }

    override def onTimer(timestamp: Long, ctx: CoProcessFunction[OrderEvent, PayEvent, String]#OnTimerContext, out: Collector[String]): Unit = {
      println("定时器时间" + timestamp)
      //五秒钟后的定时器
      if (orderState.value() != null) {
        //说明在已经下单的情况下，5秒内订单还未支付,将该时间发送到侧输出
        ctx.output(unmatchedPays, s"订单ID ${orderState.value()}已经下单，未支付")
//        orderState.clear()
      }

      if (payState.value() != null) {
        //说明在已经下单的情况下，5秒内订单还未支付,将该时间发送到侧输出
        ctx.output(unmatchedOrders, s"订单ID ${payState.value()}已经支付，未更新订单状态")
//        payState.clear()
      }
    }
  }

}

```



# 十九 Flink命令行提交任务参数

https://ci.apache.org/projects/flink/flink-docs-release-1.10/ops/cli.html

# 二十 Flink消费Kafka手动维护偏移量

检查点是Apache Flink的内部机制，可以从故障中恢复。检查点是Flink应用程序状态的一致副本，包括输入的读取位置。如果发生故障，Flink将通过从检查点加载应用程序状态并从恢复的读取位置继续恢复应用程序，就像没有发生任何事情一样。您可以将检查点视为保存计算机游戏的当前状态。如果你在游戏中保存了自己的位置后发生了什么事情，你可以随时回过头再试一次。

检查点使Apache Flink具有容错能力，并确保在发生故障时保留流应用程序的语义。应用程序可以定期触发检查点。

Apache Flink中的Kafka消费者将Flink的检查点机制与有状态运算符集成在一起，其状态是所有Kafka分区中的读取偏移量。触发检查点时，每个分区的偏移量都存储在检查点中。Flink的检查点机制确保所有操作员任务的存储状态是一致的，即它们基于相同的输入数据。当所有操作员任务成功存储其状态时，检查点完成。因此，当从潜在的系统故障重新启动时，系统提供一次性状态更新保证。

# 二十一 Flink CEP

## 21.1 CEP基本介绍

- 复杂事件处理

- Flink CEP是在Flink中实现的复杂事件处理（CEP）库

- CEP允许在无休止的事件流中检测事件模式，让我们有机会掌握数据中重要的部分。

- 一个或多个由简单事件构成的事件流通过一定的规则匹配，然后输出用户想得到的数据 - 满足规则的复杂事件

  ![image-20200709145140338](D:\ProgramFiles\Typora\图片备份\image-20200709145140338.png)

> 目标：从有序的简单事件流中发现一些高阶特性
>
> 输入：一个或多个由简单事件构成的事件流
>
> 处理：识别简单事件之间的内在联系，多个符合一定规则的简单事件构成复杂事件
>
> 输出：满足规则的复杂事件



## 21.2 CEP - Pattern 基本使用

- 处理事件的规则，被叫做模式
- Flink CEP提供了Pattern API，用于对输入流数据进行复杂事件规则定义，用来提取符合规则的事件序列。

![image-20200709213242830](D:\ProgramFiles\Typora\图片备份\image-20200709213242830.png)

### 个体模式

- 组成复杂规则的每一个单独的模式定义，就是**个体模式**

```
start.times(3).where(_.behavior.startWith("fav"))
```

- 个体模式可以包含单例（singleton）模式和循环（looping）模式
- 单例模式只接受一个事件，而循环模式可以接受多个

subtype(subClass)：定义当前模式的子类型条件。 如果事件属于此子类型，则事件只能匹配该模式

### 组合模式

- 很多个体模式组合起来，就形成了整个的模式序列
- 模式序列必须以一个初始模式开始

```
val start = Pattern.begin("start")
```

### 模式组

- 将一个模式序列作为条件嵌套在个体模式里，成为一组模式。

### 个体模式 - 量词

- 可以在一个个体模式后追加量词，也就是指定循环次数

  > ```scala
  > //匹配出现4次
  > start.times(4)
  > //匹配出现2,3或者4次
  > start.times(2,4)
  > //匹配出现2，3或者4次，并且尽可能多地重复匹配等价于贪婪匹配
  > start.times(2,4).greedy
  > 
  > //匹配出现0次或4次
  > start.times(4).optional
  > //匹配出现1次或多次
  > start.oneOrMore
  > //匹配出现0次，2次或多次，并且尽可能多地重复匹配
  > start.timeOrMore(2).optional.greedy
  > ```
  >

##### oneOrMore()

指定此模式至少发生一次匹配事件。

默认情况下，使用宽松的内部连续性。

注意：建议使用until（）或within（）来启用状态清除

```scala
pattern.oneOrMore().until(new IterativeCondition<Event>() {
    @Override
    public boolean filter(Event value, Context ctx) throws Exception {
        return ... // alternative condition
    }
});
```



##### timesOrMore(#times)

指定此模式至少需要#times次出现匹配事件。

默认情况下，使用宽松的内部连续性（在后续事件之间）。

```scala
pattern.timesOrMore(2);
```

##### times(#ofTimes)

指定此模式需要匹配事件的确切出现次数。

默认情况下，使用宽松的内部连续性（在后续事件之间）。

```scala
pattern.times(2);
```

##### times(#fromTimes, #toTimes)

指定此模式期望在匹配事件的#fromTimes次和#toTimes次之间出现。

默认情况下，使用宽松的内部连续性。

```scala
pattern.times(2, 4);
```

##### optional()

指定此模式是可选的，即有可能根本不会发生。 这适用于所有上述量词。

```scala
pattern.oneOrMore().optional();
```

##### greedy()

指定此模式是贪婪的，即它将尽可能多地重复。 这仅适用于quantifiers，目前不支持组模式。

```scala
pattern.oneOrMore().greedy();
```

##### consecutive()

与oneOrMore（）和times（）一起使用并在匹配事件之间强加严格的连续性，即任何不匹配的元素都会中断匹配。

如果不使用，则使用宽松的连续性（如followBy（））。

```scala
Pattern.<Event>begin("start").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("c");
  }
})
.followedBy("middle").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("a");
  }
}).oneOrMore().consecutive()
.followedBy("end1").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("b");
  }
});
```

> 针对上面的模式，我们假如输入序列如：C D A1 A2 A3 D A4 B
>
> 使用consecutive：{C A1 B}, {C A1 A2 B}, {C A1 A2 A3 B}
>
> 不使用:{C A1 B}, {C A1 A2 B}, {C A1 A2 A3 B}, {C A1 A2 A3 A4 B}

##### allowCombinations()

与oneOrMore（）和times（）一起使用，并在匹配事件之间强加非确定性宽松连续性（如 followedByAny()）。

如果不应用，则使用宽松的连续性（如followBy()）。

例如,这样的模式：

```scala
Pattern.<Event>begin("start").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("c");
  }
})
.followedBy("middle").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("a");
  }
}).oneOrMore().allowCombinations()
.followedBy("end1").where(new SimpleCondition<Event>() {
  @Override
  public boolean filter(Event value) throws Exception {
    return value.getName().equals("b");
  }
});
```

> 针对上面的模式，我们假如输入序列如：C D A1 A2 A3 D A4 B
>
> 使用allowCombinations：{C A1 B}, {C A1 A2 B}, {C A1 A3 B}, {C A1 A4 B}, {C A1 A2 A3 B}, {C A1 A2 A4 B}, {C A1 A3 A4 B}, {C A1 A2 A3 A4 B}
>
> 不使用:{C A1 B}, {C A1 A2 B}, {C A1 A2 A3 B}, {C A1 A2 A3 A4 B}

### 个体模式 - 条件

- 每个模式都需要指定触发条件，作为模式是否接受事实进入的判断依据。

- CEP 中的个体模式主要通过`.where()`，`or`，和`until()`来指定条件

  按不同的调用方式，可以分为以下几类：

##### 简单条件 Condition

 - 通过.where()方法对事件中的字段进行判断筛选，决定是否接受该事件。

   ```scala
   start.where(event => event.getName.startsWith("foo"))
   ```

- 还可以通过pattern.subtype（subClass）方法将接受事件的类型限制为初始事件类型的子类型。

```scala
start.subtype(SubEvent.class).where(new SimpleCondition<SubEvent>() {
    @Override
    public boolean filter(SubEvent value) {
        return ... // some condition
    }
});
```





##### 组合条件 Combining Condition

 - 将简单条件进行合并 .or()方法表示或逻辑相连，.where()的直接组合就是AND

   ```scala
   //where + or = OR
   pattern.where(event => .../* some condition */).or(event => .../* or condition*/)
   
   //where + where = AND
   pattern.where(event => .../* some condition */).where(event => .../* where condition */)
   ```

##### 终止条件 Stop Condition

 - 如果使用了oneOrMore或者oneOrMore.optional，建议使用.until()作为终止条件，以便清理状态。

   **until：**指定循环模式的停止条件。 意味着如果匹配给定条件的事件发生，则不再接受该模式中的事件。

   ```scala>   pattern.oneOrMore().until(new IterativeCondition<Event>() {
       @Override
       public boolean filter(Event value, Context ctx) throws Exception {
           return ... // alternative condition
       }
   });
   ```
   

##### 迭代条件 Iterative Condition

- 这是最常见的条件类型。 你可以指定一个条件，该条件基于先前接受的事件的属性或其子集的统计信息来接受后续事件。

- 能够对模式之前所有接收的时间进行处理。

- 下面代码说的是：如果名称以`“foo”`开头同时如果该模式的先前接受的事件的价格总和加上当前事件的价格不超过该值 5.0，则迭代条件接受名为`“middle”`的模式的下一个事件。 

  ```scala
  middle.oneOrMore()
  //将接受事件的类型限制为初始事件类型的子类型
      .subtype(classOf[SubEvent])
      .where(
          (value, ctx) => {
              //拿到迭代当前序列和之前匹配序列的sum总和
  lazy val sum = ctx.getEventsForPattern("middle").map(_.getPrice).sum
              //对结果进行校验
  value.getName.startsWith("foo") && sum + value.getPrice < 5.0
          }
      )
  ```

### 模式序列

不同的近邻模式

![image-20200710092014727](D:\ProgramFiles\Typora\图片备份\image-20200710092014727.png)

#### 严格近邻 next 

所有事件按照严格的顺序出现，中间没有任何不匹配的事件，由 .next() 指定

例如对于模式`”a  next b”`，事件序列 [a, c, b1, b2] 没有匹配

#### 宽松近邻 followedBy 

允许中间出现不匹配的事件，由 .followedBy() 指定

例如对于模式`”a followedBy b”`，事件序列 [a, c, b1, b2] 匹配为 {a, b1}

#### 非确定性宽松近邻 followedByAny 

进一步放宽条件，之前已经匹配过的事件也可以再次使用，由 .followedByAny() 指定

例如对于模式`”a followedByAny b”`，事件序列 [a, c, b1, b2] 匹配为 {a, b1}，{a, b2}

#### 不希望出现某种近邻关系

- .notNext()：不想让某个事件严格紧邻前一个事件发生。
- .notFollowedBy()：不想让某个事件在两个事件之间发生

#### 模式匹配最大时间间隔

定义事件序列进行模式匹配的最大时间间隔。 如果未完成的事件序列超过此时间，则将其丢弃：

```scala
pattern.within(Time.seconds(10));
```

#### 注意事项

1）所有模式序列必须以 .begin() 开始

2）模式序列不能以 .notFollowedBy() 结束

3）`“not” `类型的模式不能被 optional 所修饰

4）此外，还可以为模式指定时间约束，用来要求在多长时间内匹配有效。

## 21.3 CEP - Detecting Patterns 基本使用

指定要查找的模式序列后，就可以将其应用于输入流以检测潜在匹配。 要针对模式序列运行事件流，必须创建PatternStream

#### 第一种方式创建

```scala
val patternStream: PatternStream[SensorReading] =
			CEP.pattern(inputStream, pattern)
```

#### 第二种方式创建

```scala
    val comparatorPatternStream: PatternStream[SensorReading] = CEP.pattern(inputStream, pattern, new EventComparator[SensorReading] {
      override def compare(o1: SensorReading, o2: SensorReading): Int =
        //如果两个事件的时间戳相同，使用二次排序比较ID号。
        if (o1.id.toLong > o2.id.toLong) 1
        else -1
    })
```

## 21.4 CEP - Selecting From PatternStream 基本使用

获得PatternStream后，可以通过select或flatSelect方法从检测到的事件序列中进行查询。

select（）方法需要PatternSelectFunction的实现。 PatternSelectFunction具有为每个匹配事件序列调用的select方法。

### 使用匿名函数实现

```scala
  // 注意匿名函数的类型
  val func = (pattern: Map[String, Iterable[SensorReading]]) => {
    val one = pattern.getOrElse("one", null).iterator.next()
    val second = pattern.getOrElse("two", null).iterator.next()
    val three = pattern.getOrElse("three", null).iterator.next()
    s"${one.id}->${one.temperature}-->${three.temperature}温度连续上升"
  }
```

### 自定义PatternSelectFunction实现

```scala
patternStream.select(new PatternSelectFunction[SensorReading, String] {
override def select(pattern:util.Map[String,util.List[SensorReading]]): String = {
        import scala.collection.JavaConversions._
        val one = pattern.getOrElse("one", null).iterator.next()
        val second = pattern.getOrElse("two", null).iterator.next()
        val three = pattern.getOrElse("three", null).iterator.next()

        s"${one.id}->${one.temperature}-->${three.temperature}温度连续上升"
      }
    }).print()
```



## 21.5 CEP应用场景

- **风险控制：**对用户异常行为模式进行实时检测，当一个用户发生了不该发生的行为，判定这个用户是不是有违规操作的嫌疑。
- **策略营销：**用预先定义好的规则对用户的行为轨迹进行实时跟踪，对行为轨迹匹配预定义规则的用户实时发送相应策略的推广。
- **运维监控：**灵活配置多指标、多依赖来实现更复杂的监控模式。

## 21.4 模式的属性

接下来介绍一下怎样设置模式的属性。模式的属性主要分为循环属性和可选属性。

- 循环属性可以定义模式匹配发生固定次数（**times**），匹配发生一次以上（**oneOrMore**），匹配发生多次以上(**timesOrMore**)。
- 可选属性可以设置模式是贪婪的（**greedy**），即匹配最长的串，或设置为可选的（**optional**），有则匹配，无则忽略。

## 21.5 模式的有效期

由于模式的匹配事件存放在状态中进行管理，所以需要设置一个全局的有效期（within）。若不指定有效期，匹配事件会一直保存在状态中不会被清除。至于有效期能开多大，要依据具体使用场景和数据量来衡量，关键要看匹配的事件有多少，随着匹配的事件增多，新到达的消息遍历之前的匹配事件会增加 CPU、内存的消耗，并且随着状态变大，数据倾斜也会越来越严重。

## 21.6 模式间的联系

主要分为三种：严格连续性（next/notNext），宽松连续性（followedBy/notFollowedBy），和非确定宽松连续性（followedByAny）。

三种模式匹配的差别见下表所示：

![image-20200709133402423](D:\ProgramFiles\Typora\图片备份\image-20200709133402423.png)

总结如下：

- **严格连续性**：需要消息的顺序到达与模式完全一致。
- **宽松连续性**：允许忽略不匹配的事件。
- **非确定宽松连性**：不仅可以忽略不匹配的事件，也可以忽略已经匹配的事件。

## 21.7 多模式组合

除了前面提到的模式定义和模式间的联系，还可以把相连的多个模式组合在一起看成一个模式组，类似于视图，可以在这个模式视图上进行相关操作。

![image-20200709134135417](D:\ProgramFiles\Typora\图片备份\image-20200709134135417.png)

上图这个例子里面，首先匹配了一个登录事件，然后接下来匹配浏览，下单，购买这三个事件反复发生三次的用户。 

如果没有模式组的话，代码里面浏览，下单，购买要写三次。有了模式组，只需把浏览，下单，购买这三个事件当做一个模式组，把相应的属性加上 times(3) 就可以了。

## 21.8 处理结果

处理匹配的结果主要有四个接口：

PatternFlatSelectFunction，

PatternSelectFunction，

PatternFlatTimeoutFunction，

PatternTimeoutFunction

从名字上可以看出，输出可以分为两类：select 和 flatSelect 指定输出一条还是多条，timeoutFunction 和不带 timeout 的 Function 指定可不可以对超时事件进行旁路输出。

