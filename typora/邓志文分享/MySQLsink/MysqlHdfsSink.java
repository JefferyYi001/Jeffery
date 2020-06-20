package com.atguigu.custom;

import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MysqlHdfsSink extends AbstractSink implements Configurable {
    private String mysqlurl = "";
    private String username = "";
    private String password = "";
    private String tableName = "";
    private String hdfsPath = "";
    private int num = 0;

    Connection con = null;

    /**
     * 该方法负责从channel中获取event,将event写到指定的设置
     * 如果成功传输了一个或多个event就返回ready,如果从channle中获取不到event就返回backoff
     * @return
     * @throws EventDeliveryException
     */
    @Override
    public Status process() throws EventDeliveryException {
        Status status = null;
        // 获取sink对应的channel
        Channel ch = getChannel();
        // 从channel中获取事务
        Transaction txn = ch.getTransaction();
        //开启事务
        txn.begin();
        try
        {
            //从channel中获取take事务
            Event event = ch.take();

            if (event != null)
            {
                //拿到event的body
                String body = new String(event.getBody(), "UTF-8");

                //1,a
                //2,b
                if(body.contains("delete") || body.contains("drop") || body.contains("alert")){
                    status = Status.BACKOFF;
                }else{
                    //存入HDFS 获取配置文件信息
                    Configuration conf = new Configuration();
                    // 开启允许在已有的文件中追加内容 不建议在配置文件中开启
                    conf.setBoolean("dfs.support.append",true);
                    //修改HDFS配置文件 永不添加新的数据节点 在HDFS-default配置文件中可查看
                    conf.set("dfs.client.block.write.replace-datanode-on-failure.policy", "NEVER");
                    // 在hdfs配置文件中该选项默认配置就是true
                    conf.setBoolean("dfs.client.block.write.replace-datanode-on-failure.enable", true);
                    //拿到hdfs path路径
                    Path filePath = new Path(hdfsPath);
                    //通过配置信息拿到文件操作系统的对象
                    FileSystem hdfs = filePath.getFileSystem(conf);
                    //查看path路径 若没有文件则创建一个
                    if (!hdfs.exists(filePath)) {
                        hdfs.createNewFile(filePath);
                    }
                    //开启输出流
                    FSDataOutputStream outputStream = hdfs.append(filePath);
                    //将body写入path路径下
                    outputStream.write(body.getBytes("UTF-8"));
                    //写完一个event后换行
                    outputStream.write("\r\n".getBytes("UTF-8"));
                    //刷新该流的缓冲区，但并没有关闭该流，刷新之后还可以继续使用该流对象进行数据操作。
                    outputStream.flush();
                    //关闭此流，并在关闭之前先刷新该流,关闭之后流对象不可再被使用。
                    //一般情况下可以直接使用close()方法直接关闭该流，但是当数据量比较大的时候，可以使用flush()方法
                    outputStream.close();
                    hdfs.close();
                    //存入Mysql
                    num++;
                    //格式化日期
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //得到一个当前格式化的日期字符串
                    String createtime = df.format(new Date());
                    //预加载sql语句 向表中插入数据
                    PreparedStatement stmt = con.prepareStatement("insert into " + tableName + " (createtime, content, number) values (?, ?, ?)");
                    //设置预加载通配符的参数第一列 createtime=创建时间
                    stmt.setString(1, createtime);
                    //第二列内容为 content=传进来event中 body的内容
                    stmt.setString(2, body);
                    //第三列 number=行号
                    stmt.setInt(3, num);
                    //执行预加载操作
                    stmt.execute();
                    //关流
                    stmt.close();

                }

                status = Status.READY;
            }
            //如果event中没有数据 当前状态更改为backoff
            else
            {
                status = Status.BACKOFF;
            }
            //提交事务
            txn.commit();
        }
        catch (Throwable t)
        {
            txn.rollback();
            t.getCause().printStackTrace();

            status = Status.BACKOFF;
        }
        finally
        {
            txn.close();
        }

        return status;
    }

    @Override
    public void configure(Context context) {
        //mysql地址
        mysqlurl = context.getString("mysqlurl");
        //用户名
        username = context.getString("username");
        //密码
        password = context.getString("password");
        //表名
        tableName = context.getString("tablename");
        //hdfs路径
        hdfsPath = context.getString("hdfspath");
    }

    @Override
    public synchronized void stop()
    {
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.stop();
    }

    @Override
    public synchronized void start()
    {
        try
        {
            //获取mysql连接
            con = DriverManager.getConnection(mysqlurl, username, password);
            super.start();
            System.out.println("finish start");
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}


