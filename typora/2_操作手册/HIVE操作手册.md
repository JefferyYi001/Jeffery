# 1. DDL

## 1.1 DDL之库操作

### 1.1.1 增

```sql
create database [if not exists] 库名 
[comment '库的注释']
[location '库在hdfs上存放的路径']
[with dbproperties('属性名'='属性值'，...)]
```

注意： location可以省略，默认存放在/user/hive/warehouse/库名.db目录下

​			若使用 location，必须手动将目录建好。

​			dbproperties中只能存放string类型的属性，多个属性用逗号分隔

### 1.1.2 删

```sql
drop database [if exists] 库名 [cascade]
```

删除库时，是两步操作：

​	① 在mysql的DBS表中删除库的元数据

​    ② 删除hdfs上库存放的路径

以上操作只能删除空库(库中没有表)！如果库中有表，是无法删除的，如果要强制删除，需要添加**cascade**关键字.

### 1.1.3 改

只能改location和dbproperties属性！

```sql
ALTER DATABASE 库名 SET DBPROPERTIES (property_name=property_value, ...);
```

​	在改库的属性时，同名的属性会覆盖，不存在的属性会新增！

### 1.1.4 查

切换库

```sql
use 库名
```

查看库的描述

```sql
desc database 库名
```

查看库的详细描述：

```sql
desc database extended  mydb2
```

查看库中的表

```sql
show tables in 库名
```

查看当前库下的表

```sql
show tables
```



## 1.2 DDL 之表操作

### 1.2.1 创建表语法

```sql
CREATE [EXTERNAL] TABLE [IF NOT EXISTS] table_name 
-- 列的信息
[(col_name data_type [COMMENT col_comment], ...)] 
-- 表的注释
[COMMENT table_comment] 
-- 是否是分区表，及指定分区字段
[PARTITIONED BY (col_name data_type [COMMENT col_comment], ...)] 
-- 指定表中的数据在分桶时以什么字段进行分桶操作
[CLUSTERED BY (col_name, col_name, ...) 
-- 表中的数据在分桶时，以什么字段作为排序的字段
[SORTED BY (col_name [ASC|DESC], ...)] INTO num_buckets BUCKETS] 
-- 表中数据每行的格式，指定分隔符等
[ROW FORMAT row_format]
-- 如果向表中插入数据时，数据以什么格式存储
[STORED AS file_format] 
-- 表在hdfs上存储的位置
[LOCATION hdfs_path]
-- 指定表的某些属性
[TBLPROPERTIES]
```

**hive 基本数据类型**

| Hive数据类型 | Java数据类型 | 长度                                                 | 例子                                 |
| ------------ | ------------ | ---------------------------------------------------- | ------------------------------------ |
| TINYINT      | byte         | 1byte有符号整数                                      | 20                                   |
| SMALINT      | short        | 2byte有符号整数                                      | 20                                   |
| INT          | int          | 4byte有符号整数                                      | 20                                   |
| BIGINT       | long         | 8byte有符号整数                                      | 20                                   |
| BOOLEAN      | boolean      | 布尔类型，true或者false                              | TRUE FALSE                           |
| FLOAT        | float        | 单精度浮点数                                         | 3.14159                              |
| DOUBLE       | double       | 双精度浮点数                                         | 3.14159                              |
| STRING       | string       | 字符系列。可以指定字符集。可以使用单引号或者双引号。 | ‘now is the time’ “for all good men” |
| TIMESTAMP    |              | 时间类型                                             |                                      |
| BINARY       |              | 字节数组                                             |                                      |

类型转换：任何低精度类型在和高精度类型运算时，可以自动向上转型；布尔类型无法转为其他类型。

使用强制类型转换：cast('值' as 类型)，如果强转失败，则为 NULL。

**hive集合数据类型**

| 数据类型 | 描述                                                         | 语法示例 |
| -------- | ------------------------------------------------------------ | -------- |
| STRUCT   | 和c语言中的struct类似，都可以通过“点”符号访问元素内容。例如，如果某个列的数据类型是STRUCT{first STRING, last STRING},那么第1个元素可以通过字段.first来引用。 | struct() |
| MAP      | MAP是一组键-值对元组集合，使用数组表示法可以访问数据。例如，如果某个列的数据类型是MAP，其中键->值对是’first’->’John’和’last’->’Doe’，那么可以通过字段名[‘last’]获取最后一个元素 | map()    |
| ARRAY    | 数组是一组具有相同类型和名称的变量的集合。这些变量称为数组的元素，每个数组元素都有一个编号，编号从零开始。例如，数组值为[‘John’, ‘Doe’]，那么第2个元素可以通过数组名[1]进行引用。 | Array()  |

