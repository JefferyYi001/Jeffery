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
	get_json_object(split(line, '\\\\|')[1], '$.cm.mid') mid_id, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.uid') user_id, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.vc') version_code, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.vn') version_name, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.l') lang, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.sr') source, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.os') os, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.ar') area, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.md') model, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.ba') brand, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.sv') sdk_version, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.g') gmail, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.hw') height_width, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.t') app_time, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.nw') network, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.ln') lng, 
	get_json_object(split(line, '\\\\|')[1], '$.cm.la') lat, 
	event_name, 
	event_json, 
	split(line, '\\\\|')[0] server_time
FROM ods_event_log
LATERAL VIEW flat_analizer(get_json_object(split(line, '\\\\|')[1], '$.et')) tmp as event_name, event_json
WHERE dt='$do_date'

"
#3. 执行sql
hive -e "$sql"