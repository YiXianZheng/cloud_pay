package com.cloud.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringCloudApplication
@EnableFeignClients(basePackages = {"com.cloud.sysuser.provider","com.cloud.merchant.provider", "com.cloud.agent.provider",
		"com.cloud.sysconf.provider"})
@ComponentScan(basePackages = {"com.cloud.finance", "com.cloud.sysconf.common.redis","com.cloud.sysuser.provider",
		"com.cloud.merchant.provider", "com.cloud.agent.provider", "com.cloud.sysconf.provider"})
@MapperScan("com.cloud.finance.dao")
public class FinanceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceServiceApplication.class, args);
	}
}
