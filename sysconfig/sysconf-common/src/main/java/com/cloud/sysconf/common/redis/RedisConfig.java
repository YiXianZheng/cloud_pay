package com.cloud.sysconf.common.redis;

import java.util.concurrent.CountDownLatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;


/**
 * redis配置
 *  db0     用户token及其相关信息
 *
 *  db14    代理商编码等
 *  db15    分布式锁
 * @author Toney
 *
 */
@Configuration
@EnableCaching
public class RedisConfig extends CachingConfigurerSupport {
    
    public static final int USER_TOKEN_DB = 0;          //用户token及其相关信息库

    public static final int MERCHANT_INFO_DB = 1;       //商户信息

    public static final int AGENT_INFO_DB = 2;          //代理信息

    public static final int ORDER_COUNT_DB = 3;         //订单统计（系统）

    public static final int ORDER_FINANCE_GRAPH = 4;    //财务统计

    public static final int MERCHANT_CHANNEL_COUNT_DB = 5;  //商户通道交易统计

    public static final int SUCCESS_ORDER_DB = 6;       //支付成功的订单

    public static final int ERROR_LOG_DB = 7;           //一些影响数据完整性的错误日志

    public static final int MERCHANT_DAILY_PAY_COUNT_DB = 8;  //商户每日累计支付总额统计

    public static final int MERCHANT_HOUR_PAY_COUNT_DB = 9;  //商户每日分时累计支付总额统计

    public static final int TASK_RUN_LOG = 10;  //定时任务运行日志

    public static final int THIRD_PAY_CHANNEL = 11;     //第三方支付通道

    public static final int SYS_LOG_DB = 12;            //系统操作日志

    public static final int SYS_PAY_CHANNEL = 13;       //系统支付通道

    public static final int VARIABLE_CONSTANT = 14;     //可变常量库（如员工编号的增量）

    public static final int DISTRIBUTED_LOCK_DB = 15;   //分布式锁

    public static final int MERCHANT_DAILY_CHANNEL_COUNT_DB = 16;   // 商户每日通道累计支付总额

    //16-25  待通知商户  26 未能成功通知的
    public static final int UN_RESPONSE_NOTIFY = 16;    //没有收到商户响应的订单

    public static final int UN_RESPONSE_NOTIFY_FINAL = 26;    //最终都没有收到商户响应的订单


    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("chat"));

        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean
    Receiver receiver(CountDownLatch latch) {
        return new Receiver(latch);
    }

    @Bean
    CountDownLatch latch() {
        return new CountDownLatch(1);
    }

    @Bean
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    public class Receiver {


        private CountDownLatch latch;

        @Autowired
        public Receiver(CountDownLatch latch) {
            this.latch = latch;
        }

        public void receiveMessage(String message) {
            latch.countDown();
        }
    }


}
