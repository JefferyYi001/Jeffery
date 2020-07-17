# 1. Flume简介

## 1. 1 简介

​		Flume是一个针对日志数据进行高效收集、聚合和传输的框架。

## 1.2 基本单元

​		Flume是Java编写，因此在使用Flume时需要启动一个Java进程，这个进程在 Flume 中称为Agent。一个 Agent 包含 source、sink、channel 三部分。

![image-20200315004924608](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200315004924608.png)

## 1.3 核心概念

​		**Agent**：一个 Flume java 进程。

​		**Source**：负责对接数据源，数据源是不同的类型就使用不同的 source。

​		**Sink**：负责将读取到的数据写出到指定的目的地，写出的目的地不同就使用不同的 sink。

​		**Channel**：source 和 sink 之间的缓冲。

​		**Event**：flume 中数据传输的最基本单位，包含一个 header(map) 和 body(byte[])。

​		**Interceptors**：拦截器是在 source 向 Channel 写入 event 的途中，对 event 进行拦截处理。

​		**Channel Selectors**：当一个source对接多个Channel，会使用选择器选取合适的 channel。

​		**Sink Processors**：适用于从一个sink组中，挑选一个sink去channel读取数据。

![image-20200325104412150](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200325104412150.png)

## 1.4 基本常识

一个 Source 可以对接多个 Channel；一个 Channel 也可以对接多个 Sink；

一个 Channel 只能对接一个 Source；一个 Sink 也只能对接一个 Channel。

## 1.5 安装

版本选择：目前使用的较多的是 flume ng 1.7版本。

由于 Flume 使用 JAVA 编写，需要配置 JAVA_HOME 环境变量，之后解压即可。

验证：

```shell
bin/flume-ng version
```

## 1.6 使用

① Setting up an agent

​		准备一个 agent 的配置文件，这个配置文件遵循 java 的 properties 文件格式。

​		这个文件中可以配置一个或多个 agent，并且还会配置这些 agent 使用的组件的各种属性，以及这些组件如何组合连接，构成数据流。

② Configuring individual components

​		在 agent 的配置文件中，独立地配置每个需要的组件的属性等。

③ Wiring the pieces together

​		将组件组合在一起

④ Starting an agent

```bash
$ bin/flume-ng agent -n $agent_name -c conf -f conf/flume-conf.properties.template
```

-n： agent的名称，需要和配置文件中agent的名称一致

-c :   flume全局配置文件的目录

-f:   agent的配置文件

# 2. 案例1：网络端口 -> 控制台

## 2.1 要求

要求： 监听某台机器的指定端口，将收到的信息，输出到控制台。

## 2.2 所需组件

### 2.1.1 NetCat TCP Source

​		工作原理非常类似 nc -k -l [host] [port]，这个 source 可以打开一个端口号，监听端口号收到的消息，将消息的每行，封装为一个 event。

安装NC：

```powershell
sudo yum -y install nc
```

nc -k -l [host] [port]的运行：

在hadoop102执行：

```bash
nc -k -l hadoop102 4444
```

在hadoop103向hadoop102:4444发送tcp请求

```bash
nc hadoop102 4444
```

## 2.3 配置说明

### 2.3.1 source

| **type** | –    | 组件名称，必须为 `netcat` |
| -------- | ---- | ------------------------- |
| **bind** | –    | 要绑定的 ip 地址或主机名  |
| **port** | –    | 要绑定的端口号            |

### 2.3.2 Logger Sink

​		采用 logger 以 info 级别将 event 输出到指定的路径（文件或控制台）。

| **type**              | –    | 必须是 `logger`                                  |
| --------------------- | ---- | ------------------------------------------------ |
| maxBytesToLog(已无效) | 16   | Maximum number of bytes of the Event body to log |

### 2.3.3 Memory Channel

​		将 event 存储在内存中的队列中。一般适用于高吞吐量的场景，但是如果 agent 故障，会损失阶段性的数据。

| **type** | –    | The component type name, needs to be `memory` |
| -------- | ---- | --------------------------------------------- |
| capacity | 100  | 存放 event 的容量限制                         |

## 2.4 配置文件

