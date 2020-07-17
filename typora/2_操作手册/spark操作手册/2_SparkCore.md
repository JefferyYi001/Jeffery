# 第二章 SparkCore

## 2.1 RDD 概述

1. RDD（Resilient Distributed Dataset）叫做弹性分布式数据集，是Spark中最基本的数据抽象。在代码中是一个抽象类，它代表一个弹性的、不可变、可分区、里面的元素可并行计算的集合。

```scala
/**
* Internally, each RDD is characterized by five main properties:
*
*  - A list of partitions // 分区列表(分布式的并行运算)
*  - A function for computing each split // 计算切片的函数
*  - A list of dependencies on other RDDs // RDD 的依赖列表
*  - Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned) // 可选: 只针对`kv`形式`RDD`的分区器
*  - Optionally, a list of preferred locations to compute each split on (e.g. block locations for an HDFS file) // 可选: 计算每个切片的偏好位置列表
*/
```

2. RDD 特点：
   - 弹性
   - 分区
   - 只读
   - 依赖(血缘)
     - 窄依赖
     - 宽依赖
       - `coalesce`
         - 增加分区需要`shuffle`
       - `repartition`
       - `groupBy`
       - `sortBy`
   - 缓存
     - 保留血缘关系
   - `checkpoint`
     - 切断血缘关系
3. RDD 编程主要分为转换算子（transformation）和行动算子（action）两部分。
   - 转换算子
     - 只要这个算子的返回值是一个`RDD`，那么就一定是转换算子。
     - 都是`lazy`的, 只要碰到一个`action`，那么从最初的位置开始执行真正的转换。
   - 行动算子
     - 返回值不是`RDD`就一定是行动算子
     - 用来计算的触发动作
     - `job`, 如果碰到一个`action`就会创建一个`job`, 都会从转换的最初开始执行传给转换算子的那些匿名函数



## 2.2 创建 RDD

在 Spark 中创建 RDD 的方式可以分为 3 种：
•     从集合中创建 RDD。

```scala
val list1 = List(30, 50, 70, 60, 10, 20)
val rdd: RDD[Int] = sc.parallelize(list1)
// 两种方法等价
val rdd: RDD[Int] = sc.makeRDD(list1)
```

•     从外部存储创建 RDD。

```scala
sc.textFile(...)
```

•     从其他 RDD 转换得到新的 RDD。

## 2.3 普通元素的 RDD

1. map 和 mapPartitions

   - 都是在做`map`操作

   - `map`会每个元素执行一次`map`中的匿名函数

   - `mapPartitions`每个分区执行一次，效率会高一些

     > 注意: `mapPartitions` 有内存溢出的风险。
     >
     > 如果你把迭代器转成容器式集合(List, Array)的时候, 如果这个分区的数据特别大, 则会内存溢出。
     >
     > 如果没有内存溢出, 则效率要比map高。

     
   
   - scala 集合分区策略
   
     ```scala
     // parallelize 采用的分区方法 ———— 按顺序等分
     // Sequences need to be sliced at the same set of index positions for operations
     // like RDD.zip() to behave as expected
     def positions(length: Long, numSlices: Int): Iterator[(Int, Int)] = {
         (0 until numSlices).iterator.map { i =>
             val start = ((i * length) / numSlices).toInt
             val end = (((i + 1) * length) / numSlices).toInt
             (start, end)
       }
     }
     ```
   
   注意：PARTITION 侧重结果，split 侧重过程。
   注意：没有 collect 直接 foreach，分区顺序不可控。
   
2. `glom`

   把每个分区的数据放入到一个数组中，之后将得到的所有数组构成一个集合。如果有 n 个分区，得到的新的`RDD`中就有 n 个数组。

3. `distinct`

   ```scala
   object Distinct {
       def main(args: Array[String]): Unit = {
           val conf: SparkConf = new SparkConf().setAppName("Distinct").setMaster("local[2]")
           val sc: SparkContext = new SparkContext(conf)
           /*val list1 = List(30, 50, 70, 60, 10, 20, 60, 10, 20, 60, 10, 2, 10, 20, 60, 10, 20)
           val rdd1: RDD[Int] = sc.parallelize(list1, 2)
       
           val rdd2: RDD[Int] = rdd1.distinct()  // 去重
           println(rdd2.collect().mkString(", "))*/
           
           val users = User(10, "zs")::User(20, "lisi")::User(10, "abc")::Nil
           val rdd1 = sc.parallelize(users, 2)
           val rdd2: RDD[User] = rdd1.distinct()
           rdd2.collect().foreach(println)
           sc.stop() 
       }
   }
   
   case class User(age: Int, name: String){
       override def hashCode(): Int = this.age
       // 年龄相等就相等
       override def equals(obj: Any): Boolean = {
           obj match {
               case null => false
               case other: User => age == other.age
               case _ => false
           }
       }
   }
   ```

   注意：去重时，先判断 hashcode，再判断 equals；hashcode 不相等时就判定为不相等，因此除了重写 equals 方法，还要重写 hashcode 方法。

   使用 groupBy 同样可以实现去重：

   ```scala
   // group by 没有预聚合，因此使用 group by 去重效率偏低
   object Distinct_1 {
       def main(args: Array[String]): Unit = {
           val conf: SparkConf = new SparkConf().setAppName("Distinct").setMaster("local[2]")
           val sc: SparkContext = new SparkContext(conf)
           val list1 = List(30, 50, 70, 60, 10, 20, 60, 10, 20, 60, 10, 2, 10, 20, 60, 10, 20)
           val rdd1: RDD[Int] = sc.parallelize(list1, 2)
           // groupBy去重  没有预聚合, 所以效率有点低
           val rdd2 = rdd1.groupBy(x => x).map(_._1)
           println(rdd2.collect().mkString(", "))
           sc.stop()
       }
   }
   ```

   

4. `groupBy`

   - spark 集合之间的依赖关系分为宽依赖和窄依赖两种，宽依赖意味着有 shuffle 过程；窄依赖一对一，实现任务解耦。
   - `groupBy`需要`shuffle`，因为这个算子会产生宽依赖。而`shuffle`需要借助于磁盘，所以效率比较低，以后要慎用。
   - 分完组之后, `rdd`是`kv`形式的, 所以需要重新分区，默认的分区器是哈希分区器。
   - 分组必然伴随着分区，除此之外 groupby 和 partitionby 两者没有必然的联系，它们根据不同的业务需求分别从不同的角度来实现各自的功能。

5. `sample`

   数据的抽样，用于数据预评估。

   参数1: 表示是否放回。如果是 true 表示有放回抽样，即元素可以被重复抽到。所以, 后面的抽样比例取值范围为 [0, ∞)；如果是`false`, 表示无放回抽样, 即元素不会被重复抽到。所以抽样比例取值范围为 `[0, 1]`。

   参数2: 抽样比例

   参数3: 随机种子。一般使用系统的时间（默认）。如果每次种子都一样，则抽到的值也是一样的。

