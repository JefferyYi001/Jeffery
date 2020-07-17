# 1. 简介

## 1.1 介绍

​	Sqoop 是 SQL To Hadoop 的缩写。Sqoop 是一款 ETL 工具，可以将关系型数据库的数据导入导出到 Hadoop 集群。

## 1.2 原理

​	Sqoop 将用户编写的 Sqoop 命令翻译为 MR 程序，MR 程序读取关系型数据库中的数据，写入到 HDFS 或读取 HDFS 上的数据，写入到关系型数据库。

​	在 MR 程序中如果要读取关系型数据库中的数据，必须指定输入格式为 DBInputformat；

​	在 MR 程序中如果要向关系型数据库写入数据，必须指定输出格式为 DBOutputformat。

​	Sqoop 命令运行的 MR 程序，只有 Map 阶段，没有 Reduce 阶段；只需要做数据传输，不需要对数据进行合并和排序。

## 1.3 版本兼容

​	Sqoop2 不兼容 Sqoop1，目前使用的是 Sqoop1 的 1.46 版本。

## 1.4 安装和配置

### 1.4.1 安装

​		解压即可。

### 1.4.2 配置

环境的配置： 

​		需配置 HADOOP_HOME 环境变量；

​		如果要将 MySQL 的数据导入到 hive 中，必须保证当前 bash 有 HIVE_HOME；

​		如果要将 MySQL 的数据导入到 hbase 中，必须保证当前 bash 有 HBASE_HOME 以及 ZOOKEEPER_HOME。

将访问关系型数据库的 JDBC 驱动放入到 Sqoop 的 lib 目录下

```bash
cp mysql-connector-java-5.1.27-bin.jar /opt/module/sqoop/lib/
```

## 1.5 测试连接关系型数据库

```bash
bin/sqoop list-databases --connect jdbc:mysql://hadoop103:3306/ --username root --password root
```

# 2. Sqoop 的导入

## 2.1 导入介绍

import：将关系型数据库的数据导入到 hdfs。

## 2.2 数据准备

```sql
CREATE TABLE `t_emp` (
 `id` INT(11) NOT NULL AUTO_INCREMENT,
 `name` VARCHAR(20) DEFAULT NULL,
  `age` INT(3) DEFAULT NULL,
 `deptId` INT(11) DEFAULT NULL,
empno int  not null,
 PRIMARY KEY (`id`),
 KEY `idx_dept_id` (`deptId`)
 #CONSTRAINT `fk_dept_id` FOREIGN KEY (`deptId`) REFERENCES `t_dept` (`id`)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

```

```sql
INSERT INTO t_emp(NAME,age,deptId,empno) VALUES('风清扬',90,1,100001);
INSERT INTO t_emp(NAME,age,deptId,empno) VALUES('岳不群',50,1,100002);
INSERT INTO t_emp(NAME,age,deptId,empno) VALUES('令狐冲',24,1,100003);
INSERT INTO t_emp(NAME,age,deptId,empno) VALUES('洪七公',70,2,100004);
INSERT INTO t_emp(NAME,age,deptId,empno) VALUES('乔峰',35,2,100005);
INSERT INTO t_emp(NAME,age,deptId,empno) VALUES('灭绝师太',70,3,100006);
INSERT INTO t_emp(NAME,age,deptId,empno) VALUES('周芷若',20,3,100007);
INSERT INTO t_emp(NAME,age,deptId,empno) VALUES('张三丰',100,4,100008);
INSERT INTO t_emp(NAME,age,deptId,empno) VALUES('张无忌',25,5,100009);
INSERT INTO t_emp(NAME,age,deptId,empno) VALUES('韦小宝',18,null,100010);
```

## 2.3 导入案例

### 2.3.1 导入参数说明

