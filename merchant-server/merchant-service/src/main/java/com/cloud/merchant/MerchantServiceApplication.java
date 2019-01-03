package com.cloud.merchant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringCloudApplication
@EnableFeignClients(basePackages = {"com.cloud.merchant", "com.cloud.sysuser.provider", "com.cloud.agent.provider"})
@ComponentScan(basePackages = {"com.cloud.merchant", "com.cloud.sysuser.provider", "com.cloud.sysconf.common.redis",
		"com.cloud.agent.provider"})
@MapperScan("com.cloud.merchant.dao")
public class MerchantServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MerchantServiceApplication.class, args);
	}
}
