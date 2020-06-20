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

insert overwrite TABLE dwd_start_log PARTITION(dt='$do_date')
SELECT
    get_json_object(line,'$.mid') mid_id,
    get_json_object(line,'$.uid') user_id,
    get_json_object(line,'$.vc') version_code,
    get_json_object(line,'$.vn') version_name,
    get_json_object(line,'$.l') lang,
    get_json_object(line,'$.sr') source,
    get_json_object(line,'$.os') os,
    get_json_object(line,'$.ar') area,
    get_json_object(line,'$.md') model,
    get_json_object(line,'$.ba') brand,
    get_json_object(line,'$.sv') sdk_version,
    get_json_object(line,'$.g') gmail,
    get_json_object(line,'$.hw') height_width,
    get_json_object(line,'$.t') app_time,
    get_json_object(line,'$.nw') network,
    get_json_object(line,'$.ln') lng,
    get_json_object(line,'$.la') lat,
    get_json_object(line,'$.entry') entry,
    get_json_object(line,'$.open_ad_type') open_ad_type,
    get_json_object(line,'$.action') action,
    get_json_object(line,'$.loading_time') loading_time,
    get_json_object(line,'$.detail') detail,
    get_json_object(line,'$.extend1') extend1
FROM ods_start_log
WHERE dt='$do_date'
"
#3. 执行sql
hive -e "$sql"