```properties
# 命名每个组件 a1代表agent的名称 
#a1.sources代表a1中配置的source,多个使用空格间隔
#a1.sinks代表a1中配置的sink,多个使用空格间隔
#a1.channels代表a1中配置的channel,多个使用空格间隔
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = netcat
a1.sources.r1.bind = hadoop103
a1.sources.r1.port = 44444

# 配置sink
a1.sinks.k1.type = logger
a1.sinks.k1.maxBytesToLog = 100

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

## 2.5 启动命令

```shell
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/netcatSource-loggersink.conf -Dflume.root.logger=DEBUG,console
```

后台运行，将原本在 console 的输出定向到 flume 目录下的 nohup.out 文件中：

```bash
nohup bin/flume-ng agent -c conf/ -n a1 -f flumeagents/netcatSource-loggersink.conf -Dflume.root.logger=DEBUG,console &
```

将原本在 console 的输出的内容打印到日志中：

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/netcatSource-loggersink.conf
```

日志的存放位置可以在 flume/conf/log4j.properties 中进行配置，默认存放在 flume/logs 目录下：

```properties
#flume.root.logger=DEBUG,console
flume.root.logger=INFO,LOGFILE
flume.log.dir=./logs
flume.log.file=flume.log
```



# 3. 案例2：实时日志 -> HDFS

## 3.1 要求

实时监控Hive日志，并上传到HDFS中

## 3.2 所需组件

### 3.2.1 Exec Source

​		Exec Source在启动后执行一个linux命令，期望这个命令可以持续地在标注输出中产生内容。一旦命令停止了，进程也就停止了，因此像 cat / tail -f 这些可以产生持续数据的命令是合适的，而像 date 这些只能产生一条信息，之后就结束的命令，是不适合的。

| **type**    | –    | `exec`       |
| ----------- | ---- | ------------ |
| **command** | –    | 要执行的命令 |

ExecSource存在的问题：

​			和其他的异步source一样，ExecSource无法保证在出现故障时，可以将event放入 channel，并通知客户端。异步Source在异常情况下，如果无法把从客户端读取的event进行缓存的话，是有丢失数据的风险的。因此建议使用 Spooling Directory Source, Taildir Source来替换ExecSource。

### 3.2.2 HDFS Sink

​		HDFS Sink负责将数据写到HDFS。

- 目前支持创建 text 和 SequnceFile 文件。
- 以上两种文件格式，都可以使用压缩。
- 文件可以基于时间周期性滚动或基于文件大小滚动或基于 events 的数量滚动。
- 可以根据数据产生的时间戳或主机名对数据进行分桶或分区。
- 上传的路径名可以包含格式化的转义序列，转义序列会在文件/目录真正上传时被替换。
- 如果要使用这个 sink，必须已经按照了 hadoop，这样 flume 才能使用 Jar 包和 hdfs 通信。

必配属性：

| **type**      | –    | `hdfs`       |
| ------------- | ---- | ------------ |
| **hdfs.path** | –    | 上传的路径名 |

可选属性：

配置文件的滚动策略：0都代表禁用。

| hdfs.rollInterval | 30   | 每间隔多少秒滚动一次文件          |
| ----------------- | ---- | --------------------------------- |
| hdfs.rollSize     | 1024 | 文件一旦达到多少bytes就触发滚动   |
| hdfs.rollCount    | 10   | 文件一旦写入多少个event就触发滚动 |

配置文件的类型和压缩类型。

| hdfs.codeC    | –            | 支持的压缩类型：gzip, bzip2, lzo, lzop, snappy               |
| ------------- | ------------ | ------------------------------------------------------------ |
| hdfs.fileType | SequenceFile | 文件格式，当前支持： `SequenceFile`, `DataStream` or `CompressedStream`。DataStream 代表不使用压缩也就是纯文本CompressedStream 代表使用压缩 |

配置目录的滚动策略。

| hdfs.round      | false  | 代表时间戳是否需要向下舍去，如果为true，会影响所有的基于时间的转义序列，除了%t |
| --------------- | ------ | ------------------------------------------------------------ |
| hdfs.roundValue | 1      | 将时间戳向下舍到离此值最高倍数的一个时间，小于等于当前时间   |
| hdfs.roundUnit  | second | 时间单位 - `second`, `minute` or `hour`.                     |

**最关键的属性**

| hdfs.useLocalTimeStamp | false | 使用flume进程所在的本地时间，替换event header中的timestamp属性，替换后，用来影响转义序列 |
| ---------------------- | ----- | ------------------------------------------------------------ |
|                        |       |                                                              |

注意： 所有和时间相关的转义序列，都要求 event 的 header 中有 timestamp 的属性名，值为时间戳。除非配置了 hdfs.useLocalTimeStamp=true，此时会使用服务器的本地时间，来生成时间戳，替换header中的timestamp属性。或者可以使用 TimestampInterceptor 生成时间戳的 key。

## 3.3 配置文件

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = exec
a1.sources.r1.command=tail -f /tmp/jeffery/hive.log


