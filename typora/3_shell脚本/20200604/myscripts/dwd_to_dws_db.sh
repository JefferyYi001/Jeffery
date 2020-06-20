#!/bin/bash
#1. 确定要导入数据的日期
if [ -n "$1" ]
then 
	do_date=$1
else
	do_date=$(date -d 'yesterday' '+%F')
fi

#2. 导入命令 hive默认连接的是default库，如果要使用别的库，请先使用use，或用库名.表名/函数名声明

sql="
use gmall;

insert overwrite table dws_uv_detail_daycount partition(dt='$do_date')
select mid_id,
       concat_ws('|', collect_set(user_id)),
       concat_ws('|', collect_set(version_code)),
       concat_ws('|', collect_set(version_name)),
       concat_ws('|', collect_set(lang)),
       concat_ws('|', collect_set(source)),
       concat_ws('|', collect_set(os)),
       concat_ws('|', collect_set(area)),
       concat_ws('|', collect_set(model)),
       concat_ws('|', collect_set(brand)),
       concat_ws('|', collect_set(sdk_version)),
       concat_ws('|', collect_set(gmail)),
       concat_ws('|', collect_set(height_width)),
       concat_ws('|', collect_set(app_time)),
       concat_ws('|', collect_set(network)),
       concat_ws('|', collect_set(lng)),
       concat_ws('|', collect_set(lat)),
       count(*)
from dwd_start_log
where dt = '$do_date'
group by mid_id;


insert overwrite table dws_user_action_daycount partition (dt='$do_date')
select
user_id, sum(login_count),  sum(cart_count),  sum(cart_amount),  sum(order_count),  sum(order_amount),  sum(payment_count),  sum(payment_amount)
from
(
select user_id, count(*) login_count, 0 cart_count, 0 cart_amount,
       0 order_count, 0 order_amount, 0 payment_count, 0 payment_amount
from dwd_start_log
where dt='$do_date' and user_id is not null
group by user_id
union all
select user_id, 0 login_count, count(*) cart_count, sum(cart_price*sku_num) cart_amount,
       0 order_count, 0 order_amount, 0 payment_count, 0 payment_amount
from dwd_fact_cart_info
where dt='$do_date' and date_format(create_time, 'yyyy-MM-dd')='$do_date' and user_id is not null
group by user_id
union all
select user_id, 0 login_count, 0 cart_count, 0 cart_amount,
       count(*) order_count, sum(total_amount) order_amount, 0 payment_count, 0 payment_amount
from dwd_fact_order_detail
where dt='$do_date'
group by user_id
union all
select user_id, 0 login_count, 0 cart_count, 0 cart_amount, 0 order_count, 0 order_amount,
       count(*) payment_count, sum(payment_amount) payment_amount
from dwd_fact_payment_info
where dt='$do_date'
group by user_id)tmp
group by user_id;


insert overwrite table dws_sku_action_daycount partition (dt='$do_date')
select
    sku_id,
    sum(order_count),
    sum(order_num),
    sum(order_amount),
    sum(payment_count),
    sum(payment_num),
    sum(payment_amount),
    sum(refund_count),
    sum(refund_num),
    sum(refund_amount),
    sum(cart_count),
    sum(cart_num),
    sum(favor_count),
    sum(appraise_good_count),
    sum(appraise_mid_count),
    sum(appraise_bad_count),
    sum(appraise_default_count)
