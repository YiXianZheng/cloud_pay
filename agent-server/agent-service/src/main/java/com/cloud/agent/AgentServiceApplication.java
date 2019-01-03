package com.cloud.agent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringCloudApplication
@EnableFeignClients(basePackages = {"com.cloud.agent.provider", "com.cloud.sysuser.provider"})
@ComponentScan(basePackages = {"com.cloud.agent", "com.cloud.sysuser.provider", "com.cloud.sysconf.common.redis"})
@MapperScan("com.cloud.agent.dao")
public class AgentServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgentServiceApplication.class, args);
	}
}
