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


insert overwrite table dwt_uv_topic
select
nvl(new.mid_id, old.mid_id) mid_id,
concat_ws('|', old.user_id, new.user_id) user_id,
concat_ws('|', old.version_code, new.version_code) version_code,
concat_ws('|', old.version_name, new.version_name) version_name,
concat_ws('|', old.lang, new.lang) lang,
concat_ws('|', old.source, new.source) source,
concat_ws('|', old.os, new.os) os,
concat_ws('|', old.area, new.area) area,
concat_ws('|', old.model, new.model) model,
concat_ws('|', old.brand, new.brand) brand,
concat_ws('|', old.sdk_version, new.sdk_version) sdk_version,
concat_ws('|', old.gmail, new.gmail) gmail,
concat_ws('|', old.height_width, new.height_width) height_width,
concat_ws('|', old.app_time, new.app_time) app_time,
concat_ws('|', old.network, new.network) network,
concat_ws('|', old.lng, new.lng) lng,
concat_ws('|', old.lat, new.lat) lat,
if(old.mid_id is null, '$do_date', old.login_date_first) login_date_first,
if(new.mid_id is null, old.login_date_last, '$do_date') login_date_last,
nvl(new.login_count, 0) login_day_count,
(nvl(old.login_count, 0)+if(new.login_count is null, 0, 1)) login_count
from
dwt_uv_topic old
full join
(select *
from dws_uv_detail_daycount
where dt='$do_date')new
on old.mid_id=new.mid_id;


insert overwrite table dwt_user_topic
select
t1.user_id,
login_date_first,
login_date_last,
login_count,
login_last_30d_count,
order_date_first,
order_date_last,
order_count,
order_amount,
order_last_30d_count,
order_last_30d_amount,
payment_date_first,
payment_date_last,
payment_count,
payment_amount,
payment_last_30d_count,
payment_last_30d_amount
from
(select
nvl(old.user_id, new.user_id) user_id,
if(old.user_id is null, '$do_date', old.login_date_first) login_date_first,
if(old.user_id is null, '$do_date', old.order_date_first) order_date_first,
if(old.user_id is null, '$do_date', old.payment_date_first) payment_date_first,
if(new.user_id is null, old.login_date_last, '$do_date') login_date_last,
if(new.user_id is null, old.order_date_last, '$do_date') order_date_last,
if(new.user_id is null, old.payment_date_last, '$do_date') payment_date_last,
(nvl(old.login_count, 0)+if(new.login_count is null, 0, 1)) login_count,
(nvl(old.login_count, 0)+nvl(new.order_count, 0)) order_count,
(nvl(old.order_amount, 0)+nvl(new.order_amount, 0)) order_amount,
(nvl(old.payment_count, 0)+nvl(new.payment_count, 0)) payment_count,
(nvl(old.payment_amount, 0)+nvl(new.payment_amount, 0)) payment_amount
from dwt_user_topic old
full join
(select * from
dws_user_action_daycount
where dt='$do_date') new
on old.user_id=new.user_id) t1
join
(select
user_id,
sum(if(login_count>0, 1, 0)) login_last_30d_count,
sum(nvl(order_amount, 0)) order_last_30d_amount,
sum(nvl(order_count, 0)) order_last_30d_count,
sum(nvl(payment_count, 0)) payment_last_30d_count,
sum(nvl(payment_amount, 0)) payment_last_30d_amount
from dws_user_action_daycount
where dt>=date_sub('$do_date', 29)
group by user_id) t2
on t1.user_id=t2.user_id;