**hive分隔符（默认）**

![img](E:\note\youdao\qq745C056D5E743D9039FEE344813AA557\a39140c84bf44f9db70ff175ce56f42f\clipboard.png)

^A 输入方式： 在vim中，先进入编辑模式，再按 ctrl+V，再按 ctrl+A

cat -T text.data：带分隔符查看文件中的内容

### 1.2.2 管理表和外部表

在创建表时，如果加了 EXTERNAL，那么创建的表的类型为外部表；

如果没有指定 EXTERNAL，那么创建的表的类型为内部表或管理表。

区别：

① 如果当前表是外部表，那么意味指hive是不负责数据生命周期管理的；如果删除了hive中的表，那么只会删除表的schame信息，而不会删除表目录中的数据。

② 如果为管理表，意味着hive可以管理数据的生命周期；如果删除了hive中的表，那么不仅会删除mysql中的schame信息，还会删除hdfs上的数据。



### 1.2.3 管理表和外部表的转换

可以通过查看表的Table Type属性，来判断表的类型。在hive中除了属性名和属性值，其他不区分大小写。

MANAGED_TABLE--->EXTERNAL_TABLE

```sql
alter table 表名 set TBLPROPERTIES('EXTERNAL'='TRUE')
```

EXTERNAL_TABLE--->MANAGED_TABLE

```sql
alter table 表名 set TBLPROPERTIES('EXTERNAL'='FALSE')
```



### 1.2.4 分区表

#### 1.2.4.1 建表操作

① 创建一个一级分区（只有一个分区字段）表

```sql
create table t2(id int,name string,sex string) partitioned by(province string)
```

② 准备数据

```
1^ATom^Amale
2^AJack^Amale
3^AMarry^Afemale
```

③ 导入数据

put 方式： 只能上传数据，无法对分区表生成元数据。

④ 手动创建分区

手动创建分区，不仅可以生成分区目录，还会生成分区的元数据

```sql
alter table 表名 add partition(分区字段名=分区字段值)
```

查看表的分区元数据

```sql
show partitions 表名
```

⑤或使用命令自动修复分区的元数据

```
msck repair table 表名
```



#### 1.2.4.2 load

```sql
load data local inpath '/home/jeffery/hivedatas/t2.data' into table t2 partition(province='guangxi')
```

load 的方式不仅可以帮我们将数据上传到分区目录，还可以自动生成分区的元数据。

分区的元数据存放在 metastore.PARTITIONS表中。



#### 1.2.4.3 删除分区内数据

```sql
alter table 表名 drop patition(分区字段名=分区字段值),patition(分区字段名=分区字段值)
```

分区结构一经创建就不能修改，只能删除分区内的数据内容。

删除分区一定会删除分区内的元数据，如果表是管理表，还会删除分区目录！



#### 1.2.4.4 多级分区表

```sql
create table t3(id int,name string,sex string) partitioned by(province string,city string,area string)
```

加载数据：

```sql
 load data local inpath '/home/jeffery/hivedatas/t2.data' into table t3 partition(province='guangxi',city='nanning',area='buzhidao')
```

#### 1.2.4.5 动态分区

想要用动态分区要先做一些设置来修改默认的配置。

```java
set hive.exec.dynamic.partition=true;(可通过这个语句查看：set hive.exec.dynamic.partition;) 
set hive.exec.dynamic.partition.mode=nonstrict; 
SET hive.exec.max.dynamic.partitions=100000;(如果自动分区数大于这个参数，将会报错)
SET hive.exec.max.dynamic.partitions.pernode=100000;
```

建立分区表的语法:

```sql
Drop table table_name; --先删除表 没有则直接建表了
CREATE TABLE table_name    --创建表
(col1 string, col2 date, col3 double) 
partitioned by (datekey date)  --可以多个字段的组合分区 
 ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' Stored AS TEXTFILE;
```

插入数据：

```sql
INSERT INTO TABLE table_Name
PARTITION (DateKey)
SELECT col1,col2,col3,DateKey FROM otherTable
WHERE DATEKEY IN ('2017-02-26','2013-06-12','2013-09-24')
GROUP BY col1,col2,col3,DateKey  
DISTRIBUTE BY DateKey
```

