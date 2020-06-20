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
	echo "===== starting zookeeper ====="
	zk.sh start
	echo "===== starting kafka ====="
	kfk.sh start
	echo "===== starting hadoop ====="
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
	echo "===== starting hbase ====="
	start-hbase.sh
	echo "===== starting log service ====="
	log.sh start
	echo "===== starting redis ====="
	redis-server /home/atguigu/myRedis/6379.conf
	xcall jps
elif [ $1 = stop ]
then
	echo "===== stoping redis ====="
	redis-cli -h hadoop103 -p 6379 shutdown
	echo "===== stoping log service ====="
	log.sh stop
	echo "===== stoping hbase ====="
	stop-hbase.sh
	echo "===== stoping kafka ====="
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
    echo "===== stoping zookeeper ====="
	zk.sh stop
	echo "===== stoping hadoop ====="
	hd.sh stop
	xcall jps
else
	echo 请输入单个start/stop作为参数
fi
