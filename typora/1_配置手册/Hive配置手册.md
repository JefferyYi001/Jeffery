# 1. 前置条件

HDFS 和 YARN 可以运行。

# 2. Hive 安装部署

完成Hive的安装和环境变量配置。注意配置环境变量：sudo vim /etc/profile

```bash
# HIVE_HOME
export HIVE_HOME=/opt/module/hive
export PATH=$PATH:$HIVE_HOME/bin
```

# 3. 安装 MySQL

## 3.1 MySQL 安装

上传rpm包，检测当前机器是否已经安装了mysql

```
rpm -qa | grep mysql
rpm -qa | grep MySQL
```

卸载之前安装的残留包

```
sudo rpm -e --nodeps mysql-libs-5.1.73-7.el6.x86_6
```

安装服务端

```
sudo rpm -ivh MySQL-server-5.6.24-1.el6.x86_64.rpm
```

安装客户端

```
sudo rpm -ivh MySQL-client-5.6.24-1.el6.x86_64.rpm
```

如果是5.6的mysql,需要先为root@localhost设置密码：

查看随机生成的密码：

```
sudo cat /root/.mysql_secret
```

启动服务：

```
sudo service mysql start
```

登录后修改密码：

```
mysql -uroot -p刚查询的随机密码
```

修改密码：

```
SET PASSWORD=password('密码')
```

之后退出，使用新密码登录！

## 3.2  MySQL 卸载

查询当前安装的mysql版本

```
rpm -qa | grep MySQL
```

停止当前的mysql服务

```
sudo service mysql stop
```

卸载服务端

```
sudo rpm -e MySQL-server-5.6.24-1.el6.x86_64
```

删除之前mysql存放数据的目录

```
sudo rm -rf /var/lib/mysql/
```

## 3.3 开放远程访问权限

查询当前有哪些用户：

```
select host,user,password from mysql.user;
```

删除除了localhost的所有用户

```
delete from mysql.user where host <> 'localhost';
```

修改root用户可以从任意机器登录：

```
update mysql.user set host='%' where user='root';
```

刷新权限

```
flush privileges;
```

重启服务：

```
sudo service mysql restart
```

验证本机登录：

```
sudo mysql -uroot -p123456   
```

验证从外部地址登录：

```
sudo mysql -h hadoop103 -uroot -p123456
```

查看当前连接的线程：

```
sudo mysqladmin processlist -uroot -p123456
```

# 4. 修改 hive 元数据的存储目录

注意： （1）mysql 是开机自启动的，不要开机后重复启动。

​			（2）service 命令需要使用 sudo 才可以运行。

在$HIVE_HOME/conf/目录下新建hive-site.xml，将以下内容复制到文件中并保存：

```xml
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
	<property>
	  <name>javax.jdo.option.ConnectionURL</name>
	  <value>jdbc:mysql://hadoop103:3306/metastore?createDatabaseIfNotExist=true</value>
	  <description>JDBC connect string for a JDBC metastore</description>
	</property>

	<property>
	  <name>javax.jdo.option.ConnectionDriverName</name>
	  <value>com.mysql.jdbc.Driver</value>
	  <description>Driver class name for a JDBC metastore</description>
	</property>

	<property>
	  <name>javax.jdo.option.ConnectionUserName</name>
	  <value>root</value>
	  <description>username to use against metastore database</description>
	</property>

	<property>
	  <name>javax.jdo.option.ConnectionPassword</name>
	  <value>root</value>
	  <description>password to use against metastore database</description>
	</property>
</configuration>

```

**注意**：主机名、用户名、密码需要根据实际配置进行修改。

之后将mysql的驱动包，拷贝到$HIVE_HOME/lib下。

**注意**：建议手动创建metastore数据库！metastore数据的编码必须为latin1!

如果是mysql5.5,请修改mysql服务端的配置文件/etc/my.cnf,在[mysqld]下添加

```
 binlog_format=ROW
```

重启mysql服务器。

# 5. 配置 JDBC 访问

## 5.1 启动 JDBC 服务

​		在hive中，hive使用 hiveserver2 服务作为支持 JDBC 连接的服务端！

​		先启动 hiveserver2

​		默认是前台运行

```
hiveserver2
```

​	设置后台运行

```
hiveserver2 &
```

## 5.2 使用 Beeline 连接

Beeline 是一个支持JDBC连接的客户端工具。

启动beeline，之后创建一个新的连接

```
!connect 'jdbc:hive2://hadoop103:10000'
```

之后回车，输入用户名atguigu（涉及HDFS权限问题，需与HDFS权限用户一致），密码随意。

断开连接

```
!close
```

退出beeline

```
!exit
```



## 5.3 使用Java程序访问Hive

创建Maven工程，引入hive-jdbc的驱动

```xml
 <dependency>
            <groupId>org.apache.hive</groupId>
            <artifactId>hive-jdbc</artifactId>
            <version>1.2.1</version>
 </dependency>
```

