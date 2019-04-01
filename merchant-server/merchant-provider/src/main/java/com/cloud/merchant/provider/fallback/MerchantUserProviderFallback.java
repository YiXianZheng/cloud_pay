package com.cloud.merchant.provider.fallback;

import com.cloud.merchant.provider.MerchantUserProvider;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Auther Toney
 * @Date 2018/7/17 14:53
 * @Description:
 */
@Component
public class MerchantUserProviderFallback implements MerchantUserProvider {

    private final Logger log = LoggerFactory.getLogger(MerchantUserProviderFallback.class);

    @Override
    public ApiResponse initMerchantToRedis(String merchantCode) {
        log.error("========= >> merchant/user/initMerchantToRedis 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse detailByCode(String code) {
        log.error("========= >> merchant/user/detailByCode 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse detailById(String id) {
        log.error("========= >> merchant/user/detail 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse detailByUserId(String sysUserId) {
        log.error("========= >> merchant/user/detailByUserId 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse tablePage(PageQuery pageQuery) {
        log.error("========= >> merchant/user/tablePage 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

}