# 配置sink
a1.sinks.k1.type = hdfs
a1.sinks.k1.hdfs.path = hdfs://hadoop102:9000/flume/%Y%m%d/%H%M
#上传文件的前缀
a1.sinks.k1.hdfs.filePrefix = logs-
# 滚动目录 一分钟滚动一次目录
a1.sinks.k1.hdfs.round = true
a1.sinks.k1.hdfs.roundValue = 1
a1.sinks.k1.hdfs.roundUnit = minute
# 是否使用本地时间戳
a1.sinks.k1.hdfs.useLocalTimeStamp = true
# 配置文件滚动
a1.sinks.k1.hdfs.rollInterval = 30
a1.sinks.k1.hdfs.rollSize = 134217700
a1.sinks.k1.hdfs.rollCount = 0
# 使用文件格式存储数据
a1.sinks.k1.hdfs.fileType=DataStream 

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000


# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

## 3.4 启动命令

```shell
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/execsource-hdfssink.conf -Dflume.root.logger=INFO,console
```

# 4. 案例3：离线日志 -> HDFS

## 4.1 要求

​		监控目录中新增的日志文件的内容，上传到HDFS。

## 4.2 所需组件

### 4.2.1 SpoolingDirSource

​		适用于：已经在一个目录中生成了大量的离线日志，且日志不会再进行写入和修改的场合。

​		SpoolingDirSource 在监控一个目录中新放入的文件的数据，一旦发现就数据封装为event。在目录中，已经传输完成的数据，会使用重命名或删除来标识这些文件已经传输完成。

​		SpoolingDirSource 要求放入目录的文件必须是一成不变（不能修改）的，且不能重名。

​		一旦发现放入的文件，又发生了写操作，或重名，agent 就会故障停机。

必须配置：

| **type**     | –    | `spooldir`. |
| ------------ | ---- | ----------- |
| **spoolDir** | –    | 监控的目录  |

可选配置：

| fileSuffix    | .COMPLETED | 为已经读完的文件标识后缀                                |
| ------------- | ---------- | ------------------------------------------------------- |
| deletePolicy  | never      | 文件读完后，是立刻删除还是不删除 `never` or `immediate` |
| fileHeader    | false      | 是否在header中存放文件的绝对路径属性                    |
| fileHeaderKey | file       | 存放的绝对路径的key                                     |

## 4.3 配置文件

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = spooldir
a1.sources.r1.spoolDir=/home/jeffery/flume

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

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000


# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

## 4.4 启动命令

```shell
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/execsource-hdfssink.conf -Dflume.root.logger=INFO,console
```

# 5. 案例4：多实时日志 -> HDFS

## 5.1 需求

​		实时监控多个文件，避免使用 ExecSouce，使用 LoggerSink 输出到控制台。

## 5.2 所需组件

### 5.2.1 TailDirSource

​		TailDirSource 以接近实时的速度监控文件中写入的新行，并且将每个文件 tail 的位置记录在一个 JSON 的文件中；即便 agent 挂掉，重启后，source 依然可以从上次记录的位置继续执行 tail 操作。用户可以通过修改 Position 文件的参数，来改变source继续读取的位置；如果 postion 文件丢失了，那么 source 会重新从每个文件的第一行开始读取(重复读)。

必须配置：

| **type**                          | –    | `TAILDIR`.                     |
| --------------------------------- | ---- | ------------------------------ |
| **filegroups**                    | –    | 组名                           |
| **filegroups.filegroup.filename** | –    | 一个组中可以配置多个文件的路径 |

可选参数

|              |                       |
| ------------ | --------------------- |
| positionFile | 存放postionfile的路径 |

## 5.3 配置文件

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = TAILDIR
a1.sources.r1.filegroups = f1 f2
a1.sources.r1.filegroups.f1 = /home/jeffery/a.txt
a1.sources.r1.filegroups.f2 = /home/jeffery/b.txt
a1.sources.r1.positionFile=/home/jeffery/taildir_position.json
# 配置sink
a1.sinks.k1.type = logger

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

## 5.4 启动命令

```shell
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/taildirSource-loggersink.conf -Dflume.root.logger=INFO,console
```



# 6. 复杂案例1：实时日志 -> 本地 + HDFS

## 6.1 需求

Agent1：execsource--2个memoeyChannel（放入相同的数据）-----2Avrosink

Agent2：AvroSource----memoeyChannel---hdfssink

Agent3：AvroSource----memoeyChannel---FileRollssink

## 6.2 所需组件

​		Avro Sink 和 Avro Source 是搭配使用的。

### 6.2.1 Avro Sink

