package com.cloud.sysconf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringCloudApplication
@EnableFeignClients(basePackages = {"com.cloud.sysuser.provider"})
@ComponentScan(basePackages = {"com.cloud.sysconf", "com.cloud.sysuser.provider"})
@MapperScan("com.cloud.sysconf.dao")
public class SysconfServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SysconfServiceApplication.class, args);
	}
}