6. `coalesce`

   ```scala
   用来改变 RDD 分区数.
   coalesce 只能减少分区,不能增加分区。因为 coalesce 默认是不 shuffle，如果启动shuffle, 也可以增加分区.
   
   以后, 如果减少分区, 尽量不要shuffle, 只有增加的分区的时候才shuffle
   
   实际应用:
       如果减少分区就使用 coalesce
       如果是增加分区就是用 repartition
   ```
   
7. `sortBy`

   ```
   sortBy
       整个 RDD 进行的全局排序
       参数 1: 排序指标
       参数 2: 是否升序(默认true)
       参数 3: 排序后新的 RDD 的分区数。默认和排序前的 rdd 的分区数一致
   ```

   ```scala
   object SortByTest {
     def main(args: Array[String]): Unit = {
       val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("MapTest")
       val sc: SparkContext = new SparkContext(conf)
       val list1: List[String] = List("aa", "a", "12", "dfd", "erd", "rye", "qwre", "fvf", "dhd")
       val rdd1: RDD[String] = sc.parallelize(list1, 2)
       val rdd2 = rdd1.sortBy(x => (x.length, x), true)(Ordering.Tuple2(Ordering.Int.reverse, Ordering.String), ClassTag(classOf[(Int, String)]))
       rdd2.collect.foreach(println)
     }
   }
   ```

8. `pipe`

   使用`linux`命令或者脚本去处理`RDD`中数据。

   脚本案例：

   ```bash
   #!/bin/bash
   echo "excuting one partiton..."
   while read ele; do
           echo "<<<"$ele
   done
   ```

   脚本执行情况:

   ```scala
   rdd1.pipe("./p1.sh")
   // 每个分区都执行一次这个脚本。
   ```

9. `zip`

   注意：spark 中的 zip 与 scala 中的 zip 不同。

   spark 中的 zip 比 scala 中的 zip 要求更加苛刻：

   ```scala
   // 1. 两个RDD的分区数必须相同
   // 2. 对应的分区必须拥有相同的元素数(总的元素数必须相同)
   object ZipTest {
     def main(args: Array[String]): Unit = {
       val conf: SparkConf = new SparkConf().setAppName("ZipPractice").setMaster("local[2]")
       val sc: SparkContext = new SparkContext(conf)
       val list1 = List(30, 50, 70, 60, 10, 20)
   
       val rdd1 = sc.parallelize(list1.init, 2)
       val rdd2 = sc.parallelize(list1.tail, 2)
   
       val rdd3 = rdd1.zip(rdd2).map{
         case (a, b) => s"$a -> $b"
       }
       rdd3.collect.foreach(println)
       sc.stop()
     }
   }
   
   object ZipTest2 {
     def main(args: Array[String]): Unit = {
       val conf: SparkConf = new SparkConf().setAppName("ZipPractice").setMaster("local[2]")
       val sc: SparkContext = new SparkContext(conf)
       val list1 = List(30, 50, 70, 60, 10, 20)
       val list2 = List(3, 5, 7, 6)
   
       val rdd1 = sc.parallelize(list1, 2)
       val rdd2 = sc.parallelize(list2, 2)
   
       val rdd3 = rdd1.zipPartitions(rdd2)((it1, it2) => {
         it1.zip(it2)
       })
       rdd3.collect.foreach(println)
       sc.stop()
     }
   }
   ```

10. 其他操作

    ```scala
        // 并集(分区数相加)
                val rdd3: RDD[Int] = rdd1 ++ rdd2
                val rdd3: RDD[Int] = rdd1.union(rdd2)
    
        // 交集(默认情况: 分区数和前面的rdd中最大那个相等) 附带去重效果
                val rdd3 = rdd1.intersection(rdd2)
        // println(rdd3.getNumPartitions)
        // 差集(没有去重效果)
                val rdd3 = rdd1.subtract(rdd2)
        // 笛卡尔积  一般很少使用.
                val rdd3 = rdd1.cartesian(rdd2)
    ```

    

## 2.4 K-V 形式的 RDD

注意：... ByKey 都会 shuffle。

1. `partitionBy`

   ```scala
   object PartitionBy {
     def main(args: Array[String]): Unit = {
       val conf: SparkConf = new SparkConf()
                             .setAppName("PartitionBy")
                             .setMaster("local[2]")
       val sc: SparkContext = new SparkContext(conf)
       val list1 = List("hello", "hello", "world", "jeffery", "hello", "world")
   
       // 初始状态下分区数为自定义为 2，分区规则类似于 range 的分区方式，近乎均等地分到 2 个分区
       val rdd1 = sc.parallelize(list1, 2).map((_, 1))
   
       // 根据 key 的 hashcode % 3 的结果分到 3 个分区, value 不做考虑。
       // 若希望根据 value 的值进行分区, 那么应将 k - v 对调后进行分区, 分区后再对调复原。
       // 若 hashcode % 3 < 0，则 hashcode % 3 + 3 为分区值
       // 没有分到元素的分区为空
       val rdd2 = rdd1.partitionBy(new HashPartitioner(3))
   
       rdd1.glom().map(_.toList).collect.foreach(println)
       println("-------------")
       rdd2.glom().map(_.toList).collect.foreach(println)
     }
   }
   
   // HashPartitioner 的父类 ———— Partitioner
   abstract class Partitioner extends Serializable {
     // 返回分完区之后的新的 RDD 的分区数
     def numPartitions: Int
     // 每个键值对如何分区
     // 是由 key, 和value 没有任何关系
     // 传入 key，返回分区号
     def getPartition(key: Any): Int
   }
   ```

   - 只有`kv`形式的`RDD`才能使用分区器进行分区。
   - 使用分区器进行分区的时候，一般会进行`shuffle`。
   - 若`RDD1[(k, v)] ` 分区器是 `P1`, 对`RDD1`进行重新分区，使用的分区器仍为`p1`时，不会真正的分区，自然也就没有 `shuffle`。（分区器使用 hashcode 和 equals 进行比较确定是否为同一个）

