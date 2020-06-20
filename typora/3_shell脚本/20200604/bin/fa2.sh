#!/bin/bash
if(($#!=1))
then
	echo 请输入单个 start/ stop 命令
fi
# 传参校验
if [ $1 = start ]
then
	ssh hadoop104 "nohup flume-ng agent -c $FLUME_HOME/conf -n a1 -f $FLUME_HOME/myagents/loghdfs.conf -Dflume.root.logger=INFO,console > /home/atguigu/fa2.log 2>&1 &"
elif [ $1 = stop ]
then
	ssh hadoop104 "ps -ef | grep loghdfs.conf | grep -v grep | awk '{print \$2}' | xargs kill"
else
	echo 请输入单个 start/ stop 命令
fi
