# 1. HBase 简介

## 1.1 NoSQL

- NoSQL 之前指不使用 SQL 标准的数据库，现在泛指非关系型数据库。
- NoSQL 是为了解决关系型数据库在数据量过大时的性能下降而兴起的。
- NoSQL 数据库一般都是分布式数据库。
- NoSQL 数据库一般都复合 CAP 定理。
- CAP 定理指强一致性、高可用性、强分区容错性最多只能取其二，无法三者兼顾。

特点：  ① 分布式。② 性能快。③ 不支持SQL。

NoSQL 和关系型数据库是相辅相成的，谁也无法替代谁。

## 1.2 HBase

### 1.2.1 简介

​	Hbase 的本质是一个 NoSQL 数据库。

​	HBase 是 Google 开源的 BigTable 论文的 Java 实现。

​	HBase 需要依赖于 Hadoop。

​	HBase 的目标是为大表(十亿级行*百万级列)中的数据提供实时、随机的读写访问。

​	HBase 是开源的，分布式的，基于版本号的分布式数据库。

​	HBase 需要基于 Hadoop 和 HDFS。

### 1.2.2 随机读写

​	HDFS 只支持追加写，不支持随机写。

​	HBase 基于 HDFS，hbase 的数据由 hdfs 进行读写，hbase 支持随机写。

​	随机写操作： update + delete

​		① 可以借助  insert + 时间戳(版本号)。

​		② 只允许客户端返回时间戳最新(版本号最大)的数据。



### 1.2.3 实时读写

如何做到海量数据的实时读写

① 分布式。

② 将数据不管是读还是写都放入内存。

③ 索引，LSM 树。

④ kv 存储。

⑤ 列式存储(HFile)。

⑥ 布隆过滤器(查询)。



## 1.3 安装

### 1.3.1 环境要求

首先确保配置了 JAVA_HOME 环境变量。

hbase 基于 hadoop 的 hdfs，要求必须已经安装了 hadoop，确保配置了 HADOOP_HOME 环境变量，之后启动 HDFS。

hbase 依赖 zk 存储一些表的元数据，要求必须已经安装了 zk，之后启动 zk。

### 1.3.2 修改配置

① 编辑 hbase-env.sh，注释掉46，47行。

② 设置 hbase 不使用内置的 zk 集群，128行，编辑。

```properties
export HBASE_MANAGES_ZK=false
```

③ 编辑 hbase-site.xml

```xml
<property>
                <name>hbase.rootdir</name>
                <value>hdfs://hadoop103:9000/HBase</value>
        </property>

        <property>
                <name>hbase.cluster.distributed</name>
                <value>true</value>
        </property>

        <property>
                <name>hbase.zookeeper.quorum</name>
             <value>hadoop102:2181,hadoop103:2181,hadoop104:2181</value>
        </property>

```

④ 配置环境变量

在 /etc/profile 中增加以下内容：

```properties
# HBASE_HOME
export HBASE_HOME=/opt/module/hbase-1.3.1
export PATH=$PATH:$HBASE_HOME/bin
```

之后使用同步脚本分发配置。

### 1.3.3 启动和停止

#### 1.3.3.1 单点启动

```bash
# 启动regionserver
hbase-daemon.sh start regionserver
# 启动master
hbase-daemon.sh start master
```

查看：

① jps查看。

② 访问 web 页面查看。

```
master 所在机器的主机名:16010
```

#### 1.3.3.2 群起

借助 hbase-daemons.sh，群起 regionserver，需要先配置，要在哪些机器上群起 regionserver。

① 配置 conf/regionservers

```
hadoop102
hadoop103
hadoop104
```

② 可以执行群起命令

```bash
hbase-daemons.sh start regionserver
```

或直接使用

```bash
start-hbase.sh
stop-hbase.sh
```

群起命令 start-hbase.sh 和 stop-hbase.sh 也需要在 regionservers 配置完成后才可以使用。

### 1.3.4 常见的端口

| 进程         | 协议 | 端口  |
| ------------ | ---- | ----- |
| master       | rpc  | 16000 |
| master       | http | 16010 |
| regionserver | rpc  | 16020 |
| regionserver | http | 16030 |

# 2. HBase 的使用

## 2.1 hbase 核心概念

![image-20200704164831317](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200704164831317.png)

### 2.1.1 namespace（库）

​		默认有 default、hbase 两个名称空间。

​		default 库，用来作为用户的默认库。

​		hbase 库，用来存放一些系统表，其中默认有两张表：

​		**① hbase:meta：存储用户创建的表的 region 的信息**

​		**② hbase:namespace：存储用户定义的库的信息**

库在 hdfs 上实为一层目录。

### 2.1.2 table：表

​		表是库路径下的一层子目录。

### 2.1.3 region：区域

​		表的连续的若干行，组成一个 region（行组）。

​		region 是 hbase 中在处理请求时的基本单位。一个 region 由一个 regionserver 负责，当需要向指定的 region 中读写数据时，由 regionserver 找到此 region 执行读写。

​		region 在 hdfs 上是表中的一层子目录。

### 2.1.4 column family：列族

​		一个 region 下，可能有多个列族；每个列族在 hdfs 上是 region 目录下的一层子目录。

**注意：官方不建议设置太多列族，出于以下几点考虑：**

​	**① hbase 中的文件是存储在 HDFS 上的。进行 region 级的 flush 时，会触发多个 store 的 flush，一方面会带来 IO 负担，另一方面数据倾斜时会产生小文件，不利于 HDFS 操作。**