2. 聚合算子

   （1）简介

   ```scala
   // 所有聚合类的算子都有预聚合
   reduceByKey(使用最多)
       聚合算子:
       1. 只能用在 kv 形式的聚合。
       2. 按照 key 进行聚合, 对相同的 key 的 value 进行聚合。
       3. 分区内进行预聚合，分区间进行最终聚合。
           
   foldByKey:(很鸡肋)
   	多一个 zero
       1. zero 的类型必须与 v 的类型一致
       2. zero 只在分区内聚合(预聚合, map端)的时候参与运算。分区间聚合(最终聚合, reduce端)不参与。
       3. 对一个 key, zero 最多参与 n 次计算 (n 是分区数)。
       4. 若某个分区中不包含该元素，则在该分区内聚合时 zero 值不计入该元素。
   // foldByKey reduceByKey 共同点:他们在分区内聚合和分区间的逻辑是一样.
   
   aggregateByKey:(次之)
   	1. 分区内聚合和分区间的聚合可以采用不一样的逻辑。
   	2. 分区内聚合时 zero 的类型可以与 v 的类型不一致。
   	3. zero 只在分区内聚合(预聚合, map端)的时候参与运算。分区间聚合(最终聚合, reduce端)不参与。
   
   combineByKey: 
       combineByKey[C](
             createCombiner: V => C,
             mergeValue: (C, V) => C,
             mergeCombiners: (C, C) => C)
      // createCombiner: 在每个分区内, 不同的key来说, 都会执行一次这个方法, 返回一个值, 相当于 scala 中的 zero
      // mergeValue: 分区内聚合
   // mergeCombiners:分区间的聚合
      // 分区内聚合和分区间的聚合可以采用不一样的逻辑。
   // 分区内聚合时 zero 的类型可以与 v 的类型不一致。
   ```
   
   （2）四者之间的联系:
   
   ```scala
   // 四者最终都调用了：combineByKeyWithClassTag
   combineByKey
       combineByKeyWithClassTag(createCombiner, mergeValue, mergeCombiners)(null)
   
   aggregateByKey
       combineByKeyWithClassTag[U]((v: V) => cleanedSeqOp(createZero(), v),
             cleanedSeqOp, combOp, partitioner)
   
   foldByKey
       combineByKeyWithClassTag[V]((v: V) => cleanedFunc(createZero(), v),
             cleanedFunc, cleanedFunc, partitioner)
   
   reduceByKey
       combineByKeyWithClassTag[V]((v: V) => v, func, func, partitioner)
   // 使用指导:
   1. 如果分区内和分区间的聚合逻辑不一样, 用 aggregateByKey
   2. 如果分区内和分区间逻辑一样  reduceByKey
   ```
   
   （3）aggregateByKey 应用案例
   
   ```scala
      object AggregateByKey {
        def main(args: Array[String]): Unit = {
          val conf: SparkConf = new SparkConf()
                                .setAppName("FoldByKey")
                                .setMaster("local[2]")
          val sc: SparkContext = new SparkContext(conf)
          val rdd1 = sc.parallelize(List(("a", 3), ("a", 2), ("c", 4), ("b", 3), ("c", 6), ("c", 8)), 2)
          // 分区内: 最大和最小
          // 分区间: 最大的和和最小的和
          val rdd2 = rdd1.aggregateByKey((Int.MinValue, Int.MaxValue))({
            case ((max, min), ele) => (max.max(ele), min.min(ele))
          }, {
            case ((max1, min1), (max2, min2)) => (max1 + max2, min1 + min2)
          })
          rdd2.collect.foreach(println)
          // 计算每个 key 的平均值
          // 分区内: 求和与 key 出现的个数  分区间: 和相加 与 个数相加
          println("-----------------------------------------")
          val rdd3 = rdd1.aggregateByKey((0, 0))({
            case ((sum, count), value) => (sum + value, count + 1)
          }, {
            case ((sum1, count1), (sum2, count2)) => (sum1 + sum2, count1 + count2)
          })
            .mapValues {
              case (sum, count) => sum.toDouble / count
            }
          rdd3.collect.foreach(println)
       sc.stop()
        }
   }
   ```
   
   （4）combineByKey 应用案例
   
   ```scala
      // 注意：combineByKey 不能识别偏函数
      object CombineByKey {
        def main(args: Array[String]): Unit = {
          val conf: SparkConf = new SparkConf().setAppName("FoldByKey").setMaster("local[2]")
          val sc: SparkContext = new SparkContext(conf)
          val rdd1 = sc.parallelize(List(("a", 3), ("a", 2), ("c", 4), ("b", 3), ("c", 6), ("c", 8)), 2)
      
          val rdd2 = rdd1.combineByKey((v: Int) => (v, 1)
            , (sumCount: (Int, Int), value) => (sumCount._1 + value, sumCount._2 + 1)
            , (sumCount1: (Int, Int), sumCount2: (Int, Int)) => (sumCount1._1 + sumCount2._1, sumCount1._2 + sumCount2._2)
          )
      
          rdd2.collect.foreach(println)
       sc.stop()
        }
   }
   ```

3. `reduceByKey`和`groupByKey`
   
   - 如果是聚合应用使用`reduceByKey`, 因为他有预聚合, 可以提高性能。
   - 如果分组的目的不是为了聚合, 这个时候就应该使用`groupByKey`。
   - 如果分组的目的是为了聚合, 则不要使用``groupByKey``, 因为他没有预聚合。
      - `groupbykey` 最终也用了 `combineByKeyWithClassTag`，只不过没有进行预聚合。
   
4. 排序
   
      ```scala
      sortBy	这个使用更广泛, 可以用在任意的 RDD 上, 用的更多些。
   // sortBy 在底层调用了 sortByKey，将其 value 去序列化后转换作为 key 进行排序。完成排序后再取其 value。
      Key 这个只能用在 kv 上, 按照 k 进行排序。
   ```


5. `join`
   
   其实就是`sql`中的连接
   
   - `sql`
   
     - 内连接
   
       `on a.id=b.id`
   
     - 左外
   
     - 右外
   
     - 全外(`hive`支持, `mysql`不支持)
   
   - `spark 的 rdd中`都支持，但只能用于`kv`形式的`RDD`，将 k 相等的连在一起。
   
        - 外连接时，不确定的值用 Option 对象进行封装，存在为 Some，否则为 None。
   
      ```scala
   object JoinTest {
        def main(args: Array[String]): Unit = {
       val conf: SparkConf = new SparkConf().setAppName("Join").setMaster("local[2]")
          val sc: SparkContext = new SparkContext(conf)
          val rdd1 = sc.parallelize(Array((1, "a"), (1, "b"), (2, "c"), (4, "d")))
          val rdd2 = sc.parallelize(Array((1, "aa"), (3, "bb"), (2, "cc"), (2, "dd")))
      
          val rdd3 = rdd1.join(rdd2)
          val rdd4 = rdd1.leftOuterJoin(rdd2)
          val rdd5 = rdd2.rightOuterJoin(rdd1)
          val rdd6 = rdd1.fullOuterJoin(rdd2)
      
          rdd3.collect.foreach(println)
          println("+++++++++++++++")
          rdd4.collect.foreach(println)
          println("+++++++++++++++")
          rdd5.collect.foreach(println)
          println("+++++++++++++++")
          rdd6.collect.foreach(println)
        }
      }
      ```

