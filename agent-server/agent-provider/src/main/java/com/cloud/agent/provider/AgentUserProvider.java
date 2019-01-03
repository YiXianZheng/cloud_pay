package com.cloud.agent.provider;

import com.cloud.agent.provider.config.AgentFeignConfigure;
import com.cloud.agent.provider.fallback.AgentUserProviderFallback;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * 提供供其它服务调用的接口
 * @Auther Toney
 * @Date 2018/7/9 15:23
 * @Description:
 */
@EnableAspectJAutoProxy(proxyTargetClass = true)
@FeignClient(name = "agent-service", fallback = AgentUserProviderFallback.class, decode404 = true,
        configuration = AgentFeignConfigure.class)
public interface AgentUserProvider {


    /**
     * 通过商户号初始化商户信息到Redis
     * @param agentCode
     * @return
     */
    @RequestMapping(value = "/agent/user/initAgentToRedis", method = RequestMethod.POST)
    ApiResponse initAgentToRedis(@RequestParam("agentCode") String agentCode);

    /**
     *  获取商户详情
     * @param code
     * @return
     */
    @RequestMapping(value = "/agent/user/detailByCode", method = RequestMethod.POST)
    ApiResponse detailByCode(@RequestParam("code") String code);

    /**
     *  获取商户详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/agent/user/detail", method = RequestMethod.POST)
    ApiResponse detailById(@RequestParam("id") String id);
}
