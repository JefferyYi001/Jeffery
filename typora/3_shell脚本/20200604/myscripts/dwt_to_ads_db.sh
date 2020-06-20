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
set hive.strict.checks.cartesian.product=false;

insert into TABLE ads_gmv_sum_day
select 
'$do_date',
sum(order_count) gmv_count,
sum(order_amount) gmv_amount,
sum(payment_amount) gmv_payment
from dws_user_action_daycount
where dt='$do_date';

insert into TABLE ads_user_topic
select 
    '$do_date',
    sum(IF( login_date_last = '$do_date',1,0)) day_users,
    sum(IF( login_date_first = '$do_date',1,0)) day_new_users,
    sum(IF( payment_date_first = '$do_date',1,0)) day_new_payment_users,
    sum(if(payment_count>0,1,0)) payment_users,
    count(*) users,
   cast(sum(IF( login_date_last = '$do_date',1,0))/count(*)*100 as decimal(10,2)) day_users2users,
   cast(sum(if(payment_count>0,1,0))/count(*)*100 as decimal(10,2)) payment_users2users,
   cast( sum(IF( login_date_first = '$do_date',1,0)) / sum(IF( login_date_last = '$do_date',1,0)) * 100 as decimal(10,2)) 
from dwt_user_topic;

insert into table ads_uv_count
select 
    '$do_date',
    day_count,is_weekend,is_monthend,wk_count,mn_count
from
(SELECT 
    count(*) day_count, 
    if( date_sub(next_day('$do_date','mo'),1)='$do_date','Y','N') is_weekend, 
    if(last_day('$do_date')='$do_date','Y','N') is_monthend
FROM dwt_uv_topic
WHERE login_date_last='$do_date') t1
join
(select 
      count(*)  wk_count 
from dwt_uv_topic
where login_date_last>=date_sub(next_day('$do_date','mo'),7)) t2
join
(select 
    count(*) mn_count
from dwt_uv_topic
where date_format(login_date_last,'yyyy-MM')=date_format('$do_date','yyyy-MM')) t3;

insert into TABLE ads_user_action_convert_day
    select 
        '$do_date',
        count(*) total_visitor_m_count,
        sum(if(cart_count>0,1,0)) cart_u_count,
        cast (sum(if(cart_count>0,1,0)) / count(*) * 100 as decimal(10,2)) visitor2cart_convert_ratio,
        sum(if(order_count>0,1,0)) order_u_count,
        cast (sum(if(order_count>0,1,0)) / sum(if(cart_count>0,1,0)) * 100 as decimal(10,2)) cart2order_convert_ratio,
        sum(if(payment_count>0,1,0)) payment_u_count,
        cast ( sum(if(payment_count>0,1,0)) / sum(if(order_count>0,1,0) * 100) as decimal(10,2)) order2payment_convert_ratio
    from dws_user_action_daycount 
    where dt='$do_date';

"
hive="/opt/module/spark-yarn/bin/spark-sql --master yarn"
#hive -e "$sql"
#hive="/opt/module/spark-local/bin/spark-sql"
$hive -e "$sql"
#/opt/module/spark-local/bin/spark-sql -e "$sql"