注意：insert into 与 insert overwrite 都可以向hive表中插入数据，但是insert into直接追加到表中数据的尾部，而insert overwrite会重写数据，既先进行删除，再写入。如果存在分区的情况，insert overwrite会只重写当前分区数据。


### 1.2.5 分桶表

```sql
-- 指定表中的数据在分桶时以什么字段进行分桶操作
[CLUSTERED BY (col_name, col_name, ...) 
-- 表中的数据在分桶时，以什么字段作为排序的字段
[SORTED BY (col_name [ASC|DESC], ...)] 
INTO num_buckets BUCKETS] 
```

#### 1.2.5.1 概念

​		分桶和MR中的分区是一个概念，指在向表中使用insert 语句导入数据时， insert语句会翻译为一个MR程序，MR程序在运行时，可以根据分桶的字段，对数据进行分桶。

​		同一种类型的数据，就可以分散到同一个文件中！可以对文件根据类型进行抽样查询！

#### 1.2.5.2 注意

​		①如果需要实现分桶，那么必须使用 Insert 的方式向表中导入数据，只有 insert 会运行MR。

​		②分桶的字段是基于表中的已有字段进行选取

​		③如果要实现分桶操作，那么 reduceTask 的个数需要 > 1

#### 1.2.5.3 案例

① 准备数据

```
1001	ss1
1002	ss2
1003	ss3
1004	ss4
1005	ss5
1006	ss6
1007	ss7
1008	ss8
1009	ss9
1010	ss10
1011	ss11
1012	ss12
1013	ss13
1014	ss14
1015	ss15
1016	ss16
```

② 创建分桶表

```sql
create table stu_buck(id int, name string)
clustered by(id) 
into 4 buckets
row format delimited fields terminated by '\t';
```

③ 创建临时表

```sql
create table stu_buck_tmp(id int, name string)
row format delimited fields terminated by '\t';
```

④ 先把数据load到临时表

```sql
load data local inpath '/home/jeffery/hivedatas/t4.data' into table stu_buck_tmp;
```

⑤ 使用insert 语句向分桶表导入数据

​		导入数据之前，需要打开强制分桶的开关：

```
set hive.enforce.bucketing=true;
```

​		需要让reduceTask的个数=分的桶数，但是此时不需要额外设置，默认reduceTask的个数为-1，-1代表由hive自动根据情况设置reduceTask的数量。

```
mapreduce.job.reduces=-1
```

​		导入数据

```sql
insert overwrite table  stu_buck select * from  stu_buck_tmp
```



#### 1.2.5.4 排序

① 创建分桶表，指定按照id进行降序排序

```sql
create table stu_buck2(id int, name string)
clustered by(id) 
SORTED BY (id desc)
into 4 buckets
row format delimited fields terminated by '\t';
```

② 向表中导入数据

​		如果需要执行排序，提前打开强制排序开关。

```
set hive.enforce.sorting=true;
```

​		导入数据

```sql
insert overwrite table  stu_buck2 select * from  stu_buck_tmp
```

#### 1.2.5.5 抽样查询

​		基于分桶表进行抽样查询，表必须是分桶表。

```sql
select * from 分桶表 tablesample(bucket x out of y on 分桶字段);
```

​		假设当前分桶表，一共分了z桶！

​		x: 代表从当前的第几桶开始抽样

​					0<x<=y

​		y:  z/y 代表一共抽多少桶！

​					y必须是z的因子或倍数！

​		怎么抽：  从第x桶开始抽，当y<=z每间隔y桶抽一桶，直到抽满 z/y桶

举例1：

```sql
select * from stu_buck2 tablesample(bucket 1 out of 2 on id);
```

​		从第1桶开始抽，每间隔2桶抽一桶，一共抽2桶！

​			桶号：  x+y*(n-1)  抽0号桶和2号桶

举例2：

```sql
select * from stu_buck2 tablesample(bucket 1 out of 1 on id);
```

​		从第1桶开始抽，每间隔1桶抽一桶，一共抽4桶！

​		 抽0,1,2,3号桶

举例3：

```sql
select * from stu_buck2 tablesample(bucket 2 out of 8 on id);
```

​		从第2桶开始抽，一共抽0.5桶！

​		 抽1号桶的一半

### 1.2.6 基于现有表创建表

① 基于源表，复制其表结构，创建新表，表中无数据。

注：分区表、分桶表均可创建。

```sql
create table 表名 like 源表名
```