​		Avro sink将event以 avro 序列化的格式发送到另外一台机器的指定进程。

| **type**     | –    | `avro`.            |
| ------------ | ---- | ------------------ |
| **hostname** | –    | source绑定的主机名 |
| **port**     | –    | 绑定的端口号       |

### 6.2.2 Avro Source

​		source 读取 avro 格式的数据，反序列化为 event 对象。启动Avro Source时，会自动绑定一个RPC端口，这个端口可以接受Avro Sink发送的数据。

| **type** | –    | `avro`               |
| -------- | ---- | -------------------- |
| **bind** | –    | 绑定的主机名或ip地址 |
| **port** | –    | 绑定的端口号         |

### 6.2.3 File Roll Sink

​		将event写入到本地磁盘。数据在写入到目录后，会自动进行滚动文件。

| **type**           | –    | `file_roll`.   |
| ------------------ | ---- | -------------- |
| **sink.directory** | –    | 数据写入到目录 |

可选：

sink.rollInterval=30，每间隔多久滚动一次。

## 6.3 配置案例

### 6.3.1 Agent3

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = avro
a1.sources.r1.bind = hadoop104
a1.sources.r1.port = 1234

# 配置sink
a1.sinks.k1.type = file_roll
a1.sinks.k1.sink.directory=/home/jeffery/flume
a1.sinks.k1.sink.rollInterval=600

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

### 启动命令（hadoop104 - agent3先起）

```shell
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/example1-agent3.conf -Dflume.root.logger=INFO,console
```



### 6.3.2 Agent2

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = avro
a1.sources.r1.bind = hadoop102
a1.sources.r1.port = 12345

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

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

### 启动命令（hadoop102）

```shell
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/example1-agent2.conf -Dflume.root.logger=INFO,console
```



### 6.3.3 Agent1

```properties
a1.sources = r1
a1.sinks = k1 k2
a1.channels = c1 c2

#指定使用复制的channel选择器，此选择器会选中所有的channel,每个channel复制一个event,可以省略，默认
#a1.sources.r1.selector.type = replicating
# 配置source
a1.sources.r1.type = exec
a1.sources.r1.command=tail -f /tmp/jeffery/hive.log

# 配置sink
a1.sinks.k1.type = avro
a1.sinks.k1.hostname=hadoop102
a1.sinks.k1.port=12345

a1.sinks.k2.type = avro
a1.sinks.k2.hostname=hadoop104
a1.sinks.k2.port=1234

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

a1.channels.c2.type = memory
a1.channels.c2.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1 c2
a1.sinks.k1.channel = c1
a1.sinks.k2.channel = c2
```

### 启动命令（hadoop103）

```shell
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/example1-agent1.conf -Dflume.root.logger=INFO,console
```



# 7. 复杂案例2：Sink Processor 应用

## 7.1 需求

​		SinkProcessor 的应用场景就是多个 sink 同时从一个 channel 拉取数据。

Agent1：netcatsource----memorychannel-----2AvroSink (hadoop102)

Agent2：ArvoSource----memorychannel-----loggersink (103)

Agent3：ArvoSource----memorychannel-----loggersink (104)

## 7.2 所需组件

### 7.2.1 Default Sink Processor

​		若 agent 中只有一个 sink，此时默认使用 Default Sink Processor，不强制用户显式地配置 Sink Processor 和 sink 组。

### 7.2.2 Failover Sink Processor

​		Failover Sink Processor：故障转移的sink处理器。这个sink处理器会维护一组有优先级的sink，默认挑选优先级最高（数值越大）的sink来处理数据。故障的sink会放入池中冷却一段时间，恢复后，重新加入到存活的池中，此时在 live pool(存活的池) 中选优先级最高的接替工作。

配置：

| **sinks**                       | –         | 空格分割的，多个sink组成的集合 |
| ------------------------------- | --------- | ------------------------------ |
| **processor.type**              | `default` | `failover`                     |
| **processor.priority.**sinkName | –         | 优先级                         |

示例：

```properties
a1.sinkgroups = g1
a1.sinkgroups.g1.sinks = k1 k2
a1.sinkgroups.g1.processor.type = failover
a1.sinkgroups.g1.processor.priority.k1 = 5
a1.sinkgroups.g1.processor.priority.k2 = 10
a1.sinkgroups.g1.processor.maxpenalty = 10000
```

### 7.2.3 Load balancing Sink Processor

​		Load balancing Sink Processor：使用 round_robin` or `random 两种算法，让多个激活的 sink 间的负载均衡(多个sink轮流干活)。

配置：

