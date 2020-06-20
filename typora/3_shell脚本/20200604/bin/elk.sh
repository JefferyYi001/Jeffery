#!/bin/bash
es_home=/opt/module/elasticsearch-6.3.1
kbn_home=/opt/module/kibana-6.3.1-linux-x86_64
case $1 in
	"start")
		for i in hadoop102 hadoop103 hadoop104; do
			ssh $i "source /etc/profile; nohup $es_home/bin/elasticsearch > $es_home/logs/es.log 2>$1 &"
		done

		nohup $kbn_home/bin/kibana > $kbn_home/kibana.log 2>&1 &
		;;

	"stop")
		pkill -f kibana
		for i in hadoop102 hadoop103 hadoop104; do
			ssh $i "source /etc/profile; pkill -f Elasticsearch"
		done
		;;

	*)
		echo "你启动的姿势不正确, 请使用参数 start 来启动es集群, 使用参数 stop 来关闭es集群"
		;;
esac