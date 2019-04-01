package com.cloud.merchant.provider;

import com.cloud.merchant.provider.config.MerchantFeignConfigure;
import com.cloud.merchant.provider.fallback.MerchantUserProviderFallback;
import com.cloud.sysconf.common.utils.page.PageQuery;
import com.cloud.sysconf.common.vo.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.RequestBody;
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
@FeignClient(name = "merchant-service", fallback = MerchantUserProviderFallback.class, decode404 = true,
        configuration = MerchantFeignConfigure.class)
public interface MerchantUserProvider {


    /**
     * 通过商户号初始化商户信息到Redis
     * @param merchantCode
     * @return
     */
    @RequestMapping(value = "/merchant/user/initMerchantToRedis", method = RequestMethod.POST)
    ApiResponse initMerchantToRedis(@RequestParam("merchantCode") String merchantCode);

    /**
     *  获取商户详情
     * @param code
     * @return
     */
    @RequestMapping(value = "/merchant/user/detailByCode", method = RequestMethod.POST)
    ApiResponse detailByCode(@RequestParam("code") String code);

    /**
     *  获取商户详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/merchant/user/detail", method = RequestMethod.POST)
    ApiResponse detailById(@RequestParam("id") String id);

    /**
     * 通过用户id获取商户详情
     * @param sysUserId
     * @return
     */
    @RequestMapping(value = "/merchant/user/detailByUserId")
    ApiResponse detailByUserId(@RequestParam("sysUserId") String sysUserId);

    /**
     * 获取商户列表
     * @param pageQuery
     * @return
     */
    @RequestMapping(value = "/merchant/user/tablePage")
    ApiResponse tablePage(@RequestBody PageQuery pageQuery);
}
