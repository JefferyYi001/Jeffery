# 一 Sources

##### NetCat Source

```
NetCat Source：绑定的端口（tcp、udp），将流经端口的每一个文本行数据作为Event输入；
```

**必须配置:**

| **type** | –    | 组件名称，source的类型必须为 `netcat`                        |
| -------- | ---- | ------------------------------------------------------------ |
| **bind** | –    | **要绑定的 ip地址或主机名此监听不是过滤发送方。一台电脑不是说只有一个IP。有多网卡的电脑，对应多个IP。** |
| **port** | –    | **要绑定的本地端口号**                                       |

```properties
# 命名每个组件 a1代表agent的名称 
#a1.sources代表a1中配置的source,多个使用空格间隔
a1.sources = r1

# 配置source
a1.sources.r1.type = netcat
a1.sources.r1.bind = hadoop103
a1.sources.r1.port = 44444
```

#####  Avro Source

```
		source读取avro格式的数据，反序列化为event对象！启动Avro Source时，会自动绑定一个RPC的端口！
```

**必须配置:**

| **type** | –    | `avro`                                                 |
| -------- | ---- | ------------------------------------------------------ |
| **bind** | –    | **绑定的主机名或ip地址**                               |
| **port** | –    | **绑定的端口号,这个端口可以接受Avro Sink发送的数据！** |

```properties
a1.sources = r1

# 配置source
a1.sources.r1.type = avro
a1.sources.r1.bind = hadoop104
# 绑定的端口号
a1.sources.r1.port = 1234
```

##### Exec Source

```
Exec Source在启动后执行一个linux命令，期望这个命令可以持续地在标注输出中产生内容！

一旦命令停止了，进程也就停止了！因此类似cat ，tail -f 这些可以产生持续数据的命令是合适的，而类似date这些只能产生一条信息，之后就结束的命令，是不适合的！

​		ExecSource的问题：

​			和其他的异步source一样，ExecSource无法保证在出现故障时，可以将event放入 channel，且通知客户端！

​			异步Source在异常情况下，如果无法把从客户端读取的event进行缓存的话，是有丢失数据的风险的！

​			建议使用 Spooling Directory Source, Taildir Source来替换ExecSource!
```



**必须配置:**

| **type**    | –    | `exec`           |
| ----------- | ---- | ---------------- |
| **command** | –    | **要执行的命令** |

```properties
a1.sources = r1


# 配置source
a1.sources.r1.type = exec
a1.sources.r1.command=tail -F /opt/module/hive/logs/hive.log
```



##### Spooling Directory Source

```
		适用于：已经在一个目录中生成了大量的离线日志，且日志不会再进行写入和修改！

​		spooling(自动收集)。

​		SpoolingDirSource在监控一个目录中新放入的文件的数据，一旦发现，就将数据封装为event! 在目录中，已经传输完成的数据，会使用重命名或删除来标识这些文件已经传输完成！

​		SpoolingDirSource要求放入目录的文件必须是一成不变（不能修改）的，且不能重名！

​		一旦发现放入的文件，又发生了写操作，或重名，agent就会故障！
```

**必须配置：**

| **type**     | –    | `spooldir`     |
| ------------ | ---- | -------------- |
| **spoolDir** | –    | **监控的目录** |

可选配置：

| fileSuffix    | .COMPLETED | 为已经读完的文件标识后缀                                |
| ------------- | ---------- | ------------------------------------------------------- |
| deletePolicy  | never      | 文件读完后，是立刻删除还是不删除 `never` or `immediate` |
| fileHeader    | false      | 是否在header中存放文件的绝对路径属性                    |
| fileHeaderKey | file       | 存放的绝对路径的key                                     |

```properties
a1.sources = r1

# 配置source
a1.sources.r1.type = spooldir
a1.sources.r1.spoolDir=/home/atguigu/flume
```

##### TailDirSource

```
		TailDirSource以接近实时的速度监控文件中写入的新行！

​		TailDirSource检测文件中写入的新行，并且将每个文件tail的位置记录在一个JSON的文件中！

​		即便agent挂掉，重启后，source从上次记录的位置继续执行tail操作！

​		用户可以通过修改Position文件的参数，来改变source继续读取的位置！如果postion文件丢失了，那么source会重新从每个文件的第一行开始读取(重复读)！
```

**必须配置：**

| **type**                          | –    | `TAILDIR`.                         |
| --------------------------------- | ---- | ---------------------------------- |
| **filegroups**                    | –    | **组名**                           |
| **filegroups.filegroup.filename** | –    | **一个组中可以配置多个文件的路径** |