② 基于一条查询语句，根据查询语句中字段的名称，类型和顺序创建新表,表中有数据

```sql
create table 表名 as ‘select语句’
```

注意：不能通过此方式创建分区表（可以复制里边的数据，但是分区结构复制不了）

### 1.2.7 删除

```sql
drop table [if exists] 表名
```

清空表中的数据（表必须是管理表）

```sql
truncate table 表名
```



### 1.2.8 查询

查看表的描述

```sql
desc  表名
```

查看表的详细描述

```sql
desc extended 表名
```

格式化表的详细描述

```sql
desc formatted 表名
```

查看表的建表语句

```sql
show create table 表名
```



### 1.2.9 修改

#### 1.2.9.1 修改表的某个属性

```sql
alter table 表名 set TBLPROPERTIES('属性名'='属性值')
```

#### 1.2.9.2 修改列的信息

```sql
ALTER TABLE table_name CHANGE [COLUMN] col_old_name col_new_name column_type [COMMENT col_comment] [FIRST|AFTER column_name]
```

例：

```sql
alter table t1 change id newid int;
alter table t1 change id newid string after sex;
alter table t1 change newid id string first;
```

#### 1.2.9.3 重命名表

```sql
ALTER TABLE table_name RENAME TO new_table_name
```

#### 1.2.9.4 重置表的所有列

```sql
ALTER TABLE table_name REPLACE COLUMNS (col_name data_type [COMMENT col_comment], ...) 
```

#### 1.2.9.5 添加列

```sql
ALTER TABLE table_name ADD COLUMNS (col_name data_type [COMMENT col_comment], ...) 
```

# 2. DML

## 2.1 导入

### 2.1.1 load

```sql
load data [local] inpath '数据路径' [overwrite] into table 表名 [partition]
```

带 local：从本地将数据put到hdfs上的表目录。

不带 local：代表将hdfs上的数据，mv到hdfs上的表的目录。

overwrite：删除表中原有数据。

### 2.1.2 insert

insert导入数据会运行MR程序，在特殊的场景下，只能使用insert不能用load，例如：  

​	① 分桶

​	② 希望向hive表中导入的数据以SequnceFile或ORC等其他格式存储！

语法（支持单条数据插入和批量导入）：

```sql
insert into | overwrite  table 表名 [partition()] values(),(),() | select 语句
```

insert into：向表中追加写

insert overwrite：覆盖写，清空表目录（**hdfs层面，和外部表无关**），再向表中导入数据



多插入模式：从一张源表查询，执行多条insert语句，插入到多个目的表

```sql
from 源表
insert into | overwrite  table 目标表1 select xxxx
insert into | overwrite  table 目标表2 select xxxx
insert into | overwrite  table 目标表3 select xxxx
```

示例：

```sql
from t3
insert overwrite table t31 partition(province='henan',city='mianchi',area='chengguanzhen') select id,name,sex 
where province='guangdong' and city='shenzhen' and area='baoan'
insert overwrite table t32 partition(province='hebei',city='mianchi',area='chengguanzhen') select id,name,sex 
where province='guangxi' and city='liuzhou' and area='buzhidao'
insert overwrite table t33 partition(province='hexi',city='mianchi',area='chengguanzhen') select id,name,sex 
where province='guangxi' and city='nanning' and area='buzhidao'
```

### 2.1.3 location

建表时可以指定表的location属性（表在hdfs上的目录）。适用于数据已经存在在hdfs上了，只需要指定表的目录和数据存放的目录关联即可。

```sql
create table t1(id int, name string, gender string) location '/t1';
```



### 2.1.4 import

注：import 必须导入的数据是由 export 命令导出的数据。

```sql
IMPORT [[EXTERNAL] TABLE new_or_original_tablename [PARTITION (part_column="value"[, ...])]]
  FROM 'source_path'
  [LOCATION 'import_target_path']
```

要求：  

① 如果要导入的表不存在，那么 hive会根据 export表的元数据生成目标表，再导入数据和元数据

② 如果表已经存在，在导入之前会进行 schame 的匹配检查，检查不复合要求，则无法导入。

③ 如果目标表已经存在，且 schame 和要导入的表结构匹配，那么要求要导入的分区必须不能存在。

例：

```sql
import table import1 from '/t2export'
```



## 2.2 导出

### 2.2.1 insert

命令：

```sql
insert overwrite [local] directory '导出的路径'
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'  
select 语句;
```

