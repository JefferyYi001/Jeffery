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



"
#3. 执行sql
hive -e "$sql"