| 参数名                 | 说明                                                         | 备注                                                         |
| ---------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| --connect              | 连接的关系型数据库的 url                                     |                                                              |
| --username             | 使用哪个用户登录                                             |                                                              |
| --password             | 用户的密码                                                   |                                                              |
| --table                | 要导入哪个表的数据                                           |                                                              |
| --target-dir           | 导入到 hdfs 的哪个路径                                       |                                                              |
| --delete-target-dir    | MR程序要求输出目录必须不存在，如果目标目标存在就删除后再导入 |                                                              |
| --fields-terminated-by | 导入到 Hdfs 上后，每个字段使用什么参数进行分割               |                                                              |
| --num-mappers          | 要启动几个 MapTask                                           | 默认 4 个                                                    |
| --split-by             | 数据集根据哪个字段进行切分，切分后每个  MapTask 负责一部分   |                                                              |
| --column               | 导入表的哪些列                                               |                                                              |
| --where                | 指定过滤的 where 语句                                        | where 语句最好使用引号包裹                                   |
| --query                | 自由查询                                                     | 如果使用了--query，就不能指定--table、--columns和 --where。--query 语句后面必须拼接 $CONDITIONS 占位符。--query 必须跟--target-dir。 |
| hive-import            | 导入到 hive                                                  |                                                              |
| --hive-overwrite       | 覆盖 hive 表目录导入                                         | 防止导入一半时，程序报错；重新导入时造成数据重复。           |
| --hive-table           | 指定要导入的hive的表名                                       |                                                              |
| --hbase-create-table   | hbase中的表不存在是否要创建                                  |                                                              |
| --hbase-row-key        | 将导入数据的哪一列作为rowkey                                 |                                                              |
| --hbase-table          | 导入的hbase的表名                                            |                                                              |
| --column-family        | 导入的列族                                                   |                                                              |



### 2.3.2 全表导入 HDFS

```shell
bin/sqoop import \
--connect jdbc:mysql://hadoop103:3306/mydb \
--username root \
--password root \
--table t_emp \
--target-dir /emp \
--delete-target-dir \
--fields-terminated-by "\t" \
--num-mappers 2 \
--split-by id
```

### 2.3.3 只将部分列导入 HDFS

```shell
bin/sqoop import \
--connect jdbc:mysql://hadoop103:3306/mydb \
--username root \
--password root \
--table t_emp \
--columns id,name,age \
--target-dir /emp \
--delete-target-dir \
--fields-terminated-by "\t" \
--num-mappers 2 \
--split-by id
```

### 2.3.4 自定义将部分列导入 HDFS

使用 --where 指定一个 where 语句

```shell
bin/sqoop import \
--connect jdbc:mysql://hadoop103:3306/mydb \
--username root \
--password 123456 \
--table t_emp \
--columns id,name \
--where 'id>6' \
--target-dir /emp \
--delete-target-dir \
--fields-terminated-by "\t" \
--num-mappers 2 \
--split-by id
```

### 2.3.5 自定义查询导入 HDFS

使用 --query 'select语句'，根据查询语句执行的结果将数据进行导入。

注意： ① 如果使用了 --query，就不能指定 --table、--columns 和 --where。

​					--query 和 --table 一定不能同时存在；

​					--where 和 --query 同时存在时，--where 失效；

​					--columns 和 --query 同时存在时，columns 有效；

​					但是不建议 --query 和 --columns、--where 一起写。

​			② 如果是并行导入，此时 sqoop 会让每个 MapTask 都执行指定的查询语句，并且会自动在执行查询语句时，为语句拼接上边界值。在拼接时，sqoop需要用户在查询语句后面拼接 $CONDITIONS 占位符，这个占位符会在执行时，自动被替换。

​			③ --query 必须跟 --target-dir。

```shell
bin/sqoop import \
--connect jdbc:mysql://hadoop103:3306/mydb \
--username root \
--password root \
--columns id,name \
--where 'id>6' \
--query 'select * from t_emp where id>3 and $CONDITIONS' \
--target-dir /emp \
--delete-target-dir \
--fields-terminated-by "\t" \
--num-mappers 2 \
--split-by id
```

建议写法：

```shell
bin/sqoop import \
--connect jdbc:mysql://hadoop103:3306/mydb \
--username root \
--password root \
--query 'select * from t_emp where id>3 and $CONDITIONS' \
--target-dir /emp \
--delete-target-dir \
--fields-terminated-by "\t" \
--num-mappers 2 \
--split-by id
```

