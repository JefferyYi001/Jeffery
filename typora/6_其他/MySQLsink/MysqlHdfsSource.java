package com.atguigu.custom;


import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.SimpleEvent;
import org.apache.flume.source.AbstractSource;

import java.util.ArrayList;

public class MysqlHdfsSource extends AbstractSource implements Configurable, PollableSource {

    //间隔睡眠时间
    private Long sleep;
    private String content;

    @Override
    public Status process() throws EventDeliveryException {
        Status status = Status.READY;
        //封装event
        ArrayList<Event> events = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            SimpleEvent e = new SimpleEvent();
            //封装数据 获取从配置文件中获取的内容
            e.setBody((content + "--->第" + i + "个Event").getBytes());
            //封装成一批
            events.add(e);
        }
        try {
            //获取当前source的channel处理器
            ChannelProcessor channelProcessor = getChannelProcessor();
            //由channel处理器将一批event放入channel
            channelProcessor.processEventBatch(events);
            //间隔sleep秒放入一批
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            //异常就改变状态
            status = Status.BACKOFF;
            e.printStackTrace();
        }
        return status;
    }

    @Override
    public long getBackOffSleepIncrement() {
        return 1000;
    }

    @Override
    public long getMaxBackOffSleepInterval() {
        return 5000;
    }

    @Override
    public void configure(Context context) {
        sleep = context.getLong("sleep");
        content = context.getString("content", "无");
    }
}