可选配置:

| positionFlile | 存放postionFile的路径 |
| ------------- | --------------------- |
|               |                       |

```properties
a1.sources = r1

# 配置source
a1.sources.r1.type = TAILDIR
a1.sources.r1.filegroups = f1 f2
a1.sources.r1.filegroups.f1 = /home/atguigu/a.txt
a1.sources.r1.filegroups.f2 = /home/atguigu/b.txt
# 可选配置
a1.sources.r1.positionFile=/home/atguigu/taildir_position.json
```





# 二 Sinks

#####  Logger Sink

```
		采用 logger以info级别将event输出到指定的路径（文件或控制台）。
```

**必须配置:**

| **type**                | –      | **必须是 `logger`**                                  |
| ----------------------- | ------ | ---------------------------------------------------- |
| **maxBytesToLog(无效)** | **16** | **Maximum number of bytes of the Event body to log** |

```properties
#a1.sinks代表a1中配置的sink,多个使用空格间隔
a1.sinks = k1

# 配置sink
a1.sinks.k1.type = logger
a1.sinks.k1.maxBytesToLog=100
```

##### HDFS Sink

```
		HDFS Sink负责将数据写到HDFS。

- 目前支持创建 text和SequnceFile文件！
- 以上两种文件格式，都可以使用压缩
- 文件可以基于时间周期性滚动或基于文件大小滚动或基于events的数量滚动
- 可以根据数据产生的时间戳或主机名对数据进行分桶或分区
- 上传的路径名可以包含 格式化的转义序列，转义序列会在文件/目录真正上传时被替换
- 如果要使用这个sink，必须已经安装了hadoop，这样flume才能使用Jar包和hdfs通信

注意： 所有和时间相关的转义序列，都要求event的header中有timestamp的属性名，值为时间戳！

​			除非配置了hdfs.useLocalTimeStamp=true，此时会使用服务器的本地时间，来生成时间戳，替换header中的timestamp属性。或者，可以使用TimestampInterceptor生成时间戳的key!
```

**必须配置:**

| **type**      | –    | `hdfs`           |
| ------------- | ---- | ---------------- |
| **hdfs.path** | –    | **上传的路径名** |

可选配置: 

配置文件的滚动策略：0都代表禁用！

| hdfs.rollInterval | 30   | 每间隔多少秒滚动一次文件          |
| ----------------- | ---- | --------------------------------- |
| hdfs.rollSize     | 1024 | 文件一旦达到多少bytes就触发滚动   |
| hdfs.rollCount    | 10   | 文件一旦写入多少个event就触发滚动 |

配置文件的类型和压缩类型：

| hdfs.codeC    | –            | 支持的压缩类型：gzip, bzip2, lzo, lzop, snappy               |
| ------------- | ------------ | ------------------------------------------------------------ |
| hdfs.fileType | SequenceFile | 文件格式，当前支持： `SequenceFile`, `DataStream` or `CompressedStream`。DataStream 代表不使用压缩也就是纯文本CompressedStream 代表使用压缩 |

配置目录的滚动策略：

| hdfs.round      | false  | 代表时间戳是否需要向下舍去，如果为true，会影响所有的基于时间的转义序列，除了%t |
| --------------- | ------ | ------------------------------------------------------------ |
| hdfs.roundValue | 1      | 将时间戳向下舍到离此值最高倍数的一个时间，小于等于当前时间   |
| hdfs.roundUnit  | second | 时间单位 - `second`, `minute` or `hour`.                     |

最关键的属性：

| hdfs.useLocalTimeStamp | false | 使用flume进程所在的本地时间，替换event header中的timestamp属性，替换后，用来影响转义序列 |
| ---------------------- | ----- | ------------------------------------------------------------ |
|                        |       |                                                              |

```properties
a1.sinks = k1

# 配置sink
a1.sinks.k1.type = hdfs
a1.sinks.k1.hdfs.path = hdfs://hadoop102:9000/flume/%Y%m%d/%H%M
#上传文件的前缀
a1.sinks.k1.hdfs.filePrefix = logs-
#滚动目录 一分钟滚动一次目录
a1.sinks.k1.hdfs.round = true
a1.sinks.k1.hdfs.roundValue = 1
a1.sinks.k1.hdfs.roundUnit = minute
#是否使用本地时间戳
a1.sinks.k1.hdfs.useLocalTimeStamp = true
#配置文件滚动
a1.sinks.k1.hdfs.rollInterval = 30
a1.sinks.k1.hdfs.rollSize = 134217700
a1.sinks.k1.hdfs.rollCount = 0
#使用文件格式存储数据
a1.sinks.k1.hdfs.fileType=DataStream 
```

