package com.cloud.apigateway.service;

import java.util.Map;

/**
 * @Auther Toney
 * @Date 2018/8/13 17:00
 * @Description:
 */
public interface AuthSysuserFilterService {

    /**
     * 通过token 获取用户信息
     * @param token
     * @return
     */
    Map<String, String> getUserByToken(String token);
}