6. `cogroup`
   
      先将两个集合执行一次 groupByKey，之后对进行结果进行全外连接，结果类型为：`RDD[(Int, (Iterable[String], Iterable[String]))]`
   
      集合中独有的 key 与另一个集合运算后得到空集。
   
      案例：使用 cogroup 实现 join
   
      ```scala
      object CoGroupTest {
        def main(args: Array[String]): Unit = {
          val conf: SparkConf = new SparkConf().setAppName("Join").setMaster("local[2]")
          val sc: SparkContext = new SparkContext(conf)
          val rdd1 = sc.parallelize(Array((1, "a"), (1, "b"), (2, "c"), (4, "d")))
          val rdd2 = sc.parallelize(Array((1, "aa"), (3, "bb"), (2, "cc"), (2, "dd")))
      // 解法一：使用 for 循环嵌套
          val result = rdd1
            .cogroup(rdd2)
            .flatMap {
              case (k, (it1, it2)) => for (i <- it1; j <- it2) 
                yield (k, (i, j))
            }
      // 解法二：使用 flatMap/ map 嵌套
          val result2 = rdd1
            .cogroup(rdd2)
            .flatMap {
              case (k, (it1, it2)) => it1
                .flatMap(x => it2.map(y => (k, (x, y))))
            }
      
          result.collect.foreach(println)
          println("-------------------")
          result2.collect.foreach(println)
          sc.stop()
        }
      }
      
      ```


   7. `repartitionAndSortWithinPartitions `
   
      按照 k 进行分区并在分区内进行排序，以保证在分区内有序。
   
   8. `flatMapValues`
   
      对 value 进行 flatMap 操作，之后将结果作为 value 拼接各自的 key。
   
   9. `countByValue`
   
      统计一个 RDD 中各个 value 的出现次数。返回一个 map，map 的 key 是元素的值，value 是出现的次数。
   
   10. 综合练习

   ```scala
   object RDDPractice {
       def main(args: Array[String]): Unit = {
           /*
           需求
           1.	数据结构：时间戳，省份，城市，用户，广告，字段使用空格分割。
                   1516609143867 6 7 64 16
                   1516609143869 9 4 75 18
                   1516609143869 1 7 87 12
           2.	需求: 统计出每一个省份广告被点击次数的 TOP3
           
           -------------
           倒推法来分析:
           => 元数据做map
           => RDD((pro, ads), 1)  reduceByKey
           => RDD((pro, ads), count)   map
           => RDD(pro -> (ads, count), ....)      groupByKey
           => RDD( pro1-> List(ads1->100, abs2->800, abs3->600, ....),  pro2 -> List(...) )  map: 排序,前3
           => RDD( pro1-> List(ads1->100, abs2->800, abs3->600),  pro2 -> List(...) )
           
            */
           val conf: SparkConf = new SparkConf().setAppName("RDDPractice").setMaster("local[2]")
           val sc: SparkContext = new SparkContext(conf)
           // 1. 读取原始数据
           val lineRDD: RDD[String] = sc.textFile("c:/agent.log")
           // 2. 调整成我需要格式  ((pro, ads), 1)
           val proAdsAndOneRDD = lineRDD.map(line => {
               val split = line.split(" ")
               ((split(1), split(4)), 1)
           })
           // 3. RDD((pro, ads), count)
           val proAdsAndCountRDD = proAdsAndOneRDD.reduceByKey(_ + _)
           // 4. RDD(pro -> (ads, count), ....)
           val proAndAdsCountRDD = proAdsAndCountRDD.map {
               case ((pro, ads), count) => (pro, (ads, count))
           }
           // 5. RDD( pro1-> List(ads1->100, abs2->800, abs3->600, ....),  pro2 -> List(...) )  map: 排序,前3
           val resultRDD = proAndAdsCountRDD
               .groupByKey()
               .map {
                   case (pro, adsCountIt: Iterable[(String, Int)]) =>
                       (pro, adsCountIt.toList.sortBy(-_._2).take(3))
               }
               // .sortByKey()
               .sortBy(_._1.toInt)
           resultRDD.collect.foreach(println)
        sc.stop()
       }
   }
   ```

   

## 2.5 行动算子

```scala
val rdd2 = rdd1.filter(x => {
            println("filter " + x)
            x > 30
        }).map(x => {
            println("map " + x)
            x * x
        })
```

`.map .filter`这些算子的调用是在驱动端。DAG 也是在驱动端完成构建的。

传入匿名函数的执行都是`lazy`，将来都是在`executor`(进程) 上启动任务 (`task`, 线程) 来执行。

当碰到行动算子的时候，才开始按照 DAG 来运算。

同一个`stage`(阶段) 内的`task`是并行运算的。

1. `countByKey`

   ```
   用来计算相同 key 的元组各有多少个。
   ```

   `reduceByKey`和`countByKey`

   - `reduceByKey`是一个**转换算子**, 聚合时把相同 key 的 value 聚合在一起。
   - `countByKey`是一个**行动算子**, 仅仅是对 key 进行计数, 和 value 的值没有任何关系。
   - `countByKey`的底层其实就是利用`reduceByKey + collect + toMap` 来计算的。因此当数据量比较大时，官方建议使用 `rdd.mapValues(_ => 1L).reduceByKey(_ + _)` 进行运算。

2. `foreach`

   ```scala
   // spark 的 foreach 和 scala 的 foreach 不是一回事
   foreach 是遍历 RDD 中的每个元素。 // 在各个分区内依次遍历。
   将来我计算后的数据, 有的时候会存储到外部存储, 比如 mysql。此时可以用来与外部存储进行通信. 把数据写入到外部存储。
   为了节省磁盘 IO，可以使用 foreachPartition 进行批量传输。
   ```