##### Avro Sink

```
		sink将event以avro序列化的格式发送到另外一台机器的指定进程
```

**必须配置:**

| **type**     | –    | `avro`                       |
| ------------ | ---- | ---------------------------- |
| **hostname** | –    | **传输到source绑定的主机名** |
| **port**     | –    | **绑定的端口号**             |

```properties
#多个sink以空格间隔
a1.sinks = k1 k2

# 配置sink
a1.sinks.k1.type = avro
a1.sinks.k1.hostname=hadoop103
a1.sinks.k1.port=12345

a1.sinks.k2.type = avro
a1.sinks.k2.hostname=hadoop104
a1.sinks.k2.port=1234
```

##### File Roll Sink

```
		将event写入到本地磁盘！数据在写入到目录后，会自动进行滚动文件！
```

**必须配置:**

| **type**           | –    | `file_roll`.       |
| ------------------ | ---- | ------------------ |
| **sink.directory** | –    | **数据写入到目录** |

可选配置:

sink.rollInterval=30，每间隔多久滚动一次！

```properties
a1.sinks = k1

# 配置sink
a1.sinks.k1.type = file_roll
a1.sinks.k1.sink.directory=/home/atguigu/flume
a1.sinks.k1.sink.rollInterval=600
```

##  三 Sink Processor

##### 1.Default Sink Processor

```
agent中只有一个sink，此时就使用Default Sink Processor，不强制用户来配置Sink Processor和sink组！
```



##### 2. Failover Sink Processor

```
		Failover Sink Processor：故障转移的sink处理器！ 
		这个sink处理器会维护一组有优先级的sink！默认挑选优先级最高（数值越大）的sink来处理数据！故障的sink会放入池中冷却一段时间，恢复后，重新加入到存活的池中，此时在live pool(存活的池)中选优先级最高的，接替工作！
```

**必须配置:**

| **sinks**                       | –           | 空格分割的，多个sink组成的集合 |
| ------------------------------- | ----------- | ------------------------------ |
| **processor.type**              | **default** | **`failover`**                 |
| **processor.priority.sinkName** | –           | **优先级**                     |

```properties
a1.sinkgroups = g1
a1.sinkgroups.g1.sinks = k1 k2
a1.sinkgroups.g1.processor.type = failover
a1.sinkgroups.g1.processor.priority.k1 = 5
a1.sinkgroups.g1.processor.priority.k2 = 10
# 故障的节点最大的黑名单时间(毫秒)
a1.sinkgroups.g1.processor.maxpenalty = 10000
```



##### 3. Load balancing Sink Processor

```
		Load balancing Sink Processor: 使用round_robin or random两种算法，让多个激活的sink间的负载均衡(多个sink轮流干活)！
```

**必须配置:**

| **processor.sinks**    | –               | 空格分割的，多个sink组成的集合                               |
| ---------------------- | --------------- | ------------------------------------------------------------ |
| **processor.type**     | **default**     | **load_balance**                                             |
| **processor.selector** | **round_robin** | ' **round_robin '， ' random '或继承自' AbstractSinkSelector '的自定义类的FQCN** |

```properties
a1.sinkgroups = g1

a1.sinkgroups.g1.sinks = k1 k2
a1.sinkgroups.g1.processor.type = load_balance
a1.sinkgroups.g1.processor.backoff = true
a1.sinkgroups.g1.processor.selector = random

# 扩展
a1.sinkgroups.g1.processor.backoff=true
a1.sinkgroups.g1.processor.selector.maxTimeOut=30000
```

```
扩展说明:
backoff：开启后，故障的节点会列入黑名单，过一定时间再次发送，如果还失败，则等待是指数增长；直到达到最大的时间。

如果不开启，故障的节点每次都会被重试。

selector.maxTimeOut：最大的黑名单时间（单位为毫秒）。
```



# 四 channel

##### Memory Channel

```
将event存储在内存中的队列中！一般适用于高吞吐量的场景，但是如果agent故障，会损失阶段性的数据！
```

**必须配置:**

| **type**     | –       | `memory`                |
| ------------ | ------- | ----------------------- |
| **capacity** | **100** | **存放event的容量限制** |

```properties
#a1.channels代表a1中配置的channel,多个使用空格间隔
a1.channels = c1

# 配置channel

# 使用内存作为缓存
a1.channels.c1.type = memory
# 最多缓存的Event个数
a1.channels.c1.capacity = 1000
# 单次传输的Event个数
a1.channels.c1.transactionCapacity = 100
```