hive 还依赖于 hadoop，还需要将 hadoop 的依赖也加入进去。如果是放在 hadoop 项目下的子module，则不需要。

示例程序：

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by VULCAN on 2020/3/4
 */
public class HiveJdbcTest {

    public static void main(String[] args) throws Exception {

        //注册驱动  可选，只要导入hive-jdbc.jar，会自动注册驱动
        //Class.forName("org.apache.hive.jdbc.HiveDriver");

        //创建连接  url,driverClass,username,password
        Connection connection = DriverManager.getConnection("jdbc:hive2://hadoop103:10000", "atguigu", "");

        //准备sql
        String sql="select * from person";

        PreparedStatement ps = connection.prepareStatement(sql);

        //执行查询
        ResultSet resultSet = ps.executeQuery();

        //遍历结果
        while (resultSet.next()){

            System.out.println("name:"+resultSet.getString("name")+"   age:"+
                    resultSet.getInt("age"));
        }
        //关闭资源
        resultSet.close();
        ps.close();
        connection.close();
    }
}

```

# 5. Hive 交互命令

## 5.1 查看 hive 中的变量

查看 hive 启动后加载的所有的变量

```
set
```

查看某个指定的参数的值

```
set 属性名
```

对加载的参数进行修改，此次修改只有当前的 cli 有效，一旦退出就需要重新设置

```
set 属性名=属性值;
```

使用 hive -d 变量名=变量值，启动后，可以在cli使用 ${变量名} 引用变量名，获取它的值。而使用 set 属性名=属性值 进行赋值的变量，不能通过 ${变量名} 引用。

**注：不能识别时用引号括起来**

## 5.2 hive交互命令

该部分命令多用于编写脚本。

```
hive 
	-d key=value:  定义一个变量名=变量值
	--database 库名： 让hive初始化连接指定的库
	-e <quoted-query-string> ： hive读取命令行的sql语句执行，结束后退出cli
	-f sql文件：   hive读取文件中的sql语句执行，结束后退出cli
	--hivevar <key=value> : 等价于-d
	--hiveconf <property=value>: 在启动hive时，定义hive中的某个属性设置为指定的值
	 -i <filename> ： 在启动hive后，先初始化地执行指定文件中的sql，不退出cli
	 -S,--silent :  静默模式，不输出和结果无关的信息
```

## 5.3 hive中属性加载的顺序

​		a) hive 依赖于 hadoop，hive 启动时，默认会读取 Hadoop 的8个配置文件

​		b) 加载 hive 默认的配置文件 hive-default.xml

​		c) 加载用户自定义的 hive-site.xml

​		d) 如果用户在输入 hive 命令时，指定了--hiveconf，那么用户指定的参数会覆盖之前已经读取的同名的参数

## 5.4 在 cli 中使用其他命令

访问hdfs

```
dfs 命令
```

运行shell中的命令

```
!命令
```

## 5.5 其他属性配置

### 	5.5.1 查询后信息显示配置

​	在hive-site.xml文件中添加如下配置信息，就可以实现显示当前数据库，以及查询表的头信息配置。

```xml
<property>
	<name>hive.cli.print.header</name>
	<value>true</value>
</property>

<property>
	<name>hive.cli.print.current.db</name>
	<value>true</value>
</property>
```

# 6. Hive 建表

## 6.1 数据案例

```java
{
    "name": "songsong",
    "friends": ["bingbing" , "lili"] ,       //列表Array, 
    "children": {                      //键值Map,
        "xiao song": 18 ,
        "xiaoxiao song": 19
    }
    "address": {                      //结构Struct,
        "street": "hui long guan" ,
        "city": "beijing" 
    }
}

```

```
songsong,bingbing_lili,xiao song:18_xiaoxiao song:19,hui long guan_beijing
yangyang,caicai_susu,xiao yang:18_xiaoxiao yang:19,chao yang_beijing
```

## 6.2 建表语句

```sql
create table t1(name string,friends array<string>,children map<string,int>,
              address struct<street:string,city:string> )
row format delimited fields terminated by ','
collection items terminated by '_'
map keys terminated by ':';
```

注意：一个表对应的数据中，所有的集合类型，元素的分隔符必须是一致的！否则会造成集合无法识别！

## 6.3 上传数据到表目录

除了直接使用 hdfs 操作外，还可以使用 hive 的 load 命令，只是两者上传后的文件权限不同。

```
load data [local] inpath '路径名' into table '表名' 
```

注：需在hive环境下执行。

## 6.4 查询局部信息

```sql
-- 数组使用 [下标] 访问，map 的 value 使用 [key] 访问，结构体使用 .属性 访问
select friends[1], children['xiaoxiao song'], address.street from t1 where name = 'songsong';
select map('aa','AA', 'bb', 'BB')['aa'];
select array('aa','AA', 'bb', 'BB')[1];
```





