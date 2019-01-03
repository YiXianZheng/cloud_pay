package com.cloud.sysuser.provider.config;

import com.netflix.loadbalancer.ILoadBalancer;
import feign.Feign;
import feign.Retryer;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther Toney
 * @Date 2018/7/18 00:58
 * @Description:
 */
@ConditionalOnClass({ ILoadBalancer.class, Feign.class })
@Configuration
@AutoConfigureBefore(FeignConfigure.class)
public class FeignConfigure {

//    @Bean
//    public Retryer feignRetryer() {
//        return new Retryer.Default(100, 10000, 5);
//    }
}
