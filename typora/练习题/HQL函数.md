

## 1.练习1

### 1.1 建表

数据：

```
悟空	A	男
大海	A	男
宋宋	B	男
凤姐	A	女
婷姐	B	女
婷婷	B	女
```

需求：求出不同部门男女各多少人。结果如下

```
A     2       1
B     1       2
```

建表：

```sql
create table emp_sex(
name string, 
dept_id string, 
sex string) 
row format delimited fields terminated by "\t";
```

加载数据：

```
load data local inpath '/home/atguigu/hivedatas/test1' into table emp_sex;
```

### 1.2 思路1

关键词： 不同部门，男女，各多少人

思路1：① 按部门分组，group by

​			② 按男女性别过滤，where

​			③ 求每个部门中记录的条数，count(*)

sql：

```sql
select t1.dept_id,male_count,female_count
from
(select  dept_id,count(*) male_count
from emp_sex
where sex='男'
group by dept_id)t1
join
(select  dept_id,count(*) female_count
from emp_sex
where sex='女'
group by dept_id)t2
on t1.dept_id=t2.dept_id
```

此条SQL会启动3个Job，因为嵌套了子查询！

在写hivesql时，尽量避免子查询！

### 1.3 思路2

关键词： 不同部门，男女，各多少人

思路2： 用sum来累加统计！如果统计的是男人，只要这条记录是男性，记1，否则记0！

​				对以上值进行累加求和可以求出总的男性数量！

​				根据部门进行分组即可

SQL：

```sql
select dept_id,sum(if(sex='男',1,0)) male_count,
sum(if(sex='女',1,0)) female_count
from emp_sex
group by dept_id
```

## 2.练习2

### 2.1建表

```
create table person_info(
name string, 
constellation string, 
blood_type string) 
row format delimited fields terminated by "\t";
```

加载数据：

```
孙悟空	白羊座	A
大海	射手座	A
宋宋	白羊座	B
猪八戒	白羊座	A
凤姐	射手座	A
```



```
load data local inpath '/home/atguigu/hivetest/test/test2' into table person_info;
```

需求：把星座和血型一样的人归类到一起，结果如下：

```
射手座,A            大海|凤姐
白羊座,A            孙悟空|猪八戒
白羊座,B            宋宋
```



### 2.2 思路1

关键词：  星座，血型一致的人归类到一起！

思路：  ①按照星座，血型分组

​			 ②结果有两列

​				第一列是星座和血型，使用,拼接后的结果 concat()

​				第二列是name列多行数据转一行数据的拼接的结果，collect_list和concat_ws

sql：

```sql
select concat(constellation,',',blood_type),concat_ws('|',collect_list(name))
from  person_info
group by constellation,blood_type
```

在group by的语句中，select后面的表达式可以写什么？

​		select只能写group by 后面的字段和聚集函数中的字段

​		

## 3.练习3

### 3.1 建表

```
create table movie_info(
    movie string, 
    category array<string>) 
row format delimited fields terminated by "\t"
collection items terminated by ",";
```

加载数据：

```
《疑犯追踪》	悬疑,动作,科幻,剧情
《Lie to me》	悬疑,警匪,动作,心理,剧情
《战狼2》	战争,动作,灾难

```



```
load data local inpath '/home/atguigu/hivetest/test/test3' into table movie_info;
```

### 3.2 需求

​		将电影名称和电影的类型展开！

错误：

```sql
select t2.movie,t1.c1
from movie_info t2
left join 
(select explode(category)  c1 from movie_info) t1
```

在Join时，由于没有关联字段，造成笛卡尔集，并不能达到我们预期的效果！



需求： 炸裂的临时结果集中的每一行，可以和炸裂之前的所在行的其他字段进行join!

hive 提供了支持此需求的实现，称为LATERAL VIEW(侧写)

语法：  

```sql
select 临时列名，其他字段
from 表名
-- 将 UDTF函数执行的返回的结果集临时用临时表名代替，结果集返回的每一列，按照顺序分别以临时列名代替
lateral view UDTF() 临时表名 as 临时列名,...
```

示例：

```
select  movie,col1
from movie_info
lateral view explode(category) tmp1 as col1
```