| **processor.sinks** | –             | 空格分割的，多个sink组成的集合                               |
| ------------------- | ------------- | ------------------------------------------------------------ |
| **processor.type**  | `default`     | `load_balance`                                               |
| processor.selector  | `round_robin` | `round_robin`, `random` or FQCN of custom class that inherits from `AbstractSinkSelector` |

```properties
a1.sinkgroups = g1
a1.sinkgroups.g1.sinks = k1 k2
a1.sinkgroups.g1.processor.type = load_balance
a1.sinkgroups.g1.processor.backoff = true
a1.sinkgroups.g1.processor.selector = random
```



## 7.3 故障转移案例

### 7.3.1 Agent1

netcatsource----memorychannel-----2AvroSink (hadoop102)

```properties
a1.sources = r1
a1.sinks = k1 k2
a1.channels = c1

# 配置source
a1.sources.r1.type = netcat
a1.sources.r1.bind = hadoop102
a1.sources.r1.port = 44444

# 配置sink组
a1.sinkgroups = g1
a1.sinkgroups.g1.sinks = k1 k2
a1.sinkgroups.g1.processor.type = failover
a1.sinkgroups.g1.processor.priority.k1 = 5
a1.sinkgroups.g1.processor.priority.k2 = 10
a1.sinkgroups.g1.processor.maxpenalty = 10000

# 配置sink
a1.sinks.k1.type = avro
a1.sinks.k1.hostname=hadoop103
a1.sinks.k1.port=12345

a1.sinks.k2.type = avro
a1.sinks.k2.hostname=hadoop104
a1.sinks.k2.port=1234

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
a1.sinks.k2.channel = c1
```

### 启动命令

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/example2-agent1.conf -Dflume.root.logger=INFO,console
```



### 7.3.2 Agent2

Agent2： ArvoSource-----memorychannel-----loggersink (103)

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = avro
a1.sources.r1.bind = hadoop103
a1.sources.r1.port = 12345

# 配置sink
a1.sinks.k1.type = logger

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

### 	启动命令

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/example2-agent2.conf -Dflume.root.logger=INFO,console
```



### 7.3.3 Agent3

Agent3： ArvoSource----memorychannel-----loggersink (104)

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = avro
a1.sources.r1.bind = hadoop104
a1.sources.r1.port = 1234

# 配置sink
a1.sinks.k1.type = logger

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

### 	启动命令

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/example2-agent3.conf -Dflume.root.logger=INFO,console
```



## 7.4 负载均衡案例

将 agent1 的 sink processor 进行修改即可，其他配置不变。

```properties
a1.sources = r1
a1.sinks = k1 k2
a1.channels = c1

# 配置source
a1.sources.r1.type = netcat
a1.sources.r1.bind = hadoop102
a1.sources.r1.port = 44444

#配置sink组
a1.sinkgroups = g1
a1.sinkgroups.g1.sinks = k1 k2
a1.sinkgroups.g1.processor.type = load_balance
a1.sinkgroups.g1.processor.selector = random


# 配置sink
a1.sinks.k1.type = avro
a1.sinks.k1.hostname=hadoop103
a1.sinks.k1.port=12345

a1.sinks.k2.type = avro
a1.sinks.k2.hostname=hadoop104
a1.sinks.k2.port=1234

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
a1.sinks.k2.channel = c1
```

### 	启动命令

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/example2-agent4.conf -Dflume.root.logger=INFO,console
```

# 8. 复杂案例3：interceptor + Multiplexing Channel Selector  应用

## 8.1 需求

案例主要介绍Multiplexing Channel Selector的使用。

在102机器：

（1）agent1（netcatsource---memorychannel---avrosink）

（2）agent2（execsource---memorychannel---avrosink）

在103机器：

agent3( avrosouce---- 2 memorychannel---2sink(loggersink,hdfssink))

其中，loggersink只写出来自agent1的数据；hdfssink只写出来自agent2的数据。

![image-20200314225229560](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200314225229560.png)

## 8.2 所需组件

### 8.2.1 Multiplexing Channel Selector

Multiplexing Channel Selector： 将event分类到不同的channel。

如何分类：  固定根据配置读取 event header 中指定 key 的 value，根据 value 的映射，分配到不同的channel。

配置：

| selector.type      | replicating           | `multiplexing`                |
| ------------------ | --------------------- | ----------------------------- |
| selector.header    | flume.selector.header | 默认读取event中的header的名称 |
| selector.default   | –                     | 默认分配到哪个channel         |
| selector.mapping.* | –                     | 自定义的映射规则              |

示例：

