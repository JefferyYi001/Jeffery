# 一	配置HDFS-HA集群

## 1.环境搭建

##### 1.母机信息

```shell
1 用户atguigu信息	
	useradd atguigu
2 主机用户名映射(100~106) 
	linux: vim /etc/hosts
	windows10: C:\Windows\System32\drivers\etc\hosts
3 环境变量信息
    #JAVA_HOME
    export JAVA_HOME=/opt/module/jdk1.8.0_144
    PATH=$PATH:$JAVA_HOME/bin
    #HADOOP_HOME
    export HADOOP_HOME=/opt/module/hadoop-2.7.2
    PATH=$PATH:$HADOOP_HOME/bin
    PATH=$PATH:$HADOOP_HOME/sbin
    #ZOOKEEPER_HOME
    export ZOOKEEPER_HOME=/opt/module/zookeeper
    PATH=$PATH:$ZOOKEEPER_HOME/bin
    #HIVE_HOME
    export HIVE_HOME=/opt/module/hive
    PATH=$PATH:$HIVE_HOME/bin
4 用户atguigu ALL权限(91行)
	/etc/sudoers
5 opt目录下创建module和software
	所属用户和所在组更改: chown atguigu:atguigu module/ software/
6 上传jdk,hadoop,zookeeper,hive,mysql 安装文件到 /opt/software

8088端口: YARN查看运行进程 
50070端口: HDFS查看集群文件
19888端口: 查看JobHistory历史日志
50090端口: SecondaryNameNode信息
```

##### 2.克隆虚拟机

```shell
集群环境：
centOs6.8：hadoop102，hadoop103，hadoop104
jdk版本：jdk1.8.0_144
hadoop版本：Hadoop 2.7.2
首先准备三台客户机（hadoop102，hadoop103，hadoop104），关闭防火墙，修改为静态ip和ip地址映射

1 删除重复的eth0
	vim /etc/udev/rules.d/70-persistent-net.rules
2 修改静态IP
	vim /etc/sysconfig/network-scripts/ifcfg-eth0 
3 修改主机名
	vim /etc/sysconfig/network
4 查看主机映射(母机和win10上已经配置过了)
	vim /etc/hosts
5 查看防火墙状态(母机上是关闭状态)
	service iptables status
6 解压/opt/software下 jdk和hadoop
	tar -zxvf RPM包名 -C 解压路径
7 查看环境变量(母机上已经配置过了)
	vim /etc/profile
```

## 2.SSH无密登录配置

##### 1.生成公钥和秘钥

```
ssh-keygen -t rsa
敲三个回车就会生成两个文件
id_rsa（私钥）、id_rsa.pub（公钥）
```

##### 2.公钥拷贝到客户机

```
将公钥拷贝到要免密登录的目标机器上
ssh-copy-id hadoop102
ssh-copy-id hadoop103
ssh-copy-id hadoop104
注意：ssh访问自己也需要输入密码，所以我们需要将公钥也拷贝给102
注意: 配置了NameNode和resourcemanager的客户机都需要无密登录权限
```



## 3.编写脚本

##### 1.分发脚本

```shell
#!/bin/bash
#1 获取输入参数个数，如果没有参数，直接退出
pcount=$#
if((pcount==0)); then
echo no args;
exit;
fi

#2 获取文件名称
p1=$1
fname=`basename $p1`
echo fname=$fname

#3 获取上级目录到绝对路径 –P指向实际物理地址，防止软连接
pdir=`cd -P $(dirname $p1); pwd`
echo pdir=$pdir

#4 获取当前用户名称
user=`whoami`

#5 循环
for((host=102; host<=104; host++)); do
        echo ------------------- hadoop$host --------------
        rsync -rvl $pdir/$fname $user@hadoop$host:$pdir
done
```

##### 2.JPS脚本

```shell
#!/bin/bash
for i in hadoop102 hadoop103 hadoop104
do
        echo "================           $i        ================="
        ssh $i '/opt/module/jdk1.8.0_144/bin/jps'
done
```

##### 3.群起ZK及hadoop相关进程