3. `ruduce, fold, aggregate`

   - 行动算子，所有的`RDD`都适用。
   - 使用方式和`scala`类似, 只不过是在分布式环境下的 excutor 中运行。
   - `rdd1.reduce((x, y) => x + y)` 分区内聚合和分区间的聚合逻辑一样。
   - `fold`的零值类型必须和`RDD`中元素的类型保持一致。且零值在每个分区内聚合的时候各分区内使用一次, 分区间聚合的时候也会使用一次。所以参数运算的次数是: 分区数 + 1。
   - `reduce`与`fold`分区内和分区间的聚合逻辑一样。
   - 这三个用的不多，了解就行， 一般都是应转换型的聚合算子。

   小结：

   （1）scala 中 reduce 算子、RDD 中的 reduceByKey 算子、行动算子 reduce 的执行逻辑相差不大，区别是 RDD 中的 reduceByKey 是根据 key 值分组后再各组中执行。

   （2）scala 中 foldLeft 算子的 zero 值类型可以与元素类型不一致；RDD 算子 foldByKey 则要求 zero 值类型与元素类型一致，且每个分区计入一次 zero；行动算子 fold 在 RDD 的基础上，分区间聚合时也计入一次 zero。

   （3）RDD 算子 aggregateByKey、行动算子 aggregate 是为了弥补 zero 值类型与元素类型不一致的场景需求。除此之外，还有更强大的地方：分区内聚合和分区间聚合的执行逻辑可以不同。	RDD 算子 aggregateByKey 在每个分区内计入一次 zero，而行动算子 aggregate 不仅在每个分区内计入一次 zero，在分区间聚合时也会计入一次 zero。

## 2.6 序列化的问题

当传递给高阶算子的函数是个闭包时，要保证函数中用到的一些**属性**和**方法**支持序列化。（如果在 excutor 端不调用则不需要）

```scala
// 高阶函数的赋值在驱动端进行，具体的执行在集群执行
```

实现方式：

1. 让类序列化（样例类默认实现了 Serializable ）
2. 使用匿名函数 + 局部变量

新的序列化机制:

`kryo`一个第三方的序列化机制，比`java`的序列化机制要轻量级，速度也更快。`spark2.0`才开始支持，在内部值类型和值类型的数组已经默认采用这种机制。

自定义类型, 需要做一些配置:

```scala
// 更换序列化器，上面这行是可以省略的
.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
// 注册需要序列化的类
.registerKryoClasses(Array(classOf[Searcher2]))
```

> 注意: 即使换成了`kryo`序列化, 自定义的类型也需要实现`Serializable`。

## 2.7 job 的划分

1. Linux 命令行中查看依赖关系

   ```scala
   scala> rdd4.toDebugString
   res4: String =
   (2) ShuffledRDD[4] at reduceByKey at <console>:30 []
    +-(2) MapPartitionsRDD[3] at map at <console>:28 []
       | MapPartitionsRDD[2] at flatMap at <console>:26 []
       | ./words.txt MapPartitionsRDD[1] at textFile at <console>:24 []
       | ./words.txt HadoopRDD[0] at textFile at <console>:24 []
   // 括号中的值即分区数
   ```

   ```scala
   scala> rdd4.dependencies
   res31: Seq[org.apache.spark.Dependency[_]] = List(org.apache.spark.ShuffleDependency@4809035f)
   ```

2. job 的划分

   ```scala
   1. application
   	应用. 创建一个 SparkContext 可以认为创建了一个 Application。
   2. job
   	在一个 application 中, 每执行一次行动算子, 就会创建一个 job。
   3. stage
   	阶段. 默认会有一个 stage, 再每碰到一个 shuffle 算子, 会产生一个新的 stage。
   	一个 job 中, 至少包含一个 stage。
   4. task
   	任务。反映阶段执行时的并行度。
   	假设一个 RDD 有100个分区. 处理时每个分区的数据分配一个 task 来计算。
   	一个阶段可以有多个 task。
   // 对于 RDD 来说，每个分区都会被一个计算任务处理（Task）处理，分区数决定了并行计算的粒度。
   // 分区数和 task 数是相等的：分区是站数据的存储角度；task 是站的计算的角度。
   // Job 和 Job 之间是串行的
   // 集群中一个节点 (设备) 可以运行多个`executor`(进程)
   // 一个 executor 可以运行多个`task`, 每个`task`是一个线程
   // 核心数表示能够同时运行的`task`数量
   // driver 实际上也是一个线程
   // 分区数并不完全等同于并行度，影响并行度的还有 CPU 核心数。并行度为分区数和 CPU 核心数两者的最小值。
   总结:
		application
   		多个 job
   			多个 stage
   				多个 task
   ```
   
   

## 2.8 持久化和 checkpoint 的比较

1. 持久化

   ```scala
   1. 使用方式
       rdd.persist(存储级别)
       rdd.cache // 实际上还是调用了 persist
   2. 不会重新起 job 来专门的持久化, 而是使用上一个 job 的结果来进行持久化。
   3. 血缘关系还在, 一旦持久化的出现问题可以通过血缘关系重建 rdd。
   4. 持久化起作用后相应节点在 Web UI 中增加绿点。
   ```

   ```scala
   rdd.unpersist  // 释放缓存，之后再用到该 rdd 时会重新计算，Web UI 中绿点消失。
   ```

2. `checkepoint`

   ```scala
   1. 使用方式
      sc.setCheckpointDir("./ck1")
      rdd.checkpoint()
   2. checkpoint 的时候, 会重新启动一个新的 job 在刚才的 job 之后专门做 checkpoint。
   3. checkpoint 会切断 rdd 的血缘关系。
   4. checkpoint 起作用后相应节点之前的依赖关系消失。
   ```

   > 1. 不管是持久化还是`checkepoint`, 都是针对在同一个` app`中使用
   >
   > 2. `rdd`被重复使用才需要做缓存或 checkpoint
   > 3. shuffle 能够自动执行 cache，shuffle 触发的缓存默认是 memory_only。（在 DAG 图中缓存的环节是灰色的）

   > 注意: 在实际使用的时候, 一般会把缓存和 checkpoint 配合起来使用, 这样 checkpoint  就不会再单独运行 job 了。这时起效的是 checkpoint，依赖关系被切断。
   >
   > cache 语句和 checkpoint 语句谁放在前谁放在后无所谓。

## 2.9 分区器的一些概念

用来在`rdd`的`shuffle` 之后，决定哪些`kv`值应该进入到哪个分区。执行分区时使用的分区器一般是`HashPartitioner`。

```scala
// 所有集合默认是没有分区器的。在 Linux shell 中使用 rdd.partitioner 可以看到分区器为 None。执行 reparation 时如果不指定分区器，分区器依然为 None.
// 执行一次 reduceByKey 后可以看到结果使用的是`HashPartitioner`分区器。
(10, v) 20.. 30 10 50 100
new HashPartioner(2)
// 易出现严重的数据倾斜
```

