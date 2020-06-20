#!/bin/bash
if(($#!=1))
then
	echo 请输入单个 start/ stop 命令
fi
# 传参校验
if [ $1 = start ]
then
	cmd="nohup flume-ng agent -c $FLUME_HOME/conf -n a1 -f $FLUME_HOME/myagents/logkfk.conf -Dflume.root.logger=INFO,console > /home/atguigu/fa1.log 2>&1 &"
elif [ $1 = stop ]
then
	cmd="ps -ef | grep logkfk.conf | grep -v grep | awk '{print \$2}' | xargs kill"
else
	echo 请输入单个 start/ stop 命令
fi
# 执行参数
for i in hadoop102 hadoop103
do
	echo ------------------$i---------------------
	ssh $i $cmd
done
