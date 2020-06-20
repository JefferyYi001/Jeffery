# 一 HBase基本操作

## 1.命令行操作

##### 进入shell命令界面

```
bin/HBase shell
```

##### 查看操作用户及组信息

```
whoami
```

##### 查看帮助信息

```
help
```

##### 查看具体指令的帮助

```
help 'ddl'
```

##### 查看hbase集群状态

```
status
```



# 二 .DDL

## 1.DDL之库操作

### 增

##### 创建namespace

```shell
create_namespace '库名'
示例:
create_namespace 'ns1'

 create_namespace 'ns1', {'PROPERTY_NAME'=>'PROPERTY_VALUE'}
实例：
create_namespace 'ns1',{'master'=>'childwen'}
```

##### 增加/修改namespace属性

```shell
alter_namespace '库名', {METHOD => 'set', 'PROPERTY_NAME' => 'PROPERTY_VALUE'}
示例:
alter_namespace 'ns1',{METHOD => 'set','master' => 'deng'}
```



### 删

##### 删除namespace

```shell
drop_namespace '库名'
示例:
drop_namespace 'ns1'
```

##### 删除namespace属性

```shell
alter_namespace '库名', {METHOD => 'unset', NAME=>'PROPERTY_NAME'}
示例
alter_namespace 'ns1', {METHOD => 'unset', NAME=>'master'}
```

### 查

##### 查看所有namespace

```shell
list_namespace
```

##### 查看指定namespace下所有表

```shell
list_namespace_tables '库名'
示例:
list_namespace_tables 'ns1'
```

##### 查看namespace描述信息

```shell
describe_namespace'库名'
示例:
describe_namespace 'ns1'
```

## 2.DDL之表操作

```shell
  Group name: ddl
Commands: alter, alter_async, alter_status, create, describe, disable, disable_all, drop, drop_all, enable, enable_all, exists, get_table, is_disabled, is_enabled, list, locate_region, show_filters
```



### 增

##### 创建表

```shell
Creates a table. Pass a table name, and a set of column family
specifications (at least one), and, optionally, table configuration.
Column specification can be a simple string (name), or a dictionary
(dictionaries are described below in main help output), necessarily 
including NAME attribute. 
#创建一个表,至少需要传递一个表名和一组列族,以及可选的表配置,可以是简单的字符串
Examples:

#在namespace=ns1下创建表t1,列族f1
create 'ns1:t1', {NAME => 'f1', VERSIONS => 5}

#在默认的default库下创建表t1的几种方式
  #方式一:创建表,不添加列族属性
  hbase> create 't1', {NAME => 'f1'}, {NAME => 'f2'}, {NAME => 'f3'}
  #方式二:不添加列族属性时可简化
  create 't1', 'f1', 'f2', 'f3'
  #方式三:创建表,添加多个列族属性
  create 't1', {NAME => 'f1', VERSIONS => 1, TTL => 2592000, BLOCKCACHE => true}
  #方式四:创建表,列族属性嵌套
  hbase> create 't1', {NAME => 'f1', CONFIGURATION => {'hbase.hstore.blockingStoreFiles' => '10'}}
  
Table configuration options can be put at the end.
# 表配置选项可以放在最后
Examples:
#在namespace=ns1下创建表t1
  hbase> create 'ns1:t1', 'f1', SPLITS => ['10', '20', '30', '40']
  
#在默认的default库下创建表t1
  hbase> create 't1', 'f1', SPLITS => ['10', '20', '30', '40']
  hbase> create 't1', 'f1', SPLITS_FILE => 'splits.txt', OWNER => 'johndoe'
  hbase> create 't1', {NAME => 'f1', VERSIONS => 5}, METADATA => { 'mykey' => 'myvalue' }
  hbase> # Optionally pre-split the table into NUMREGIONS, using
  hbase> # SPLITALGO ("HexStringSplit", "UniformSplit" or classname)
  hbase> create 't1', 'f1', {NUMREGIONS => 15, SPLITALGO => 'HexStringSplit'}
  hbase> create 't1', 'f1', {NUMREGIONS => 15, SPLITALGO => 'HexStringSplit', REGION_REPLICATION => 2, CONFIGURATION => {'hbase.hregion.scan.loadColumnFamiliesOnDemand' => 'true'}}
  hbase> create 't1', {NAME => 'f1', DFS_REPLICATION => 1}

You can also keep around a reference to the created table:

  hbase> t1 = create 't1', 'f1'

Which gives you a reference to the table named 't1', on which you can then
call methods.
```