1. 自定义分区

   ```scala
   object MyPartitionerDemo {
     def main(args: Array[String]): Unit = {
       val conf: SparkConf = new SparkConf()
                             .setAppName("MyPartitionerDemo")
                             .setMaster("local[2]")
       val sc: SparkContext = new SparkContext(conf)
       val list1 = List(30, 50, 7, 60, 1, 20, null, null)
       val rdd1 = sc.parallelize(list1, 4).map((_, 1))
       val rdd2 = rdd1.partitionBy(new MyPartitioner(2))
       val rdd3 = rdd2.reduceByKey(new MyPartitioner(2), _ + _)
   
       rdd2.glom().map(_.toList).collect.foreach(println)
       println("___________________")
       rdd3.glom().map(_.toList).collect.foreach(println)
   
       Thread.sleep(100000000)
       sc.stop()
   
     }
   }
   
   class MyPartitioner(val num: Int) extends Partitioner {
     // 返回分完区之后的分区数
     override def numPartitions: Int = num
   
     // 写一个hash分区器
     override def getPartition(key: Any): Int = {
       key match {
         case null => 0
         case _ => key.hashCode().abs % numPartitions
       }
     }
   
     override def hashCode(): Int = num
   
     override def equals(obj: Any): Boolean = {
       obj match {
         case null => false
         case o: MyPartitioner => num == o.num
         case _ => false
       }
     }
   }
   ```

2. `spark`提供了一个分区器，可以最大程度的避免数据倾斜 —— `RangePartitioner`

`RangePartitioner`：范围分区器，可以弥补`HashPartitioner`在某些情况下造成的数据倾斜。

```scala
100w 放到4个分区
理想情况是: 每个分区25w条
根据这些数据的 key 来生成一个数组, 这个数组可以把数据 4 等分。这个是数组存储其实是边界条件 ——— 边界数组
边界数组: [100, 200, 300]
如何确定边界数组? 抽样 ——— 水塘抽样
```



## 2.10 读写文件

### 2.10.1 写文本文件

   ```scala
   rdd.saveAsTextFile("路径")
   每个分区存一个文件
   ```

   ### 2.10.2 读  json  文件

   本质还是读文本文件, 然后使用`json`工具解析出来。

   > `json`文件, 必须保证每行是一个完整`json`数据
   >
   > 下面这个不行:
   >
   > ```scala
   > {
   >  "name": "zs",
   >  "age": 20
   > }
   > ```
   >
   > 这个才行: 
   >
   > ```scala
   > {"name":"Michael"}
   > {"name":"Andy", "age":30}
   > {"name":"Justin", "age":19}
   > ```
   >

   ```scala
// 首先像读文本那样将文件读入。
// 之后导入 scala 自带的 json 解析工具类。
import scala.util.parsing.json.JSON
// 将每行数据（代表一个 json 对象）解析为一个 map，并以 Option 的形式返回。 
val rdd2 = rdd1.map(x => JSON.parseFull(x))
   ```

   > 这个仅仅做了解, 后期使用`spark-sql`, 方便快捷。

### 2.10.3 seqenceFile

 `rdd`必须是`kv`格式

1. 写：

   ```scala
   val rdd1 = sc.parallelize(Array("a" ->97, "b" -> 98, "c" -> 99), 2)
   rdd1.saveAsSequenceFile("./seq")
   ```

2. 读:

   ```scala
   val rdd1 = sc.sequenceFile[String, Int]("./seq")
   ```

   注意: 读的时候一定要要指定`k 和 v`的泛型。

### 2.10.4 objectFile

任何的`rdd`都可以保存

1. 写：

   ```scala
   val rdd1 = sc.parallelize(Array("abc", "hello", "jeffery", "jack", "pony"), 2)
   rdd1.saveAsObjectFile("./obj")
   ```

2. 读：

   ```scala
   val rdd1 = sc.objectFile[String]("./obj")
   ```

   注意: 读的时候一定要要指定元素的泛型。

## 2.11 MySQL jdbc

1. 读:

   ```scala
   /*
   jdbc编程:
       加载启动
       class.forName(..)
       DiverManager.get...
       conn.prestat..
           ...
           pre.ex
           resultSet
    */
   object JDBCRead {
     def main(args: Array[String]): Unit = {
       val conf = new SparkConf().setAppName("JDBCRead").setMaster("local[2]")
       val sc = new SparkContext(conf)
   
       val driver = "com.mysql.jdbc.Driver"
       val url = "jdbc:mysql://hadoop103:3306/test"
       val user = "root"
       val pw = "root"
   
       val rdd = new JdbcRDD(sc, () => {
         Class.forName(driver)
         DriverManager.getConnection(url, user, pw)
         // 此处仅建立连接，不要关闭连接
       },
         "select id, name, age from user where id >= ? and id <= ?",
         3,
         5,
         2,
         row => {
           (row.getInt("id"), row.getString("name"), row.getInt("age"))
         }
       )
       rdd.collect.foreach(println)
       sc.stop()
     }
   }
   ```

2. 写:

   1. 如果把所有的数据拉到驱动端, 然后由驱动端统一使用`jdbc`来写入 `mysql`。  若数据量较大则容易`OOM`。
   2. 因此数据计算完毕后直接写到`jdbc`(重点)。
   3. 为了避免频繁创建和关闭数据库连接，实际执行时以分区为单位，每个分区创建一个数据库连接，分区内数据批量处理，处理后再关闭连接。

   ```scala
   object JDBCWrite {
     val driver = "com.mysql.jdbc.Driver"
     val url = "jdbc:mysql://hadoop103:3306/test"
     val user = "root"
     val pw = "root"
   
     def main(args: Array[String]): Unit = {
       // 把rdd的数据写入到 mysql
       val conf: SparkConf = new SparkConf().setAppName("JDBCWrite").setMaster("local[2]")
       val sc: SparkContext = new SparkContext(conf)
       // wordCount, 然后把wordCount的数据写入到 mysql
       val wordCount = sc
         .textFile("E:\\hiveinput\\ss.txt")
         .flatMap(_.split("\\W+"))
         .map((_, 1))
         .reduceByKey(_ + _, 3)
       val sql = "insert into wordcount values(?, ?)"
       wordCount.foreachPartition(it => {
         // it就是存储的每个分区数据
         // 建立到mysql的连接
         Class.forName(driver)
         // 获取连接
         val conn = DriverManager.getConnection(url, user, pw)
         val ps = conn.prepareStatement(sql)
         var max = 0
         it.foreach{
           case (word, count) => {
             ps.setString(1, word)
             ps.setInt(2, count)
             ps.addBatch()
             max += 1
             if (max >= 10){
               ps.executeBatch()
               max = 0
             }
           }
         }
         ps.executeBatch()
         conn.close()
       })
       sc.stop()
     }
   }
   ```

   

## 2.12 Hbase 读写

1. 依赖

   ```xml
       <dependencies>
           <dependency>
               <groupId>org.apache.hbase</groupId>
               <artifactId>hbase-server</artifactId>
               <version>1.3.1</version>
               <exclusions>
                   <exclusion>
                       <groupId>org.mortbay.jetty</groupId>
                       <artifactId>servlet-api-2.5</artifactId>
                   </exclusion>
                   <exclusion>
                       <groupId>javax.servlet</groupId>
                       <artifactId>servlet-api</artifactId>
                   </exclusion>
               </exclusions>
           </dependency>
       </dependencies>
   ```
   
   注意：与 spark 存在部分依赖冲突，需排除部分依赖。
   