insert overwrite table dwt_sku_topic
select
t1.sku_id,
t3.spu_id,
nvl(order_last_30d_count,0),
nvl(order_last_30d_num,0),
nvl(order_last_30d_amount,0),
order_count,
order_num,
order_amount,
nvl(payment_last_30d_count,0),
nvl(payment_last_30d_num,0),
nvl(payment_last_30d_amount,0),
payment_count,
payment_num,
payment_amount,
nvl(refund_last_30d_count,0),
nvl(refund_last_30d_num,0),
nvl(refund_last_30d_amount,0),
refund_count,
refund_num,
refund_amount,
nvl(cart_last_30d_count,0),
nvl(cart_last_30d_num,0),
cart_count,
cart_num,
nvl(favor_last_30d_count,0),
favor_count,
nvl(appraise_last_30d_good_count,0),
nvl(appraise_last_30d_mid_count,0),
nvl(appraise_last_30d_bad_count,0),
nvl(appraise_last_30d_default_count,0),
appraise_good_count,
appraise_mid_count,
appraise_bad_count,
appraise_default_count
from
(select
nvl(old.sku_id, new.sku_id) sku_id,
(nvl(old.order_count, 0)+nvl(new.order_count, 0)) order_count,
(nvl(old.order_num, 0)+nvl(new.order_num, 0)) order_num,
(nvl(old.order_amount, 0)+nvl(new.order_amount, 0)) order_amount,
(nvl(old.payment_count, 0)+nvl(new.payment_count, 0)) payment_count,
(nvl(old.payment_num, 0)+nvl(new.payment_num, 0)) payment_num,
(nvl(old.payment_amount, 0)+nvl(new.payment_amount, 0)) payment_amount,
(nvl(old.refund_count, 0)+nvl(new.refund_count, 0)) refund_count,
(nvl(old.refund_num, 0)+nvl(new.refund_num, 0)) refund_num,
(nvl(old.refund_amount, 0)+nvl(new.refund_amount, 0)) refund_amount,
(nvl(old.cart_count, 0)+nvl(new.cart_count, 0)) cart_count,
(nvl(old.cart_num, 0)+nvl(new.cart_num, 0)) cart_num,
(nvl(old.favor_count, 0)+nvl(new.favor_count, 0)) favor_count,
(nvl(old.appraise_good_count, 0)+nvl(new.appraise_good_count, 0)) appraise_good_count,
(nvl(old.appraise_mid_count, 0)+nvl(new.appraise_mid_count, 0)) appraise_mid_count,
(nvl(old.appraise_bad_count, 0)+nvl(new.appraise_bad_count, 0)) appraise_bad_count,
(nvl(old.appraise_default_count, 0)+nvl(new.appraise_default_count, 0)) appraise_default_count
from
dwt_sku_topic old
full join
(select * from dws_sku_action_daycount where dt='$do_date') new
on old.sku_id = new.sku_id) t1
join
(select
sku_id,
sum(order_count) order_last_30d_count,
sum(order_num) order_last_30d_num,
sum(order_amount) order_last_30d_amount,
sum(payment_count) payment_last_30d_count,
sum(payment_num) payment_last_30d_num,
sum(payment_amount) payment_last_30d_amount,
sum(refund_count) refund_last_30d_count,
sum(refund_num) refund_last_30d_num,
sum(refund_amount) refund_last_30d_amount,
sum(cart_count) cart_last_30d_count,
sum(cart_num) cart_last_30d_num,
sum(favor_count) favor_last_30d_count,
sum(appraise_good_count) appraise_last_30d_good_count,
sum(appraise_mid_count) appraise_last_30d_mid_count,
sum(appraise_bad_count) appraise_last_30d_bad_count,
sum(appraise_default_count) appraise_last_30d_default_count
from dws_sku_action_daycount where dt>=date_sub('$do_date', 29)
group by sku_id) t2
on t1.sku_id=t2.sku_id
join
(select id, spu_id
from dwd_dim_sku_info
where dt='$do_date') t3
on t1.sku_id=t3.id;

insert overwrite table dwt_coupon_topic
select
nvl(new.coupon_id, old.coupon_id),
nvl(new.get_count, 0) get_day_count,
nvl(new.using_count, 0) using_day_count,
nvl(new.used_count, 0) used_day_count,
(nvl(old.get_count, 0)+nvl(new.get_count, 0)) get_count,
(nvl(old.using_count, 0)+nvl(new.using_count, 0)) using_count,
(nvl(old.used_count, 0)+nvl(new.used_count, 0)) used_count
from
dwt_coupon_topic old
full join
(select * from
dws_coupon_use_daycount
where dt='$do_date') new
on old.coupon_id=new.coupon_id;

insert overwrite table dwt_activity_topic
select
nvl(old.id,new.id) id,
nvl(old.activity_name,new.activity_name) activity_name,
nvl(new.order_count,0) order_day_count,
nvl(new.payment_count,0) payment_day_count,
nvl(old.order_count, 0)+nvl(new.order_count,0) order_day_count,
nvl(old.payment_count, 0)+nvl(new.payment_count,0) payment_day_count
from dwt_activity_topic old
full join
(select * from
dws_activity_info_daycount
where dt='$do_date')new
on old.id = new.id;

"
#3. 执行sql
hive -e "$sql"