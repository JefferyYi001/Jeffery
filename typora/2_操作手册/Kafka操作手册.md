# 1. Kafka 的简介

## 1.1 核心概念

Broker：一台 kafka 服务器就是一个 broker，一个集群由多个 broker 组成。

Topic：主题只是逻辑上的分类，实际上数据在存储时必须存储在某个主题的分区下。Topic 可以类比为数据库中的库。

Partition：分区是物理上数据存储的路径，分区在磁盘上就是一个目录，目录名由**主题名-分区名**组成。（分区还具有一定的逻辑属性，分区中的副本才是纯物理概念）

**消费者在消费主题中的数据时，一个分区只能被同一个组中的一个消费者线程所消费。一个 topic 可以有多个分区，每个分区即一个消息队列，分区数量没有限制；每个分区可以有多个副本，多个副本之间由框架自动实现 HA，一个 leader，其他为 follower， leader 根据负载状况由集群选取。不同副本不能位于同一个 broker，且副本数量不能超过 broker 数量。**

## 1.2 安装

### 1.2.1 环境的配置

kafka 使用 scala 语言编写，scala 也需要运行在 JVM上，因此要求必须配置 JAVA_HOME 环境变量。

### 1.2.2 配置

编辑 config/server.properties

```properties
#21行，每台cluster中的broker都需要有唯一的id号，必须为整数
broker.id=103
#24行，打开注释,此行代表允许手动删除kafka中的主题
delete.topic.enable=true
#63行，配置kafka存储的数据文件的存放目录
log.dirs=/opt/module/kafka/datas
#126行，配置连接的zk集群的地址
zookeeper.connect=hadoop102:2181,hadoop103:2181,hadoop104:2181
```

分发整个kafka到集群，注意**修改其他 broker 的 broker.id**。

### 1.2.3 启动和停止

① 启动 zk 集群

② 启动broker

```shell
/opt/module/kafka/bin/kafka-server-start.sh -daemon /opt/module/kafka/config/server.properties
```

③ 停止集群

```shell
/opt/module/kafka/bin/kafka-server-stop.sh
```

# 2. Kafka 常用操作

## 2.1 主题操作

① 查看集群中的所有主题：

```bash
bin/kafka-topics.sh --zookeeper hadoop103:2181 --list
```

② 创建主题：

```bash
bin/kafka-topics.sh --zookeeper hadoop103:2181 --create --topic hello --partitions 3 --replication-factor 2
```

创建主题是必须指定分区个数和副本数。该方式新建的主题是采用赋值均衡算法，将主题的多个分区均衡地分配到多个 broke。

注意：**--replication-factor 不能超过当前集群可用的 broker 的数量**。

在新建主题时，还可以明确地告诉 kafka，我的每个分区的各个副本分别分配到哪个机器：

```bash
bin/kafka-topics.sh --zookeeper hadoop103:2181 --create --topic hello1  --replica-assignment 102:103,102:104
```

③ 查看主题的描述信息：

```bash
bin/kafka-topics.sh --zookeeper hadoop103:2181 --describe --topic hello
```

④ 扩展主题的分区：

```bash
bin/kafka-topics.sh --zookeeper hadoop103:2181 --alter --topic hello --partitions 6
```

**注：分区只能增加，不能减少。**

⑤ 重新指定分区副本的分配策略：

新建 myconf 目录，在 myconf 目录下编写 partitions-topic.json 文件并保存。

```json
{
        "partitions":
                [
                {
                        "topic": "hello1",
                        "partition": 0,
                        "replicas": [102,103]
                },
                {
                        "topic": "hello1",
                        "partition": 1,
                        "replicas": [103,104]
                },
                {
                        "topic": "hello1",
                        "partition": 2,
                        "replicas": [102,104]
                }
                ],
        "version":1
}
```

**注意：副本数量可以更改，分区数量不能更改。**

执行各分区副本重分配：

```bash
bin/kafka-reassign-partitions.sh --zookeeper hadoop103:2181 --reassignment-json-file myconf/partitions-topic.json --execute
```

⑥ 删除主题

```bash
bin/kafka-topics.sh --zookeeper hadoop103:2181 --delete --topic hello1
```



## 2.2 生产数据

生产消息需要由生产者自己写程序生成，kafka 提供基于测试的生产者。

```bash
bin/kafka-console-producer.sh --broker-list hadoop103:9092 --topic  hello
```

