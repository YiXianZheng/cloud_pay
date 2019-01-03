package com.cloud.sysuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringCloudApplication
@ComponentScan(basePackages = {"com.cloud.sysconf.common", "com.cloud.sysuser", "com.cloud.sysconf.provider"})
@EnableFeignClients(basePackages={"com.cloud.sysuser", "com.cloud.sysconf.provider"})
@MapperScan("com.cloud.sysuser.dao")
public class SysuserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SysuserServiceApplication.class, args);
	}
}