注意：即使 --query 里不加 where 语句，也必须加占位符 $CONDITIONS。

### 3.6 导入到 hive

```shell
bin/sqoop import \
--connect jdbc:mysql://hadoop103:3306/mydb \
--username root \
--password root \
--query 'select * from t_emp where id>3 and $CONDITIONS' \
--target-dir /emp \
--delete-target-dir \
--fields-terminated-by "\t" \
--hive-import \
--hive-overwrite \
--hive-database mydb1 \
--hive-table t_emp \
--num-mappers 2 \
--split-by id
```

导入到 hive 时，分为两步：首先运行 MR 程序，将数据导入到 hdfs 指定的目录；再运行程序将目录中的数据导入（move）到 hive 的表目录。如果 hive 中没有目标表，sqoop 会自动根据要导入的字段名称和类型，自动创建目标表，但是不建议这样做。

### 3.7 导入到 hbase

sqoop 在导入到 hbase 时，hbase 中的表如果不存在，是可以自动创建的。但是由于安装的hbase 版本过高，sqoop 在建表时会报错。

解决：要么手动在 hbase 中建表；要么将 sqoop1.4.6 的源码和 Hbase1.3.1 的源码重新编译后打包，替换之前的jar包。

```shell
bin/sqoop import \
--connect jdbc:mysql://hadoop103:3306/mydb \
--username root \
--password root \
--query 'select * from t_emp where id>3 and $CONDITIONS' \
--target-dir /emp \
--delete-target-dir \
--hbase-create-table \
--hbase-table "ns1:t_emp" \
--hbase-row-key "id" \
--column-family "info" \
--num-mappers 2 \
--split-by id
```

非 default 命名空间使用 “命名空间名:表名” 表示。  

# 3. 导出

## 3.1 导出介绍

​		从 hdfs 将数据导入到 mysql 等关系型数据库。

## 3.2 案例介绍

### 3.2.1 导出参数说明

| 参数名                       | 参数说明                | 备注                 |
| ---------------------------- | ----------------------- | -------------------- |
| --table                      | 导出的表名              | 导出的表需要自己创建 |
| --export-dir                 | hdfs 上导出的数据的路径 |                      |
| --input-fields-terminated-by | hdfs 上数据的分隔符     |                      |



### 3.2.2 从 hive/hdfs 导出

需事先在 MySQL 中完成表结构的创建。

```sql
CREATE TABLE t_emp2 LIKE t_emp;
```

从 hdfs 导出：

```shell
bin/sqoop export \
--connect 'jdbc:mysql://hadoop103:3306/mydb?useUnicode=true&characterEncoding=utf-8' \
--username root \
--password root \
--table t_emp3 \
--num-mappers 1 \
--export-dir /emp \
--input-fields-terminated-by "\t"
```

从 hive 导出：

```shell
bin/sqoop export \
--connect 'jdbc:mysql://hadoop103:3306/mydb?useUnicode=true&characterEncoding=utf-8' \
--username root \
--password root \
--table t_emp2 \
--num-mappers 1 \
--export-dir /hive/mydb1.db/t_emp \
--input-fields-terminated-by "\t"
```

自定义 hive 的表结构：

```
CREATE TABLE `t_emp`(
  `id` int, 
  `name` string, 
  `age` int, 
  `deptid` int, 
  `empno` int)
ROW FORMAT DELIMITED 
FIELDS TERMINATED BY '\t'
```

实为将 sqoop 中的数据，使用以下语句插入到 mysql。

```sql
INSERT INTO t_emp2 (id, name, age, deptId, empno) VALUES (5, '乔峰', 35, 2, 100005), (6, '灭绝师太', 70, 3, 100006), (7, '周芷若', 20, 3, 100007), (8, '张三丰', 100, 4, 100008), (9, '张无忌', 25, 5, 100009), (10, '韦小宝', 18, null, 100010)
```



### 3.2.3 导入重复主键的数据

在向 mysql 表中插入数据时，如果插入的数据的主键和表中已有数据的主键冲突，那么会报错

```
错误代码： 1062
Duplicate entry '5' for key 'PRIMARY'
```