​	**② 进行 split 时，数据量小的列族会产生更多小文件。**

​	**③ 同理，多个列族进行 Compaction 时，也会增加 IO 负担。**

​	**④ region 中的每个列族都会有独立的 memstore，因此列族数太多时还会增加内存负担。**

​	**综上，尽量用少的列族数量，且列族间的数据量要均衡，保证同步增长。**

### 2.1.5 column qualifier：列名

​		列名可以在插入数据时产生。

​		列名，rowkey，列值，时间戳，都存储在列族目录下的文件中。

​		数据是以文件的形式存放在列族的目录下：**库目录/ 表目录/ 区域目录/ 列族目录/ 列名文件**

一个列族目录对应一个 Sore 对象；一个列名文件对应一个 StoreFile 对象；一个 Store 中可以有多个 StoreFile 对象。 StoreFile 最终以 HFile 的格式存储在 HDFS 上。

## 2.2 同步集群时间

```shell
sudo ntpdate -u 'ntp2.aliyun.com'
```

## 2.3 hbase shell 的使用

```bash
bin/HBase shell
```

① 命令直接回车结束，不要加;，如果加了;，需要使用两个单引号''结合回车跳出。

② 如果要支持上下方向键导航，需要配置xshell的终端设置。

③ 哪里不会就使用 help  '命令名' 或 help '命令组名'。

④ hbase shell 使用 ruby 编写，不支持中文。

## 2.4 库基本操作

查看所有库：

```sql
list_namespace
```

查看库中的表：

```sql
list_namespace_tables 'hbase'
```

查看库属性：

```sql
describe_namespace 'hbase'
```

创建库：

```sql
create_namespace 'ns1'[, {'PROPERTY_NAME'=>'PROPERTY_VALUE'}]
-- [] 中的内容可以省略。
```

修改库属性：

```sql
alter_namespace 'ns1', {METHOD => 'set', 'PROPERTY_NAME' => 'PROPERTY_VALUE'}
```

删除库属性：

```sql
alter_namespace 'ns1', {METHOD => 'unset', NAME => 'PROPERTY_NAME'}
```

删库：

```sql
drop_namespace 'ns1'
```



## 2.5 表基本操作

创建表

```sql
create 'ns1:t1','cf1','cf2'
```

查看表属性

```sql
describe 'ns1:t1'
```

禁用/启用表

```bash
disable 'ns1:t1'
enable 'ns1:t1'
```

向表中添加数据

```bash
put 'ns1:t1', 'r1', 'cf1:age', '20'
```

查看表是否存在

```bash
exists 'ns1:t1'
```

清空表

```bash
truncate 'ns1:t1'
```

删除表

```bash
drop 'ns1:t1'
```

修改表属性

```sql
alter 'ns1:t1', {NAME => 'cf1', VERSIONS => '3'},{NAME => 'cf2', VERSIONS => '5'}
```

获得表的 region 信息

```sql
get_splits 'ns1:t1'
```



## 2.6 数据的增删改查

增（一次只能放一个 cell ）:

```sql
put 'ns1:t1', 'r1','cf1:name','jack'
put 'ns1:t1', 'r1','cf1:age','20'
put 'ns1:t1', 'r1','cf1:gender','male'
```

删（一次只能删一个 cell ）:

```bash
delete 'ns1:t1', 'r5', 'cf1:name'
```

改同增，自动覆盖：

查：

```bash
get 'ns1:t1', 'r1'
```

```sql
scan 'ns1:t1'
-- 限定区间（前闭后开）
scan 'ns1:t1', {STARTROW => 'r2', STOPROW => 'r4'}
-- 定义输出版本数量
scan 'ns1:t1', {RAW => true, VERSIONS => 10}
```



## 2.7 hbase 的写流程

发送给 Put 请求：

```sql
-- 格式
put '表名','rowkey','列族名:列名',value
-- 示例
put 'ns1:t1','r6','cf1:name','tom'
```

​		在 hbase 的服务端，有两种类型的请求：一种例如建表、删表、分配 region 到 regionserver 这些请求，由 Master(集群的管理者) 负责；另一种例如数据的增删改查，由 regionserver 负责处理（Zookeeper 也会分担一部分）。

![image-20200320181706018](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200320181706018.png)

第一步：必须先找到 r6 这一行在 'ns1:t1' 表中的 region 所对应的 regionserver 是哪台机器。region 和 regionserver 的对应关系，会保存在系统表 hbase:meta 表中。

① 通过查询 zookeeper 中的 /hbase/meta-region-server，找到 hbase:meta 表所在的 regionserver。

② 向 hbase:meta 表所在的 regionserver 发送请求，下载 hbase:meta 表，缓存到客户端本地，方便下次直接从本地查询。

③ 通过查询 hbase:meta 表，得知每个 region 和 regionserver 的对应关系。（通过 scan 'hbase: meta' 操作可以验证。）

如何知道 r6 在哪个 region?

每个 region 都有一个 startkey，和 stopkey，每个 region 中的 rowkey 都会进行字典排序，因此通过和 region 的 startkey 和 stopkey 进行比对，可以知道 rowkey 是否在当前 region。

④ 根据 rowkey 所在的 region，找到 regionserver，发送 put 请求。

⑤ regionserver 先将 put 请求记录到 WAL 日志文件中，再写入指定 store 的 MemStore 中。（每个 Store 有一个 MemStore，但会存储多个 StoreFile ）

