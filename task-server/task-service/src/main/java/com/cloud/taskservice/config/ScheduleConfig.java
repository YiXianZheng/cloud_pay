package com.cloud.taskservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

/**
 * @Auther Toney
 * @Date 2018/7/12 15:27
 * @Description:
 */
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        //设定一个长度的定时任务线程池  --> 10
        scheduledTaskRegistrar.setScheduler(Executors.newScheduledThreadPool(10));
    }
}
