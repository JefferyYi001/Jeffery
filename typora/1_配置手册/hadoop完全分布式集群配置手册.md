# 0. 配置规划

| **hadoop106** | **hadoop107**     | **hadoop108**   |
| ------------- | ----------------- | --------------- |
| NameNode      | SecondaryNameNode | ResourseManager |
| DataNode      | DataNode          | DataNode        |
|               | HistoryServer     |                 |

# 1. 虚拟机准备

克隆3台虚拟机，配置网络，并配置SSH、群发脚本和群控脚本，方便后边的文件传输和集中控制。

## 1.1 网络配置

（1）删除重复删除重复的 eth0 配置（CentOS6需要执行此步骤）

```bash
vim /etc/udev/rules.d/70-persistent-net.rules
```

​		删除“eth0”这一行，并将下一行的“eth1”修改为 “eth0”

（2）修改克隆虚拟机的静态IP

```bash
vim /etc/sysconfig/network-scripts/ifcfg-eth0
```

​		配置为（以hadoop106为例）：

```bash
DEVICE=eth0
TYPE=Ethernet
ONBOOT=yes
BOOTPROTO=static
NAME="eth0"
IPADDR=192.168.1.106
PREFIX=24
GATEWAY=192.168.1.2
DNS1=192.168.1.2
```

​		注：IP、网关配置需保证win、VMware、虚拟机三码合一。win、虚拟机防火墙关闭。

（3）修改主机名

```bash
vim /etc/sysconfig/network
```

​		配置为：

```bash
NETWORKING=yes
NETWORKING_IPV6=no
HOSTNAME= hadoop106
```

（4）配置主机名称映射

```bash
vim /etc/hosts
```

​		添加以下内容：

```
192.168.1.100 hadoop100
192.168.1.101 hadoop101
192.168.1.102 hadoop102
192.168.1.103 hadoop103
192.168.1.104 hadoop104
192.168.1.105 hadoop105
192.168.1.106 hadoop106
192.168.1.107 hadoop107
192.168.1.108 hadoop108
```

​		修改window7的主机映射文件（hosts文件）。进入C:\Windows\System32\drivers\etc路径，打开hosts文件并添加如下内容：

```
192.168.1.100 hadoop100

192.168.1.101 hadoop101

192.168.1.102 hadoop102

192.168.1.103 hadoop103

192.168.1.104 hadoop104

192.168.1.105 hadoop105

192.168.1.106 hadoop106

192.168.1.107 hadoop107

192.168.1.108 hadoop108
```

（4）修改window10的主机映射文件（hosts文件）。进入C:\Windows\System32\drivers\etc路径，在hosts文件中添加如下内容：

​    （c）打开桌面hosts文件并添加如下内容

```
192.168.1.100 hadoop100

192.168.1.101 hadoop101

192.168.1.102 hadoop102

192.168.1.103 hadoop103

192.168.1.104 hadoop104

192.168.1.105 hadoop105

192.168.1.106 hadoop106

192.168.1.107 hadoop107

192.168.1.108 hadoop108
```

之后重启机器即可，其他两台仿照该配置进行处理。



## 1.2 配置 SSH

NameNode 和 ResourseManager 所在节点需配置SSH，以供无密访问其他节点。

```
ssh-keygen -t rsa
```

之后连敲三次回车，完成配置，并将公钥发送到各个节点。

```
ssh-copy-id hadoop106
ssh-copy-id hadoop107
ssh-copy-id hadoop108
```



### 1.2.1 ssh 报错解决方案

修改/etc/ssh/ssh_config文件（或$HOME/.ssh/config）中的配置

```bash
sudo vim /etc/ssh/ssh_config
```

底部添加如下两行配置：

```
StrictHostKeyChecking no
UserKnownHostsFile /dev/null
```

修改好配置后，重新启动sshd服务即可，Centos6重启ssh服务命令为 ：

```bash
sudo service sshd restart
```

## 1.3 配置群发脚本和群控脚本

在/home/jeffery目录下创建bin目录，并在bin目录下创建xsync文件，文件内容如下：

