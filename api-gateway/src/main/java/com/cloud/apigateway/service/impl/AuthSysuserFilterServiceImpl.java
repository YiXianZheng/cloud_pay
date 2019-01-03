package com.cloud.apigateway.service.impl;

import com.cloud.apigateway.service.AuthSysuserFilterService;
import com.cloud.sysconf.common.redis.RedisClient;
import com.cloud.sysconf.common.redis.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/8/13 17:01
 * @Description:
 */
@Service
public class AuthSysuserFilterServiceImpl implements AuthSysuserFilterService {

    @Autowired
    private RedisClient redisClient;

    @Override
    public Map<String, String> getUserByToken(String token) {
        return redisClient.Gethgetall(RedisConfig.USER_TOKEN_DB,token);
    }
}