2. 读

   ```scala
   package com.jeffery.spark.core.core05
   
   import org.apache.hadoop.conf.Configuration
   import org.apache.hadoop.hbase.{Cell, CellUtil, HBaseConfiguration}
   import org.apache.hadoop.hbase.client.{Put, Result}
   import org.apache.hadoop.hbase.io.ImmutableBytesWritable
   import org.apache.hadoop.hbase.mapreduce.{TableInputFormat, TableOutputFormat}
   import org.apache.hadoop.hbase.util.Bytes
   import org.apache.spark.rdd.RDD
   import org.apache.spark.{SparkConf, SparkContext}
   import org.json4s.DefaultFormats
   import org.json4s.jackson.Serialization
   
   /**
    * @time 2020/5/10 - 11:13
    * @Version 1.0
    * @Author Jeffery Yi
    */
   object HbaseRead {
     def main(args: Array[String]): Unit = {
       val conf: SparkConf = new SparkConf().setAppName("HbaseRead").setMaster("local[2]")
       val sc: SparkContext = new SparkContext(conf)
   
       val hbaseConf: Configuration = HBaseConfiguration.create()
       hbaseConf.set("hbase.zookeeper.quorum", "hadoop102,hadoop103,hadoop104") // zookeeper配置
       hbaseConf.set(TableInputFormat.INPUT_TABLE, "student")
   
       // 通用的读法 noSql key-value cf
       val hbaseRDD: RDD[(ImmutableBytesWritable, Result)] = sc.newAPIHadoopRDD(
         hbaseConf,
         classOf[TableInputFormat], // InputFormat
         classOf[ImmutableBytesWritable], //hbase + mapreduce
         classOf[Result]
       )
   
       val rdd2 = hbaseRDD.map {
         case (ibw, result) =>
           //                Bytes.toString(ibw.get())
           // 把每一行所有的列都读出来, 然后放在一个map中, 组成一个json字符串
           var map = Map[String, String]()
           // 先把row放进去
           map += "rowKey" -> Bytes.toString(ibw.get())
           // 拿出来所有的列
           val cells: java.util.List[Cell] = result.listCells()
           // 导入里面的一些隐式转换函数, 可以自动把java的集合转成scala的集合
           import scala.collection.JavaConversions._
           for(cell <- cells){ // for循环, 只支持scala的集合
             val key = Bytes.toString(CellUtil.cloneQualifier(cell))
             val value = Bytes.toString(CellUtil.cloneValue(cell))
             map += key -> value
           }
           // 把map序列化成json字符串
           // json4s 专门为scala准备的json工具
           implicit val d: DefaultFormats =  org.json4s.DefaultFormats
           Serialization.write(map)
       }
       //        rdd2.collect.foreach(println)
       rdd2.saveAsTextFile("./hbase")
       sc.stop()
     }
   }
   ```

3. 写

   ```scala
   package com.jeffery.spark.core.core05
   
   import org.apache.hadoop.conf.Configuration
   import org.apache.hadoop.hbase.HBaseConfiguration
   import org.apache.hadoop.hbase.client.Put
   import org.apache.hadoop.hbase.io.ImmutableBytesWritable
   import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
   import org.apache.hadoop.hbase.util.Bytes
   import org.apache.hadoop.mapreduce.Job
   import org.apache.spark.rdd.RDD
   import org.apache.spark.{SparkConf, SparkContext}
   
   /**
       * @time 2020/5/10 - 12:12
       * @Version 1.0
       * @Author Jeffery Yi
       */
      object HbaseWrite {
        def main(args: Array[String]): Unit = {
          val conf: SparkConf = new SparkConf().setAppName("HbaseWrite").setMaster("local[2]")
          val sc: SparkContext = new SparkContext(conf)
   val list = List(
        ("2100", "zs", "male", "10"),
        ("2101", "li", "female", "11"),
        ("2102", "ww", "male", "12"))
      val rdd1 = sc.parallelize(list)
      
      // 把数据写入到Hbase
      // rdd1做成kv形式，即满足 hbase 需求的 (ImmutableBytesWritable, Put) 格式
      val resultRDD: RDD[(ImmutableBytesWritable, Put)] = rdd1.map {
        case (rowKey, name, gender, age) =>
          val rk = new ImmutableBytesWritable()
          rk.set(Bytes.toBytes(rowKey))
          val put = new Put(Bytes.toBytes(rowKey))
          put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("name"), Bytes.toBytes(name))
          put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("gender"), Bytes.toBytes(gender))
          put.addColumn(Bytes.toBytes("cf1"), Bytes.toBytes("age"), Bytes.toBytes(age))
          (rk, put)
      }
      
      // 配置待写入的 hbase 环境
      val hbaseConf: Configuration = HBaseConfiguration.create()
      hbaseConf.set("hbase.zookeeper.quorum", "hadoop102,hadoop103,hadoop104") // zookeeper配置
      hbaseConf.set(TableOutputFormat.OUTPUT_TABLE, "student")  // 输出表
      
      val job: Job = Job.getInstance(hbaseConf)
      job.setOutputFormatClass(classOf[TableOutputFormat[ImmutableBytesWritable]])
      job.setOutputKeyClass(classOf[ImmutableBytesWritable])
      job.setOutputValueClass(classOf[Put])
      // 写入配置的 hbase
      resultRDD.saveAsNewAPIHadoopDataset(job.getConfiguration)
      
      sc.stop()
     }
    }
   ```

## 2.13 累加器

