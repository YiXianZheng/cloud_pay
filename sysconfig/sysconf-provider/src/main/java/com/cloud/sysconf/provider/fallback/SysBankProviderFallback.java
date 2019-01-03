package com.cloud.sysconf.provider.fallback;

import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysBankProvider;
import com.cloud.sysconf.provider.SysRoleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Auther Toney
 * @Date 2018/7/17 14:53
 * @Description:
 */
@Component
public class SysBankProviderFallback implements SysBankProvider {

    private final Logger log = LoggerFactory.getLogger(SysBankProviderFallback.class);

    @Override
    public ApiResponse toChannelCode(String sysBankCode, String thirdChannelId) {
        log.error("========= >> /sys/bank/toChannelCode 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse getBankNameByCode(String sysBankCode) {
        log.error("========= >> /sys/bank/getBankNameByCode 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

}
