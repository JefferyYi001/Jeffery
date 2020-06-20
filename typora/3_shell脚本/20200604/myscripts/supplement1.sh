for ((i=1;i<10;i++))
do
	cmd=`date -d "$i day" +%F`
#	echo -----------"$cmd"ods_to_dwd_db------------
#	ods_to_dwd_db.sh all $cmd
	echo -----------"$cmd"dwd_to_dws------------
	dwd_to_dws.sh $cmd
	echo -----------"$cmd"dws_to_dwt------------
	dws_to_dwt.sh $cmd
done