##### File Channel

```
	用文件作为数据的存储
```

**必须配置:**

| **type**          | –    | file                                   |
| ----------------- | ---- | -------------------------------------- |
| **checkpointDir** |      | **检查点的数据存储目录(提前创建目录)** |
| **dataDirs**      |      | **数据的存储目录(提前创建目录)**       |

```properties
a1.channels = c1

# 配置channel
a1.channels.c1.type = file
a1.channels.c1.checkpointDir=/home/atguigu/flume/checkpoint
a1.channels.c1.dataDirs=/home/atguigu/flume/data
```

##### Spillable Memory Channel

```
	事件被存储在内存缓存中或者磁盘上，内存缓存作为主要存储而磁盘则是接收溢出时的事件将其存储到磁盘上。磁盘存储是由一个嵌入的File Channel来管理的。当内存缓存队列满了的时候，额外的event将被存储到File Channel。这样当agent 崩溃或者机器死机 存放在磁盘上的数据将能够被恢复，从而减少数据丢失
	This channel is currently experimental and not recommended for use in production
	- 该通道目前处于试验阶段，不建议在生产中使用。
```



| 属性名                       | 默认                  | 描述                                                         |
| ---------------------------- | --------------------- | ------------------------------------------------------------ |
| **type**                     | –                     | 组件名 ，固定值 SPILLABLEMEMORY                              |
| memoryCapacity               | 10000                 | 设定memory channel队列存储事件的最大值，如果要禁用， 将该值设置为0 |
| overflowCapacity             | 100000000             | 设定file channel中存储事件的最大值，如果要禁用， 将该值设置为0 |
| overflowTimeout              | 3                     | 内存channel满了之后，切换到file channel之前的等待时间        |
| byteCapacityBufferPercentage | 20                    | 用来限制内存channel使用物理内存量，默认20                    |
| byteCapacity                 | 进程中可用堆空间的80% | channel允许使用的最大的堆空间                                |
| avgEventSize                 | 500                   | 指定每个event的大小，用来计算内存channel可以使用的slot总数量，会把event量化为slot，而不是字节，默认500 |



# 五 Channel Selector

##### Replicating Channel Selector (default)

```
如果未指定类型，则默认为“复制”。
```

| 配置          | Default     | 描述                       |
| :------------ | :---------- | :------------------------- |
| selector.type | replicating | 组件类型名称 `replicating` |

```properties
a1.sources = r1
a1.channels = c1 c2 c3
a1.sources.r1.selector.type = replicating
a1.sources.r1.channels = c1 c2 c3

# Set of channels to be marked as optional 通道组标记为可选
a1.sources.r1.selector.optional = c3
```

##### Multiplexing Channel Selector

```
Multiplexing Channe Selector 的作用就是根据 Event 的 Header 中的某个或几个字段的值将其映射到指定的 Channel ，便于之后 Channel Processor 将Event发送至对应的Channel中去。在Flume中，Multiplexing Channel Selector一般都与 Interceptor 拦截器搭配使用，因为新鲜的Event数据中Header为空，需要Interceptor去填充所需字段
```



| Property Name      | Default               | Description                                         |
| :----------------- | :-------------------- | :-------------------------------------------------- |
| selector.type      | replicating           | The component type name, needs to be `multiplexing` |
| selector.header    | flume.selector.header |                                                     |
| selector.default   | –                     |                                                     |
| selector.mapping.* | –                     |                                                     |

```properties
a1.sources = r1
a1.channels = c1 c2 c3 c4
a1.sources.r1.selector.type = multiplexing
# 根据不同的heder当中state值走不同的channels，如果是CZ就走c1 如果是US就走c2 c3 其他默认走c4
a1.sources.r1.selector.header = state
a1.sources.r1.selector.mapping.CZ = c1
a1.sources.r1.selector.mapping.US = c2 c3
a1.sources.r1.selector.default = c4
```

示例

**flume1**

