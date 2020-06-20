#!/bin/bash
#校验参数是否合法
if(($#!=1))
then
	echo 请输入单个start/stop作为参数
	exit;
fi

#判断kafka进程是否运行
function countKafkaBrokers(){
	count=0
	for((i=102;i<=104;i++))
	do
		result=$(ssh hadoop$i "jps | grep Kafka | wc -l")
		count=$[$count+$result]
	done
	return $count
}

if [ $1 = start ]
then
	zk.sh start
	kfk.sh start
	hd.sh start
	while [ 1 ]
	do
		countKafkaBrokers
		if(($?==3))
		then
			break
		fi
		sleep 2s
	done
	fa1.sh start
	fa2.sh start
	xcall jps
elif [ $1 = stop ]
then
	fa1.sh stop
	fa2.sh stop
	kfk.sh stop
	while [ 1 ]
        do
                countKafkaBrokers
                if(($?==0))
                then
                        break
                fi
                sleep 2s
        done
	zk.sh stop
	hd.sh stop
	xcall jps
else
	echo 请输入单个start/stop作为参数
fi