⑥ 一旦写入 MemStore 完成，就通知客户端已经写完。

## 2.8 hbase 的写流程(源码解析)

```java
try {
      // ------------------------------------
      // STEP 1. Try to acquire as many locks as we can, and ensure
      // we acquire at least one.
    	 尝试获取尽可能多的锁，至少得获取一个。防止多个请求并发向同一个region的列族中写，造成数据不一致！
      // ----------------------------------
    

      // ------------------------------------
      // STEP 2. Update any LATEST_TIMESTAMP timestamps
             插入数据时，timestamp是可选的！如果插入的value没有指定timestamp!
             在插入使用服务端的时间戳！使用服务端的时间戳作为value的timestamp!
      // ----------------------------------
     
     
      

      // calling the pre CP hook for batch mutation
             CP(cooprocessor): 协处理器(类似Mysql中的触发器)
             当向指定的表或指定的行执行写操作时，会在写入之前调用协处理器的 pre CP hook（前置钩子程序），在写入之后，会调用协处理器的 postCP hook（后置钩子程序）
      

      // ------------------------------------
      // STEP 3. Build WAL edit 构建WAL编辑对象
      // ----------------------------------
     
      // -------------------------
      // STEP 4. Append the final edit to WAL. Do not sync wal.
             将最后的编辑追加到WAL对象中，但是还没有持久化最后的编辑信息到WAL磁盘文件中！
      // -------------------------
     

      // ------------------------------------
      // 乐观锁： 乐观锁是按照世界观来划分的一种锁的类型！
      //  如果对某个对象使用了乐观锁，此对象是不加悲观锁！多个线程可以同时访问此对象！
     	// 每个线程在访问数据时，会记录访问之前的版本号，在执行修改时，如果修改时版本号和之前记录的版本号一致，那么代表此期间没有别的线程修改此数据。此次更新就是安全的！写入完成，更新数据到最新的版本！
      // MVCC： (Multi-version-Concurrency-control)多版本并发控制！乐观锁的一种实现！
      // STEP 5. Write back to memstore
      // Write to memstore. It is ok to write to memstore
      // first without syncing the WAL because we do not roll
      // forward the memstore MVCC.
             将数据写入到memstore，写入到memstore是没问题的，因此在没有同步写操作到WAL日志文件之前，是不会滚动MVCC版本号的，因此就没问题
       //The MVCC will be moved up when
      // the complete operation is done. These changes are not yet
      // visible to scanners till we update the MVCC. The MVCC is
      // moved only when the sync is complete.
             MVCC只会在将写操作持久化到WAL的磁盘文件后，才会滚动，在此期间，写入到memstore的数据
             是不会被扫描器查询到的！
      // ----------------------------------
      

      // -------------------------------
      // STEP 6. Release row locks, etc.释放行锁
      // -------------------------------
     

      // -------------------------
      // STEP 7. Sync wal.
             将wal对象中记录的写操作，持久化到WAL磁盘文件中
      // -------------------------
      if (txid != 0) {
        syncOrDefer(txid, durability);
      }
		//是否需要回滚memstore
      doRollBackMemstore = false;
      // calling the post CP hook for batch mutation
    		调用协处理器的后置处理方法！
   

      // ------------------------------------------------------------------
      // STEP 8. Advance mvcc. This will make this put visible to scanners and getters.
                滚动MVCC版本号，这样scan和get就可以查询到此条记录
      // ------------------------------------------------------------------
     

      // ------------------------------------
      // STEP 9. Run coprocessor post hooks. This should be done after the wal is
      // synced so that the coprocessor contract is adhered to.
      // ------------------------------------
      if (!isInReplay && coprocessorHost != null) {
        for (int i = firstIndex; i < lastIndexExclusive; i++) {
          // only for successful puts
          if (batchOp.retCodeDetails[i].getOperationStatusCode()
              != OperationStatusCode.SUCCESS) {
            continue;
          }
          Mutation m = batchOp.getMutation(i);
          if (m instanceof Put) {
            coprocessorHost.postPut((Put) m, walEdit, m.getDurability());
          } else {
            coprocessorHost.postDelete((Delete) m, walEdit, m.getDurability());
          }
        }
      }

      success = true;
      return addedSize;
    } finally {
    	如果持久化WAL对象中的数据到磁盘失败，会回滚memstore中之前写入的数据
      // if the wal sync was unsuccessful, remove keys from memstore
      if (doRollBackMemstore) {
       
```

总结： 如何向 region 中写入数据

① 先获取锁，防止多个客户端同时向某一列写入数据。

② 获取数据的最新的时间戳。

③ 构建 WAL 对象。

④ 将写的操作(数据)写入到 WAL 对象(内存 buffer)中。

⑤ 将数据写入到 memstore。（客户端的任务已经完成）

⑥ 将 WAL 对象(内存buffer)中的写操作(数据)，持久化到磁盘的WAL对象的文件中。

⑦ 如果 sync 成功，就滚动当前列的 MVCC 版本号，此时可以查询到写的数据。

⑧ 如果 sync 失败，则回滚 memstore 之前写入的数据，写入失败。

## 2.9 hbase 的读流程

![image-20200320181746586](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200320181746586.png)

① 同读流程，通过查询 zookeeper 中的 /hbase/meta-region-server，可以找到 hbase:meta 表所在的 regionserver。

② 向 hbase:meta 表所在的 regionserver 发送请求，下载 hbase:meta 表，缓存到客户端本地，方便下次直接从本地查询。

