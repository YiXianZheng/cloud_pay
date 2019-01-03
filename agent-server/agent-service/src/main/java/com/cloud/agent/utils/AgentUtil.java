package com.cloud.agent.utils;

import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import com.cloud.sysconf.common.utils.Constant;
import org.apache.commons.lang.StringUtils;

/**
 * 代理的工具类
 * @Auther Toney
 * @Date 2018/8/3 10:30
 * @Description:
 */
public class AgentUtil {

    public static String getAgentCode(RedisClient redisClient){
        String baseIncre = redisClient.get(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_AGENT_BASE_INCRE);
        if(StringUtils.isBlank(baseIncre)){
            baseIncre = Constant.REDIS_AGENT_DEFAULT_INCRE;
            redisClient.set(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_AGENT_BASE_INCRE, Constant.REDIS_AGENT_DEFAULT_INCRE);
        }
        String count = redisClient.get(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_AGENT_COUNT);
        if(StringUtils.isBlank(count)){
            count = "1";
        }

        String agentCode = Integer.valueOf(baseIncre) + Integer.valueOf(count) + "";
        redisClient.set(RedisConfig.VARIABLE_CONSTANT, Constant.REDIS_AGENT_COUNT, Integer.valueOf(count)+1 + "");

        return agentCode;
    }
}