## 4.练习4

### 4.1数据

```
jack|tom|jerry	阳光男孩|肌肉男孩|直男	晒太阳|健身|说多喝热水
marry|nancy	阳光女孩|肌肉女孩|腐女	晒太阳|健身|看有内涵的段子
```

### 4.2 建表

```
create table person_info2(names array<string>,tags array<string>,hobbys array<string>)
row format delimited fields terminated by '\t'
collection items terminated by '|'
```

加载数据：

```
load data local inpath '/home/atguigu/hivetest/test/test4' into table person_info2;
```

### 4.3 需求

将names,hobbys,tags拆分，组合！

结果如下：

```
jack	阳光男孩	晒太阳
jack	阳光男孩	健身
jack	阳光男孩	说多喝热水
jack	肌肉男孩	晒太阳
jack	肌肉男孩	健身
jack	肌肉男孩	说多喝热水
......
```

sql:

```sql
select name,tag,hobby
from person_info2
lateral view explode(names) tmp1 as name
lateral view explode(hobbys) tmp1 as hobby
lateral view explode(tags) tmp1 as tag
```



## 5. 窗口函数练习

### 5.1 建表

```
create table business(
name string, 
orderdate string,
cost int
) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',';
```

加载数据：

```
jack,2017-01-01,10
tony,2017-01-02,15
jack,2017-02-03,23
tony,2017-01-04,29
jack,2017-01-05,46
jack,2017-04-06,42
tony,2017-01-07,50
jack,2017-01-08,55
mart,2017-04-08,62
mart,2017-04-09,68
neil,2017-05-10,12
mart,2017-04-11,75
neil,2017-06-12,80
mart,2017-04-13,94
```



```sql
load data local inpath '/home/atguigu/hivetest/test/test5' into table business;
```

需求：