③ 通过查询 hbase:meta 表，得知每个 region 和 regionserver 的对应关系。

④ 根据 rowkey 所在的 region，找到 regionserver，发送 get 请求。

![image-20200320182240542](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200320182240542.png)

⑤ regionserver 在处理 get 请求时，根据所查询的 store，初始化两种 scanner：

​		memstoreScanner —— 负责扫描 store 的 memstore。

​		StoreFileScanner —— 负责扫描列族下的所有的 storefile。

​		每个 store 都会初始化一个 memstoreScanner，初始化多个 StoreFileScanner。

​		之后将同一个列的不同的 cell 取出，取出后汇总，挑选时间戳最大的返回。

⑥ 在返回时，命中的 storefile，cell 所在的 block(64k) 会被缓存到 blockcache 中（含 HFile 索引、时间戳信息）。

⑦ 每个 Regionserver 都有一个 blockcache 对象，默认大小是 RS 所在堆的 40%。在执行下次查询时会根据 rowkey 和 region，如果命中的 block 全部位于已经缓存的 blockcache 中，那么将不会再扫描 storefile，直接从 blochcache 中取出数据，否则会再次扫描 storefile。

**注：hbase 的架构注定了其读数据的效率要低于写数据的效率。**

补充：blochcache 属于 LRU（最近最少使用）缓存，常用缓存类型如下：

​	① FIFO（First In First out）：先见先出，淘汰最先近来的页面，新进来的页面最迟被淘汰，完全符合队列。

​	② LRU（Least recently used）：最近最少使用，淘汰最近不使用的页面（时间维度）。

​	③ LFU（Least frequently used）：最近使用次数最少，淘汰使用次数最少的页面（数量维度）。

## 2.10 hbase 的 flush

### 2.10.1 原理

​		向 region 写入的数据，会被先写入到 store 的 memstore 中。当 memstore 快满时，系统会自动刷写，也可以手动刷写。为保证数据有序，flush 之前先对数据进行排序。

### 2.10.2 手动刷写

```sql
flush ‘表名’
flush ‘region名’
```

flush 之后的文件，是 HFile 文件类型，不是一个文件文件，无法直接查看。借助 Hbase 提供的工具可以查看：

```
hbase org.apache.hadoop.hbase.io.hfile.HFile -e -p -f 文件路径
```

### 2.10.3 自动刷写

**1. 基于单个 Memstore 的刷写**

​		单个 memstore 的大小，超过

```
hbase.hregion.memstore.flush.size(默认128M)
```

​		当前 memstore 所在 region 的所有的列族都会执行刷写。

​		一旦单个 memstore 使用的空间，超过

```bash
hbase.hregion.memstore.flush.size（默认值128M） * hbase.hregion.memstore.block.multiplier（阻塞因数，默认值4)
```

​		此时会阻塞客户端继续向此 memstore 写入。

**2. 基于 Regionserver 的刷写：**

​		一个 regionserver 负责多个 region，每个 region 又包含多个 store 对象，每个 store 对象都有一个 memstore，即 regionserver 和 memstore 是1对N的关系。

​		当一个 regionserver 所负责的所有的 store 对象的 memstore 总大小超过

```
java_heapsize * hbase.regionserver.global.memstore.size（默认值0.4） * hbase.regionserver.global.memstore.size.lower.limit（默认值0.95）
```

​		此时，会执行刷写，将当前 regionserver 所负责的所有的 memstore 按照大小排序，从大到小依次刷写，直到 regionserver 所负责的所有的 store 对象的 memstore 总大小低于以上阈值。

​		如果 regionserver 所负责的所有的 store 对象的 memstore 总大小已经超过

```
java_heapsize * hbase.regionserver.global.memstore.size（默认值0.4）
```

​		此时，会阻塞客户端向当前 regionserver 的所有 region 的写入。

**3. 基于时间的刷写：**

​		默认每间隔

```
hbase.regionserver.optionalcacheflushinterval（默认1小时）
```

​		执行自动刷写。

**4. 基于 WAL 处理文件的刷写：**

​		如果正在使用的 WAL 文件的数量超过32，此时会根据 WAL 文件的生成时间，从早到晚依次刷写，直至低于下限阈值。

### 2.10.4 刷写的意义

① 在执行刷写时，将部分过时的数据舍去，每次刷写，最多保留列族 VERSIONS 数量的 put 类型的 cell。

② 在刷写时，会将 memstore 中的 rowkey 进行排序后，再刷写，方便在查询时，快速检索数据。

## 2.11 storefile compaction

### 2.10.1 compaction 的意义

① 每次 memstore 刷写都会在列族所在的目录生成一个 HStoreFile 文件，HDFS不适合存储大量的小文件，会降低 NN 的服役能力，合并可以避免在列族下产生大量的小文件。

② 合并也可以将某些过期的数据进行清理，节省存储空间。

### 2.10.2 合并的实现

1. 手动实现：

   Compaction 分为两种，分别是 Minor Compaction 和 Major Compaction。Minor Compaction 会将临近的若干个较小的 HFile 合并成一个较大的 HFile，但**不会**清理过期和删除的数据。 Major Compaction 会将一个 Store 下的所有的 HFile 合并成一个大 HFile，并且**会**清理掉过期和删除的数据。

```
compact '表名'： minor compact
major_compact ‘表名’: major compact
```

2. 自动合并：

建议禁用自动 major_compact，在集群空闲时手动执行 major_compact。

## 2.12 region split

![image-20200320205250520](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200320205250520.png)