生产数据时，如果一个主题有多个分区，且在生产时只指定主题不指定分区，消息会轮询地分配到不同的分区。

在同一个分区中，消费者在消费数据时，分区内部有序，但是不代表数据整体有序。若希望数据整体有序，只能一个主题设置一个分区。

注：轮询并不是按顺序轮询。kafka 会保证所有分区负载均衡，但不保证按分区顺序轮询。

注：生产者可以自动创建 topic，分区数为 /opt/module/kafka/config/server.properties 中定义的默认分区数。

## 2.3 消费数据

可以启动基于控制台的消费者，用于测试。

```bash
bin/kafka-console-consumer.sh --bootstrap-server hadoop104:9092 --topic hello
```

默认消费者启动后，只会从分区的最后的位置开始消费。如果是一个新的消费者组，添加--from beginning 可以从分区的最新位置消费：

```bash
bin/kafka-console-consumer.sh --bootstrap-server hadoop104:9092 --topic hello --from-beginning
```

注：kafka 会自动创建 __consumer_offsets 主题，用于存储offset信息。（新版本特性，旧版本是放在 zookeeper 集群上）

可以让多个消费者线程分配到一个组中，同一个组中的消费者线程，会共同消费同一个 topic。

```bash
bin/kafka-console-consumer.sh --bootstrap-server hadoop104:9092 --topic hello --consumer-property group.id=atguigu --consumer-property client.id=test1
```

```bash
bin/kafka-console-consumer.sh --bootstrap-server hadoop104:9092 --topic hello --consumer-property group.id=atguigu --consumer-property client.id=test2
```

注：同一组的不同消费者可以运行在不同节点上；消费者 id 可以重复，运行时不报错，分别在不同线程运行。

查看当前所有消费者组：

```bash
bin/kafka-consumer-groups.sh --bootstrap-server hadoop104:9092 --list
```

查看消费者组当前状态：

```bash
bin/kafka-consumer-groups.sh --bootstrap-server hadoop104:9092 --group atguigu --describe
```



## 2.4 存储机制

![image-20200325114249568](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200325114249568.png)

由于生产者生产的消息会不断追加到 log 文件末尾，为防止log文件过大导致数据定位效率低下，Kafka 采取了**分片**和**索引**机制，将每个 partition 分为多个 segment。

每个 partition(目录) 相当于一个巨型文件被平均分配到多个大小相等的 segment(段)数据文件中（每个 segment 文件中消息数量不一定相等），这种特性也方便 old segment 的删除，即方便已被消费的消息的清理，提高磁盘的利用率。

每个segment对应两个文件——“.index”文件和“.log”文件。分别表示为 segment 索引文件和数据文件（引入索引文件的目的就是便于利用二分查找快速定位 message 位置）。这两个文件的命令规则为：partition 全局的第一个 segment 从0开始，后续每个 segment 文件名为上一个 segment 文件最后一条消息的 offset 值，数值大小为64位，20位数字字符长度，没有数字用0填充。

这些文件位于一个文件夹下（partition目录），该文件夹的命名规则为：topic名称+分区序号。例如，first这个topic有三个分区，则其对应的文件夹为first-0,first-1,first-2。

```
00000000000000000000.index
00000000000000000000.log
00000000000000170410.index
00000000000000170410.log
00000000000000239430.index
00000000000000239430.log
```

# 3. 可靠性

## 3.1 副本相关的名词

R：Replicas（副本）

AR：avaliable replicas (可用副本)

ISR：insync replicas (同步的副本)

OSR：out of sync replicas (不同步的副本)

## 3.2 副本相关的概念

如果一个分区有多个副本，那么会从多个副本中选举一个作为 Leader，其余的作为 Follower。Follower 只负责从 Leader 同步数据。如果 Follower 可以及时地从 leader 机器同步数据，这台follower 就可以进入ISR；否则，属于OSR。

ISR 和 OSR 都由 leader 进行维护，用户可以设置一个 replica.lag.time.max.ms=10(默认)，符合这个标准的副本加入ISR，不符合的就加入OSR。

注：replica.lag.time.max.ms 代表每个副本向leader发送同步数据请求的延迟时间的最大值。

## 3.3 如何判断副本属于 OSR

在以下情况副本属于OSR：

