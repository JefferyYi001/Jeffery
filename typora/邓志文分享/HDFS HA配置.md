# 1. 脚本

### JPS

```shell
#!/bin/bash
for i in hadoop102 hadoop103 hadoop104
do
        echo "================           $i             ================"
        ssh $i '/opt/module/jdk1.8.0_144/bin/jps'
done
```

### 群起ZK

```shell
#!/bin/bash
for i in hadoop102 hadoop103 hadoop104
do
        echo "================           $i             ================"
        ssh $i '/opt/module/zookeeper-3.4.10/bin/zkServer.sh start'
done
```

### 群起hadoop相关进程

```shell
#!/bin/bash
echo "================     开始启动所有节点服务            ==========="
echo "================     正在启动Zookeeper               ==========="
for i in hadoop102 hadoop103 hadoop104
do
        ssh $i '/opt/module/zookeeper-3.4.10/bin/zkServer.sh start'
done
echo "================     正在启动HDFS                    ==========="
ssh atguigu@hadoop102 '/opt/module/hadoop-2.7.2/sbin/start-dfs.sh'
echo "================     正在启动YARN                    ==========="
ssh atguigu@hadoop103 '/opt/module/hadoop-2.7.2/sbin/start-yarn.sh'
echo "================     正在开启JobHistoryServer        ==========="
ssh atguigu@hadoop102 '/opt/module/hadoop-2.7.2/sbin/mr-jobhistory-daemon.sh start historyserver'
```

### 群关zk以及hadoop相关进程

```shell
#!/bin/bash
echo "================     开始关闭所有节点服务            ==========="
echo "================     正在关闭Zookeeper               ==========="
for i in hadoop102 hadoop103 hadoop104
do
        ssh $i '/opt/module/zookeeper-3.4.10/bin/zkServer.sh stop'
done
echo "================     正在关闭HDFS                    ==========="
ssh atguigu@hadoop102 '/opt/module/hadoop-2.7.2/sbin/stop-dfs.sh'
echo "================     正在关闭YARN                    ==========="
ssh atguigu@hadoop103 '/opt/module/hadoop-2.7.2/sbin/stop-yarn.sh'
echo "================     正在关闭JobHistoryServer        ==========="
ssh atguigu@hadoop102 '/opt/module/hadoop-2.7.2/sbin/mr-jobhistory-daemon.sh stop historyserver'
```

