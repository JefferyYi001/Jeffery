#!/bin/bash
if (($#!=1)) 
then
	echo 请输入单个start或stop参数！
	exit
fi

if [ $1 = start ]
then
	xcall kafka-server-start.sh -daemon $KAFKA_HOME/config/server.properties
elif [ $1 = stop ]
	then xcall kafka-server-stop.sh
else
	echo 请输入单个start或stop参数！
fi
xcall jps