带 local 导出到本地的文件系统，不带 local 代表导出到 hdfs。

注意：导出路径最后一级必须不存在，同 MapReduce。

### 2.2.2 export

命令：

```sql
EXPORT TABLE tablename [PARTITION (part_column="value"[, ...])]
  TO 'export_target_path' [ FOR replication('eventid') ]
```

优势： 

既导出数据还**导出 metastore(元数据，表结构，且与RDMS无关)**，导出的数据和表结构可以移动到其他的 hadoop 集群或 hive 中，使用 import 导入。

例：

```sql
export table t2 partition (province = 'guangdong') to '/t2export'
```



## 2.3 排序

### 2.3.1 Order by 

​		Order by 代表全排序，即对整个数据集进行排序，要求只能有一个reduceTask。

```sql
select * from emp order by sal desc;
select * from emp order by job,sal desc;
```



### 2.3.2 Sort by

​		sort by代表部分排序，即设置多个reduceTask，每个reduceTask对所持有的分区的数据进行排序。

​		部分排序：  设置多个reduceTask，每个reduceTask对所持有的分区的数据进行排序，每个分区内部整体有序！



① 需要手动修改mapreduce.job.reduces，告诉hive我们需要启动多少个reduceTask

```sql
set mapreduce.job.reduces=3
```

② 进行部分排序

```sql
insert overwrite local directory '/home/jeffery/sortby' select * from emp sort by deptno;
```

注：sort by只是指定排序的字段，无法控制数据按照什么字段进行分区。



### 2.3.3 Distribute by

​		需结合 sort by一起使用。Distribute by 必须写在 sort by 之前（ 先分区，再排序）。Distribute by 用来指定使用什么字段进行分区。

​		需求：按照部门号，对同一个部门的薪水进行降序排序，每一个部门生成一个统计的结果文件。

​		操作：按照部门号进行分区，按薪水进行降序排序。

```sql
insert overwrite local directory '/home/jeffery/sortby' row format delimited fields terminated by '\t'  select * from emp Distribute by deptno sort by sal desc ;
```



### 2.3.4 Cluster by

​		如果sort by 和 distribute by的字段一致，且希望按照asc进行排序，那么可以简写为cluster by 

```sql
distribute by sal sort by sal  asc  等价于   cluster by sal 
```

​		注：如果使用了cluster by，不支持降序，只支持升序。

### 2.3.5 本地模式

​		MR 以 local 模式运行，数据量较小的时候，比 YARN 上运行要快。

```sql
set hive.exec.mode.local.auto=true;  //开启本地mr
//设置local mr的最大输入数据量，当输入数据量小于这个值时采用local  mr的方式，默认为134217728，即128M
set hive.exec.mode.local.auto.inputbytes.max=50000000;
//设置local mr的最大输入文件个数，当输入文件个数小于这个值时采用local mr的方式，默认为4
set hive.exec.mode.local.auto.input.files.max=10;
```



## 2.4 函数

### 2.4.1 函数的分类

UDF(user define function)：用户定义的一进一出的函数。

UDTF(user define table function)：用户定义的表生成函数， 一进多出。

UDAF(user define aggregation function)：用户定义的聚集函数， 多进一出。

函数根据来源分为系统函数和用户自定义的函数。



### 2.4.2 函数的查看

注意： 用户自定义的函数是以库为单位，在创建这个函数时，必须在要使用的库进行创建，否则需要用**库名.函数名**使用函数。

查看所有的函数：

```sql
show functions
```

查看某个函数的介绍：

```sql
desc function 函数名
```

查看某个函数的详细介绍：

```sql
desc function extended 函数名
```



### 2.4.3 NVL

```
nvl(value,default_value) - Returns default value if value is null else returns value
当value是null值时，返回default_value,否则返回value
```

一般用在计算前对null的处理上， nvl 的默认值可以是变量，类似 MySQL 中的IFNULL。

案例：

求有奖金人的平均奖金： avg聚集函数默认忽略Null

```sql
select avg(comm) from emp;
```

求所有人的平均奖金： 提前处理null值！

```sql
select avg(nvl(comm,0)) from emp;
```

### 2.4.4 字符串拼接函数

#### 2.4.4.1 concat

描述：

```
concat(str1, str2, ... strN) - returns the concatenation of str1, str2, ... strN or concat(bin1, bin2, ... binN) - returns the concatenation of bytes in binary data  bin1, bin2, ... binN
Returns NULL if any argument is NULL
```

