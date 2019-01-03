package com.cloud.sysuser.provider;

import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysuser.common.DTO.SysUserProviderDto;
import com.cloud.sysuser.provider.config.FeignConfigure;
import com.cloud.sysuser.provider.fallback.SysUserProviderFallback;
import org.springframework.cloud.openfeign.FeignClient;
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

//@EnableAspectJAutoProxy(proxyTargetClass = true)  //若需要AOP  此注解可能需要
@FeignClient(name = "sysuser", fallback = SysUserProviderFallback.class, decode404 = true,
                configuration = FeignConfigure.class)
public interface SysUserProvider {

    @RequestMapping(value = "/sys/user/updateToken", method = RequestMethod.POST)
    ApiResponse updateToken();

    /**
     * 添加新的管理账号
     * @param sysUserProviderDto
     * @return
     */
    @RequestMapping(value = "/sys/user/addNewUserForProvider", method = RequestMethod.POST)
    ApiResponse addNewUser(@RequestBody(required = true) SysUserProviderDto sysUserProviderDto);

    /**
     * 撤销添加管理账号
     * @param sysUserProviderDto
     * @return
     */
    @RequestMapping(value = "/sys/user/addNewUserCancel", method = RequestMethod.POST)
    ApiResponse addNewUserCancel(@RequestBody(required = true) SysUserProviderDto sysUserProviderDto);

    /**
     * 获取管理账号信息
     * @param userId
     * @return
     */
    @RequestMapping(value = "/sys/user/infoForProvider", method = RequestMethod.POST)
    ApiResponse userInfo(@RequestParam("userId")String userId);

    /**
     * 更新登录权限
     * @param userId
     * @param loginFlag
     * @param curUserId
     * @return
     */
    @RequestMapping(value = "/sys/user/updateLoginFlagForProvider", method = RequestMethod.POST)
    ApiResponse updateLoginFlag(@RequestParam("userId")String userId, @RequestParam("loginFlag")Integer loginFlag,
                                @RequestParam("curUserId")String curUserId);

    /**
     * 删除系统用户
     * @param userId
     * @param curUserId
     * @return
     */
    @RequestMapping(value = "/sys/user/deleteForProvider", method = RequestMethod.POST)
    ApiResponse deleteUser(@RequestParam("userId")String userId, @RequestParam("curUserId")String curUserId);


    /**
     * 删除系统用户
     * @param sysUserId
     * @return
     */
    @RequestMapping(value = "/sys/user/getMerchantCodes", method = RequestMethod.POST)
    ApiResponse getMerchantCodes(@RequestParam("sysUserId")String sysUserId);

}