```properties
a1.sources = r1
a1.channels = c1 c2 c3 c4
a1.sources.r1.selector.type = multiplexing
a1.sources.r1.selector.header = state
a1.sources.r1.selector.mapping.CZ = c1
a1.sources.r1.selector.mapping.US = c2 c3
a1.sources.r1.selector.default = c4
```

### 8.2.2 Static Interceptor

Static Interceptor允许用户向 event 添加一个静态的 key-value。

| **type**         | –     | `static`                                                     |
| ---------------- | ----- | ------------------------------------------------------------ |
| preserveExisting | true  | If configured header already exists, should it be preserved - true or false |
| key              | key   | key的名称                                                    |
| value            | value | value的值                                                    |

示例：

```properties
a1.sources = r1
a1.channels = c1
a1.sources.r1.channels =  c1
a1.sources.r1.type = seq
a1.sources.r1.interceptors = i1
a1.sources.r1.interceptors.i1.type = static
a1.sources.r1.interceptors.i1.key = datacenter
a1.sources.r1.interceptors.i1.value = NEW_YORK
```

## 8.3 配置文件

### 8.3.1 agent1（hadoop102）

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = netcat
a1.sources.r1.bind = hadoop102
a1.sources.r1.port = 44444

# 配置拦截器
# 拦截器 i1 用于本案例演示
# 拦截器 i2 用于演示时间戳 timestamp
a1.sources.r1.interceptors = i1 i2
a1.sources.r1.interceptors.i1.type = static
a1.sources.r1.interceptors.i1.key = mykey
a1.sources.r1.interceptors.i1.value = agent1
a1.sources.r1.interceptors.i2.type = timestamp

# 配置sink
a1.sinks.k1.type = avro
a1.sinks.k1.hostname=hadoop103
a1.sinks.k1.port=12345

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

### 	启动命令（先启动agent3）

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/example3-agent1.conf -Dflume.root.logger=INFO,console
```



### 8.3.2 agent2（hadoop102）

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 配置source
a1.sources.r1.type = exec
a1.sources.r1.command=tail -f /home/jeffery/hello.txt

#配置拦截器
a1.sources.r1.interceptors = i1
a1.sources.r1.interceptors.i1.type = static
a1.sources.r1.interceptors.i1.key = mykey
a1.sources.r1.interceptors.i1.value = agent2

# 配置sink
a1.sinks.k1.type = avro
a1.sinks.k1.hostname=hadoop103
a1.sinks.k1.port=12345

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

### 	启动命令（先启动agent3）

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/example3-agent2.conf -Dflume.root.logger=INFO,console
```



### 8.3.3 agent3（hadoop103）

```properties
a1.sources = r1
a1.sinks = k1 k2
a1.channels = c1 c2

# 配置source
a1.sources.r1.type = avro
a1.sources.r1.bind = hadoop103
a1.sources.r1.port = 12345

# 配置 channel 选择器
a1.sources.r1.selector.type = multiplexing
a1.sources.r1.selector.header = mykey
a1.sources.r1.selector.mapping.agent1 = c2
a1.sources.r1.selector.mapping.agent2 = c1

# 配置sink
a1.sinks.k2.type = logger

# 配置sink
a1.sinks.k1.type = hdfs
a1.sinks.k1.hdfs.path = hdfs://hadoop103:9000/flume/%Y%m%d/%H%M
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

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

a1.channels.c2.type = memory
a1.channels.c2.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1 c2
a1.sinks.k1.channel = c1
a1.sinks.k2.channel = c2
```