## 2.13 meta cache 过期处理

如果访问的 region 在 RS 上发生了改变，比如被 balancer 迁移到其他 RS 上。这个时候，通过缓存的地址访问会出现异常，在出现异常的情况下，client 需要重新走一遍上面的流程来获取新的RS 地址。

client 会移除这个 region 的 location 缓存，然后去访问 hbase 的 meta 表，用要访问的 rowkey 做一次 reverse scan，拿到这个 rowkey 所属 region 的起止 rowkey 范围和所属的 RS。更新本地缓存，然后向刚获取到的 RS 发送请求。 location 缓存是弱引用，在客户端发生 FGC 的时候，cache 可能会大量失效，需要重新读 meta 表。

![image-20200326185243271](C:\Users\Jeffery\AppData\Roaming\Typora\typora-user-images\image-20200326185243271.png)

因为 meta Region 的路由信息存放于 ZooKeeper 中，在第一次从 ZooKeeper 中读取 META Region 的地址时，需要先初始化一个 ZooKeeper Session。ZooKeeper Session 是 ZooKeeper Client 与 ZooKeeper Server 端所建立的一个会话，通过心跳机制保持长连接。通过前面建立的连接，从 ZooKeeper 中读取 meta Region 所在的 RegionServer，这个读取流程，当前已经是异步的。获取了 meta Region 的路由信息以后，再从 meta Region 中定位要读写的 RowKey 所关联的 Region 信息。

# 3. JavaAPI

## 3.1 加入依赖

```xml
<dependencies>
        <dependency>
            <groupId>org.apache.hbase</groupId>
            <artifactId>hbase-server</artifactId>
            <version>1.3.1</version>
        </dependency>

        <dependency>
            <groupId>org.apache.hbase</groupId>
            <artifactId>hbase-client</artifactId>
            <version>1.3.1</version>
        </dependency>
    </dependencies>
```

## 3.2 常见的API

### 3.2.1  Connection

- Connection 对象封装了一组连接 hbase 集群的连接，包含一个连接 zookeeper 的连接。

- 可以通过 ConncetionFactory 来构建一个 Connection，在不要使用时，使用 close() 关闭它，释放资源。
- Connection 维护连接 server，保存 meta cache，以及寻找 master，定位 region 等逻辑，而且可以在这些信息变更后自动维护，这些信息都可以被 Connection 对象所创建的 Table 和 Admin 对象共享。
- Connection 的创建是重量级的，但是是线程安全的，因此可以在多个线程中公用，一个应用最好只创建一个Connection。
- Admin 和 Table 对象不是线程安全的，创建是轻量级的。建议每个线程都拥有自己的 Admin 和 Table 对象。

结论： 先创建 Connection 来连接集群。连接后，可以使用从 Connection 中获取的 Admin 对象对集群中的表和库进行管理；可以从 Connection 中获取 Table，Table 代表某一个具体的表，使用 Table 对象完成数据的增删改查。

构建：

```
ConnectionFactory.createConnection()
```

最终会返回一个 Connection 对象，这个 Conection 对象是基于 Configuration 对象创建。

Configuration 对象已经读取项目类路径下的8个 hadoop 的配置文件和 hbase 的2个配置文件。

### 3.2.2 Admin

​		admin 包含了对库，和表等相关管理性的 API。

获取：

```
Connection.getAdmin();
```

关闭：

```
Admin.close();
```

Admin 的创建是轻量级的，线程不安全的，因此一个方法或一个线程应拥有自己的 admin 对象。

### 3.2.3 NamespaceDescriptor

​		NamespaceDescriptor 代表一个 NameSpace，可以在这个类中来定义和描述一个名称空间。

库名： NamespaceDescriptor.getName()

获取已有库的描述：

```java
 Admin.getNamespaceDescriptor(String namespaceName);
```

获取新的库的描述： 

```java
NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(nsname).build();
```



### 3.2.4 HTableDescriptor

​		HTableDescriptor代表一个表的描述。

### 3.2.5 TableName

​		TableName 代表表名。

​		TableName.getNameAsString() 获取表的名称。

​		可以通过 valueOf(String 库名，String 表名) 获取一个表的名称。表的名称先从缓存中取，如果取不到，再创建。

### 3.2.6 HColumnDescriptor

​		列族的定义和描述，可使用 HTableDescriptor. addFamily 加入到 HTableDescriptor 中。

### 3.2.7  Table

​		通过 connection 对象的 connection. getTable(TableName) 方法获得 Table 对象。与 Admin 类似，使用完之后要调用 close 方法显式地关闭。

### 3.2.8 Put

​		Put 对象封装了要向一行中添加的值的表名，列名等参数。调用 Table. put(Put) 方法执行增改操作。

### 3.2.9 Get

​		代表查询当行数据的对象，可以在 Get 中设置详细的查询参数。调用 Table. get(Get) 方法执行单条查询操作，返回 Result 结果。

### 3.2.10 Result

​		代表 Get 或 Scan 查询返回的单行结果，封装了多个 Cell 对象。

### 3.2.11 Cell

​		封装在 Result 对象中，代表一个版本的数据。其中包含 rowkey，列族名，列名，value，时间戳，MVCC版本号等。

### 3.2.12 Scan

​		scan 代表扫描器，可以在 scan 中指定查询的参数，如列族名、列名、起止行、版本范围等。调用 Table.getScanner(scan) 方法执行多行查询操作，返回 ResultScanner 对象。

### 3.2.13 ResultScanner

