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

INSERT overwrite TABLE dwd_base_event_log PARTITION(dt='$do_date')
SELECT
	base_analizer(line, 'mid') mid_id, 
	base_analizer(line, 'uid') user_id, 
	base_analizer(line, 'vc') version_code, 
	base_analizer(line, 'vn') version_name, 
	base_analizer(line, 'l') lang, 
	base_analizer(line, 'sr') source, 
	base_analizer(line, 'os') os, 
	base_analizer(line, 'ar') area, 
	base_analizer(line, 'md') model, 
	base_analizer(line, 'ba') brand, 
	base_analizer(line, 'sv') sdk_version, 
	base_analizer(line, 'g') gmail, 
	base_analizer(line, 'hw') height_width, 
	base_analizer(line, 't') app_time, 
	base_analizer(line, 'nw') network, 
	base_analizer(line, 'ln') lng, 
	base_analizer(line, 'la') lat, 
	event_name, 
	event_json, 
	base_analizer(line, 'ts') server_time
FROM ods_event_log
LATERAL VIEW flat_analizer(base_analizer(line,'et')) tmp as event_name, event_json
WHERE dt='$do_date'

"
#3. 执行sql
hive -e "$sql"