```bash
#!/bin/bash
#1 获取输入参数个数，如果没有参数，直接退出
pcount=$#
if ((pcount==0)); then
echo no args;
exit;
fi

#2 获取文件名称
p1=$1
fname=`basename $p1`
echo fname=$fname

#3 获取上级目录到绝对路径
pdir=`cd -P $(dirname $p1); pwd`
echo pdir=$pdir

#4 获取当前用户名称
user=`whoami`

#5 循环
for host in hadoop106 hadoop107 hadoop108
do
    echo ------------------- $host --------------
    rsync -av $pdir/$fname $user@$host:$pdir
done
```

在bin目录下创建xcall文件，文件内容如下：

```bash
#!/bin/bash
params=$@

for((i=106 ;i<=108 ;i=$i+1 ));do
	echo ==========hadoop$i $params==========
	ssh hadoop$i "source /etc/profile;$params"
done
```

注：脚本中的主机名需要根据实际配置进行重命名。

# 2. 安装 JDK 和 Hadoop

可以采用安装的方式使用以下命令进行安装，也可以从其他已经安装了 JDK 和 Hadoop 的节点上使用 scp 命令进行复制。

```bash
tar -zxvf jdk-8u144-linux-x64.tar.gz -C /opt/module/
```

```bash
tar -zxvf hadoop-2.7.2.tar.gz -C /opt/module/
```

之后配置环境变量

```bash
 sudo vim /etc/profile
```

在下方加入以下内容

```bash
#JAVA_HOME
export JAVA_HOME=/opt/module/jdk1.8.0_144
export PATH=$PATH:$JAVA_HOME/bin

##HADOOP_HOME
export HADOOP_HOME=/opt/module/hadoop-2.7.2
export PATH=$PATH:$HADOOP_HOME/bin
export PATH=$PATH:$HADOOP_HOME/sbin
```

同样可以从其他已经安装了 JDK 和 Hadoop 的节点上使用 scp 命令进行复制。之后使用群发脚本xsync 将上述文档同步至其他节点。

```bash
xsync /opt/module/jdk1.8.0_144/
```

```
xsync /opt/module/hadoop-2.7.2/
```

```
xsync /etc/profile
```

注意权限问题：

（1）配置文件 profile 需用 root 进行发送。

（2）JDK 和 Hadoop 发送后需要在接收端机器修改其所有者。

```bash
sudo chown jeffery:jeffery -R /opt/module/
```

检验是否安装成功

```
xcall source /etc/profile
xcall java -version
xcall hadoop version
```

# 3. 配置集群

接下来根据集群的配置修改 /opt/module/hadoop-2.7.2/etc/hadoop/ 路径下的配置文件。

| hadoop106      | **hadoop107**     | **hadoop108**   |
| -------------- | ----------------- | --------------- |
| NameNode       | SecondaryNameNode | ResourseManager |
| DataNode       | DataNode          | DataNode        |
| 时间同步服务器 | HistoryServer     |                 |

## 3.1 核心配置文件 core-site.xml

```xml
<!-- 指定HDFS中NameNode的地址 -->
<property>
		<name>fs.defaultFS</name>
      <value>hdfs://hadoop106:9000</value>
</property>

<!-- 指定Hadoop运行时产生文件的存储目录 -->
<property>
		<name>hadoop.tmp.dir</name>
		<value>/opt/module/hadoop-2.7.2/data/tmp</value>
</property>
```

## 3.2 HDFS配置文件 hadoop-env.sh

```bash
export JAVA_HOME=/opt/module/jdk1.8.0_144
```

## 3.3 HDFS配置文件 hdfs-site.xml

```xml
<property>
		<name>dfs.replication</name>
		<value>3</value>
</property>

<!-- 指定Hadoop辅助名称节点主机配置 -->
<property>
      <name>dfs.namenode.secondary.http-address</name>
      <value>hadoop107:50090</value>
</property>
```

## 3.4 YARN配置文件 yarn-env.sh

```bash
export JAVA_HOME=/opt/module/jdk1.8.0_144
```

## 3.5 YARN配置文件 yarn-site.xml