​		一个可以迭代每行 Result 的迭代器，调用 next() 返回下一行扫描的结果，支持增强 for 循环。

### 3.2.14 Delete

​		代表对一行的删除操作，可以根据不同的删除类型，封装不同的参数。

### 3.2.15 工具类

#### 3.2.15.1 Bytes

​		Bytes.toBytes(xxx): 将某个类型转为字节数组。

​		Bytes.toXxx(byte []): 将字节数组转为某种类型。

#### 3.2.15.2 CellUtil

​		CellUtil 可以帮我们获取 Cell 中的属性。

​		CellUtil.cloneXxx() 来获取Cell中的某个属性。

# 4. HBase 和 MR 集成

## 4.1 意义

​	hbase 提供的 API，只能做简单查询，无法进行复杂运算。

​	hbase 是一个 NoSQL 数据库，不支持 SQL，没法像 hive 一样使用简单的 sql 进行分组，排序，统计等操作。

​	hbase 的数据是存储在 Hdfs 上的，因此可以使用 MapReduce 对 hdfs 的数据进行统计和计算。

## 4.2 如何集成

​		MR在运行时，需要持有 HBase 的 jar 包，这样才能从 HBase 中读取数据。

方式一： MR 在启动时，手动指定所需要的 HBase 的 jar 包，将 jar 包加载到 MR 的类路径下。

```bash
java -cp  jar包所在路径  xxx
```

方式二： 配置 HBase 的 jar 包到 hadoop 的环境脚本中，每次启动 hadoop 命令时，自动读取环境脚本，脚本自动加载 jar 包

```bash
hadoop jar  xxx
```

hadoop 的环境脚本名称为  **$HADOOP_HOME/etc/hadoop/hadoop_env.sh**，hadoop_config.sh（配置hadoop的常用的环境变量和设置等）在执行时，会调用hadoop_env.sh，读取脚本中的配置。

如何知道MR在运行时，需要哪些HBase的jar包。

```
hbase mapredcp
```

配置这些 jar 包到 hadoop_env.sh 脚本中。

在 hadoop-env.sh 的 43 行以后添加：

```bash
export HADOOP_CLASSPATH=$HADOOP_CLASSPATH:/opt/module/hbase-1.3.1/lib/*
```

**分发 hadoop_env.sh 文件。**



## 4.3 官方案例

官方提供的案例的jar包在$HBASE_HOME/lib下

示例一： 统计一个表中的cell的个数

```
hadoop jar hbase-server-1.3.1.jar CellCounter ns1:t1 /testHbaseMR1 ,
```

示例二： 统计表的行数

```
hadoop jar hbase-server-1.3.1.jar rowcounter ns1:t1
```

示例三： 将tsv数据导入hbase

注意需要先建表，字段的顺序要和数据中的顺序一致。

```
hadoop jar hbase-server-1.3.1.jar importtsv -Dimporttsv.columns=HBASE_ROW_KEY,info:deptname,info:num dept /hbasedata
```

HBASE_ROW_KEY： 代表哪一列需要作为 rowkey

## 4.4 自定义案例

### 4.4.1 需求

​		将 dept 表中 rowkey > 20 的数据迁移到 dept_mr 表中。

### 4.4.2 注意事项

1. 如果数据在 hbase中，需要使用 TableInputFormat。（官方文档）

​		切片： 将表按照 region 切片，一个 region 切一片。

​		RR：RecordReader <ImmutableBytesWritable, Result>

​							key: ImmutableBytesWritable 类型，代表 rowkey。

​							value: Result ，代表一行的 cell 集合。

​		一次读取 region 中的一行，将 rowkey 作为 key，将一行的 cell 集合作为 value。

2. Mapper 继承 TableMapper，只需要声明 KEYOUT、VALUEOUT 的类型即可，因为输入的key-value 类型已经被写死了。

3. 如果是向目标表写入数据，只需要将数据封装到 Put 对象中即可。

4. 如果 Reducer 的数据是需要写入到 HBase 数据库中，Reducer 通常可以继承TableReducer。继承之后，TableReducer 要求 Valueout 必须是一个 Mutation 类型。Mutation 类型代表一个写操作对象，例如 Put、Delete等。

​	因为通常如果使用了 TableOutPutFormat，要求 Reducer 的输出必须是 Mutatiion 类型：

```java
public abstract class TableReducer<KEYIN, VALUEIN, KEYOUT>
extends Reducer<KEYIN, VALUEIN, KEYOUT, Mutation> {
}
```

5. 在设置 Driver 时，可以使用 TableMapReduceUtil 工具类自动进行各种设置。

6. 如果 Mapper 的输出是 Put 类型，此时系统自动设置了 PutCombiner。在某些场景，例如将记录迁移的场景，是不需要 Combiner 的，如何取消？

​		① Put 不作为 Mapper 输出的 value，可能需要额外提供比较器(麻烦)。

​		② 如果 Put 作为输出的value，可以将 key 设置为唯一的值，这样即使调用了 Combiner，那么也不会运行 Combiner 逻辑，例如将每行的 Rowkey 封装为 Key。

# 5. 集成 Hive

## 5.1 意义

HBase 是一个 NoSQL 数据库，一般用作对海量大表数据的实时读写，不支持复杂的查询。

Hive 是一个数据仓库软件，主要用来对数据仓库中的数据进行分析，而且 Hive 支持使用 HQL 对表中的数据进行查询。两者均可调用 MapReduce 执行计算：

Hive ------> HQL ---> HDFS上的数据 -----> MR

