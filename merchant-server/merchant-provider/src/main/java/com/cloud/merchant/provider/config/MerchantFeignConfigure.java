package com.cloud.merchant.provider.config;

import com.netflix.loadbalancer.ILoadBalancer;
import feign.Feign;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * @Auther Toney
 * @Date 2018/7/18 00:58
 * @Description:
 */
@ConditionalOnClass({ ILoadBalancer.class, Feign.class })
@Configuration
@AutoConfigureBefore(MerchantFeignConfigure.class)
public class MerchantFeignConfigure {

//    @Bean
//    public Retryer feignRetryer() {
//        return new Retryer.Default(100, 10000, 5);
//    }
}
