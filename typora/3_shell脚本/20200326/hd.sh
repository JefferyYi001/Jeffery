#!/bin/bash
#提供对hadoop集群的一键启动和停止，只接受start或stop参数中的一个
#判断参数的个数
if(($#!=1))
then
	echo 请输入start或stop参数中的任意一个
	exit;
fi

#校验参数内容
if [ $1 = start ] || [ $1 = stop ]
then
	$1-dfs.sh
	$1-yarn.sh
	ssh hadoop104 $1-yarn.sh
	ssh hadoop102 mr-jobhistory-daemon.sh $1 historyserver
	xcall jps
else
	 echo 请输入start或stop参数中的任意一个
fi