HBase ---> API ----> HDFS上的数据 -----> MR

可以让 hive 集成 HBase，hbase 作为数据存储的介质，hive 作为分析引擎，来分析 Hbase 中已经存储的数据。此时 hbase 中数据的逻辑结构是一个表，hive 对表中的结构化数据进行分析。

## 5.2 环境要求

需要让 hive 持有 hbase 的 jar 包，hive 才能访问 hbase，读取 hbase 中的数据进行分析。

操作： 将访问hbase所需要的jar包，放入到 $HIVE_HOME/lib

```
ln -s $HBASE_HOME/lib/HBase-common-1.3.1.jar  $HIVE_HOME/lib/HBase-common-1.3.1.jar
ln -s $HBASE_HOME/lib/HBase-server-1.3.1.jar $HIVE_HOME/lib/HBase-server-1.3.1.jar
ln -s $HBASE_HOME/lib/HBase-client-1.3.1.jar $HIVE_HOME/lib/HBase-client-1.3.1.jar
ln -s $HBASE_HOME/lib/HBase-protocol-1.3.1.jar $HIVE_HOME/lib/HBase-protocol-1.3.1.jar
ln -s $HBASE_HOME/lib/HBase-it-1.3.1.jar $HIVE_HOME/lib/HBase-it-1.3.1.jar
ln -s $HBASE_HOME/lib/htrace-core-3.1.0-incubating.jar $HIVE_HOME/lib/htrace-core-3.1.0-incubating.jar
ln -s $HBASE_HOME/lib/HBase-hadoop2-compat-1.3.1.jar $HIVE_HOME/lib/HBase-hadoop2-compat-1.3.1.jar
ln -s $HBASE_HOME/lib/HBase-hadoop-compat-1.3.1.jar $HIVE_HOME/lib/HBase-hadoop-compat-1.3.1.jar
```

## 5.3 集成 hive

数据是在 hbase 的表中存储，因此需要在 hive 中建表。hive 中的表需要和 hbase 表中要分析的数据的字段一一映射，之后使用 HQL 语句进行查询。

### 5.3.1 建表

#### 5.3.3.1 情形一

​		数据已经在 hbase 中，只需要根据 hbase 中已经有的表的结构，在 hive 中进行参照，创建hive 中的表。

```sql
CREATE external TABLE hive_HBase_dept_table(
deptid int,
deptname string,
num int
)
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,info:deptname,info:num")
TBLPROPERTIES ("hbase.table.name" = "dept");
```

注意事项： ① hive 表的列的总数需要和 hbase 表中列的数量+rowkey 的数量一致。

​				   ② hive 表中列的类型需要和 hbase 表中存储的数据类型一致，或需要保证兼容。

​				   ③ 在进行列的映射时，字段的顺序要和 hive 表中的列的顺序一致。

​				   ④ 表已经在 hbase 中存储了，说明数据是已经被 hbase 中的表负责进行管理，hive 就不再负责数据的生命周期了，因此这个表只能是 external table。

本地表(native table)和非本地表(non-native table)：

​		本地表(native table)： 指传统情况下，hive 中表的数据是直接存储在 hdfs 上的；建表时，可以指定 ROW FORMAT 和 STORED AS。

​		非本地表(non-native table)： hive 表中的数据没有存储在 hdfs 上，而是存储在其他的数据库中，例如 hbase、mongdb、kafka、ES中等等。建表时指定 STORED BY ’类名‘，表示在向表中读写数据时，借助此 handler 类完成操作。

#### 5.3.3.2 情形二

​		数据还没有导入到 hbase，可以先在 hive 中建表，在 hive 的表中执行 insert 语句，将数据导入到 hbase 中。

在 hive 中建表：

```sql
CREATE  TABLE hive_HBase_dept2_table(
deptid int,
deptname string,
num int
)
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,info:deptname,info:num")
TBLPROPERTIES ("hbase.table.name" = "info:deptinfo2");
```

​		注意： 如果需要在 hive 中执行导入数据的命令，将数据导入到 hbase 中，且 hbase 中的表不存在，此时相当于 hive 负责产生数据，负责数据的生命周期，也即须为管理表。目前1.2.1的hive，持有了一个jar包**hive-hbase-handler-1.2.1.jar**，这个 jar 包负责 hbase 和 hive 的集成，但是这个 jar 提供的 API 是 0.9 版本的 hbase 的API。

目前本机安装的 hbase 是 1.3.1 版本，低版本的API，无法适配高版本，因此报错：

```
Execution Error, return code 1 from org.apache.hadoop.hive.ql.exec.DDLTask. org.apache.hadoop.hbase.HTableDescriptor.addFamily(Lorg/apache/hadoop/hbase/HColumnDescriptor;)V
```

解决： 使用 hive 1.2.1 的 src 源码，再添加 hbase1.3.1 对应的 lib 源码，重新编译一个 **hive-hbase-handler-1.2.1.jar**，这样 jar 中的 api 可以操作高版本的 hbase。

将新编译的 jar 包覆盖 $HIVE_HOME/lib 下的 **hive-hbase-handler-1.2.1.jar**，之后重启 hive，重新建表，插入数据：

```sql
insert into hive_hbase_dept2_table select * from dept;
```

# 6. HBase 优化

## 6.1 高可用 (HA)

HA (high avaliable) 的核心是避免单点故障。

