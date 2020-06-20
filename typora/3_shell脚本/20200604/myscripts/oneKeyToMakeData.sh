#!/bin/bash
#hdfs_ods_db.sh first或all 日期
#if [ $1 != first && $1 != all ]
#if [[ $1 != first ]] && [[ $1 != all ]]
#then
#        echo 第一个参数必须为first或all！
#        exit;
#fi

#动态生成application.properties
function createconfs()
{
content="logging.level.root=info\n
spring.datasource.driver-class-name=com.mysql.jdbc.Driver\n
spring.datasource.url=jdbc:mysql://hadoop103:3306/gmall?characterEncoding=utf-8&useSSL=false&serverTime
zone=GMT%2B8\n
spring.datasource.username=root\n
spring.datasource.password=root\n
logging.pattern.console=%m%n\n
mybatis-plus.global-config.db-config.field-strategy=not_null\n
mock.date=$1\n
mock.clear=$2\n
mock.user.count=50\n
mock.user.male-rate=20\n
mock.favor.cancel-rate=10\n
mock.favor.count=100\n
mock.cart.count=20\n
mock.cart.sku-maxcount-per-cart=3\n
mock.order.user-rate=80\n
mock.order.sku-rate=70\n
mock.order.join-activity=1\n
mock.order.use-coupon=1\n
mock.coupon.user-count=10\n
mock.payment.rate=70\n
mock.payment.payment-type=30:60:10\n
mock.comment.appraise-rate=30:10:10:50\n
mock.refund.reason-rate=30:10:20:5:15:5:5"
echo -e $content> ./application.properties
}




#1. 从文件中导入日期
#for do_date in `cat /opt/module/myscripts/date.txt | sort`
#do
#	echo $do_date
#done 



#连续日期循环导入
for((i=0;i<10;i++))
do
	do_date=`date -d '1 days' +'%F'`

echo ------------------当前日期$do_date--------------------

#导入用户行为数据
	dt.sh $do_date

	if [ $i -eq 0 ]
	then
		cluster.sh start
                fa1.sh start
                fa2.sh start
		nohup hive --service metastore &
	fi

	log.sh 20 200
	
#导入业务数据
	if [ $i -eq 0 ]
        then
                createconfs $do_date 1
	else
		createconfs $do_date 0
        fi

	java -jar /opt/module/makeDBDatas/gmall-mock-db-2020-03-16-SNAPSHOT.jar

	sleep 10s
#导入到hdfs
echo -----------------导入$do_date的db数据到hdfs-------------------
	if [ $i -eq 0 ]
        then
                mysql_to_hdfs.sh first $do_date
        else
                mysql_to_hdfs.sh all $do_date
        fi
#导入用户行为日志数据到ods层
echo -----------------导入$do_date的log数据到ods层-------------------
hdfs_to_ods_log.sh $do_date


#导入业务数据到ods层
echo -----------------导入$do_date的db数据到ods层-------------------
if [ $i -eq 0 ]
        then
                hdfs_to_ods_db.sh first $do_date
        else
                hdfs_to_ods_db.sh all $do_date
        fi

#导入用户行为数据到dwd层
echo -----------------导入$do_date的log数据到dwd层-------------------
ods_to_dwd_log.sh $do_date
ods_to_dwd_base_log.sh $do_date
ods_to_dwd_event_log.sh $do_date

#导入业务数据到dwd层
echo -----------------导入$do_date的db数据到dwd层-------------------
if [ $i -eq 0 ]
        then
                ods_to_dwd_db.sh first $do_date
        else
                ods_to_dwd_db.sh all $do_date
        fi
	 
done 

echo -----------------------已经全部结束-----------------------------