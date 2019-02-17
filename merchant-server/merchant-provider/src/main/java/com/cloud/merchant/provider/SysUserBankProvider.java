package com.cloud.merchant.provider;

import com.cloud.sysconf.common.dto.SysUserBankDto;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.merchant.provider.fallback.SysUserBankProviderFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

//@EnableAspectJAutoProxy(proxyTargetClass = true)  //若需要AOP  此注解可能需要
@FeignClient(name = "sysconfig", fallback = SysUserBankProviderFallback.class, decode404 = true)
public interface SysUserBankProvider {

    /**
     * 用户绑定银行卡
     * @param sysUserBankDto
     * @return
     */
    @RequestMapping(value = "/sys/user/bank/addBankCard", method = RequestMethod.POST)
    ApiResponse addBankCard(@RequestBody SysUserBankDto sysUserBankDto);
}