### 	启动命令（先启动agent3）

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/example3-agent3.conf -Dflume.root.logger=INFO,console
```

# 9. 事务

## 9.1 概念

### 9.1.1 事务的介绍

​		flume 中的事务和关系型数据库中的事务是不同的，只是名称一样。

​		Flume 中的事务代表的是一批要以**原子性**写入到 channel 或要从 channel 中移除的 events。

​		Flume 在设计时，采取的是 at least once 语义，因此在没有故障时，flume只会写入一次数据。但是，如果遇到 sink 端超时或写入文件系统失败等异常，flume可能采用重试机制，直到全部写入成功，因此可能会产生重复的数据。

​		如果数据重复，若数据不敏感，重复无所谓，如果对数据敏感，要求数据最好有一个唯一的 id 字段，在具体使用数据时，进行去重。

补充：消息队列或传输框架的设计语义

at least once：至少一次，在异常时有可能出现数据重复。

at most once：最多一次，在异常时有可能出现数据丢失。

exactly once：精准一次，不管是什么情况，数据只精准一次，不会重复，也不会丢失。

#### 9.1.1.1 put 事务

​		put 事务指 source 将封装好的 event 交给 ChannelProcessor，ChannelProcessor 在处理这批 events 时，先把 events 放入到 putList 中，放入完成后，一次性 commit()，这批 events 就可以成功写入到 channel，写入成功后执行清空 putList 操作；如果在过程中，发生任何异常，此时执行 rollback() 回滚 putList，回滚也会直接清空 putList。

#### 9.1.1.2 take 事务

​		take 事务指 sink 不断从 channel 中获取 event，每获取一批 event 中的一个，都会将这个 event 放入 takeList 中。一般一批event全部写入，执行 commit() 方法清空 takeList。如果在此期间，发生了异常，执行rollback()，此时会回滚 takeList 中的这批 event 到 channel。

### 9.1.2 事务的特点

​		事务的实现由 channel 提供，source 和 sink 在 put 和 take 数据时，只是先获取 channel 中已经定义好的事务。不同的 channel 的事务可能实现方式是不同的，但是原理和目的是一样的，都是为了 put 和 take 一批 events 的原子性。put 事务不需要 source 操作，而是由 ChannelProcessor 进行操作。

### 9.1.3 take 事务的数据重复情况

当前是 sink 的运行逻辑

```java
// 开启事务
		Transaction t=sink.getChannel().getTransaction();
		t.begin();
// sink需要从channel中取event
// 此时每次take，都会返回channel中的一个event，返回的event会移动到takeList(缓冲区)
try{
		Event e =sink.take();
    	//...连续取五次
    	wirteToHDFS(List<Event> events);
    	//如果写结束，提交事务,清空takeList中的一批events
    	t.commit()
	}catch(Exception e){
    	//回滚事务,将takeList中一批event移动回channel
    	t.rollback()
}finally{
    	//关闭此次事务
    	t.close();  
}
```

​		假如一个事务中，一部分 event 已经写入到目的地，但是随着事务的回滚，这些 event 可能重复写入。

### 9.1.4 put 事务的数据重复的情况

```java
// 开启事务
		Transaction t=source.getChannel().getTransaction;
		t.begin();
//souce一次放入一批数据
try{
		List<event> events=xxx;
		for(Event e: events){
            //向事务的缓冲区中放入event
            putList.add(e);
        }
		//放完后提交事务
		tx.commit()

}catch(Exeption e){
    	//回滚事务,清空putList
    	tx.rollback();
}finally{
    	tx.close()
}         
```

​		如果一个 source 对接多个 channel，可能出现一批数据中某些 channel put 完了，但是另一些失败了。此时 channel Processor 会重试对此批数据的 put，包括之前已经写成功的 channel。此时，之前写成功的 channel 就会出现重复。

### 9.1.5 事务中的数量关系

batchSize：每个 source 和 sink 都可以配置 batchSize，batchSize 代表一批数据的数量。

transcationCapacity：putList 和 takeList 的大小（在channel的参数中配置）。

capacity：指 channel 中存储 event 的容量。

**batchSize <= transcationCapacity <= capacity**

# 10. 自定义组件

## 10.1 source

### 10.1.1 原理

​		每次Agent启动后，会调用**PollableSourceRunner.start()**，开启一个PollableSourceRunner线程。这个线程会初始化 PollableSource 对象，可以轮询地去读取数据源中的数据。

​		PollableSource 由所在的 PollingRunner 线程控制，调用 PollableSource 的 process() 方法，来探测是否有新的数据，将新的数据封装为 event，存储到 channel 中。

### 10.1.2 自定义

```
MySource extends AbstractSource implements Configurable, PollableSource
```

### 10.1.3 实现

添加依赖：

```xml
 <dependencies>
        <!-- https://mvnrepository.com/artifact/org.apache.flume/flume-ng-core -->
        <dependency>
            <groupId>org.apache.flume</groupId>
            <artifactId>flume-ng-core</artifactId>
            <version>1.7.0</version>
        </dependency>

    </dependencies>
```

需实现 process() 方法，将定义好的 event（一般是批量）使用 ChannelProcessor 放入 channel。可通过 configure(Context context) 方法从 context 中获取配置文件属性。

编辑配置文件：

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 自定义source，type必须是类的全类名
a1.sources.r1.type = com.jeffery.flume.custom.MySource
a1.sources.r1.name = jeffery:

# 配置sink
a1.sinks.k1.type = logger

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

编译之后的 jar 包放到 /opt/module/flume/lib 目录下。

### 启动命令

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/customized_agent1.conf -Dflume.root.logger=DEBUG,console
```



## 10.2 interceptor

​		实现类似于时间戳拦截器的效果。官网中没有看到相关案例，所以参照 Staticinterceptor 进行实现。