① 当副本 (broker) 和 zookeeper 的上一次的通信时间距离现在已经过了 zookeeper.connection. timeout.ms=6000，此时集群会判断当前节点已经下线，下线的副本一定属于 OSR。

② 如果副本没有下线，假设副本和 leader 距离上次发送 fetch 请求已经超过了 replica.lag.time. max.ms，那么当前副本也会认为属于 OSR。

③ 如果副本没有下线，假设副本和leader距离上次发送fetch请求没有超过replica.lag.time.max. ms，但是无法同步最新的数据，此时副本也会认为属于 OSR

注：follower 和 consumer 向 leader 发送的拉取数据的请求都是同一种，follower 每次消费的 offset 也由 Leader 维护。

如果副本可以及时同步数据，那么也可以从 OSR 变为 ISR。

OSR + ISR = AR <= R

在 leader 发生故障时，集群总是从 ISR 列表中，选举一个成为新的 Leader。

## 3.4 生产者如何保证消息的安全性

producer 在发送数据时，可以设置 acks 参数，确保消息在何种情况下收到 brokder 的 ack 确认。acks 参数如下：

0：生成者无需等待 broker 的ack，效率最快，丢数据的风险最大。

1：leader 写完后，就返回 ack 确认消息。如果在返回 ack 确认消息之后挂掉，此时若 follower 尚未及时同步，那么选举的新的 leader 是不具有此条消息的，因此可能造成丢数据。

-1(all)：leader 及 ISR 中所有 follower 全部写完后，返回确认消息。不会丢数据，但是有可能造成数据的重复。



当 Leader 挂掉后，ISR中没用可用的副本，但是OSR中有可用的副本，此时是否会选举 OSR 中的副本作为 Leader ? 

OSR 是否可以选举为 Leader，取决于 unclean.leader.election.enable 参数的设置。

clean-elector：只会从ISR中选

unclean-elector：也会从OSR中选

此时如果 ISR 中只有一个副本可选，即使acks= -1，也可能会丢失数据，如何避免？

​		可以设置 min.insync.replicas=2，代表当ISR中的副本数满足此条件时，生产者才会向 broker 发送数据，否则会报错： NoEnoughReplicasExecption。

## 3.5 分布式系统对消费数据语义的支持

at most once：每条消息最多存一次，acks=0,1

at least once：每条消息最少存一次，acks=-1

exactly once：每条消息精准一次，enable.idempotence=true

如果要满足 exactly once，通常要求系统需要在设计时，提供幂等性功能。

幂等性：一个数字在执行任意次运算后的结果都是一样的，称为这个数字具有幂等性的特征。例如 1 的 N 次方都是 1，因此 1 具有幂等性。kafka 在 0.11 之前，无法满足 exactly once，在 0.11之后，提供了幂等性的设置，通过幂等性的设置，可以满足 exactly once。

令 enable.idempotence=true，kafka 首先会让 producer 自动将 acks=-1，再将 producer 端的retry 次数设置为 Long.MaxValue，再在集群上对每条消息进行标记去重。

去重： 在cluster端，对每个生产者线程生成的每条数据，都会添加以下的标识符： (producerid, partition, SequenceId)，通过标识符对数据进行去重。

# 4. 消费者消费的一致性

## 4.1 解释 1

![image-20200316202822164](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200316202822164.png)

LEO：log end offset。 指每个分布副本 log 文件最后的 offset 的值，即当前分区副本的最后一条记录的 offset。消费者提交的最新 offset 为其 offset + 1.

HW：high watermark，**ISR 中 LEO 最小的值**。

① HW 可以保证 consumer 在消费时，只有 HW 之前的数据可以消费，保证 leader 故障时，不会由于 leader 的更换造成消费数据的不一致。

② HW 还可以在 leader 重新选举时，使 ISR 中所有的 follower 都参照 HW，将之后多余的数据截掉，保持和 leader 的数据同步。

**注意：**对于从 OSR 切换到 ISR 的 blocker，会先截掉自己之前的 HW 后的数据，在与 leader 进行同步。

## 4.2 解释 2 

LEO 是下一个待写位，即 offset + 1。ISR 集合中最小的 LEO 即为分区的 HW，对消费者而言只能消费 HW 之前的消息。下图中日志文件的 HW 为6，表示消费者只能拉取 offset 在 0 到 5 之间的消息，offset 为6的消息对消费者而言是不可见的。

