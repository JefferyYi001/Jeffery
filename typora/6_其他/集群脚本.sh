#!/bin/bash

if(($#=1))
	then
		echo 请输入正确的参数
		exit;
	fi

	if [ $1 = allstart ]
	then
		echo "=================     开始启动所有节点服务 ================="
		echo "================      正在启动Zookeeper    ================"

		for i in hadoop102 hadoop103 hadoop104
		do
        ssh $i '/opt/module/zookeeper/bin/zkServer.sh start'
		done

		echo "=================    正在启动HDFS     ======================"
		ssh hadoop102 'start-dfs.sh'

		echo "=================    正在启动YARN     ======================"
		ssh hadoop103 'start-yarn.sh'

		echo "=================    正在启动Kafka     ====================="
		for i in hadoop102 hadoop103 hadoop104
		do
        ssh $i '/opt/module/kafka/bin/kafka-server-start.sh -daemon /opt/module/kafka/config/server.properties'
		done
		
		echo "=================    正在启动HBase     ====================="
		ssh hadoop102 'start-hbase.sh'
	
	elif [ $1 = allstop ]
		then
		echo "================     开始关闭所有节点服务       ============="
		echo "================     正在关闭Zookeeper         ============="
		for i in hadoop102 hadoop103 hadoop104
		do
        ssh $i '/opt/module/zookeeper/bin/zkServer.sh stop'
		done	
		echo "================     正在关闭HDFS       ==================="
		ssh hadoop102 'stop-dfs.sh'

		echo "================     正在关闭YARN       ==================="
		ssh hadoop103 'stop-yarn.sh'

		echo "=================    正在关闭Kafka     ====================="
		for i in hadoop102 hadoop103 hadoop104
		do
        ssh $i '/opt/module/kafka/bin/kafka-server-stop.sh stop'
		done

		echo "=================    正在关闭HBase     ====================="
		ssh hadoop102 'stop-hbase.sh'
	

	elif [ $1 = dfsstart ]
		then
	echo "=================    正在启动HDFS     ======================"
	ssh hadoop102 'start-dfs.sh'	
	elif [ $1 = dfsstop ]
		then
	echo "=================    正在关闭HDFS     ======================"
	ssh hadoop102 'stop-dfs.sh'

	elif [ $1 = yarnstart ]
		then
			ssh hadoop103 'start-yarn.sh'
	elif [ $1 = yarnstop ]
		then
			ssh hadoop103 'stop-yarn.sh'

	elif [ $1 = zkstart ]
		then
			echo "================      正在启动Zookeeper    ================"

			for i in hadoop102 hadoop103 hadoop104
			do
        	ssh $i '/opt/module/zookeeper/bin/zkServer.sh start'
			done
	elif [ $1 = zkstop ]
		then
			echo "================     正在关闭Zookeeper         ============="
			for i in hadoop102 hadoop103 hadoop104
			do
       	 	ssh $i '/opt/module/zookeeper/bin/zkServer.sh stop'
			done	
	elif [ $1 = kafkastart ]
		then
			echo "=================    正在启动Kafka     ====================="
			for i in hadoop102 hadoop103 hadoop104
			do
        	ssh $i '/opt/module/kafka/bin/kafka-server-start.sh -daemon /opt/module/kafka/config/server.properties'
			done
	elif [ $1 = kafkastop ]
		then
			echo "=================    正在关闭Kafka     ====================="
			for i in hadoop102 hadoop103 hadoop104
			do
        	ssh $i '/opt/module/kafka/bin/kafka-server-stop.sh stop'
			done
	elif [ $1 = hbasestart ]
		then
			echo "=================    正在启动HBase     ====================="
			ssh hadoop102 'start-hbase.sh'
	elif [ $1 = hbasestop ]
		then
			echo "=================    正在关闭HBase     ====================="
			ssh hadoop102 'stop-hbase.sh'
	
	fi