```shell
#!/bin/bash
echo "=================     开始启动所有节点服务 ================="
echo "================      正在启动Zookeeper    ================="
for i in hadoop102 hadoop103 hadoop104
do
        ssh $i 'source /etc/profile && /opt/module/zookeeper/bin/zkServer.sh start'
done
echo "=================     正在格式化ZKFC     ====================="
for i in hadoop102 hadoop103
do
        ssh $i 'source /etc/profile && /opt/module/hadoop-2.7.2/bin/hdfs zkfc -formatZK'
done
echo "=================     正在启动HDFS     ===================="
ssh atguigu@hadoop102 '/opt/module/hadoop-2.7.2/sbin/start-dfs.sh'
echo "=================    正在启动YARN       ====================="
ssh atguigu@hadoop104 '/opt/module/hadoop-2.7.2/sbin/start-yarn.sh'
echo "=================    正在启动RM备胎    ==================="
ssh atguigu@hadoop103 '/opt/module/hadoop-2.7.2/sbin/yarn-daemon.sh start resourcemanager'
echo "================= 正在开启JobHistoryServer  ==============="
ssh atguigu@hadoop102 '/opt/module/hadoop-2.7.2/sbin/mr-jobhistory-daemon.sh start historyserver'
```

##### 4.群关zk及hadoop相关进程

```shell
#!/bin/bash
echo "================     开始关闭所有节点服务       ============="
echo "================     正在关闭Zookeeper         ============="
for i in hadoop102 hadoop103 hadoop104
do
        ssh $i 'source /etc/profile && /opt/module/zookeeper/bin/zkServer.sh stop'
done
echo "================     正在关闭HDFS       ==================="
ssh atguigu@hadoop102 '/opt/module/hadoop-2.7.2/sbin/stop-dfs.sh'
echo "================     正在关闭YARN        ==================="
ssh atguigu@hadoop104 '/opt/module/hadoop-2.7.2/sbin/stop-yarn.sh'
echo "================     正在关闭RM备胎        ==================="
ssh atguigu@hadoop103 '/opt/module/hadoop-2.7.2/sbin/yarn-daemon.sh stop resourcemanager'
echo "================    正在关闭JobHistoryServer   ============="
ssh atguigu@hadoop102 '/opt/module/hadoop-2.7.2/sbin/mr-jobhistory-daemon.sh stop historyserver'
```

##### 5.同步执行命令

```shell
#!/bin/bash
#验证参数
if(($#==0))
then
        echo 请传入要执行的命令
        exit;
fi

echo "要执行的命令是:$@"

#批量执行
for((i=102;i<=104;i++))
do
        echo -----------------------hadoop$i---------------------
        ssh  hadoop$i $@
done
```

```shell
修改脚本xsync具有执行权限，并调用脚本，将脚本复制到其他节点
chmod 777 xsync
xsync /home/atguigu/bin
```

## 4.更改集群配置文件

### core核心配置文件

##### ① 配置core-site.xml

```xml
<configuration>
<!-- 把两个NameNode）的地址组装成一个集群mycluster -->
		<property>
			<name>fs.defaultFS</name>
        	<value>hdfs://mycluster</value>
		</property>

		<!-- 指定hadoop运行时产生文件的存储目录 -->
		<property>
			<name>hadoop.tmp.dir</name>
			<value>/opt/module/hadoop-2.7.2/data/tmp</value>
		</property>
    	<!-- 故障转移需要的zookeeper集群设置-->
        <property>
        <name>ha.zookeeper.quorum</name>
        <value>hadoop102:2181,hadoop103:2181,hadoop104:2181</value>
    	</property>
</configuration>
```

### HDFS配置文件

##### ② 配置hadoop-env.sh

```shell
export JAVA_HOME=/opt/module/jdk1.8.0_144
```

##### ③ 配置hdfs-site.xml

