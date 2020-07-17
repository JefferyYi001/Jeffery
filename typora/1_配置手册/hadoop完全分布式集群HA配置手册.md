# 1. 环境准备

1.  修改IP

2. 修改主机名及主机名和IP地址的映射

3. 关闭防火墙

4. ssh免密登录

5. 安装JDK，配置环境变量等

# 2. 规划集群

| hadoop102       | hadoop103       | hadoop104   |
| --------------- | --------------- | ----------- |
| NameNode        | NameNode        |             |
| JournalNode     | JournalNode     | JournalNode |
| DataNode        | DataNode        | DataNode    |
| ZK              | ZK              | ZK          |
| ResourceManager | ResourceManager |             |
| NodeManager     | NodeManager     | NodeManager |

# 3. 配置Zookeeper集群



## 3.1 集群规划

在hadoop102、hadoop103和hadoop104三个节点上部署Zookeeper。



## 3.2 解压安装

（1）解压Zookeeper安装包到/opt/module/目录下

```bash
tar -zxvf zookeeper-3.4.10.tar.gz -C /opt/module/
```

（2）同步/opt/module/zookeeper-3.4.10目录内容到hadoop103、hadoop104

```bash
xsync zookeeper-3.4.10/
```



## 3.3 配置服务器编号

（1）在/opt/module/zookeeper-3.4.10/这个目录下创建zkData

```bash
mkdir -p zkData
```

（2）在/opt/module/zookeeper-3.4.10/zkData目录下创建一个myid的文件

```bash
touch myid
```

（3）编辑myid文件

```bash
vi myid
```

​    在文件中添加与server对应的编号：2

（4）拷贝配置好的zookeeper到其他机器上

```bash
xsync myid
```

并分别在hadoop102、hadoop103上修改myid文件中内容为3、4



## 3.4 配置zoo.cfg文件

（1）重命名 /opt/module/zookeeper-3.4.10/conf 这个目录下的 zoo_sample.cfg 为 zoo.cfg

```bash
mv zoo_sample.cfg zoo.cfg
```

（2）打开zoo.cfg文件

```bash
vim zoo.cfg
```

修改数据存储路径配置

```bash
dataDir=/opt/module/zookeeper-3.4.10/zkData
```

增加如下配置

```bash
#######################cluster##########################
server.2=hadoop102:2888:3888
server.3=hadoop103:2888:3888
server.4=hadoop104:2888:3888
```

（3）同步zoo.cfg配置文件

```bash
xsync zoo.cfg
```



# 4. 配置 HDFS-HA 集群



## 4.1 配置 core-site.xml

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
    <!-- 自动故障转移 -->
    <property>
	<name>ha.zookeeper.quorum</name>
	<value>hadoop102:2181,hadoop103:2181,hadoop104:2181</value>
</property>

</configuration>
```



## 4.2 配置 hdfs-site.xml

```xml
<configuration>
	<!-- 完全分布式集群名称 -->
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
	<value>qjournal://hadoop102:8485;hadoop103:8485;hadoop104:8485/mycluster</value>
	</property>

	<!-- 配置隔离机制，即同一时刻只能有一台服务器对外响应 -->
	<property>
		<name>dfs.ha.fencing.methods</name>
		<value>sshfence</value>
	</property>

	<!-- 使用隔离机制时需要ssh无秘钥登录-->
	<property>
		<name>dfs.ha.fencing.ssh.private-key-files</name>
		<value>/home/jeffery/.ssh/id_rsa</value>
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

	<!-- 访问代理类：client，mycluster，active配置失败自动切换实现方式-->
	<property>
  		<name>dfs.client.failover.proxy.provider.mycluster</name>
	<value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>
	</property>
    
    <!-- 自动故障转移-->
    <property>
	<name>dfs.ha.automatic-failover.enabled</name>
	<value>true</value>
</property>

</configuration>

```

拷贝配置好的 hadoop 环境到其他节点

```bash
xsync hdfs-site.xml
xsync core-site.xml
```



## 4.3 手动启动

首先删除数据与日志文件

```bash
xcall rm -rf /opt/module/hadoop-2.7.2/data
xcall rm -rf /opt/module/hadoop-2.7.2/logs
```

启动 journalnode 服务

```bash
hadoop-daemons.sh start journalnode
```

在[nn1]上，对其进行格式化，并启动

```bash
hdfs namenode -format
hadoop-daemon.sh start namenode
```

在[nn2]上，同步nn1的元数据信息

```bash
hadoop-daemon.sh start namenode
```

在[nn1]上，启动所有datanode

```bash
sbin/hadoop-daemons.sh start datanode
```

将[nn1]切换为Active

```bash
bin/hdfs haadmin -transitionToActive nn1
```

查看是否Active

```bash
bin/hdfs haadmin -getServiceState nn1
```



## 4.4 集中启动

（1）关闭所有HDFS服务：

```bash
sbin/stop-dfs.sh
```

（2）启动Zookeeper集群：

```bash
bin/zkServer.sh start
```

（3）初始化HA在Zookeeper中状态：

```bash
bin/hdfs zkfc -formatZK
```

（4）启动HDFS服务：

```bash
sbin/start-dfs.sh
```

（5）在各个NameNode节点上启动DFSZK Failover Controller，先在哪台机器启动，哪个机器的NameNode就是Active NameNode

```bash
sbin/hadoop-daemon.sh start zkfc
```



# 5. 配置 YARN-HA 集群



## 5.1 配置 yarn-site.xml

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
        <name>yarn.resourcemanager.cluster-id</name>
        <value>cluster-yarn1</value>
    </property>

    <property>
        <name>yarn.resourcemanager.ha.rm-ids</name>
        <value>rm1,rm2</value>
    </property>

    <property>
        <name>yarn.resourcemanager.hostname.rm1</name>
        <value>hadoop102</value>
    </property>

    <property>
        <name>yarn.resourcemanager.hostname.rm2</name>
        <value>hadoop103</value>
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

</configuration>

```

同步更新其他节点的配置信息

```bash
xsync yarn-site.xml
```

启动YARN 

（1）在hadoop102中执行：

```bash
sbin/start-yarn.sh
```

（2）在hadoop103中执行：

```bash
sbin/yarn-daemon.sh start resourcemanager
```

（3）查看服务状态

```bash
bin/yarn rmadmin -getServiceState rm1
```

