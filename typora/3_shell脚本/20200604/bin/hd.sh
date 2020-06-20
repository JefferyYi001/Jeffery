#!/bin/bash
#hadoop集群的一键启动脚本，只接收单个start或stop参数
if(($#!=1))
then
        echo 请输入单个start或stop参数！
        exit
fi

#对传入的单个参数进行校验，且执行相应的启动和停止命令
if [ $1 = start ] || [ $1 = stop ]
then
        $1-dfs.sh
        $1-yarn.sh
        ssh hadoop102 mr-jobhistory-daemon.sh $1 historyserver
else
        echo 请输入单个start或stop参数！
fi
xcall jps