```xml
<configuration>
    <!-- 指定数据冗余份数,备份数 -->
    <property>
        <name>dfs.replication</name>
        <value>3</value>
    </property>
    
	<!-- 完全分布式集群名称,和core-site集群名称必须一致 -->
	<property>
		<name>dfs.nameservices</name>
		<value>mycluster</value>
	</property>

	<!-- 集群中NameNode节点都有哪些 -->
	<property>
		<name>dfs.ha.namenodes.mycluster</name>
		<value>nn1,nn2</value>
	</property>

	<!-- nn1的RPC通信地址 -->
	<property>
		<name>dfs.namenode.rpc-address.mycluster.nn1</name>
		<value>hadoop102:9000</value>
	</property>

	<!-- nn2的RPC通信地址 -->
	<property>
		<name>dfs.namenode.rpc-address.mycluster.nn2</name>
		<value>hadoop103:9000</value>
	</property>

	<!-- nn1的http通信地址 -->
	<property>
		<name>dfs.namenode.http-address.mycluster.nn1</name>
		<value>hadoop102:50070</value>
	</property>

	<!-- nn2的http通信地址 -->
	<property>
		<name>dfs.namenode.http-address.mycluster.nn2</name>
		<value>hadoop103:50070</value>
	</property>

	<!-- 指定NameNode元数据在JournalNode上的存放位置 -->
	<property>
		<name>dfs.namenode.shared.edits.dir</name>
	<value>qjournal://hadoop102:8485;hadoop103:8485;hadoop104:8485/cluster</value>
	</property>

	<!-- 配置隔离机制，即同一时刻只能有一台服务器对外响应 -->
	<property>
		<name>dfs.ha.fencing.methods</name>
		<value>sshfence</value>
	</property>

	<!-- 使用隔离机制时需要ssh无秘钥登录-->
	<property>
		<name>dfs.ha.fencing.ssh.private-key-files</name>
		<value>/home/atguigu/.ssh/id_rsa</value>
	</property>

	<!-- 声明journalnode服务器存储目录-->
	<property>
		<name>dfs.journalnode.edits.dir</name>
		<value>/opt/module/hadoop-2.7.2/data/jn</value>
	</property>

	<!-- 关闭权限检查-->
	<property>
		<name>dfs.permissions.enable</name>
		<value>false</value>
	</property>

	<!-- 访问代理类：client，cluster，active配置失败自动切换实现方式-->
	<property>
  		<name>dfs.client.failover.proxy.provider.mycluster</name>	<value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>
	</property>
    
　　<!-- 故障自动转移设置为true -->
　　<property>
  　　 <name>dfs.ha.automatic-failover.enabled</name>
  　　 <value>true</value>
　　</property>
    
     <!--发生failover时，Standby的节点要执行一系列方法把原来那个Active节点中不健康的NameNode服务给杀掉，
 这个叫做fence过程。sshfence会通过ssh远程调用fuser命令去找到Active节点的NameNode服务并杀死它-->
 <property>
     <name>dfs.ha.fencing.methods</name>
     <value>shell(/bin/true)</value>
  </property>
    
</configuration>
```

### YARN配置文件

##### ④ 配置yarn-env.sh

```shell
export JAVA_HOME=/opt/module/jdk1.8.0_144
```

##### ⑤ 配置yarn-site.xml

```xml
<configuration>

    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>

    <!--启用resourcemanager ha-->
    <property>
        <name>yarn.resourcemanager.ha.enabled</name>
        <value>true</value>
    </property>
 
    <!--声明两台resourcemanager的地址-->
    <property>
        <name>yarn.resourcemanager.mycluster-id</name>
        <value>mycluster-yarn1</value>
    </property>

    <property>
        <name>yarn.resourcemanager.ha.rm-ids</name>
        <value>rm1,rm2</value>
    </property>

    <property>
        <name>yarn.resourcemanager.hostname.rm1</name>
        <value>hadoop103</value>
    </property>

    <property>
        <name>yarn.resourcemanager.hostname.rm2</name>
        <value>hadoop104</value>
    </property>
 
    <!--指定zookeeper集群的地址--> 
    <property>
        <name>yarn.resourcemanager.zk-address</name>
        <value>hadoop102:2181,hadoop103:2181,hadoop104:2181</value>
    </property>

    <!--启用自动恢复--> 
    <property>
        <name>yarn.resourcemanager.recovery.enabled</name>
        <value>true</value>
    </property>
 
    <!--指定resourcemanager的状态信息存储在zookeeper集群--> 
    <property>
        <name>yarn.resourcemanager.store.class</name>     <value>org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore</value>
	</property>

    <!-- 日志聚集功能 -->
    <property>
        <name>yarn.log-aggregation-enable</name>
        <value>true</value>
    </property>

    <!-- 日志保留时间设置7天 -->
    <property>
        <name>yarn.log-aggregation.retain-seconds</name>
            <value>604800</value>
    </property>
    
</configuration>
```

### MapReduce配置文件

##### ⑥ 配置mapred-env.sh

```shell
export JAVA_HOME=/opt/module/jdk1.8.0_144
```

##### ⑦ 配置mapred-site.xml

