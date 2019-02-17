package com.cloud.merchant.provider.fallback;

import com.cloud.sysconf.common.dto.SysUserBankDto;
import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.merchant.provider.SysUserBankProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SysUserBankProviderFallback implements SysUserBankProvider {

    private final Logger log = LoggerFactory.getLogger(SysUserBankProviderFallback.class);

    @Override
    public ApiResponse addBankCard(SysUserBankDto sysUserBankDto) {
        log.error("========= >> /sys/user/bank/addBankCard 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }
}
