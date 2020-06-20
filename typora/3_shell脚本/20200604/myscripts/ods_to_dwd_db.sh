#!/bin/bash
#hdfs_ods_db.sh first或all 日期
if [ $1 != first ] && [ $1 != all ]
then
        echo 第一个参数必须为first或all！
        exit;
fi

#1. 确定要导入数据的日期
if [ -n "$2" ]
then 
	do_date=$2
else
	do_date=$(date -d 'yesterday' '+%F')
fi

#2. 导入命令 hive默认连接的是default库，如果要使用别的库，请先使用use，或用库名.表名/函数名声明

sql="

use gmall;

set hive.exec.dynamic.partition.mode=nonstrict;

WITH 
t1 as
(select id category1_id,name category1_name FROM ods_base_category1 where dt='$do_date'), 
t2 as
(select id category2_id,name category2_name,category1_id FROM ods_base_category2 where dt='$do_date'),
t3 as
(select id category3_id,name category3_name,category2_id FROM ods_base_category3 where dt='$do_date'),
t4 as
(SELECT id spu_id, spu_name from ods_spu_info where dt='$do_date'),
t5 as
(SELECT tm_id , tm_name from ods_base_trademark where dt='$do_date'),
t6 as
(SELECT id,spu_id,price,sku_name, sku_desc ,weight, tm_id, category3_id,create_time from ods_sku_info where dt='$do_date')
insert overwrite table dwd_dim_sku_info PARTITION(dt='$do_date')
SELECT t6.id, t6.spu_id, t6.price, t6.sku_name, t6.sku_desc,
t6.weight, t6.tm_id, t5.tm_name, 
t6.category3_id, t2.category2_id, 
t1.category1_id, t3.category3_name,
t2.category2_name, t1.category1_name, t4.spu_name, t6.create_time
FROM t6 left join t5 on t6.tm_id=t5.tm_id
left join t4 on t6.spu_id=t4.spu_id
left join t3 on t3.category3_id=t6.category3_id
left join t2 on t3.category2_id=t2.category2_id
left join t1 on t1.category1_id=t2.category1_id;

INSERT overwrite TABLE dwd_dim_coupon_info PARTITION(dt='$do_date')
SELECT id, coupon_name, coupon_type, condition_amount, condition_num,
activity_id, benefit_amount, benefit_discount, create_time, range_type, 
spu_id, tm_id, category3_id, limit_num, operate_time, expire_time
FROM ods_coupon_info
where dt='$do_date';

INSERT overwrite table dwd_dim_activity_info PARTITION(dt='$do_date')
SELECT t1.id, t1.activity_name, t1.activity_type,
t2.condition_amount, t2.condition_num, t2.benefit_amount, 
t2.benefit_discount, t2.benefit_level, 
t1.start_time, t1.end_time, t1.create_time
FROM
(SELECT id,activity_name, activity_type,
start_time, end_time, create_time
FROM ods_activity_info  WHERE dt='$do_date') t1
JOIN
(SELECT activity_id,condition_amount, condition_num, benefit_amount, benefit_discount, 
benefit_level
FROM ods_activity_rule  WHERE dt='$do_date') t2
on t1.id=t2.activity_id;

insert overwrite table dwd_fact_order_detail PARTITION(dt='$do_date')
select 
    t1.id,  t1.order_id,  t1.user_id,  t1.sku_id,  t1.sku_name,
     t1.order_price,  t1.sku_num,  t1.create_time, 
    t2.province_id,
    order_price*sku_num total_amount
from
(select 
    *
from ods_order_detail
where dt='$do_date') t1
left join 
(select 
    id,province_id
from ods_order_info
where dt='$do_date') t2
on t1.order_id=t2.id;

insert overwrite table dwd_fact_payment_info PARTITION(dt='$do_date')
select 
    t1.id, t1.out_trade_no, t1.order_id, t1.user_id,
    t1.alipay_trade_no, t1.total_amount payment_amount, t1.subject,
    t1.payment_type, t1.payment_time,
    t2.province_id
from
(select * from ods_payment_info where dt='$do_date') t1
LEFT JOIN
(SELECT id,province_id from ods_order_info where dt='$do_date')t2
on t1.order_id=t2.id;

insert overwrite table dwd_fact_order_refund_info PARTITION(dt='$do_date')
SELECT id, user_id, order_id, sku_id, refund_type, 
refund_num, refund_amount, refund_reason_type,
create_time
FROM ods_order_refund_info
where dt='$do_date';

insert overwrite table dwd_fact_comment_info PARTITION(dt='$do_date')
SELECT id, user_id, sku_id, spu_id, order_id, appraise, 
create_time
FROM ods_comment_info
where dt='$do_date';