### 删

##### 删除表

```shell
删除指定的表,必须首先禁用表:
  hbase> drop 't1'
  hbase> drop 'ns1:t1'
 
Drop all of the tables matching the given regex:
#删除所有与给定正则表达式匹配的表:
hbase> drop_all 't.*'
hbase> drop_all 'ns:t.*'
hbase> drop_all 'ns:.*'
```

### 改

```shell
Alter a table. If the "hbase.online.schema.update.enable" property is set to
false, then the table must be disabled (see help 'disable'). If the 
"hbase.online.schema.update.enable" property is set to true, tables can be 
altered without disabling them first. Altering enabled tables has caused problems 
in the past, so use caution and test it before using in production. 
改变一个表。如果“hbase.online.schema.update。属性设置为
false，则必须禁用该表(请参阅帮助“禁用”)。如果
“hbase.online.schema.update。使“属性设置为真，表可以
在没有禁用它们之前改变。更改启用的表会导致问题
在过去，请谨慎使用，并在生产中使用前进行测试。

You can use the alter command to add, 
modify or delete column families or change table configuration options.
Column families work in a similar way as the 'create' command. The column family
specification can either be a name string, or a dictionary with the NAME attribute.
Dictionaries are described in the output of the 'help' command, with no arguments.
您可以使用alter命令添加，
修改或删除列系列或更改表配置选项。
列族的工作方式与“创建”命令类似。 列族
规范可以是名称字符串，也可以是具有NAME属性的字典。
字典在'help'命令的输出中描述，不带参数。

For example, to change or add the 'f1' column family in table 't1' from 
current value to keep a maximum of 5 cell VERSIONS, do:
例如，要更改或添加表“ t1”中的“ f1”列族
当前值以保持最多5个单元格版本，请执行以下操作：
  hbase> alter 't1', NAME => 'f1', VERSIONS => 5

You can operate on several column families:
您可以对多个列系列进行操作：
  hbase> alter 't1', 'f1', {NAME => 'f2', IN_MEMORY => true}, {NAME => 'f3', VERSIONS => 5}

To delete the 'f1' column family in table 'ns1:t1', use one of:

  hbase> alter 'ns1:t1', NAME => 'f1', METHOD => 'delete'
  hbase> alter 'ns1:t1', 'delete' => 'f1'

You can also change table-scope attributes like MAX_FILESIZE, READONLY, 
MEMSTORE_FLUSHSIZE, DURABILITY, etc. These can be put at the end;
for example, to change the max size of a region to 128MB, do:

  hbase> alter 't1', MAX_FILESIZE => '134217728'

You can add a table coprocessor by setting a table coprocessor attribute:

  hbase> alter 't1',
    'coprocessor'=>'hdfs:///foo.jar|com.foo.FooRegionObserver|1001|arg1=1,arg2=2'

Since you can have multiple coprocessors configured for a table, a
sequence number will be automatically appended to the attribute name
to uniquely identify it.

The coprocessor attribute must match the pattern below in order for
the framework to understand how to load the coprocessor classes:

  [coprocessor jar file location] | class name | [priority] | [arguments]

You can also set configuration settings specific to this table or column family:

  hbase> alter 't1', CONFIGURATION => {'hbase.hregion.scan.loadColumnFamiliesOnDemand' => 'true'}
  hbase> alter 't1', {NAME => 'f2', CONFIGURATION => {'hbase.hstore.blockingStoreFiles' => '10'}}

You can also remove a table-scope attribute:

  hbase> alter 't1', METHOD => 'table_att_unset', NAME => 'MAX_FILESIZE'

  hbase> alter 't1', METHOD => 'table_att_unset', NAME => 'coprocessor$1'

You can also set REGION_REPLICATION:

  hbase> alter 't1', {REGION_REPLICATION => 2}

There could be more than one alteration in one command:

  hbase> alter 't1', { NAME => 'f1', VERSIONS => 3 }, 
   { MAX_FILESIZE => '134217728' }, { METHOD => 'delete', NAME => 'f2' },
   OWNER => 'johndoe', METADATA => { 'mykey' => 'myvalue' }
hbase(main):005:0> 
hbase(main):005:0> help 'alter'
Alter a table. If the "hbase.online.schema.update.enable" property is set to
false, then the table must be disabled (see help 'disable'). If the 
"hbase.online.schema.update.enable" property is set to true, tables can be 
altered without disabling them first. Altering enabled tables has caused problems 
in the past, so use caution and test it before using in production. 

You can use the alter command to add, 
modify or delete column families or change table configuration options.
Column families work in a similar way as the 'create' command. The column family
specification can either be a name string, or a dictionary with the NAME attribute.
Dictionaries are described in the output of the 'help' command, with no arguments.

For example, to change or add the 'f1' column family in table 't1' from 
current value to keep a maximum of 5 cell VERSIONS, do:

  hbase> alter 't1', NAME => 'f1', VERSIONS => 5

You can operate on several column families:

  hbase> alter 't1', 'f1', {NAME => 'f2', IN_MEMORY => true}, {NAME => 'f3', VERSIONS => 5}

To delete the 'f1' column family in table 'ns1:t1', use one of:

  hbase> alter 'ns1:t1', NAME => 'f1', METHOD => 'delete'
  hbase> alter 'ns1:t1', 'delete' => 'f1'

You can also change table-scope attributes like MAX_FILESIZE, READONLY, 
MEMSTORE_FLUSHSIZE, DURABILITY, etc. These can be put at the end;
for example, to change the max size of a region to 128MB, do:

  hbase> alter 't1', MAX_FILESIZE => '134217728'

You can add a table coprocessor by setting a table coprocessor attribute:

  hbase> alter 't1',
    'coprocessor'=>'hdfs:///foo.jar|com.foo.FooRegionObserver|1001|arg1=1,arg2=2'

Since you can have multiple coprocessors configured for a table, a
sequence number will be automatically appended to the attribute name
to uniquely identify it.

The coprocessor attribute must match the pattern below in order for
the framework to understand how to load the coprocessor classes:

  [coprocessor jar file location] | class name | [priority] | [arguments]

You can also set configuration settings specific to this table or column family:

  hbase> alter 't1', CONFIGURATION => {'hbase.hregion.scan.loadColumnFamiliesOnDemand' => 'true'}
  hbase> alter 't1', {NAME => 'f2', CONFIGURATION => {'hbase.hstore.blockingStoreFiles' => '10'}}

You can also remove a table-scope attribute:

  hbase> alter 't1', METHOD => 'table_att_unset', NAME => 'MAX_FILESIZE'

  hbase> alter 't1', METHOD => 'table_att_unset', NAME => 'coprocessor$1'

You can also set REGION_REPLICATION:

  hbase> alter 't1', {REGION_REPLICATION => 2}

There could be more than one alteration in one command:

  hbase> alter 't1', { NAME => 'f1', VERSIONS => 3 }, 
   { MAX_FILESIZE => '134217728' }, { METHOD => 'delete', NAME => 'f2' },
   OWNER => 'johndoe', METADATA => { 'mykey' => 'myvalue' }

```



### 禁用表

```shell
Start disable of named table:
#启动禁用的命名表:
  hbase> disable 't1'
  hbase> disable 'ns1:t1'
```



### 启动表

```shell
Start enable of named table:
#启动启用的命名表: 
hbase> enable 't1'
hbase> enable 'ns1:t1'
  
Enable all of the tables matching the given regex:
#启用所有与给定正则表达式匹配的表:
hbase> enable_all 't.*'
hbase> enable_all 'ns:t.*'
hbase> enable_all 'ns:.*'
```

