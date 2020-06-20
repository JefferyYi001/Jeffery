# 1. Phoenix 和 HBase 的映射关系

| Phoenix  | HBase         |
| -------- | ------------- |
| database | namespace     |
| table    | table         |
| column   | 列族名 : 列名 |
| 主键     | rowkey        |

通常在 sql 中建表时，可以指定某些列作为联合主键。在 Phnoeix 中有联合主键 (a,b)，对应的 hbase 中 的 rowkey 必须是 a,b 拼接起来一起作为 rowkey。

# 2. 安装 Phoenix

解压 jar 包后复制 HBase 需要用到 server 和 client 2 个 jar 包

```bash
cp phoenix-4.14.2-HBase-1.3-server.jar /opt/module/hbase-1.3.1/lib
cp phoenix-4.14.2-HBase-1.3-client.jar /opt/module/hbase-1.3.1/lib
```

分发刚刚复制的两个 jar 包

```bash
xsync /opt/module/hbase-1.3.1/lib
```

配置环境变量

```properties
export PHOENIX_HOME=/opt/module/phoenix
export PHOENIX_CLASSPATH=$PHOENIX_HOME
export PATH=$PATH:$PHOENIX_HOME/bin
```

启动 hadoop、zookeeper、HBase 后启动 Phoenix（若已启动，需重启 hbase）

```bash
bin/sqlline.py hadoop103:2181
```

# 3. 使用 Phoenix

## 3.1 情形一

hbase 还没有表，希望在 Phoenix 中使用 SQL 创建一个表，Phoenix 在解析建表语句时，可以帮助我们在 hbase 中创建表。

```sql
CREATE TABLE IF NOT EXISTS us_population (
      state CHAR(2) NOT NULL,
      city VARCHAR NOT NULL,
      population BIGINT
      CONSTRAINT my_pk PRIMARY KEY (state, city));
```

注意：在建表时，小写的表名或列名，都会自动在 sqlline.py 中转为大写；且在查询时，只能使用大写进行查询。如果必须使用小写，需要在表名等字段上添加双引号，因此建议不要使用小写的表名或字段。创建表后，再使用 SQL 向 hbase 中插入数据。

```sql
upsert into us_population values('CA','LOS',11100); //插入
delete from us_population where STATE='CA'; //删除
upsert into "dept"("deptid","num") values('50','2000'); //修改
```

删除后在 hbase 中使用 

```sql
scan 'US_POPULATION', {RAW => TRUE, VERSIONS => 5}
```

依然可以看到该行被打上了 **DeleteFamily**  标记。

## 3.2 情形二

hbase 中已经有表存在，希望使用 Phoenix 操作 hbase 中已经有的表。

如果只有查询的需求，可以创建视图 (对于视图，只能查询不能修改) 进行映射。

```sql
create view "ns1"."dept"(
"deptid" VARCHAR primary key,
    "info"."deptname" VARCHAR(20),
    "info"."num"  VARCHAR 
)column_encoded_bytes=0
```

映射规则： 

① Phoneix 的表名必须和 hbase的 库名.表名 一致。

注意，创建非 default 库时，需要在 hbase-site.xml 中加入以下 schema 配置并分发：（否则会报 ERROR 505 (42000): Table is read only. (state=42000,code=505) ）

```xml
<property>
  <name>phoenix.schema.isNamespaceMappingEnabled</name>
  <value>true</value>
</property>
<property>
  <name>phoenix.schema.mapSystemTablesToNamespace</name>
  <value>true</value>
</property>
```

之后软链接到 phoenix/bin/

```bash
ln -s /opt/module/hbase-1.3.1/conf/hbase-site.xml /opt/module/phoenix/bin/hbase-site.xml
```

再重启 hbase 和 Phoenix。

如果报 Schema does not exist schemaName=ns1，还要创建与命名空间对应的 schema：

```sql
CREATE SCHEMA IF NOT EXISTS "ns1";
```

② Phoneix 的表的列的数量，必须和 hbase 表 列的数据+rowkey列 的数量一致。

③ Phoneix 的表的主键的列名一般对应 rowkey 列，名称可以随意，但是类型得匹配。