insert overwrite table dwd_fact_cart_info PARTITION(dt='$do_date')
SELECT id, user_id, sku_id, cart_price, sku_num, 
sku_name, create_time, operate_time, is_ordered, order_time
FROM gmall.ods_cart_info
where dt='$do_date';

insert overwrite table dwd_fact_favor_info PARTITION(dt='$do_date')
SELECT id, user_id, sku_id, spu_id, is_cancel, 
create_time, cancel_time
FROM gmall.ods_favor_info
where dt="$do_date";

insert overwrite table dwd_fact_coupon_use PARTITION(dt)
SELECT
    nvl(new.id,old.id) id,
    nvl(new.coupon_id,old.coupon_id) coupon_id,
    nvl(new.user_id,old.user_id) user_id,
    nvl(new.order_id,old.order_id) order_id,
    nvl(new.coupon_status,old.coupon_status) coupon_status,
    nvl(new.get_time,old.get_time) get_time,
    nvl(new.using_time,old.using_time) ,
    nvl(new.used_time,old.used_time) ,
    date_format(nvl(new.get_time,old.get_time),'yyyy-MM-dd')
FROM
(
   select 
        *
   from dwd_fact_coupon_use
   where dt in
    (select 
        date_format(get_time,'yyyy-MM-dd')
    from ods_coupon_use
    where dt='$do_date' and get_time<'$do_date')
)old
FULL OUTER JOIN
(
   select 
        *
    from ods_coupon_use
    where dt='$do_date' 
)new
on old.id=new.id;

insert overwrite table dwd_fact_order_info PARTITION(dt)
select 
    nvl(new.id,old.id),
     nvl(new.order_status,old.order_status),
      nvl(new.user_id,old.user_id),
       nvl(new.out_trade_no,old.out_trade_no),
       nvl(new.create_time,old.create_time),
        nvl(new.timeMap['1002'],old.payment_time),
        nvl(new.timeMap['1003'],old.cancel_time),
        nvl(new.timeMap['1004'],old.finish_time),
        nvl(new.timeMap['1005'],old.refund_time),
        nvl(new.timeMap['1006'],old.refund_finish_time),
         nvl(new.province_id,old.province_id),
          nvl(new.activity_id,old.activity_id),
           nvl(new.original_total_amount,old.original_total_amount),
            nvl(new.benefit_reduce_amount,old.benefit_reduce_amount),
            nvl(new.feight_fee,old.feight_fee),
            nvl(new.final_total_amount,old.final_total_amount),
           date_format(nvl(new.create_time,old.create_time),'yyyy-MM-dd')        
from
(select *
from dwd_fact_order_info
where dt in 
(
   select 
        date_format(create_time,'yyyy-MM-dd')
   from ods_order_info
   where dt='$do_date' and create_time<'$do_date'
)) old
FULL OUTER JOIN
(SELECT
    t1.*,t2.activity_id,t3.timeMap
from
(select * from ods_order_info where dt='$do_date') t1
join
(select order_id,str_to_map(concat_ws(',',collect_set(concat(order_status,'=',operate_time))),',','=') timeMap
from ods_order_status_log where dt='$do_date'
group by order_id) t3
on t3.order_id=t1.id
left join
(select order_id,activity_id  from ods_activity_order where dt='$do_date') t2
on t1.id=t2.order_id) new
on old.id=new.id;


insert overwrite TABLE  dwd_dim_user_info_his_tmp
select 
    old.id,
    old.name, old.birthday, old.gender, old.email, old.user_level, 
    old.create_time, old.operate_time, old.start_date,
    if (new.id is not null and old.end_date='9999-99-99',date_sub('$do_date',1),old.end_date)
FROM dwd_dim_user_info_his old 
left join
(select 
    *
FROM ods_user_info
where dt='$do_date') new
on old.id=new.id
UNION all
select 
    id, name, birthday, gender, email, user_level, create_time, 
    operate_time, '$do_date', '9999-99-99'
FROM ods_user_info
where dt='$do_date';

insert overwrite table dwd_dim_user_info_his select * from dwd_dim_user_info_his_tmp;

"
#导入省份维度表和日期维度表
sql2="

load data local inpath '/home/atguigu/date_info.txt' overwrite into  table dwd_dim_date_info;

insert overwrite table dwd_dim_base_province 
SELECT 
    op.id, op.name province_name, area_code, iso_code, region_id, 
    region_name
FROM ods_base_province op JOIN ods_base_region ore
on op.region_id=ore.id;

insert overwrite TABLE  dwd_dim_user_info_his
select 
    *,'9999-99-99'
FROM ods_user_info
where dt='$do_date';
"

#3. 执行sql
hive -e "$sql"

if [ $1 = first ]
then
	hive -e "$sql2"
fi