```xml
<!-- Reducer获取数据的方式 -->
<property>
		<name>yarn.nodemanager.aux-services</name>
		<value>mapreduce_shuffle</value>
</property>

<!-- 指定YARN的ResourceManager的地址 -->
<property>
		<name>yarn.resourcemanager.hostname</name>
		<value>hadoop108</value>
</property>

<!-- 日志聚集功能使能 -->
<property>
    <name>yarn.log-aggregation-enable</name>
    <value>true</value>
</property>

<!-- 日志保留时间设置7天 -->
<property>
    <name>yarn.log-aggregation.retain-seconds</name>
    <value>604800</value>
</property>
```

## 3.6 MapReduce 配置文件 mapred-env.sh

```bash
export JAVA_HOME=/opt/module/jdk1.8.0_144
```

## 3.7 MapReduce 配置文件 mapred-site.xml

```xml
<!-- 指定MR运行在Yarn上 -->
<property>
		<name>mapreduce.framework.name</name>
		<value>yarn</value>
</property>

<!-- 历史服务器端地址 -->
<property>
    <name>mapreduce.jobhistory.address</name>
    <value>hadoop107:10020</value>
</property>

<!-- 历史服务器web端地址 -->
<property>
    <name>mapreduce.jobhistory.webapp.address</name>
    <value>hadoop107:19888</value>
</property>

```

## 3.8 集群配置文件 slaves

```
hadoop106
hadoop107
hadoop108
```

## 3.9 集群时间同步

### 3.9.1 时间服务器配置（必须root用户）

​			检查 ntp 是否安装

```bash
rpm -qa|grep ntp
```

​			修改 ntp 配置文件

```bash
vim /etc/ntp.conf
```

​			修改内容如下

a）修改1（授权192.168.10.0-192.168.10.255网段上的所有机器可以从这台机器上查询和同步时间）

```bash
# 取消注释
# restrict 192.168.10.0 mask 255.255.255.0 nomodify notrap
restrict 192.168.10.0 mask 255.255.255.0 nomodify notrap
```

b）修改2（集群在局域网中，不使用其他互联网上的时间）

```bash
# 注释以下内容
server 0.centos.pool.ntp.org iburst

server 1.centos.pool.ntp.org iburst

server 2.centos.pool.ntp.org iburst

server 3.centos.pool.ntp.org iburst

# server 0.centos.pool.ntp.org iburst

# server 1.centos.pool.ntp.org iburst

# server 2.centos.pool.ntp.org iburst

# server 3.centos.pool.ntp.org iburst
```

c）添加3（当该节点丢失网络连接，依然可以采用本地时间作为时间服务器为集群中的其他节点提供时间同步）

```bash
server 127.127.1.0
fudge 127.127.1.0 stratum 10
```

​			修改 /etc/sysconfig/ntpd 文件

```bash
vim /etc/sysconfig/ntpd
```

​			增加内容如下（让硬件时间与系统时间一起同步）

```bash
SYNC_HWCLOCK=yes
```

​			重新启动 ntpd 服务

```bash
service ntpd status
# ntpd 已停
service ntpd start
```

​			设置ntpd服务开机启动

```bash
chkconfig ntpd on
```

### 3.9.2 其他机器配置（必须root用户）

（1）在其他机器配置1分钟与时间服务器同步一次

```bash
crontab -e
```

编写定时任务如下：

```bash
*/1 * * * * /usr/sbin/ntpdate hadoop106
```

（2）修改任意机器时间测试

```bash
date -s "2017-9-11 11:11:11"
```

（3）1分钟后查看机器是否与时间服务器同步

```
date
```

# 4. 群起集群

首先在 NameNode 所在节点执行一次格式化

```bash
hdfs namenode -format
```

**注意**：格式化之前需删除 /opt/module/hadoop-2.7.2/data 和 /opt/module/hadoop-2.7.2/logs。

```bash
xcall rm -rf /opt/module/hadoop-2.7.2/data
xcall rm -rf /opt/module/hadoop-2.7.2/logs
```

**注意**：rm -rf 命令使用要格外谨慎，尤其是部分代码需复制时，不能复制换行符！！！

之后再在 NameNode 所在节点启动HDFS，并用 jps 查看是否启动成功

```bash
start-dfs.sh
```

之后再在 HistoryServer 所在节点启动历史服务器，并用 jps 查看是否启动成功

```bash
mr-jobhistory-daemon.sh start historyserver
```

最后再在 ResourseManager 所在节点启动YARN，并用 jps 查看是否启动成功

```
start-yarn.sh
```