![image-20200326151540023](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200326151540023.png)

# 5. 消费者的分区

## 5.1 独立消费者

​		在启动消费者时，如果明确指定了要消费的主题、分区，以及消费的位置，此时启动的消费者，称为独立消费者，例如：

```bash
bin/kafka-console-consumer.sh --bootstrap-server hadoop104:9092 --topic test --partition 1
```

​		在启动消费者时，只指定了消费的主题，没有指定要消费哪个分区，这个消费者称为非独立消费者。

​		区别： 独立消费者在消费数据时，kafka集群不会帮消费者维护消费的Offset；查看消费者组的命令不会显示独立消费者。

## 5.2 消费者的分区

​		**再平衡**(rebalance):	一个非独立消费者组中如果新加入了消费者或有消费者挂掉，此时都会由系统自动再进行主题分区的分配，这个过程称为再平衡。

​		若创建的消费者是非独立消费者，此时会由 kafka 集群自动帮消费者组中的每个消费者分配要消费的分区自动分配时，有两种分配的策略：round_rabin(轮询分区) 和 range（范围分区）。

### 5.2.1 range(默认)

​		首先会统计一个消费者，一共订阅了哪些主题（一个消费者可以订阅多个主题）。之后以主题为单位，  计算主题的分区数 / 当前主题订阅的消费者个数 ，根据结果进行范围的分配。

举例：

```
有atguigu消费者组，组内有3个消费者[a,b,c]
a 订阅了 hello(0,1,2)
b 订阅了 hello(0,1,2)
c 订阅了 hello1(0,1,2)
```

此时分配的结果如下：

```
a 消费了 hello(0,1)
b 消费了 hello(2)
c 消费了 hello1(0,1,2)
```

问题：① 如果一个消费者订阅的主题越多，分配得到的分区越多。

​		   ② 如果出现同一个主题被多个消费者订阅，那么排名后靠前的消费者多分配分区，容易出现负载不均衡。

极端情况：

```
a 订阅了 hello(0,1,2)，hello1(0,1,2)，hello2(0,1,2)，hello3(0,1,2)
b 订阅了 hello(0,1,2)，hello1(0,1,2)，hello2(0,1,2)，hello3(0,1,2)
```

a比b多分配4个分区。

### 2.2.2 round_robin

首先会统计一个消费者组，一共订阅了哪些主题。之后以主题为单位，将主题的分区进行排序，排序后采取轮询的策略，将主题轮流分配到订阅这个主题的消费者上，如果出现组内有消费者没有定于这个主题，默认轮空(跳过)，继续轮询。一个主题分配完后继续分配下一个主题。

举例：

```
有atguigu消费者组，组内有3个消费者[a,b,c]
a 订阅了 hello(0,1,2)
b 订阅了 hello(0,1,2)
c 订阅了 hello1(0,1,2)
```

此时分配的结果如下：

```
a 消费了 hello(0,2)
b 消费了 hello(1)
c 消费了 hello1(0,1,2)
```

## 5.3 分区原则

producer 将发送的数据封装成一个**ProducerRecord**对象。


（1）ProducerRecord 对象中指明 partition 的情况下，直接将指明的值直接作为 partiton 值；

（2）没有指明 partition 值但有 key 的情况下，将 key **序列化后**的 hash 值与 topic 的 partition 数进行取余得到 partition 值；

（3）既没有 partition 值又没有 key 值的情况下，第一次调用时随机生成一个整数（后面每次调用在这个整数上自增），将这个值与 topic 可用的 partition 总数取余得到 partition 值，也就是上文中说的 round-robin 算法。

# 6. 拓展内容

## 6.1 kafka 高效的原因

### 1. 顺序写磁盘

​		Kafka 的 producer 生产数据，要写入到 log 文件中，写的过程是一直追加到文件末端，为顺序写。官网有数据表明，同样的磁盘，顺序写能到 600M/s，而随机写只有 100K/s。这与磁盘的机械机构有关，顺序写之所以快，是因为其省去了大量磁头寻址的时间。

### 2. 磁盘页缓存技术

​	现代操作系统中，可以把磁盘的一片区域当作临时的缓存使用。

### 3. 零拷贝技术

![image-20200319101329864](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200319101329864.png)

## 6.2 Zookeeper 在 kafka 中的作用

![image-20200316200150949](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200316200150949.png)

