package com.cloud.sysconf.provider;

import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.fallback.SysBankProviderFallback;
import com.cloud.sysconf.provider.fallback.SysRoleProviderFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * 提供供其它服务调用的接口
 * @Auther Toney
 * @Date 2018/7/9 15:23
 * @Description:
 */

//@EnableAspectJAutoProxy(proxyTargetClass = true)  //若需要AOP  此注解可能需要
@FeignClient(name = "sysconfig", fallback = SysBankProviderFallback.class, decode404 = true)
public interface SysBankProvider {

    /**
     * 获取第三方通道的银行编码
     * @param sysBankCode
     * @param thirdChannelId
     * @return
     */
    @RequestMapping(value = "/sys/bank/toChannelCode", method = RequestMethod.POST)
    ApiResponse toChannelCode(@RequestParam(required = true) String sysBankCode, @RequestParam(required = true) String thirdChannelId);

    /**
     * 获取银行名称
     * @param sysBankCode
     * @return
     */
    @RequestMapping(value = "/sys/bank/getBankNameByCode", method = RequestMethod.POST)
    ApiResponse getBankNameByCode(@RequestParam(required = true) String sysBankCode);

}