from
(select sku_id, count(*) order_count, sum(sku_num) order_num, sum(total_amount) order_amount,
       0 payment_count, 0 payment_num, 0 payment_amount,
       0 refund_count, 0 refund_num, 0 refund_amount,
       0 cart_count, 0 cart_num,
       0 favor_count,
       0 appraise_good_count, 0 appraise_mid_count, 0 appraise_bad_count, 0 appraise_default_count
from dwd_fact_order_detail
where dt='$do_date'
group by sku_id
union all
select sku_id, 0 order_count, 0 order_num, 0 order_amount,
       count(*) payment_count, sum(sku_num) payment_num, sum(total_amount) payment_amount,
       0 refund_count, 0 refund_num, 0 refund_amount,
       0 cart_count, 0 cart_num,
       0 favor_count,
       0 appraise_good_count, 0 appraise_mid_count, 0 appraise_bad_count, 0 appraise_default_count
from
(select sku_id, order_id, sku_num, total_amount
from dwd_fact_order_detail
where dt='$do_date' or dt=date_sub('$do_date', 1)) o
join
(select order_id
from dwd_fact_payment_info
where dt='$do_date') p
on o.order_id=p.order_id
group by sku_id
union all
select sku_id, 0 order_count, 0 order_num, 0 order_amount,
       0 payment_count, 0 payment_num, 0 payment_amount,
       count(*) refund_count, sum(refund_num) refund_num, sum(refund_amount) refund_amount,
       0 cart_count, 0 cart_num,
       0 favor_count,
       0 appraise_good_count, 0 appraise_mid_count, 0 appraise_bad_count, 0 appraise_default_count
from dwd_fact_order_refund_info
where dt='$do_date'
group by sku_id
union all
select sku_id, 0 order_count, 0 order_num, 0 order_amount,
       0 payment_count, 0 payment_num, 0 payment_amount,
       0 refund_count, 0 refund_num, 0 refund_amount,
       count(*) cart_count, sum(sku_num) cart_num,
       0 favor_count,
       0 appraise_good_count, 0 appraise_mid_count, 0 appraise_bad_count, 0 appraise_default_count
from dwd_fact_cart_info
where dt='$do_date' and date_format(create_time, 'yyyy-MM-dd')='$do_date'
group by sku_id
union all
select sku_id, 0 order_count, 0 order_num, 0 order_amount,
       0 payment_count, 0 payment_num, 0 payment_amount,
       0 refund_count, 0 refund_num, 0 refund_amount,
       0 cart_count, 0 cart_num,
       count(*) favor_count,
       0 appraise_good_count, 0 appraise_mid_count, 0 appraise_bad_count, 0 appraise_default_count
from dwd_fact_favor_info
where dt='$do_date' and date_format(create_time, 'yyyy-MM-dd')='$do_date'
group by sku_id
union all
select sku_id,0 order_count, 0 order_num, 0 order_amount,
       0 payment_count, 0 payment_num, 0 payment_amount,
       0 refund_count, 0 refund_num, 0 refund_amount,
       0 cart_count, 0 cart_num,
       0 favor_count,
       sum(if(appraise='1201',1,0)) appraise_good_count,
       sum(if(appraise='1202',1,0)) appraise_mid_count,
       sum(if(appraise='1203',1,0)) appraise_bad_count,
       sum(if(appraise='1204',1,0)) appraise_default_count
from dwd_fact_comment_info
where dt='$do_date'
group by sku_id)tmp
group by sku_id;


insert overwrite table dws_coupon_use_daycount partition (dt='$do_date')
select
coupon_id, coupon_name, coupon_type, condition_amount, condition_num, activity_id, benefit_amount,
       benefit_discount, create_time, range_type, spu_id, tm_id, category3_id, limit_num,
       get_count, using_count, used_count
from
(select id, coupon_name, coupon_type, condition_amount, condition_num, activity_id, benefit_amount,
        benefit_discount, create_time, range_type, spu_id, tm_id, category3_id, limit_num
from dwd_dim_coupon_info
where dt='$do_date') t1
left join
(select coupon_id, sum(if(dt='$do_date', 1, 0)) get_count,
       sum(if(date_format(using_time, 'yyyy-MM-dd')='$do_date', 1, 0)) using_count,
       sum(if(date_format(used_time, 'yyyy-MM-dd')='$do_date', 1, 0)) used_count
from dwd_fact_coupon_use
where dt='$do_date'
group by coupon_id) t2
on t1.id=t2.coupon_id;


insert overwrite table dws_activity_info_daycount partition (dt='$do_date')
select
id,
activity_name,
activity_type,
start_time,
end_time,
create_time,
order_count,
payment_count
from
(select id, activity_name, activity_type, start_time, end_time, create_time
from
dwd_dim_activity_info
where dt='$do_date'
group by id, activity_name, activity_type, start_time, end_time, create_time) t1
join
(select activity_id,
       sum(if(dt='$do_date', 1, 0)) order_count,
       sum(if(date_format(payment_time, 'yyyy-MM-dd')='$do_date', 1, 0)) payment_count
from
dwd_fact_order_info
where dt='$do_date' or dt=date_sub('$do_date', 1)
group by activity_id) t2
on t1.id=t2.activity_id;

insert overwrite table dws_sale_detail_daycount partition (dt='$do_date')
select
t1.user_id,
t3.sku_id,
t1.user_gender,
t1.user_age,
t1.user_level,
t3.order_price,
t3.sku_name,
t3.sku_tm_id,
t3.sku_category3_id,
t3.sku_category2_id,
t3.sku_category1_id,
t3.sku_category3_name,
t3.sku_category2_name,
t3.sku_category1_name,
t3.spu_id,
t2.total_sku_num,
t2.order_count,
t2.order_amount
from
(select
id user_id,
floor(datediff('$do_date', birthday)/365) user_age,
gender user_gender,
user_level
from dwd_dim_user_info_his
where end_date = '9999-99-99') t1
join
(select
user_id,
sku_id,
sum(sku_num) total_sku_num,
count(*) order_count,
sum(total_amount) order_amount
from dwd_fact_order_detail
where dt='$do_date'
group by user_id, sku_id) t2
on t1.user_id=t2.user_id
join
(select
id sku_id,
spu_id,
price order_price,
sku_name,
tm_id sku_tm_id,
category3_id sku_category3_id,
category2_id sku_category2_id,
category1_id sku_category1_id,
category3_name sku_category3_name,
category2_name sku_category2_name,
category1_name sku_category1_name
from dwd_dim_sku_info
where dt='$do_date') t3
on t2.sku_id=t3.sku_id;

"
#3. 执行sql
#hive -e "$sql"
hive="/opt/module/spark-yarn/bin/spark-sql --master yarn"
$hive -e "$sql"