解决方法：指定当插入时，主键重复时时，对于重复的记录，只做更新，不做插入。

```sql
INSERT INTO t_emp2 VALUE(5,'jack',30,3,1111) 
ON DUPLICATE KEY UPDATE NAME=VALUES(NAME),deptid=VALUES(deptid),
empno=VALUES(empno);
```



### 3.2.4 默认导出模式

在使用 Sqoop 时，遇到重复主键的问题，也可以指定 Sqoop 的处理方式。

默认情况下，导出数据，遇到重复主键的问题，会报错。

```
ERROR [Thread-10] org.apache.sqoop.mapreduce.AsyncSqlOutputFormat: Got exception in update thread: com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Duplicate entry '5' for key 'PRIMARY'
```

如果用默认情况的导出，那么每一条导出的记录都会使用 Insert 语句向目标表插入数据。若不希望在插入数据时触犯目标表的约束条件，仅在向一个空表导出数据时使用默认的导出方式。



### 3.2.5 updateonly 模式

如果向一个非空的表导入数据时，可以指定--update-key参数，这样会根据指定的列，作为参考，更新指定列所在行的其他列！

```shell
bin/sqoop export \
--connect 'jdbc:mysql://hadoop103:3306/mydb?useUnicode=true&characterEncoding=utf-8' \
--username root \
--password root \
--table t_emp2 \
--num-mappers 1 \
--export-dir /hive/t_emp \
--input-fields-terminated-by "\t" \
--update-key id
```

将sqoop导出转为update语句，只进行更新！

```sql
UPDATE `t_emp2` SET name='张无忌', age=25, deptId=5, empno=100009 WHERE id=9
```



### 3.2.6  allowinsert 模式

```shell
bin/sqoop export \
--connect 'jdbc:mysql://hadoop103:3306/mydb?useUnicode=true&characterEncoding=utf-8' \
--username root \
--password root \
--table t_emp2 \
--num-mappers 1 \
--export-dir /hive/mydb1.db/t_emp \
--input-fields-terminated-by "\t" \
--update-key id \
--update-mode  allowinsert
```

此时数据在插入 mysql 时，使用以下语句：

```sql
INSERT INTO `t_emp2`(id, name, age, deptId, empno) VALUES(5, '乔峰', 35, 2, 100005),(6, '灭绝师太', 70, 3, 100006),(7, '周芷若', 20, 3, 100007),(8, '张三丰', 100, 4, 100008),(9, '张无忌', 25, 5, 100009),(10, '韦小宝', 18, null, 100010) ON DUPLICATE KEY UPDATE id=VALUES(id), name=VALUES(name), age=VALUES(age), deptId=VALUES(deptId), empno=VALUES(empno)
```



### 3.2.7 开启 mysql 的 binlog 记录

开启 binlog，可以查看导出命令在插入数据到数据库底层时，执行的语句。

```
sudo vim /etc/my.cnf
在新文件中添加如下代码：
[mysqld]
#开启binlog日志功能
log-bin=mysql-bin
```

重启 mysql 服务

```bash
sudo service mysql restart
```

前往 /var/lib/mysql/ 目录，使用 mysqlbinlog 工具查看日志

```bash
cd /var/lib/mysql/
sudo mysqlbinlog mysql-bin.000001
```

注意：该方法会导致 hive 不能建库，报以下异常：

```
FAILED: Execution Error, return code 1 from org.apache.hadoop.hive.ql.exec.DDLTask. MetaException(message:For direct MetaStore DB connections, we don't support retries at the client level.)
```

建库时将 my.cnf 注释起来即可。

# 4. 脚本模式运行

编写自定义脚本：

```bash
vim mysqoop
```

```bash
export
--connect
'jdbc:mysql://hadoop103:3306/mydb?useUnicode=true&characterEncoding=utf-8'
--username
root
--password
root
--table
t_emp2
--num-mappers
1
--export-dir
/hive/mydb1.db/t_emp
--input-fields-terminated-by 
"\t"
```

注意：将 sqoop 编写进脚本后，参数名和参数值之间需要使用换行。

运行脚本：

```bash
bin/sqoop --options-file mysqoop
```