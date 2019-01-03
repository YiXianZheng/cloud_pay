package com.cloud.sysconf.provider;

import com.cloud.sysconf.common.vo.ApiResponse;
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
@FeignClient(name = "sysconfig", fallback = SysRoleProviderFallback.class, decode404 = true)
public interface SysRoleProvider {

    /**
     * 保存用户角色关系
     * @param userId
     * @param roleId
     * @return
     */
    @RequestMapping(value = "/sys/role/saveRoleUser", method = RequestMethod.POST)
    ApiResponse saveRoleUser(@RequestParam(required = true) String userId, @RequestParam(required = true) String roleId);

    /**
     * 取消保存用户角色关系
     * @param userId
     * @param roleId
     * @return
     */
    @RequestMapping(value = "/sys/role/saveRoleUserCancel", method = RequestMethod.POST)
    ApiResponse saveRoleUserCancel(@RequestParam(required = true) String userId, @RequestParam(required = true) String roleId);

    /**
     * 保存默认用户角色关系
     * @param userId
     * @param roleType
     * @return
     */
    @RequestMapping(value = "/sys/role/saveDefaultRoleUser", method = RequestMethod.POST)
    ApiResponse saveDefaultRoleUser(@RequestParam(required = true) String userId, @RequestParam(required = true) String roleType);

    /**
     * 获取角色详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/sys/role/detail", method = RequestMethod.POST)
    ApiResponse getRoleDetail(@RequestParam(required = true) String id);

}