(1）查询在2017年4月份购买过的顾客及总人数
(2）查询顾客的购买明细及月购买总额
(3）查询顾客的购买明细要将cost按照日期进行累加
(4）查询顾客的购买明细及顾客上次的购买时间
(5)  查询顾客的购买明细及顾客下次的购买时间
(6)  查询顾客的购买明细及顾客本月第一次购买的时间
(7)  查询顾客的购买明细及顾客本月最后一次购买的时间
(8)  查询顾客的购买明细及顾客最近三次cost花费
(9)  查询前20%时间的订单信息

### 5.2需求

#### 5.2.1 查询在2017年4月份购买过的顾客及总人数

思路： ①where 过滤 2017年4月份的数据

​			②求顾客总人数

​			总人数 不等于 总人次



查询在2017年4月份购买过的总人数

```sql
select '2017-04',count(*)
from
(select name
from business
where year(orderdate)=2017 and month(orderdate)=4
group by name)tmp
```

```sql
select *
from business
where substring(orderdate,1,7)='2017-04'
```



错误：

```
select count(*)
from business
where year(orderdate)=2017 and month(orderdate)=4
group by name
```

聚集函数是分组后在组内统计，聚集函数默认工作的范围(窗口)是组内！



窗口函数对窗口中的每一条记录都进行计算！



使用窗口函数： 需要指定count()运行的窗口大小为整个结果集而不是组内！

查询在2017年4月份购买过的顾客及总人数

```sql
select name, count(*) over(ROWS BETWEEN UNBOUNDED  PRECEDING AND UNBOUNDED FOLLOWING)
from business
where year(orderdate)=2017 and month(orderdate)=4
group by name
```

统计明细，为每条明细都附加一个总的结果，一般使用窗口函数！

如果只要结果不要明细，没有必要使用窗口函数！



以上窗口函数还可以简写：

```sql
select name,count(*) over()
from business
where year(orderdate)=2017 and month(orderdate)=4
group by name
```



#### 5.2.2 查询顾客的购买明细及月购买总额

按照顾客和月份分区，在区内计算所有的购买金额的总和！

```sql
 select *,sum(cost) over(partition by name,substring(orderdate,1,7) ) total_month_cost
 from business
```

#### 5.2.3 查询顾客的购买明细要将cost按照日期进行累加

```sql
 select *,sum(cost) over(partition by name order by orderdate ) total_cost
 from business
```

#### 5.2.4 查询顾客的购买明细及顾客上次的购买时间

lag不支持在over()中定义window子句！

```sql
 select *,lag(orderdate,1,'无') over(partition by name order by orderdate ) last
 from business
```



#### 5.2.5 查询顾客的购买明细及顾客下次的购买时间



```sql
 select *,lead(orderdate,1,'无') over(partition by name order by orderdate ) next
 from business
```



#### 5.2.6 查询顾客的购买明细及顾客本月第一次购买的时间

让窗口可以取到第一个值

```sql
 select *,first_value(orderdate) over(partition by name,substring(orderdate,1,7) order by orderdate ) first_date
 from business
```



#### 5.2.7查询顾客的购买明细及顾客本月最后一次购买的时间

让窗口可以取到最后一个值

```sql
 select *,last_value(orderdate) over(partition by name,substring(orderdate,1,7) order by orderdate 
ROWS BETWEEN UNBOUNDED  PRECEDING AND UNBOUNDED FOLLOWING ) last_date
 from business
```



#### 5.2.8查询顾客的购买明细及顾客最近三次cost花费

最近三次：  

​		如果当前购买记录是最新的一次，那么最近三次就是当前此次+之前两次

​		如果当前购买记录不是最新的一次，是历史记录，那么也有可能取当前记录+上下各一次！

当前此次+之前两次：

```sql
 select *,sum(cost) over(partition by name order by orderdate 
ROWS BETWEEN 2  PRECEDING AND CURRENT ROW) mycost
 from business
```

当前记录+上下各一次:

```sql
 select *,sum(cost) over(partition by name order by orderdate 
ROWS BETWEEN 1  PRECEDING AND 1 FOLLOWING) mycost
 from business
```

(9)  查询前20%时间的订单信息

## 6. 谷粒影音

### 6.1 建视图

```sql
create view categoryview2 as
select videoid, uploader, category2, `views`, ratings
from gulivideo_ori
lateral view explode(category) tmp as category2;
```

### 6.2 练习

```sql
-- 1. 统计视频观看数 Top10
select videoid, `views`, uploader
from gulivideo_ori
order by `views` desc
limit 10;

-- 2.1 每个类别热度前十的视频
select videoid, category2, rankn
from
    (select videoid, category2 ,rank() over (partition by category2 order by `views` desc ) rankn
    from categoryview2) t1
where rankn <= 10;

-- 2.2 求前十最热的类别
select category2, sum(`views`) sumview
from categoryview2
group by category2
order by sumview desc
limit 10;

-- 3. 统计出视频观看数最高的20个视频的所属类别以及类别包含Top20视频的个数
--         ①求看数最高的20个视频
--         ②这20个视频，各属于哪些类别
--         ③求这些类别各包含这20个视频多少个

select t3.videoid, t3.`views`, t3.category2, t2.count2
from
    (select category2, count(*) count2
    from
        (select videoid, `views`, category2
        from categoryview2
        order by `views` desc
        limit 20) t1
    group by category2) t2
join
    (select videoid, `views`, category2
     from categoryview2
     order by `views` desc
     limit 20) t3
where t2.category2 = t3.category2;

-- 4. 统计视频观看数Top50所关联视频的所属类别rank（按照 views 进行rank）
--         ①求top50观看数视频的relatedid(去重)
--         ②求他们的关联视频的所属类别,每条视频的热度
--         ③rank排名

select *, rank() over (order by sumview desc)
from
    (select t3.category2, sum(t3.`views`) sumview
    from
        (select distinct relatedvideoid
        from
            (select videoid, `views`, relatedid
            from gulivideo_ori
            order by `views` desc
            limit 50) t1
        lateral view explode(relatedid) tmp as relatedvideoid)t2
    join categoryview2 t3
    on t2.relatedvideoid = t3.videoid
    group by t3.category2) t4;

-- 5.统计每个类别中的视频热度Top10，以Music为例

select videoid, `views`
from categoryview2
where category2='Music'
order by `views` desc
limit 10;

-- 6.统计每个类别中视频流量Top10，以Music为例

select videoid, ratings
from categoryview2
where category2='Music'
order by ratings desc
limit 10;

select *
from
    (select videoid, ratings, category2,
    rank() over (partition by category2 order by ratings desc) rkn
    from categoryview2) t1
where rkn <= 10;

--   7. 统计上传视频最多的用户Top10以及他们上传的观看次数在前20的视频
--         ①从gulivideo_user_ori，取上传视频最多的用户Top10
--         ②关联gulivideo_ori，求出他们上传的所有的视频的观看次数
--         ③排序，取前20的视频

select t1.uploader, t2.videoid, `views`
from
    (select uploader, videos
    from gulivideo_user_ori
    order by videos desc
    limit 10) t1
join gulivideo_ori t2
on t1.uploader = t2.uploader
order by `views` desc
limit 20;


--   8. 统计每个类别视频观看数Top10
select *
from
    (select videoid, `views`, category2, rank() over (partition by category2 order by `views` desc ) rkn
    from categoryview2) t1
where rkn <= 10;

```



## 7. 蚂蚁森林

### 7.1 练习

```sql
-- 蚂蚁森林种树问题

-- 1.蚂蚁森林植物申领统计
-- 问题：假设2017年1月1日开始记录低碳数据（user_low_carbon），假设2017年10月1日之前满足申领条件的用户都申领了一颗p004-胡杨，
-- 剩余的能量全部用来领取“p002-沙柳”?。
-- 统计在10月1日累计申领“p002-沙柳”?排名前10的用户信息；以及他比后一名多领了几颗沙柳。
-- 期间的总能量、且满足胡杨条件

-- 建视图
drop view carbonview2;
create view carbonview2 as
select user_id, regexp_replace (data_dt, '/', '-') date2, low_carbon
from user_low_carbon;


    -- 1. 先求出每个用户2017年1月1日到2017年10月1日领取的总能量
    -- 2. 筛选出能领胡杨的用户，并求出所领的沙柳数量
    -- 3. 求出排名前10的用户信息；以及他比后一名多领了几颗沙柳

select user_id, sumcarbon, slcount, slcount-lead(slcount) over (order by slcount desc )
from
    (select user_id, sumcarbon, if(sumcarbon>hycost, floor((sumcarbon-hycost)/slcost), -1) slcount
    from
        (select user_id, sumcarbon, hycost, slcost
        from
            (select user_id, sum(low_carbon) sumcarbon
            from carbonview2
            where date (date2) between date('2017-01-01') and date('2017-10-01')
            group by user_id) t1
        join
            (select plant_name, low_carbon hycost
             from plant_carbon
             where plant_name='胡杨')t2
        join
            (select plant_name, low_carbon slcost
             from plant_carbon
             where plant_name='沙柳')t3)t4
    order by slcount desc
    limit 11) t5
limit 10;


-- 2、蚂蚁森林低碳用户排名分析
-- 问题：查询user_low_carbon表中每日流水记录，条件为：
-- 用户在2017年，连续三天（或以上）的天数里，
-- 每天减少碳排放（low_carbon）都超过100g的用户低碳流水。
-- 需要查询返回满足以上条件的user_low_carbon表中的记录流水。
-- 例如用户u_002符合条件的记录如下，因为2017/1/2~2017/1/5连续四天的碳排放量之和都大于等于100g：

    -- 1. 求出每个用户每天收取的能量总和并筛选出大于100g的用户
    -- 2. 建立辅助列，求取时间差列
    -- 3. 对时间差列求和，筛选出大于等于3的行
    -- 4. join 原表
select carbonview2.*, t4.daycarbon, t4.count2
from
    (select user_id, date2, daycarbon, count2
    from
        (select user_id, date2, daycarbon, count(*) over(partition by user_id, date3) count2
        from
            (select user_id, date2, daycarbon, date_sub(date2, count1) date3
            from
                (select user_id, date2, sum(low_carbon) daycarbon,
                       row_number() over (partition by user_id order by date2) count1
                from carbonview2
                group by user_id, date2
                having daycarbon >= 100) t1) t2) t3
    where count2 >= 3) t4
join carbonview2
on carbonview2.user_id=t4.user_id and carbonview2.date2=t4.date2

```















