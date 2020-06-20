# 1. Zookeeper 的本地安装部署



## 1.1 解压

拷贝Zookeeper安装包到Linux系统下，并解压到指定目录

```bash
tar -zxvf zookeeper-3.4.10.tar.gz -C /opt/module/
```



## 1.2 配置修改

将 /opt/module/zookeeper-3.4.10/conf 这个路径下的zoo_sample.cfg 修改为 zoo.cfg；打开zoo.cfg文件，修改dataDir路径：

```bash
mv zoo_sample.cfg zoo.cfg
vim zoo.cfg
```

修改如下内容：

dataDir=/opt/module/zookeeper-3.4.10/zkData

在 /opt/module/zookeeper-3.4.10/ 这个目录上创建 zkData 文件夹：

```bash
mkdir zkData
```



# 2. Zookeeper 分布式安装部署



## 2.1 集群规划

在hadoop102、hadoop103和hadoop104三个节点上部署Zookeeper。



## 2.2 解压安装

（1）解压Zookeeper安装包到/opt/module/目录下

```bash
tar -zxvf zookeeper-3.4.10.tar.gz -C /opt/module/
```

（2）同步/opt/module/zookeeper-3.4.10目录内容到hadoop103、hadoop104

```bash
xsync zookeeper-3.4.10/
```



## 2.3 配置服务器编号

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



## 2.4 配置zoo.cfg文件

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