在 hbase 中主要有两种进程：master，regionserver。regionserver 通常是启动多个，在高并发的情况下及时处理客户端的请求，一旦其中的一个 regionserver 挂掉，master 会将挂掉的regionserver 负责的 region 重新分配给其他的 regionserver，唯一需要解决的就是 master 的故障。如果master故障了，不影响数据的读写，但会影响建表，改表等操作。

解决思路：启动多个 master，其中一个为 active 状态，其他为 backup 状态；当 active 状态的 master 挂掉后，可以让 backup 状态的 master 顶上。

操作： 将备用的 master 配置在 $HBASE_HOME/conf/backup-masters 中即可。（文件需要自己创建，且文件名必须为 backup-masters）

```
hadoop102
hadoop104
```



## 6.2 预分区

### 6.2.1 意义

​		每次在 hbase 中新创建一个表时，默认只有一个 region。当向这个 region 写入大量的数据时，region 的大小会增大；增大到一定程度，region 会自动触发切分策略，进行自动切分。在自动切分时，通常是选择当前 region 所有 rowkey 的中间值一分为二。自动切分在某些情况下可能造成切分后 regioniserver 依旧负载不均衡。

因此在建表时，可以对数据进行抽样查询；在定义表时，提前对表进行预分区（提前划分region）。这样在建表后，就有多个 region；数据在写入 region 时，就可以负载均衡。

### 6.2.2 实现

#### 6.2.2.1 实现一

手动指定每个 region 的边界值

```sql
create 'staff1','info', SPLITS => ['1000','2000','3000','4000']
```

#### 6.2.2.2 实现二

使用预分区算法，生成边界值。 只指定要生成的分区数，使用算法生成分区边界

```sql
create 'staff2','info', {NUMREGIONS => 15, SPLITALGO => 'HexStringSplit'}
```

此种做法，数据在插入到表中之前，需要对数据的 rowkey 使用16进制进行转换，转换后插入，才能负载均衡。示例：

```java
@Test
    public void convertRowkey() throws IOException {

        String rowkey="abc";

        char[] chars = rowkey.toCharArray();

        String result="";

        for (char c : chars) {

            result+=Integer.toHexString(c);

        }

        System.out.println(result);

    }
```

也可以使用下文中的散列函数将数据打散后插入。

#### 6.2.2.3 实现三

将边界值，写在文件中。在建表时，读取文件中的边界值，先根据字典顺序对文件中的边界值进行排序后，再进行切分。

```sql
create 'staff3','partition3',SPLITS_FILE => 'splits.txt'
```

#### 6.2.2.4 API实现

指定 region 的个数和起始终止边界，使用随机算法生成：

```java
admin.createTable(hTableDescriptor, Bytes.toBytes("aaa"),Bytes.toBytes("bbb"),5);
```

注意：regions 的个数必须超过3。

自定义边界值

```java
byte [] [] splitKeys=new byte[4][] ;
splitKeys[0]= Bytes.toBytes("aaaa");
splitKeys[1]= Bytes.toBytes("bbbb");
splitKeys[2]= Bytes.toBytes("cccc");
splitKeys[3]= Bytes.toBytes("dddd");
admin.createTable(hTableDescriptor,splitKeys);
```

## 6.3 Rowkey 的设计

hbase 的数据是 key-value 结构，因此一条数据的唯一标识就是 rowkey。

region 也是根据 rowkey 进行排序，根据 rowkey 进行切分。

rowkey 设计的好，可以提供系统负载均衡的能力。

如何让 regionserver 负载均衡： 让数据可以基于 rowkey 排序后，均匀地分散到所有的 region，防止数据倾斜。

如何实现：

① 可以采取随机数、hash 或 散列运算，让 rowkey 足够散。

② 字符串反转。

③ 字符串拼接。

## 6.4 布隆过滤器

布隆过滤器的功能： 针对读（查询）的场景。在查询时，如果使用了布隆过滤器，布隆过滤器可以快速高效第判断查询的元素是否在集合中存在。**但只能判断要查询的元素在集群中是否一定不存在或可能存在。**因此布隆过滤器存在一定的误判： 布隆过滤器经过计算，判断元素在集合中可能存在，但是在真正扫描集合后，发现元素并不存在，这称为误判。即便有误判，布隆过滤器的应用也很普遍，因为它可以显著加速查询。

在 hbase 中，布隆过滤器可在列族上进行配置。在 hbase 中布隆过滤器有两种配置：

ROW(默认) | ROWCOL

两种设置的区别在于，布隆过滤器在计算时，只使用 rowkey 作为参数进行运算，还是使用 rowkey+col 作为参数来运算。

```sql
举例： 有列族 info info2
storefile1: （r1,info:age=20）,（r2,info:age=20）
storefile2: （r3,info2:age=20）,（r4,info2:age=20）

使用的是 ROW 的布隆过滤器，在过滤时，只使用 rowkey 作为计算的参数。
get(r1),如果判断 r1 在 storefile2 中一定不存在，就无需扫描 storefile2。


举例： 有列族 info  info2
storefile1: （r1,info:age=20）,（r2,info:age=20）（r3,info:age=30）
storefile2: （r3,info2:age=20）,（r4,info2:age=20）

使用的是 ROWCOL 的布隆过滤器，在过滤时，只使用 rowkey+col 作为计算的参数！
get(r3,'info2'),如果判断r3-info2 在storefile1 中一定不存在，就无需扫描 storefile1。
```

布隆过滤器一般只适用于 get 查询，scan 的作用不大。

如果启动了布隆过滤器，会占用额外的内存，布隆过滤器一般是在 blockcache 中。