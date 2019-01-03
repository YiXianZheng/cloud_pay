package com.cloud.sysconf.common.redis;

/**
 * @Auther Toney
 * @Date 2018/8/13 19:17
 * @Description:
 */
public class RedisConstants {


    public static final int USER_TOKEN_DB = 0;          //用户token及其相关信息库

    public static final int MERCHANT_INFO_DB = 1;       //商户信息

    public static final int AGENT_INFO_DB = 2;          //代理信息

    public static final int ORDER_COUNT_DB = 3;         //订单统计

    public static final int SUCCESS_ORDER_DB = 6;       //支付成功的订单

    public static final int ERROR_ORDER_DB = 7;         //支付失败的订单

    public static final int VARIABLE_CONSTANT = 14;     //可变常量库（如员工编号的增量）

    public static final int DISTRIBUTED_LOCK_DB = 15;   //分布式锁
}
