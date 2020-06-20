for ((i=0;i<13;i++))
do
	do_date=`date -d "$i day" +%F`
	sql="
	use gmall;
	insert overwrite table dwd_fact_favor_info PARTITION(dt='$do_date')
	SELECT id, user_id, sku_id, spu_id, is_cancel, 
	create_time, cancel_time
	FROM gmall.ods_favor_info
	where dt='$do_date';
	"
#3. 执行sql
hive -e "$sql"
done