# Zabbix

## 1. 安装

### 1.1  准备编译前环境

① 在哪台机器部署 zabbix-web，需要将那台机器的**SELINUX=disabled**

```
sudo vim /etc/selinux/config
修改为以下内容
SELINUX=disabled
```

② 为zabbix创建一个专属的用户，这个用户专门用来启动zabbix程序

```
sudo groupadd --system zabbix
```

创建一个组名为 zabbix 的系统组，系统组和普通的组都是组角色，没有区别，唯一的区别就是标识此组是给系统中的某个程序使用，而非给人使用。

```
sudo useradd --system -g zabbix -d /usr/lib/zabbix -s /sbin/nologin -c "Zabbix Monitoring System" zabbix
-g：指定用户组
--system：当前是系统用户，用来执行某个程序的角色
-d：指定家目录，zabbix软件也会安装到此目录
-s：指定使用的shell
-c：注释
```

③ 创建并初始化zabbix需要使用的数据库

数据库导入脚本在 /opt/software/zabbix-4.2.8/database/mysql

```
create database zabbix default character set utf8 collate utf8_bin;
use zabbix;
source schema.sql;
source data.sql;
source images.sql;
```

④ 安装MySQL5.6除了client和server的其他模块

```
sudo rpm -ivh MySQL*
```

⑤ 安装编译所需要的依赖

```
sudo rpm -e --nodeps libxml2-python-2.7.6-21.el6.x86_64

sudo rpm -ivh  http://www.city-fan.org/ftp/contrib/yum-repo/rhel6/x86_64/city-fan.org-release-2-1.rhel6.noarch.rpm

sudo yum-config-manager --enable city-fan.org

sudo rpm -ivh http://dl.fedoraproject.org/pub/epel/6/x86_64/Packages/l/libnghttp2-1.6.0-1.el6.1.x86_64.rpm

sudo yum install -y libcurl libcurl-devel libxml2 libxml2-devel net-snmp-devel libevent-devel pcre-devel gcc-c++
```

### 1.2  开始编译和安装

```
cd /opt/software/zabbix-4.2.8

./configure --enable-server --enable-agent --with-mysql --enable-ipv6 --with-net-snmp --with-libcurl --with-libxml2

sudo make install
```

如果忘记加上了 sudo，需要先 make clean，再继续 sudo make install

### 1.3 配置

① 配置 zabbix server 连接的数据库

```
sudo vim /usr/local/etc/zabbix_server.conf

DBHost=hadoop102
DBName=zabbix
DBUser=root
DBPassword=123456

```

② 配置 zabbix-agent 连接的 server 配置

```
sudo vim /usr/local/etc/zabbix_agentd.conf

Server=hadoop102
#ServerActive=127.0.0.1
#Hostname=Zabbix server

```

### 1.4 服务制作规范

所有 service 命令能管理的服务，必须以脚本的形式放在/etc/init.d目录下。服务的编写可以参考：/usr/share/doc/initscripts-9.03.53/sysvinitfiles

在以上文件中，可以查看到一个服务脚本的编写格式：

```
#!/bin/bash
#Tag list  ，从系统支持的tag list中取，例如可以写
#chkconfig: <startlevellist> <startpriority> <endpriority>
#description: <multi-line description of service>
#...
---空行---
#普通的注释

# 读取系统函数定义的脚本.
. /etc/init.d/functions

#正文
start(){
	#启动程序
	echo -n "starting <servicename>"
	启动命令，如果没有启动脚本，可以使用daemon函数启动
	创建一个锁文件： touch /var/xxx
	return 状态码

}

stop(){
	#停止程序
	echo -n "Stopping <servicename>"
	停止命令，如果没有停止脚本，可以使用killproc函数启动
	删除之前创建的锁文件： touch /var/xxx
	return 状态码

}

case ....
```

### 1.5 将 zabbix 制作为服务，设置开机自启动

```
#编辑服务文件，请复制word
sudo vim /etc/init.d/zabbix-server
sudo vim /etc/init.d/zabbix-agent

#修改执行权限
sudo chmod +x /etc/init.d/zabbix-server
sudo chmod +x /etc/init.d/zabbix-agent

#添加到开机自启动列表
sudo chkconfig --add zabbix-agent
sudo chkconfig --add zabbix-server

#开启全级别开机自启动
sudo chkconfig  zabbix-agent on
sudo chkconfig  zabbix-server on
```

### 1.6 配置 zabbix-web

① 安装 apache 服务器

```
sudo yum -y install httpd
```

② 添加 zabbix-web 的配置到 apache 服务器的配置文件

```
sudo vim /etc/httpd/conf/httpd.conf
```

```
<IfModule mod_php5.c>
         php_value max_execution_time 300
          php_value memory_limit 128M
         php_value post_max_size 16M
          php_value upload_max_filesize 2M
          php_value max_input_time 300
          php_value max_input_vars 10000
          php_value always_populate_raw_post_data -1
          php_value date.timezone Asia/Shanghai
</IfModule>
```

③ 将 zabbix-web 的项目页面等资源拷贝到 apache 服务器资源目录

```
sudo mkdir /var/www/html/zabbix
sudo cp -a /opt/module/zabbix-4.2.8/frontends/php/* /var/www/html/zabbix/
```

### 1.7 安装php5.6

centos 自带的是5.3，5.3运行 zabbix-web 会报错。

查看当前php的版本

```
php -v
```

查看当前安装的php有哪些软件包

```
rpm -qa | grep php
```

安装php5.6之前需要先下载yum源：

```
sudo rpm -ivh http://dl.fedoraproject.org/pub/epel/6/x86_64/epel-release-6-8.noarch.rpm

sudo rpm -ivh http://rpms.famillecollet.com/enterprise/remi-release-6.rpm

sudo yum-config-manager --enable remi-php56

sudo yum install -y php php-bcmath php-mbstring php-xmlwriter php-xmlreader php-mcrypt php-cli php-gd php-curl php-mysql php-ldap php-zip php-fileinfo
```

### 1.8 在 hadoop102 和 hadoop104 安装 agent

参考 word 文档即可。



## 2.启动

① 在Hadoop103启动zabbix-server服务，在所有的机器启动zabbix-agent

```
sudo service zabbix-server start
sudo service zabbix-agent start
```

② 启动apache服务器

```
sudo service httpd start
```

