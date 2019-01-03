package com.cloud.taskservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringCloudApplication
@EnableFeignClients(basePackages = {"com.cloud.sysuser.provider", "com.cloud.sysconf.provider", "com.cloud.finance.provider"})
@ComponentScan(basePackages = {"com.cloud.taskservice", "com.cloud.sysuser.provider", "com.cloud.sysconf.provider",
        "com.cloud.finance.provider", "com.cloud.sysconf.common.redis"})
@EnableAsync
@EnableScheduling
//      若服务没有定义数据源，需要加上这个注释
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class TaskServiceApplication {

    @Bean
    @LoadBalanced
    RestTemplate restTemplate(){
        return  new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(TaskServiceApplication.class, args);
    }
}