```properties
# flume1:此配置用于监控单个或多个指定文件将其追加内容生成的Event先通过自定义的TypeInterceptor
# 根据Body中的内容向其Header中添加type字段,然后使用Multiplexing Channel Selector将不同
# type的Event传输到不同的Channel中,最后分别输出到flume2和flume3的控制台
# a1:TailDir Source-> TypeInterceptor -> Multiplexing Channel Selector ->
#   Memory Channel -> Avro Sink

# Agent
a1.sources = r1
a1.channels = c1 c2
a1.sinks = k1 k2

# Sources
# a1.sources.r1
a1.sources.r1.type = TAILDIR
# 设置Json文件存储路径(最好使用绝对路径)
# 用于记录文件inode/文件的绝对路径/每个文件的最后读取位置等信息
a1.sources.r1.positionFile = /opt/module/flume-1.8.0/.position/taildir_position.json
# 指定监控的文件组
a1.sources.r1.filegroups = f1
# 配置文件组中的被监控文件
# 设置f2组的监控文件,注意:使用的是正则表达式,而不是Linux通配符
a1.sources.r1.filegroups.f1 = /tmp/logs/^.*log$

# Interceptor
# a1.sources.r1.interceptors
# 配置Interceptor链,Interceptor调用顺序与配置循序相同
a1.sources.r1.interceptors = typeInterceptor
# 指定使用的自定义Interceptor全类名,并使用其中的静态内部类Builder
# 要想使用自定义Interceptor,必须将实现的类打包成jar包放入$FLUME_HOME/lib文件夹中
# flume运行Java程序时会将此路径加入到ClassPath中
a1.sources.r1.interceptors.typeInterceptor.type = com.tomandersen.interceptors.TypeInterceptor$Builder

# Channels
# a1.channels.c1
# 使用内存作为缓存/最多缓存的Event个数/单次传输的Event个数
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 100
# a1.channels.c2
a1.channels.c2.type = memory
a1.channels.c2.capacity = 1000
a1.channels.c2.transactionCapacity = 100

# Channel Selector
# a1.sources.r1.selector
# 使用Multiple Channel Selector
a1.sources.r1.selector.type = multiplexing
# 设置匹配Header的字段
a1.sources.r1.selector.header = type
# 设置不同字段的值映射至各个Channel,其余的Event默认丢弃
a1.sources.r1.selector.mapping.Startup = c1
a1.sources.r1.selector.mapping.Event = c2

# Sinks
# a1.sinks.k1
a1.sinks.k1.type = avro
a1.sinks.k1.hostname = hadoop102
a1.sinks.k1.port = 4141
# a1.sinks.k2
a1.sinks.k2.type = avro
a1.sinks.k2.hostname = hadoop103
a1.sinks.k2.port = 4141

# Bind
# r1->TypeInterceptor->Multiplexing Channel Selector->c1->k1
# r1->TypeInterceptor->Multiplexing Channel Selector->c2->k2
a1.sources.r1.channels = c1 c2
a1.sinks.k1.channel = c1
a1.sinks.k2.channel = c2


```

**flume2**

```properties
# flume2:此配置用于将来自指定Avro端口的数据输出到控制台中
# a2:Avro Source->Memory Channel->Logger Sink

# Agent
a2.sources = r1
a2.channels = c1
a2.sinks = k1

# Sources
a2.sources.r1.type = avro
a2.sources.r1.bind = 0.0.0.0
a2.sources.r1.port = 4141

# Channels
a2.channels.c1.type = memory
a2.channels.c1.capacity = 1000
a2.channels.c1.transactionCapacity = 100

# Sinks
# 运行时设置参数 -Dflume.root.logger=INFO,console 即输出到控制台实时显示
a2.sinks.k1.type = logger
# 设置Event的Body中写入log的最大字节数(默认值为16)
a2.sinks.k1.maxBytesToLog = 256

# Bind
r1->c1->k1
a2.sources.r1.channels = c1
a2.sinks.k1.channel = c1

```

**flume3**

```properties
# flume3:此配置用于将来自指定Avro端口的数据输出到控制台中
# a3:Avro Source->Memory Channel->Logger Sink

# Agent
a3.sources = r1
a3.channels = c1
a3.sinks = k1

# Sources
a3.sources.r1.type = avro
a3.sources.r1.bind = 0.0.0.0
a3.sources.r1.port = 4141

# Channels
a3.channels.c1.type = memory
a3.channels.c1.capacity = 1000
a3.channels.c1.transactionCapacity = 100

# Sinks
# 运行时设置参数 -Dflume.root.logger=INFO,console 即输出到控制台实时显示
a3.sinks.k1.type = logger
# 设置Event的Body中写入log的最大字节数(默认值为16)
a3.sinks.k1.maxBytesToLog = 256

# Bind
r1->c1->k1
a3.sources.r1.channels = c1
a3.sinks.k1.channel = c1

```

**启动命令**

```
./bin/flume-ng agent -n a1 -c conf -f flume1.properties
./bin/flume-ng agent -n a2 -c conf -f flume2.properties -Dflume.root.logger=INFO,console
./bin/flume-ng agent -n a3 -c conf -f flume3.properties -Dflume.root.logger=INFO,console
```

