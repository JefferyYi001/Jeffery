#! /bin/bash
case $1 in
"start"){
	for i in hadoop102 hadoop103 hadoop104
	do
		ssh $i "source /etc/profile && /opt/module/kafka/bin/kafka-server-start.sh -daemon /opt/module/kafka/config/server.properties"
	done
};;
"stop"){
	for i in hadoop102 hadoop103 hadoop104
	do
		ssh $i "source /etc/profile && /opt/module/kafka/bin/kafka-server-stop.sh"
	done
};;
esac
xcall jps
