package com.cloud.sysconf.provider;

import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.fallback.SysPayChannelProviderFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * 提供供其它服务调用的接口
 * @Auther Toney
 * @Date 2018/7/9 15:23
 * @Description:
 */

//@EnableAspectJAutoProxy(proxyTargetClass = true)  //若需要AOP  此注解可能需要
@FeignClient(name = "sysconfig", fallback = SysPayChannelProviderFallback.class, decode404 = true)
public interface SysPayChannelProvider {

    /**
     * 获取可用的通道
     * @return
     */
    @RequestMapping(value = "/sys/pay/channel/loadValidChannel", method = RequestMethod.POST)
    ApiResponse loadValidChannel();

    /**
     * 初始化通道数据到redis
     * @return
     */
    @RequestMapping(value = "/sys/pay/channel/initChannelToRedis", method = RequestMethod.POST)
    ApiResponse initChannelToRedis();

}
