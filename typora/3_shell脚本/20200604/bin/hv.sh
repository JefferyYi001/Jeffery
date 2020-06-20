#!/bin/bash
if(($#!=1))
then
	echo 请输入单个 start/ stop 命令
fi
# 传参校验
if [ $1 = start ]
then
	nohup hive --service metastore &
	nohup hive --service hiveserver2 &
elif [ $1 = stop ]
then
	ps -ef | grep HiveServer2 | grep -v grep | awk '{print $2}' | xargs kill
	ps -ef | grep metastore | grep -v grep | awk '{print $2}' | xargs kill
else
	echo 请输入单个 start/ stop 命令
fi