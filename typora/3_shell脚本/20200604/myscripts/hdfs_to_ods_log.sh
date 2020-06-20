#!/bin/bash
#1. 确定要导入数据的日期
if [ -n "$1" ]
then 
	do_date=$1
else
	do_date=$(date -d 'yesterday' '+%F')
fi

#2. 导入命令 hive默认连接的是default库，如果要使用别的库，请先使用use，或用库名.表名/函数名声明

sql="
use gmall;

load data inpath '/origin_data/gmall/log/topic_start/$do_date' overwrite into table ods_start_log partition(dt='$do_date');

load data inpath '/origin_data/gmall/log/topic_event/$do_date' overwrite into table ods_event_log partition(dt='$do_date');
"
#3. 执行sql
hive -e "$sql"

#4. 为导入的数据创建索引
hadoop jar /opt/module/hadoop-2.7.2/share/hadoop/common/hadoop-lzo-0.4.20.jar com.hadoop.compression.lzo.DistributedLzoIndexer /warehouse/gmall/ods/ods_start_log/dt=$do_date
hadoop jar /opt/module/hadoop-2.7.2/share/hadoop/common/hadoop-lzo-0.4.20.jar com.hadoop.compression.lzo.DistributedLzoIndexer /warehouse/gmall/ods/ods_event_log/dt=$do_date
