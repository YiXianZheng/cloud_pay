package com.cloud.sysconf.common.redis.lockutil;

/**
 * 全局锁  含锁的名称
 * @Auther Toney
 * @Date 2018/8/1 10:12
 * @Description:
 */
public class Lock {

    private String name;

    private String value;

    public Lock(String name, String value){
        this.name = name;
        this.value = value;
    }

    public String getName(){
        return name;
    }

    public String getValue(){
        return value;
    }
}