```bash
public class MyInterceptor implements Interceptor
```

需实现 intercept() 方法，获取 event 并进行逻辑处理，之后将处理后的 event return。需实现处理单个 event 的 intercept() 方法和处理批量 event 的 intercept() 方法。常规操作是实现处理单个 event 的 intercept() 方法，在处理批量 event 的 intercept() 方法中调用前者即可。

另外，拦截器初始化时会调用一次 initialize() 方法，关闭时会调用一次 close() 方法。需要特别说明的是，还需要定义静态内部类 Builder，通过成员方法 build() 返回一个 Interceptor 实例，还可以通过 configure(Context context) 方法从 context 中获取配置文件属性。

## 10.3 Sink

### 10.3.1 原理

​		每个Sink都由一个 SinkRunner 线程负责调用其 process() 方法，完成从 channel 抽取数据，存储到外部设备的逻辑。

### 10.3.2 自定义

```
public class MySink extends AbstractSink implements Configurable
```

需实现 process() 方法，从 channel 中 take 出一个个 event，并使用 event 的 getHeaders() 、getBody() 方法获取 event 中的值，进行相应的处理。可通过 configure() 方法从 context 中获取配置文件属性。需要特别注意的是，自定义 Sink 需要从 channel 中获取 transaction 并执行事务。

### 10.3.3 配置文件

```properties
a1.sources = r1
a1.sinks = k1
a1.channels = c1

# 自定义source，type必须是类的全类名
a1.sources.r1.type = com.jeffery.flume.custom.MySource
a1.sources.r1.name = jeffery:

#为source添加拦截器
a1.sources.r1.interceptors = i1
#type必须写Bulider的全类名
a1.sources.r1.interceptors.i1.type = com.jeffery.flume.custom.MyInterceptor$Builder

# 配置sink
a1.sinks.k1.type = com.jeffery.flume.custom.MySink
a1.sinks.k1.prefix = ***jeffery:
a1.sinks.k1.suffix = :go!

# 配置channel
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000

# 绑定和连接组件
a1.sources.r1.channels = c1
a1.sinks.k1.channel = c1
```

### 启动命令

```bash
bin/flume-ng agent -c conf/ -n a1 -f flumeagents/customized_agent2.conf -Dflume.root.logger=DEBUG,console
```



# 11. 监控

## 11.1 需求

​		在监控 flume 时，我们的需求是获取：

- channel 当前的容量是多少？
- channel 已经用了多少容量？
- source 向 channel 写了多少个 event ？
- source 向 channel 尝试写了多少次 ？
- sink 从 channel 读了多少个 event ?
- sink 尝试读了多少次 ？



## 11.2 原理

JMX： 针对Java应用的一种实时监控。改变Java程序的一些参数的技术。

JMX（java monitor extension）: java的监控扩展模块。

JMX 中的核心概念：

① MBean(monitor bean)：需要将监控的参数封装到一个bean对象中，这个 bean 称为 MBean。

② JMX的monitor服务：实现了JMX规范的服务可以在程序中接收获取 MBean 请求，处理请求。

flume 由 java 编写，已经提供了基于JMX的服务实现，内置 MBean。用户唯一需要做的就是启动JMX服务，使用工具，或写代码，向JMX的monitor服务发送请求，获取其中的 MBean。



## 11.3 获取 MBean 的客户端

#### 11.3.1 基于 JCONSOLE 查看 MBean

① 开启基于报告的JMX服务

在 conf/flume.env.sh 中进行配置：

```shell
export JAVA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=5445 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
```

② 运行agent

③ 打开 jconsole，连接 jmx

#### 11.3.2 基于JSON的监控

① 开启基于JSON的监控

在conf/flume.env.sh中进行配置：

```shell
export JAVA_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=5445 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
-Dflume.monitoring.type=http -Dflume.monitoring.port=34545"
```

② 开启 agent

③ 使用浏览器发送 http 请求

#### 11.3.3 总结

① 基于 JMX 提供一个可以访问 MBean 的监控服务（flume 已经提供）。

② 开启基于 JMX 的监控服务。

③ 使用客户端发送请求获取 MBean 的信息。

​	主流公司会使用 JMXTrans 或 Metrics 向 JMX 服务发请求，请求MBean。

④ 对获取的 MBean的 信息进行可视化

​	将获取的信息存入到 InfluxDB（时序数据库）中，在使用第三方的可视化框架(grafana)从数据库按照时间顺序读取数据显示。

⑤ 主流配置：

JMXTRANS + INFLUXDB + GRAFANA

