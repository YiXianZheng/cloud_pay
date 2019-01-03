package com.cloud.agent.provider.fallback;

import com.cloud.agent.provider.AgentUserProvider;
import com.cloud.sysconf.common.utils.ResponseCode;
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
public class AgentUserProviderFallback implements AgentUserProvider {

    private final Logger log = LoggerFactory.getLogger(AgentUserProviderFallback.class);

    @Override
    public ApiResponse initAgentToRedis(String agentCode) {
        log.error("========= >> agent/user/initAgentToRedis 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse detailByCode(String code) {
        log.error("========= >> agent/user/detailByCode 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse detailById(String id) {
        log.error("========= >> agent/user/detail 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

}