```xml
注:第一次配置需要将mapred-site.xml.template文件重命名为mapred-site.xml

<!-- 指定MR运行在Yarn上 -->
<property>
		<name>mapreduce.framework.name</name>
		<value>yarn</value>
</property>

<!-- =======mapred-site.xml下配置历史服务器====== -->

<!-- 历史服务器端地址 -->
<property>
    <name>mapreduce.jobhistory.address</name>
    <value>hadoop102:10020</value>
</property>
<!-- 历史服务器web端地址 -->
<property>
    <name>mapreduce.jobhistory.webapp.address</name>
    <value>hadoop102:19888</value>
</property>
```

### slaves配置文件

```
1. 配置slaves
2. 切换目录到：hadoop安装目录/etc/hadoop/
3. 在目录下的slaves文件中添加如下内容
# 注意结尾不能有空格，文件中不能有空行
hadoop102
hadoop103
hadoop104
同步所有节点配置文件
xsync slaves
```

```
指定hadoop节点的配置文件目录
xsync /opt/module/hadoop-2.7.2/etc/hadoop/
```

## 5.集群时间同步

### 1.配置时间服务器

```shell
1 检查ntp是否安装(必须是root用户配置)
rpm -qa|grep ntp
	ntp-4.2.6p5-10.el6.centos.x86_64
    fontpackages-filesystem-1.41-1.1.el6.noarch
    ntpdate-4.2.6p5-10.el6.centos.x86_64
2 修改ntp配置文件
vi /etc/ntp.conf
	修改一: 授权192.168.1.0-192.168.1.255网段上的所有机器可以从这台机器上查询和同步时间
		#restrict 192.168.1.0 mask 255.255.255.0 nomodify notrap 
			restrict 192.168.1.0 mask 255.255.255.0 nomodify notrap
	修改二: 集群在局域网中，不使用其他互联网上的时间
        server 0.centos.pool.ntp.org iburst
        server 1.centos.pool.ntp.org iburst
        server 2.centos.pool.ntp.org iburst
        server 3.centos.pool.ntp.org iburst	
        	将上面这四段注销即可
    添加三: 当该节点丢失网络连接，依然可以采用本地时间作为时间服务器为集群中的其他节点提供时间同步
    	server 127.127.1.0
		fudge 127.127.1.0 stratum 10	
3 修改/etc/sysconfig/ntpd 文件
	vim /etc/sysconfig/ntpd
		增加内容:让硬件时间与系统时间一起同步
		SYNC_HWCLOCK=yes
4 重新启动ntpd服务
	service ntpd status
	service ntpd start
5 设置开机自启动
	chkconfig ntpd on
6 其他机器配置(必须是root用户)
	1)配置10分钟与时间服务器同步一次
		crontab -e
		*/10 * * * * /usr/sbin/ntpdate hadoop102
	2)修改任意机器时间测试
		date -s "2017-9-11 11:11:11"
```

# 二	部署ZooKeeper

## 1.配置zoo.cfg文件

```shell
1.重命名zookeeper目录下conf目录下的zoo_sample.cfg为zoo.cfg文件
mv zoo_sample.cfg zoo.cfg
2.修改配置文件信息
vim zoo.cfg
	修改zookeeper数据存储路径
dataDir=/opt/module/zookeeper-3.4.10/zkData
	增加配置
server.102=hadoop102:2888:3888
server.103=hadoop103:2888:3888
server.104=hadoop104:2888:3888
3.分发zoo.cfg
```

## 2.创建数据存储路径

```shell
mkdir /opt/module/zookeeper-3.4.10/zkData
```

## 3.配置服务器编号

```
在创建好的zookeeper数据存储目录下创建一个myid文件
在文件中添加对应的节点编号(注意空格和缩进)
例在hadoop102节点上对应的编号为:102
```

# 三 部署Hive

## 1.安装Hive

```
解压安装即可使用
```



## 2.部署MySQL存储Metastore

##### 1.安装MySQL

```
1.检测当前机器是否已经安装了mysql
rpm -qa | grep mysql
rpm -qa | grep MySQL
2.卸载之前安装的残留包
sudo rpm -e --nodeps mysql-libs-5.1.73-7.el6.x86_6
3.安装服务端
sudo rpm -ivh MySQL-server-5.6.24-1.el6.x86_64.rpm
4.安装客户端
sudo rpm -ivh MySQL-client-5.6.24-1.el6.x86_64.rpm
5.查看随机生成的密码
sudo cat /root/.mysql_secret
6.启动服务
sudo service mysql start
7.登录后修改密码
mysql -uroot -p刚查询的随机密码
8.修改密码
SET PASSWORD=password('密码')
```