1. 选举 Controller。
2. 监控各 brocker 状态。
3. 选举 Leader。

# 7. JavaAPI

## 7.1 引入依赖

```xml
<dependencies>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-clients</artifactId>
            <version>0.11.0.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka_2.11</artifactId>
            <version>0.11.0.0</version>
        </dependency>
    </dependencies>
```

## 7.2 Producer

![image-20200316230937367](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200316230937367.png)

生产消息的一般流程：
	① 创建一个 KakfaProducer 的对象。
	② 调用 KakfaProducer.send(ProducerRecord)。
	③ 调用拦截器的 onsend() 对每一个记录进行拦截处理。
	④ 调用序列化器，对 key-value 进行序列化。
	⑤ 使用分区器计算分区。
	⑥ 添加其他的 header 或 ts 等。
	⑦ 将记录追加到缓冲区中，如果满足一个批次或新创建一个批次，通知 sender 线程发送数据。

### 7.2.1 简单的生产者

Producer 中所有的参数都已经提前汇总到 producerConfig。

```java
public static void main(String[] args) {
        //producer的配置信息
        Properties props = new Properties();
        // 服务器的地址和端口
        props.put("bootstrap.servers", "hadoop102:9092,hadoop103:9092,hadoop104:9092");
        // 接受服务端ack确认消息的参数，0,-1,1
        props.put("acks", "all");
        // 如果接受ack超时，重试的次数
        props.put("retries", 3);
        // sender一次从缓冲区中拿一批的数据量
        props.put("batch.size", 16384);
        // 如果缓冲区中的数据不满足batch.size，只要和上次发送间隔了linger.ms也会执行一次发送
        props.put("linger.ms", 1);
        // 缓存区的大小
        props.put("buffer.memory", 33554432);
        //配置生产者使用的key-value的序列化器
        props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // <key,value>，泛型，必须要和序列化器所匹配
        Producer<Integer, String> producer = new KafkaProducer<>(props);

        for (int i = 0; i < 10; i++){
        producer.send(new ProducerRecord<Integer, String>("test1", i, "atguigu"+i));
        }

        producer.close();
    }
```

### 7.2.2 带回调的异步发送

```java
//异步带回调的发送
//只需在 producer.send() 方法中传入 Callback() 实例
            producer.send(new ProducerRecord<Integer, String>("test2", i, "atguigu" + i), new Callback() {
              //  一旦发送的消息被server通知了ack，此时会执行onCompletion()
                // RecordMetadata: 当前record生产到broker上对应的元数据信息
                // Exception： 如果发送失败，会将异常封装到exception返回
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    //没有异常
                    if (exception==null){
                        //查看数据的元数据信息
                        System.out.println("partition:"+metadata.topic()+"-"+metadata.partition()+",offset:"+
                                metadata.offset());
                    }
                }
            });
```

### 2.3 同步发送

```java
// 使用 producer.send(new ProducerRecord()).get() 方法阻塞当前线程，直至获得返回值。
RecordMetadata result=producer.send(new ProducerRecord()).get();
```

## 7.3 自定义分区器

① 编写分区器

```java
public class MyPartitioner implements Partitioner {

    // 为每个ProduceRecord计算分区号
    // 根据key的hashCode() % 分区数
    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        //获取主题的分区数
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);

        int numPartitions = partitions.size();

        return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
    }

    // Producer执行close()方法时调用
    @Override
    public void close() {

    }

    // 从Producer的配置文件中读取参数,在partition之前调用
    @Override
    public void configure(Map<String, ?> configs) {

        System.out.println(configs.get("welcomeinfo"));

    }
}
```

② 在producer中设置

```java
props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG,"com.atguigu.kafka.custom.MyPartitioner");
```

## 7.4 自定义拦截器

① 自定义拦截器

```java
// 实现时间戳拦截器
public class TimeStampInterceptor implements ProducerInterceptor<Integer,String> {

    //发送数据之前拦截数据
    @Override
    public ProducerRecord<Integer, String> onSend(ProducerRecord<Integer, String> record) {

        String newValue=System.currentTimeMillis()+"|"+record.value();

        return new ProducerRecord<Integer, String>(record.topic(),record.key(),newValue);
    }

    //当拦截器收到此条消息的ack时，会自动调用onAcknowledgement()
    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {

    }

    // Producer关闭时，调用拦截器的close()
    @Override
    public void close() {

    }

    //读取Producer中的配置
    @Override
    public void configure(Map<String, ?> configs) {

    }
}
```

