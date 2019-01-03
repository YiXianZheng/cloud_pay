package com.cloud.sysuser.provider.fallback;

import com.cloud.sysconf.common.utils.ResponseCode;
import com.cloud.sysconf.common.vo.ApiResponse;
import com.cloud.sysuser.common.DTO.SysUserProviderDto;
import com.cloud.sysuser.provider.SysUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Auther Toney
 * @Date 2018/7/17 14:53
 * @Description:
 */
@Component
public class SysUserProviderFallback implements SysUserProvider {

    private final Logger log = LoggerFactory.getLogger(SysUserProviderFallback.class);

    @Override
    public ApiResponse updateToken() {
        log.error("========= >> sys/user/updateToken 接口调用异常");

//        if(response.status() >= 400 && response.status() <= 499){
//            return new ResultVo(ResultCode.REQUIRE_ERR);
//        }

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse addNewUser(SysUserProviderDto sysUserProviderDto) {
        log.error("========= >> sys/user/addNewUser 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse addNewUserCancel(SysUserProviderDto sysUserProviderDto) {
        log.error("========= >> sys/user/addNewUserCancel 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse userInfo(String userId) {
        log.error("========= >> sys/user/infoForProvider 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse updateLoginFlag(String userId, Integer loginFlag, String curUserId) {
        log.error("========= >> sys/user/updateLoginFlagForProvider 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse deleteUser(String userId, String curUserId) {
        log.error("========= >> sys/user/deleteForProvider 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

    @Override
    public ApiResponse getMerchantCodes(String sysUserId) {
        log.error("========= >> sys/user/getMerchantCodes 接口调用异常");

        return ApiResponse.creatFail(ResponseCode.Base.SYSTEM_ERR);
    }

}