示例：

```sql
select concat('123','321','abc','cba');
```

concat 可以完成多个字符串的拼接，一旦拼接的字符串中有一个 NULL 值，返回值就为 NULL。因此在 concat 拼接前，一定要先保证数据没有为 NULL 的。

#### 2.4.4.2 concat_ws

描述：

```
concat_ws(separator, [string | array(string)]+) - returns the concatenation of the strings separated by the separator.
```

返回多个字符串或字符串数组的拼接结果，拼接时，每个字符串会使用 separator 作为分割。concat_ws 不受 NULL 值影响， NULL 值会被忽略。

示例：

```sql
SELECT concat_ws('.', 'www', array('facebook', 'com')) ;
```



### 2.4.5 行转列函数

1列N行  转为 1列1行，通常属于聚集函数。

#### 2.4.5 .1 collect_set

描述：

```
collect_set(x) - Returns a set of objects with duplicate elements eliminated
```

返回一组去重后的数据组成的set集合。

示例：

```sql
select collect_set(job) from emp;
```



#### 2.4.5.2 collect_list

描述：

```
collect_list(x) - Returns a list of objects with duplicates
```

返回一组数据组成的list集合，不去重。

示例：

```sql
select collect_list(job) from emp;
```



### 2.4.6 判断句式

#### 2.4.6.1 if

​		类似三元运算符，用于单层判断。

​		语法：

```sql
if('条件判断','为true时','为false时')
```

​		示例：

```sql
select empno,ename,sal,if(sal<1500,'Poor Gay','Rich Gay') from emp;
```



#### 2.4.6.2 case-when

​		类似swith-case，用于多层判断。

​		语法：

```sql
case 列名
	when 值1 then 值2
	when 值3 then 值4
	when 值5 then 值6
	...
	else 值7
end
```

​	示例：

```sql
select empno,ename,job,case job when 'CLERK' then 'a' when 'SALESMAN' then 'b' else 'c' end from emp;
```



### 2.4.7 列转行

#### 2.4.7.1 含义

​		列传行： 1列1行 转为 N列N行

#### 2.4.7.2 explode 

​		描述：

```
explode(a) - separates the elements of array a into multiple rows, or the elements of a map into multiple rows and columns
```

​		explode使用的对象是array或map，可以将一个array中的元素分割为N行1列。

```sql
select explode(friends) from default.t1 where name='songsong';
```

​		explode函数还可以将一个 map中的元素(entry)分割为N行2列。

```sql
select explode(children) from default.t1 where name='songsong';
```

注意： explode 函数在查询时不能写在 select 之外，也不能嵌套在表达式中。若在 select 中写了 explode 函数，select 中只能有 explode 函数不能有别的表达式。

#### 1.4.7.3 lateral view

explode 的临时结果集中的每一行，可以和 explode 之前的所在行的其他字段进行join。上述过程通过 LATERAL VIEW（侧写）实现。

语法：  

```sql
select 临时列名，其他字段
from 表名
-- 将 UDTF函数执行的返回的结果集临时用 临时表名代替，结果集返回的每一列，按照顺序分别以临时--列名代替
lateral view UDTF() 临时表名 as 临时列名,...
```

示例：

```
select  movie,col1
from movie_info
lateral view explode(category) tmp1 as col1
```

注：LATERAL VIEW 支持多级连续调用



### 2.4.8 窗口函数

​		在mysql5.5,5.6版本，不支持窗口函数；在oracle和sqlserver中支持窗口函数；hive支持窗口函数。

```
https://cwiki.apache.org/confluence/display/Hive/LanguageManual+WindowingAndAnalytics
```

​		窗口函数 = 函数 + 窗口

​		函数： 要运行的函数，只有以下函数称为窗口函数：

​		① 开窗函数：

​			LEAD:  用来返回当前行以下行的数据！

​					用法： LEAD (列名 [,offset] [,default])

​					offset是偏移量，默认为1，

​					default： 取不到值就使用默认值代替

​			LAG: 用来返回当前行以上行的数据！

​					用法： LAG (列名 [,offset] [,default])

​					offset是偏移量，默认为1，

​					default： 取不到值就使用默认值代替

​			FIRST_VALUE: 返回指定列的第一个值

​					用法： FIRST_VALUE(列名，[false是否忽略null值])

​			LAST_VALUE:返回指定列的最后一个值

​					用法： LAST_VALUE(列名，[false是否忽略null值])

