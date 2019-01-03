package com.cloud.sysconf.provider.fallback;

import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysconf.provider.SysRoleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Auther Toney
 * @Date 2018/7/17 14:53
 * @Description:
 */
@Component
public class SysRoleProviderFallback implements SysRoleProvider {

    private final Logger log = LoggerFactory.getLogger(SysRoleProviderFallback.class);

    @Override
    public ApiResponse saveRoleUser(String userId, String roleId) {
        log.error("========= >> /sys/role/saveRoleUser 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse saveRoleUserCancel(String userId, String roleId) {
        log.error("========= >> /sys/role/saveRoleUserCancel 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse saveDefaultRoleUser(String userId, String roleType) {
        log.error("========= >> /sys/role/saveDefaultRoleUser 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse getRoleDetail(String id) {
        log.error("========= >> /sys/role/detail 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }
}