```java
// 打印生产成功的消息数和生产失败的消息数
public class CounterInterceptor implements ProducerInterceptor<Integer,String> {
    private int successCount;
    private int faildCount;

    //拦截数据
    @Override
    public ProducerRecord<Integer, String> onSend(ProducerRecord<Integer, String> record) {

        return record;
    }

    //当拦截器收到此条消息的ack时，会自动调用onAcknowledgement()
    @Override
    public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
        if (exception==null){
            successCount++;
        }else{
            faildCount++;
        }
    }

    // Producer关闭时，调用拦截器的close()
    @Override
    public void close() {
        System.out.println("faildcount:"+faildCount);
        System.out.println("SuccessCount:"+successCount);
    }

    //读取Producer中的配置
    @Override
    public void configure(Map<String, ?> configs) {
    }
}
```

②设置

```java
		//拦截器链
        ArrayList<String> interCeptors = new ArrayList<>();

        // 添加的是全类名，注意顺序，先添加的会先执行
        interCeptors.add("com.atguigu.kafka.custom.TimeStampInterceptor");
        interCeptors.add("com.atguigu.kafka.custom.CounterInterceptor");
         //设置拦截器
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,interCeptors);
```

注：可以设置多个拦截器，作为一个拦截器链依次执行。



## 7.5 Consumer

### 7.5.1 普通消费者

```java
public class MyConsumer {

    public static void main(String[] args) {

        // consumer的配置
        Properties props = new Properties();
        // 连接的集群地址
        props.put("bootstrap.servers", "hadoop102:9092,hadoop103:9092,hadoop104:9092");
        // 消费者组id
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test1");
        // 消费者id
        props.put("client.id", "test01");
        //控制从主题的哪个位置开始消费,只有是一个从未提交过offset的组才可以
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        // 允许在消费完数据后，自动提交offset
        props.put("enable.auto.commit", "false");
        // 每次自动提交offset的间隔时间
        props.put("auto.commit.interval.ms", "1000");
        // key-value的反序列化器，必须根据分区存储的数据类型，选择合适的反序列化器
        props.put("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        //基于配置创建消费者对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        //订阅主题
        consumer.subscribe(Arrays.asList("hello"));
        //消费数据，采取poll的方式主动去集群拉取数据
        while (true) {
            //每次poll，拉取一批数据，如果当前没有可用的数据，就休息timeout单位时间
            ConsumerRecords<String, String> records = consumer.poll(100);
            //遍历数据，执行真正的消费逻辑
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
            
            //手动同步提交  等offset提交完成后，再继续运行代码
            //consumer.commitSync();
            //手动异步提交
            consumer.commitAsync();
        }
    }
}
```

### 7.5.2 独立消费者

```java
public class MyDependentConsumer {
    
    public static void main(String[] args) {

        // consumer的配置
        Properties props = new Properties();
        // 连接的集群地址
        props.put("bootstrap.servers", "hadoop102:9092,hadoop103:9092,hadoop104:9092");
        // 消费者组id
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test2");

        // 消费者id
        props.put("client.id", "test01");
        // 允许在消费完数据后，自动提交offset,独立消费者的offset不由kafka维护
        props.put("enable.auto.commit", "true");
        // 每次自动提交offset的间隔时间
        props.put("auto.commit.interval.ms", "1000");
        // key-value的反序列化器，必须根据分区存储的数据类型，选择合适的反序列化器
        props.put("key.deserializer", "org.apache.kafka.common.serialization.IntegerDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        //基于配置创建消费者对象
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        //要订阅的分区
        List<TopicPartition> partitions=new ArrayList<>();

        TopicPartition tp1 = new TopicPartition("hello", 0);
        TopicPartition tp2 = new TopicPartition("hello", 1);

        partitions.add(tp1);
        partitions.add(tp2);

        //分配主题和分区
        consumer.assign(partitions);

        //指定offset
        consumer.seek(tp1,20);
        consumer.seek(tp2,30);

        //消费数据，采取poll的方式主动去集群拉取数据
        while (true) {
            //每次poll，拉取一批数据，如果当前没有可用的数据，就休息timeout单位时间
            ConsumerRecords<String, String> records = consumer.poll(100);
            //遍历数据，执行真正的消费逻辑
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
            }
        }
    }
}
```