```scala
// 一次计算出元素的和, 个数, 平均值, 最大值, 最小值
// Map("sum" -> .., "avg"-> ...)
object MyAcc {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf()
      .setAppName("Acc2")
      .setMaster("local[2]")
    val sc: SparkContext = new SparkContext(conf)
    val list1 = List(30, 50, 70, 60, 10, 20)
    // 一次计算出来 元素的和, 个数, 平均值, 最大值, 最小值
    // Map("sum" -> .., "avg"-> ...)
    val rdd1 = sc.parallelize(list1, 3)

    val acc = new MyAcc
    sc.register(acc, "mapAcc")
    rdd1.foreach(x => acc.add(x))
    val result = acc.value
    println(result)
    println("------------------------------------------------------------")

    val acc2 = acc.copy()
    sc.register(acc2, "mapAcc2")
    rdd1.foreach(x => acc2.add(x))
    val result2 = acc2.value
    println(result)
    println(result2)
    sc.stop()
  }
}

class MyAcc extends AccumulatorV2[Int, Map[String, Double]]{
  private var map = Map[String, Double]()
  override def isZero: Boolean = map.isEmpty

  override def copy(): AccumulatorV2[Int, Map[String, Double]] = {
    val acc = new MyAcc
    acc.map = map
    acc
  }

  override def reset(): Unit = map = Map.empty[String, Double]

  override def add(v: Int): Unit = {
    map += "sum" -> (map.getOrElse("sum", 0D) + v)
    map += "count" -> (map.getOrElse("count", 0D) + 1D)
    map += "max" -> map.getOrElse("max", Double.MinValue).max(v)
    map += "min" -> map.getOrElse("min", Double.MaxValue).min(v)
  }

  override def merge(other: AccumulatorV2[Int, Map[String, Double]]): Unit = {
    other match {
      case o: MyAcc =>
        map += "sum" -> (o.map.getOrElse("sum", 0D) + map.getOrElse("sum", 0D))
        map += "count" -> (o.map.getOrElse("count", 0D) + map.getOrElse("count", 0D))
        map += "max" -> o.map.getOrElse("max", Double.MinValue).max(map.getOrElse("max", Double.MinValue))
        map += "min" -> o.map.getOrElse("min", Double.MaxValue).min(map.getOrElse("min", Double.MaxValue))
    }
  }

  override def value: Map[String, Double] = {
    map += "avg" -> (map.getOrElse("sum", 0D) / map.getOrElse("count", 1D))
    map
  }
}
```

总结: 

1. 继承类`AccumulateV2[In, Out]`
2. 一些方法进行具体的实现。
3. 累加器建议只在**行动算子**中使用, 不要用在转换算子中。否则可能带来重复计算引起的 bug。

## 2.14 共享变量的问题

1. 累加器解决的是共享变量的什么问题?
   
   - 共享变量的写的问题(修改)。
   
2. 共享变量读的问题?
   - 广播变量解决的是变量读的问题。
   - 应用于大变量读的场景，每个 excutor 只需一个变量即可。
   - 对广播变量，不要去改，只能读。
   
3. 使用方法

   ```scala
   // arr 为待广播的大变量
   // bdArr 为广播后的大变量
   val bdArr = sc.broadcast(arr)
   ```

   ```scala
   object BCTest {
     val arr = Array(30, 50, 10, 100, 300)
     def main(args: Array[String]): Unit = {
       val conf: SparkConf = new SparkConf()
                             .setAppName("BdDemo")
                             .setMaster("local[2]")
       val sc: SparkContext = new SparkContext(conf)
   
       val list1 = List(30, 50, 70, 60, 10, 20)
       val rdd1: RDD[Int] = sc.parallelize(list1, 3)
   
       val bdArr = sc.broadcast(arr)
       val rdd2 = rdd1.filter(bdArr.value.contains(_))
       rdd2.collect.foreach(println)
   
       sc.stop()
     }
}
   ```
   
   

## 2.15 分区策略

### 2.15.1 scala 集合数据放到 RDD 分区策略

```scala
val list = List(1,2,3,4,5,6)
sc.parallelize(list)
默认分分区数: 申请到的 cpu 的核心数
sc.parallelize(list, 5)  // 指定分区数

区别:5 表示是 rdd 的分区数
	cpu的核心数(2), 是在启动 app 的申请. 表示的是最大并行度
```

### 2.15.2 读取文件时的分区策略

```scala
def textFile(
      path: String,
      minPartitions: Int = defaultMinPartitions): RDD[String] = withScope {
    assertNotStopped()
    hadoopFile(path, classOf[TextInputFormat], classOf[LongWritable], classOf[Text],
      minPartitions).map(pair => pair._2.toString).setName(path)
  }

```

最小分区数:

```scala
// - 要么是1要么是2, 一般都是2
// - minPartitions = 2
/**
   * Default min number of partitions for Hadoop RDDs when not given by user
   * Notice that we use math.min so the "defaultMinPartitions" cannot be higher than 2.
   * The reasons for this are discussed in https://github.com/mesos/spark/pull/718
   */
  def defaultMinPartitions: Int = math.min(defaultParallelism, 2)
```

```java
// 对传入有文件切片. 每个切片对应rdd中的一个分区
val inputSplits = inputFormat.getSplits(jobConf, minPartitions)
    
long totalSize = 0;   // 所有文件的总大小  1400 B
// 列出传入目录下所有的文件
FileStatus[] files = listStatus(job);
    // Save the number of input files for metrics/loadgen
    job.setLong(NUM_INPUT_FILES, files.length);
    long totalSize = 0;                           // compute total size
    for (FileStatus file: files) {                // check we have valid files
      if (file.isDirectory()) {
          // 若有子目录，则抛异常
        throw new IOException("Not a file: "+ file.getPath());
      }
      totalSize += file.getLen();
    }

// 目标尺寸:goalSize =  1400 / (2 == 0 ? 1 : 2)  =  700 B
long goalSize = totalSize / (numSplits == 0 ? 1 : numSplits);

// minSize = Math.max(1, 1) = 1   // 表示每个切片的最小长度
long minSize = Math.max(job.getLong(org.apache.hadoop.mapreduce.lib.input.
      FileInputFormat.SPLIT_MINSIZE, 1), minSplitSize);

// 之后遍历每个文件进行切片:

long blockSize = file.getBlockSize();  // 本地文件是: 32M
// 每个切片应该多大? (700, 1, 32M)   splitSize = 700
// 与 hadoop 略有不同
// computeSplitSize(Math.max(minSize,Math.min(maxSize,blocksize)))
long splitSize = computeSplitSize(goalSize, minSize, blockSize);

// 待切的长度
long bytesRemaining = length;
```

总结: 

1. 文件的尺寸和总大小的一半的 1.1 倍(或者 32M 或者 128M)做比较。
2. 如果大于则切；否则不切。

## 2.16 知识点补充

### 2.16.1 数据格式转换

```scala
// 使用 DecimalFormat 类定义格式化实例
val f = new DecimalFormat(".00%")
// 使用格式化实例对数据进行格式化（返回值类型为 String）
val result = f.format(numerator.toDouble / denominator)
```

### 2.16.2 项目收获

```scala
1. 拿到需求后先缕清思路：需求是什么？每一步输入输出是什么？既不能拖泥带水，也不能急功近利。
2. 分区器是对 key 进行分区计算的。
3. groupByKey 后的数据结构为 key -> Iterable 构成的RDD。
4. groupBy(Key) 后的数据结构也为 key -> Iterable 构成的RDD。
5. mapPartitions 是对每个分区内的数据 Iterator 进行操作，执行的结果依然是 Iterator 。但是 Iterator 的元素泛型和结构、数量是可以改变的（分别由 foreach、map、filter 实现）。
```

