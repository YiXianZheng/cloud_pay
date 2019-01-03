package com.cloud.apigateway.service;

import com.cloud.sysconf.common.vo.ApiResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @Auther Toney
 * @Date 2018/8/13 16:26
 * @Description:
 */
public interface MerchantApiFilterService {

    /**
     * 校验商户的权限
     * @param request
     * @return
     */
    ApiResponse checkApi(HttpServletRequest request);
}