④ Phoneix 的表的普通的列名，必须和 hbase 的 列族.列名 一致。

⑤ 表映射的结尾，必须添加 column_encoded_bytes=0，不然无法从 hbase 中查询到数据。

视图的另外一个作用是，如果视图创建错误，在删除视图时，不会删除 hbase 表中的数据。

如果不仅有查询的需求，还有增删改的需求，此时只能创建表进行映射。

```sql
create table "dept"(
"deptid" VARCHAR primary key,
    "info"."deptname" VARCHAR(20),
    "info"."num"  VARCHAR 
)column_encoded_bytes=0
```

# 3. 二级索引

​		索引：索引是为了加快查询的一种数据结构。

​		一级索引：rowkey 作为每一条记录的唯一标识，可以对 rowkey 创建索引，这个索引称为一级索引。

​		二级索引：基于某个列创建的索引，称为二级索引。

需要先给 HBase 配置支持创建二级索引：

1. 添加如下配置到 HBase 的 Hregionerver 节点的 hbase-site.xml

```xml

<!-- phoenix regionserver 配置参数 -->
<property>
    <name>hbase.regionserver.wal.codec</name>
    <value>org.apache.hadoop.hbase.regionserver.wal.IndexedWALEditCodec</value>
</property>

<property>
    <name>hbase.region.server.rpc.scheduler.factory.class</name>
    <value>org.apache.hadoop.hbase.ipc.PhoenixRpcSchedulerFactory</value>
<description>Factory to create the Phoenix RPC Scheduler that uses separate queues for index and metadata updates</description>
</property>

<property>
    <name>hbase.rpc.controllerfactory.class</name>
    <value>org.apache.hadoop.hbase.ipc.controller.ServerRpcControllerFactory</value>
    <description>Factory to create the Phoenix RPC Scheduler that uses separate queues for index and metadata updates</description>
</property>


```

2. 添加如下配置到 HBase 的 Hmaster 节点的 hbase-site.xml

```xml

<!-- phoenix master 配置参数 -->
<property>
    <name>hbase.master.loadbalancer.class</name>
    <value>org.apache.phoenix.hbase.index.balancer.IndexLoadBalancer</value>
</property>

<property>
    <name>hbase.coprocessor.master.classes</name>
    <value>org.apache.phoenix.hbase.index.master.IndexMasterObserver</value>
</property>
```

注意：删 zookeeper 集群配置后的端口号，否则会报错。

创建索引：

```sql
create index 索引名  on 表名(列名...)
```

```sql
create index idx_num on "dept"("info"."num");
```

删除索引：

```
drop index 索引名 on 表名
```

查看索引：

```
!tables
```

查看 SQL 是否使用上了二级索引：

mysql中，使用 explain + SQL 语句，如果 TYPE=ALL，说明进行了全表扫描，没有使用索引。

phoenix中，使用 explain + SQL 语句，如果出现了 RANGE SCAN 说明使用了索引，如果出现了FULL SCAN，说明没有使用上索引。

```sql
explain select "num" from "dept" where "num"='1800';
```

注意： 查询语句，不能写 * ，一旦写了，是无法使用索引的。

# 4. 本地索引和全局索引

## 4.1 概念

本地索引： create local index 索引名  on 表名(列名...)

全局索引： create index 索引名  on 表名(列名...)

## 4.2 区别

全局索引，将索引的信息写在 hbase 的索引表中。

本地索引，以列族的形式，存储在当前索引所在表中。

## 4.3 作用

不管是本地索引还是全局索引，在功能上没有任何差别，都是为了加快某个列的查询。但适合的情景略有不同：

当向 hbase 的表中插入数据时，数据在更新时，也需要更新索引。

本地索引：索引以列族的形式存储在表中，在更新数据和更新索引时，只需要向数据所在的regionserver 发请求即可；读数据时则需要读取每个 region 的索引信息。因此本地索引适合多写的场景。

全局索引：索引以表的形式存储在 hbase 中，索引所在的 region，也是由一个 regionserver 负责的。与本地索引相反，全局索引适合是多读的场景。