​		② 标准的聚集函数：MAX,MIN,AVG,COUNT,SUM

​		③ 分析排名函数：

  - RANK()：允许并列，并列后跳号

- ROW_NUMBER()：连续，不并列，不跳号

- DENSE_RANK()：连续，允许并列，并列不跳号

- CUME_DIST()：当前值及以上的所有的值，占总数据集的比例

- PERCENT_RANK()：rank()-1/总数据集 -1

- NTILE(x)：将窗口中的数据平均分配到x个组中，返回当前数据的组号

  注：

  排名函数可以跟over()，但是不能在over()中定义window_clause；先排序，再排名

  排名函数只记号，不负责排序，且必须结合 sort by 一起使用

  

  窗口： 函数在运行时，计算的结果集的范围。窗口函数指以上特定函数在运算时，可以自定义一个窗口（计算的范围）。

#### 2.4.8.1 语法

​		函数  over( [partition by 字段1,字段2] [order by 字段 asc|desc] [window clause] )

​		partition by : 根据某些字段对整个数据集进行分区！

​		order by: 对分区或整个数据集中的数据按照某个字段进行排序！

​		注意： 如果对数据集进行了分区，那么窗口的范围不能超过分区的范围，即窗口必须在区内指定。

#### 2.4.8.3 window clause

```
(ROWS | RANGE) BETWEEN (UNBOUNDED | [num]) PRECEDING AND ([num] PRECEDING | CURRENT ROW | (UNBOUNDED | [num]) FOLLOWING)
(ROWS | RANGE) BETWEEN CURRENT ROW AND (CURRENT ROW | (UNBOUNDED | [num]) FOLLOWING)
(ROWS | RANGE) BETWEEN [num] FOLLOWING AND (UNBOUNDED | [num]) FOLLOWING
```

本质即定义起始行和终止行的范围。

两个特殊情况：

① 当over()既没有写order by，也没有写window 子句时，窗口默认等同于上无边界到下无边界（整个数据集）。

② 当over()中，指定了order by 但是没有指定 window 子句时，窗口默认等同于上无边界到当前行。

另外需要强调的是，支持Over()，但是不支持在over中定义windows子句的函数如下：

**Ranking functions: Rank, NTile, DenseRank, CumeDist, PercentRank.**

**Lead and Lag functions**

注意：同时使用了窗口函数和 group by 后，聚集函数（sum、count、avg、max、min）调用时机略有变化。不使用窗口函数，则聚集函数的调用随着 group by 的进行而进行；若使用了窗口函数，则聚集函数在 group by 调用之后再在窗口函数的限定下调用。

### 2.4.9 常用函数

#### 2.4.9.1 日期函数

```sql
unix_timestamp:返回当前或指定时间的时间戳	
from_unixtime：将时间戳转为日期格式
current_date：当前日期
current_timestamp：当前的日期加时间
*to_date：抽取日期部分
year：获取年
month：获取月
day：获取日
hour：获取时
minute：获取分
second：获取秒
weekofyear：当前时间是一年中的第几周
dayofmonth：当前时间是一个月中的第几天
* months_between： 两个日期间的月份，前-后
* add_months：日期加减月
* datediff：两个日期相差的天数，前-后
* date_add：日期加天数
* date_sub：日期减天数
* last_day：日期的当月的最后一天
* date_format：调整日期格式，例如：SELECT date_format('2015-04-08', 'yyyy-MM');
```

#### 2.4.9.2 取整函数

```
*常用取整函数
round： 四舍五入
ceil：  向上取整
floor： 向下取整
```

#### 2.4.9.3 字符串操作函数

```
常用字符串操作函数
upper： 转大写
lower： 转小写
length： 长度
* trim：  前后去空格
lpad： 使用指定字符向左补齐，到指定长度
rpad： 使用指定字符向右补齐，到指定长度
* regexp_replace： 使用正则表达式匹配目标字符串，匹配成功后替换！
```

#### 2.4.9.4 集合操作

```
集合操作
size： 集合（map和list）中元素的个数
map_keys： 返回map中的key
map_values: 返回map中的value
* array_contains: 判断array中是否包含某个元素
sort_array： 将array中的元素排序
```



### 2.4.10 用户自定义函数

#### 2.4.10.1 编写函数

① 引入依赖

```xml
 <dependency>
            <groupId>org.apache.hive</groupId>
            <artifactId>hive-exec</artifactId>
            <version>1.2.1</version>
  </dependency>
```