# 8. KafkaSink

## 8.1 介绍

​		KafkaSink 本质就是一个生产者，负责将 flume channel 中的数据，生产到 kafka 集群的 topic 中。类似地还有 KafkaChannel、KafkaSource。

必须属性：

| **type**                    | –                   | `org.apache.flume.sink.kafka.KafkaSink`                      |
| --------------------------- | ------------------- | ------------------------------------------------------------ |
| **kafka.bootstrap.servers** | –                   | 集群地址                                                     |
| kafka.topic                 | default-flume-topic | 向哪个主题生成                                               |
| flumeBatchSize              | 100                 | 一批数据量                                                   |
| kafka.producer.acks         | 1                   | acks                                                         |
| **useFlumeEventFormat**     | **false**           | **默认将 event body 的字节数组直接放入 kafka 队列。置为 true 时会像 Flume Avro 一样放置 event，用于和KafkaSource、Kafka Channel 配合使用的场合，使用时需将 parseAsFlumeEvent 配为相同配置，以保留 event head**. |

useFlumeEventFormat = false 时，将 flume 每个 event 中 body 的内容直接写到 kafka。

useFlumeEventFormat=true 时，存储的 event 就默认为 flume 的 avro 格式。在生成时，会将flume 的 event 的 header 内容也存入 kafka。

使用 true 的时机：需要和 KafkaSource 及 Kafka Channel 的 parseAsFlumeEvent 一起使用，配套为 true 或者 false。

## 8.2 配置文件

netcatsource -- memerychannel -- kafkasink

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = netcat
a1.sources.r1.bind = hadoop103
a1.sources.r1.port = 44444
a1.sources.r1.interceptors = i1 i2
a1.sources.r1.interceptors.i1.type = static
a1.sources.r1.interceptors.i1.key = topic
a1.sources.r1.interceptors.i1.value = hello
a1.sources.r1.interceptors.i2.type = static
a1.sources.r1.interceptors.i2.key = key
a1.sources.r1.interceptors.i2.value = 1

# 配置sink
a1.sinks.k1.type = org.apache.flume.sink.kafka.KafkaSink
a1.sinks.k1.kafka.bootstrap.servers=hadoop102:9092,hadoop103:9092,hadoop104:9092
a1.sinks.k1.kafka.topic=test3
a1.sinks.k1.useFlumeEventFormat=false

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

## 8.3 启动命令

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/kafkaagent.conf -Dflume.root.logger=DEBUG,console
```

# 9. kafka 监控

## 9.1 kafka manager

可以在可视化界面下实现 kafka/ bin 目录下的所有脚本功能。

1. 解压

```bash
unzip kafka-manager-1.3.3.15.zip -d /opt/module/
```

2. 修改 /opt/module/kafka-manager-1.3.3.15/conf/application.conf 中的这一行：

```properties
kafka-manager.zkhosts="hadoop102:2181,hadoop103:2181,hadoop104:2181"
```

3. 修改权限并启动。
4. 登录 hadoop102:9000 页面查看详细信息。

## 9.2 KafkaOffsetMonitor

实时监控所有 consumer 的 offset。

1. 上传jar包KafkaOffsetMonitor-assembly-0.4.6.jar到集群

2. 在/opt/module/下创建kafka-offset-console文件夹

3. 将上传的jar包放入刚创建的目录下

4. 在/opt/module/kafka-offset-console目录下创建启动脚本start.sh，内容如下：

```bash
\#!/bin/bash
java -cp KafkaOffsetMonitor-assembly-0.4.6.jar \
com.quantifind.kafka.offsetapp.OffsetGetterWeb \
--offsetStorage kafka \
--kafkaBrokers hadoop102:9092,hadoop103:9092,hadoop104:9092 \
--kafkaSecurityProtocol PLAINTEXT \
--zk hadoop102:2181,hadoop103:2181,hadoop104:2181 \
--port 8086 \
--refresh 10.seconds \
--retain 2.days \
--dbName offsetapp_kafka &
```

5. 在 /opt/module/kafka-offset-console 目录下创建 mobile-logs 文件夹

```bash
mkdir /opt/module/kafka-offset-console/mobile-logs
```

6. 启动 KafkaMonitor

```bash
./start.sh
```

7. 登录页面 hadoop103:8086 端口查看详情