##### 2.提供一个可以从任一机器访问服务的用户

```
1.查询当前有哪些用户
select host,user,password from mysql.user;
2.删除除了localhost的所有用户
delete from mysql.user where host <> 'localhost';
3.修改root用户可以从任意机器登录
update mysql.user set host='%' where user='root';
4.刷新权限
flush privileges;
5.从外部登录
sudo mysql -h hadoop102 -uroot -p123123
6.查看当前连接的线程
sudo mysqladmin processlist -uroot -p123123
```

##### 3.Hive元数据配置到MySQL

**驱动拷贝**

```shell
解压mysql-connector-java-5.1.27.tar.gz驱动包
tar -zxvf mysql-connector-java-5.1.27.tar.gz
拷贝mysql-connector-java-5.1.27-bin.jar到/opt/module/hive/lib/

```

**配置Metastore到MySQL**

```
在hive目录下conf目录下创建一个hive-site.xml根据官方文档配置参数,拷贝数据到hive-site.xml文件中
```

```xml
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    
    <!-- 元数据存储在MySQL服务器中 -->
	<property>
	  <name>javax.jdo.option.ConnectionURL</name>
	  <value>jdbc:mysql://hadoop102:3306/metastore?createDatabaseIfNotExist=true</value>
	  <description>JDBC connect string for a JDBC metastore</description>
	</property>
	<!-- MySQL JDBC驱动程序类  -->
	<property>
	  <name>javax.jdo.option.ConnectionDriverName</name>
	  <value>com.mysql.jdbc.Driver</value>
	  <description>Driver class name for a JDBC metastore</description>
	</property>
	<!-- 连接MySQL服务器的用户名  -->
	<property>
	  <name>javax.jdo.option.ConnectionUserName</name>
	  <value>root</value>
	  <description>username to use against metastore database</description>
	</property>
	<!-- 连接MySQL服务器的密码  -->
	<property>
	  <name>javax.jdo.option.ConnectionPassword</name>
	  <value>123123</value>
	  <description>password to use against metastore database</description>
	</property>
    <!-- Hive数据仓库位置  -->
	<property>
	  <name>hive.metastore.warehouse.dir</name>
	  <value>/user/hive/warehouse</value>
	  <description>location of default database for the warehouse</description>
	</property>	
    <!-- 显示表头信息  -->
    <property>
	  <name>hive.cli.print.header</name>
	  <value>true</value>
	</property>
	<!-- 显示当前数据库  -->
    <property>
        <name>hive.cli.print.current.db</name>
        <value>true</value>
    </property>
    
</configuration>
```

##### 4.Hive运行日志信息配置

```shell
1．Hive的log默认存放在/tmp/atguigu/hive.log目录下（当前用户名下）
2．修改hive的log存放日志到/opt/module/hive/logs
	（1）修改/opt/module/hive/conf/hive-log4j.properties.template文件名称为
hive-log4j.properties
	（2）在hive-log4j.properties文件中修改log存放位置
hive.log.dir=/opt/module/hive/logs 
	注:不需要手动创建自己会产生logs文件夹
```

# 第一次启动注意事项

```shell
我试了一下 需要先启动集群start-dfs.sh 然后节点上的nn都起不来猜测是因为集群不知道是那一台nn工作所以应该是hadoop的判定机制防止脑裂现象 这时候起来的有JN和DN 还开启了ZK的故障转移模式(但是我并没有启动zk集群)这个时候再进行格式化102节点的nn,就可以正常格式化了 生成文件data和logs后 在使用备胎nn去同步镜像文件和日志 成功后就可以启动备胎NN了 
1.启动集群
start-dfs.sh
2.格式化namenode
hdfs namenode -format
3.启动nn1的namenode
hadoop-daemon.sh namenode start
4.在nn2上同步nn1的镜像文件和日志
hdfs namenode -bootstrapStandby
5.启动nn2
hadoop-daemon.sh start namenode
6.关闭所有服务
stop-dfs.sh
7.使用脚本群起
clusterStart
```