② 自定义类，继承UDF类

提供多个evaluate()方法，返回不能是void类型，必须有返回值！可以返回null！

```java
public class MyUDF  extends UDF {

    public String evaluate(String str){

        return "hello "+str;
    }
}
```

#### 2.4.10.2 引入函数

③打包，上传到$HIVE_HOME/auxlib

④重启hive，之后创建函数

```sql
create function 函数名 as '函数的全类名'
```

用户自定义的函数有库的范围，在哪个库下创建，就默认在这个库下使用，否则需要使用库名.函数名调用。

函数一经创建就永久存在与 MySQL 中，下次启动依然可以直接使用。若需要删除函数，则使用：

```sql
drop function 函数名
```

#### 2.4.10.3 UDF 的四种加载方式

第一种：

是最常见但也不招人喜欢的方式是使用 ADD JAR(s) 语句，之所以说是不招人喜欢是，通过该方式添加的 jar 文件只存在于当前会话中，当会话关闭后不能够继续使用该 jar 文件，最常见的问题是创建了永久函数到 metastore 中，再次使用该函数时却提示 ClassNotFoundException。所以使用该方式每次都要使用 ADD JAR(s) 语句添加相关的 jar 文件到 Classpath 中。

 第二种：

修改 hive-site.xml 文件。修改参数 hive.aux.jars.path 的值指向 UDF 文件所在的路径。该参数需要手动添加到 hive-site.xml 文件中。

```properties
<property>
<name>hive.aux.jars.path</name>
<value>file:///jarpath/all_new1.jar,file:///jarpath/all_new2.jar</value>
</property>
```

 第三种：

是在 ${HIVE_HOME} 下创建 auxlib 目录，将 UDF 文件放到该目录中，这样 hive 在启动时会将其中的 jar 文件加载到 classpath 中。（推荐）

 第四种：

是设置 HIVE_AUX_JARS_PATH 环境变量，变量的值为放置 jar 文件的目录，可以拷贝${HIVE_HOME}/conf 中的 hive-env.sh.template 为 hive-env.sh 文件，并修改最后一行的 #export HIVE_AUX_JARS_PATH= 为 exportHIVE_AUX_JARS_PATH=jar 文件目录来实现，或者在系统中直接添加 HIVE_AUX_JARS_PATH 环境变量。

## 2.5 其他操作

### 2.5.1 创建视图（view）

```sql
视图(view)：
        ①视图是一种特殊(逻辑上存在，实际不存在)的表
        ②视图是只读的
        ③视图可以将敏感的字段进行保护，只将用户需要的字段暴露在视图中，保护数据的隐私

创建语法： create view 视图名 as select 语句
```

### 2.5.2 添加 snappy 压缩

#### 2.5.2.1 查看

查看当前集群是否支持snappy压缩：

```
hadoop checknative
```

#### 2.5.2.2 安装

将snappy和hadoop2.7.2编译后的so文件，放置到HADOOP_HOME/lib/native目录下即可。

#### 2.5.2.3 分发

分发至其他节点，否则只有当前节点可以执行snappy压缩。

#### 2.5.2.4 开启Map输出阶段压缩

开启map输出阶段压缩可以减少job中map和Reduce task间数据传输量。具体配置如下：

1．开启hive中间传输数据压缩功能

```sql
set hive.exec.compress.intermediate=true;
```

2．开启mapreduce中map输出压缩功能

```sql
set mapreduce.map.output.compress=true;
```

3．设置mapreduce中map输出数据的压缩方式

```sql
set mapreduce.map.output.compress.codec = org.apache.hadoop.io.compress.SnappyCodec;
```



#### 2.5.2.5 开启 Reduce 输出阶段压缩

1．开启hive最终输出数据压缩功能

```sql
set hive.exec.compress.output=true;
```

2．开启mapreduce最终输出数据压缩

```sql
set mapreduce.output.fileoutputformat.compress=true;
```

3．设置mapreduce最终数据输出压缩方式

```sql
set mapreduce.output.fileoutputformat.compress.codec =
 org.apache.hadoop.io.compress.SnappyCodec;
```

4．设置mapreduce最终数据输出压缩为块压缩

```sql
set mapreduce.output.fileoutputformat.compress.type=BLOCK;
```



### 2.5.3 hive 和 MapReduce 的关系

![img](E:\note\youdao\qq745C056D5E743D9039FEE344813AA557\ea611672aa0947749488d663a11c30f0\clipboard.png)