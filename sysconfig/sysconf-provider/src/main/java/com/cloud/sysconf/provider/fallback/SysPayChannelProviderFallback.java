package com.cloud.sysconf.provider.fallback;

import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysPayChannelProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Auther Toney
 * @Date 2018/7/17 14:53
 * @Description:
 */
@Component
public class SysPayChannelProviderFallback implements SysPayChannelProvider {

    private final Logger log = LoggerFactory.getLogger(SysPayChannelProviderFallback.class);

    @Override
    public ApiResponse loadValidChannel() {
        log.error("========= >> /sys/pay/channel/loadValidChannel 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse initChannelToRedis() {
        log.error("========= >> /sys/pay/channel/initChannelToRedis 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }
}
